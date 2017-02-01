package gov.ca.cwds.jobs;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

import gov.ca.cwds.dao.elasticsearch.ElasticsearchConfiguration;
import gov.ca.cwds.dao.elasticsearch.ElasticsearchDao;
import gov.ca.cwds.data.cms.OtherClientNameDao;
import gov.ca.cwds.data.persistence.cms.OtherClientName;
import gov.ca.cwds.inject.CmsSessionFactory;
import gov.ca.cwds.jobs.inject.JobsGuiceInjector;
import gov.ca.cwds.rest.api.domain.es.Person;

/**
 * Job to load OtherClientName from CMS into ElasticSearch
 * 
 * @author CWDS API Team
 */
public class OtherClientNameIndexerJob extends JobBasedOnLastSuccessfulRunTime {

  private static final Logger LOGGER = LogManager.getLogger(OtherClientNameIndexerJob.class);

  private static ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
  private static ObjectMapper MAPPER = new ObjectMapper();

  private OtherClientNameDao otherClientNameDao;
  private ElasticsearchDao elasticsearchDao;

  public OtherClientNameIndexerJob(OtherClientNameDao otherClientNameDao,
      ElasticsearchDao elasticsearchDao, String lastJobRunTimeFilename) {
    super(lastJobRunTimeFilename);
    this.otherClientNameDao = otherClientNameDao;
    this.elasticsearchDao = elasticsearchDao;
  }

  public static void main(String... args) throws Exception {
    if (args.length != 2) {
      throw new Error(
          "Usage: java gov.ca.cwds.jobs.OtherClientNameIndexerJob esconfigFileLocation lastJobRunTimeFilename");
    }

    Injector injector = Guice.createInjector(new JobsGuiceInjector());
    SessionFactory sessionFactory =
        injector.getInstance(Key.get(SessionFactory.class, CmsSessionFactory.class));

    OtherClientNameDao otherClientNameDao = new OtherClientNameDao(sessionFactory);

    File file = new File(args[0]);
    ElasticsearchConfiguration configuration =
        YAML_MAPPER.readValue(file, ElasticsearchConfiguration.class);
    ElasticsearchDao elasticsearchDao = new ElasticsearchDao(configuration);

    OtherClientNameIndexerJob job =
        new OtherClientNameIndexerJob(otherClientNameDao, elasticsearchDao, args[1]);
    try {
      job.run();
    } catch (JobsException e) {
      LOGGER.error("Unable to complete job", e);
    } finally {
      sessionFactory.close();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see gov.ca.cwds.jobs.JobBasedOnLastSuccessfulRunTime#_run(java.util.Date)
   */
  @Override
  public Date _run(Date lastSuccessfulRunTime) {
    try {
      elasticsearchDao.start();
      List<OtherClientName> results =
          otherClientNameDao.findAllUpdatedAfter(lastSuccessfulRunTime);;
      LOGGER.info(MessageFormat.format("Found {0} people to index", results.size()));
      Date currentTime = new Date();
      for (OtherClientName otherClientName : results) {
        final Person esPerson = new Person(otherClientName.getPrimaryKey().toString(),
            otherClientName.getFirstName(), otherClientName.getLastName(), "", // Gender
            "", // DOB
            "", // SSN
            otherClientName.getClass().getName(), MAPPER.writeValueAsString(otherClientName));
        indexDocument(esPerson);
      }
      LOGGER.info(MessageFormat.format("Indexed {0} people", results.size()));
      LOGGER.info(MessageFormat.format("Updating last succesful run time to {0}",
          jobDateFormat.format(currentTime)));
      return currentTime;
    } catch (IOException e) {
      throw new JobsException("Could not parse configuration file", e);
    } catch (Exception e) {
      throw new JobsException(e);
    } finally {
      try {
        elasticsearchDao.stop();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void indexDocument(Person person) throws Exception {
    String document = MAPPER.writeValueAsString(person);
    elasticsearchDao.index(document, person.getId().toString());
  }
}
