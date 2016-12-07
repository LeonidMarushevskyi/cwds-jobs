package gov.ca.cwds.jobs;

import gov.ca.cwds.dao.elasticsearch.ElasticsearchConfiguration;
import gov.ca.cwds.dao.elasticsearch.ElasticsearchDao;
import gov.ca.cwds.jobs.inject.CmsSessionFactory;
import gov.ca.cwds.jobs.inject.JobsGuiceInjector;
import gov.ca.cwds.rest.api.persistence.cms.OtherClientName;
import gov.ca.cwds.rest.jdbi.cms.OtherClientNameDao;

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

/**
 * Job to load OtherClientName from CMS into ElasticSearch
 * 
 * @author CWDS API Team
 */
public class OtherClientNameIndexerJob extends JobBasedOnLastSuccessfulRunTime {

  private static final Logger LOGGER = LogManager.getLogger(PersonIndexerJob.class);

  private static ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
  private static ObjectMapper MAPPER = new ObjectMapper();

  private OtherClientNameDao otherClientNameDao;
  private ElasticsearchDao elasticsearchDao;

  public OtherClientNameIndexerJob(OtherClientNameDao otherClientNameDao,
      ElasticsearchDao elasticsearchDao) {
    super();
    this.otherClientNameDao = otherClientNameDao;
    this.elasticsearchDao = elasticsearchDao;
  }

  public static void main(String... args) throws Exception {
    if (args.length != 1) {
      throw new Error("Usage: java gov.ca.cwds.jobs.OtherClientNameIndexerJob configFileLocation");
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
        new OtherClientNameIndexerJob(otherClientNameDao, elasticsearchDao);
    try {
      job.run();
    } catch (JobsException e) {
      LOGGER.error("Unable to complete job", e);
    } finally {
      sessionFactory.close();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.ca.cwds.jobs.JobBasedOnLastSuccessfulRunTime#_run(java.util.Date)
   */
  @SuppressWarnings("null")
  @Override
  public Date _run(Date lastSuccessfulRunTime) {
    try {
      List<OtherClientName> results = otherClientNameDao.findAllUpdatedAfter(lastSuccessfulRunTime);;
      LOGGER.info(MessageFormat.format("Found {0} people to index", results.size()));
      Date currentTime = new Date();
      elasticsearchDao.start();
      for (OtherClientName otherClientName : results) {
        gov.ca.cwds.rest.api.elasticsearch.ns.Person esPerson =
            new gov.ca.cwds.rest.api.elasticsearch.ns.Person(otherClientName.getPrimaryKey(),
                otherClientName.getFirstName(), otherClientName.getLastName(), "", // Gender
                "", // DOB
                "", // SSN
                otherClientName.getClass().getName(), MAPPER.writeValueAsString(otherClientName));
        indexDocument(esPerson);

      }
      LOGGER.info(MessageFormat.format("Indexed {0} people", results.size()));
      LOGGER.info(MessageFormat.format("Updating last succesful run time to {0}",
          DATE_FORMAT.format(currentTime)));
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

  private void indexDocument(gov.ca.cwds.rest.api.elasticsearch.ns.Person person) throws Exception {
    String document = MAPPER.writeValueAsString(person);
    elasticsearchDao.index(document, person.getId().toString());
  }
}
