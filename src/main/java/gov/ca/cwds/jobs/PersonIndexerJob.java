package gov.ca.cwds.jobs;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

import gov.ca.cwds.dao.elasticsearch.ElasticsearchConfiguration;
import gov.ca.cwds.dao.elasticsearch.ElasticsearchDao;
import gov.ca.cwds.jobs.inject.JobsGuiceInjector;
import gov.ca.cwds.rest.api.domain.DomainObject;
import gov.ca.cwds.rest.api.domain.PostedPerson;
import gov.ca.cwds.rest.api.persistence.ns.Person;
import gov.ca.cwds.rest.jdbi.ns.PersonDao;

public class PersonIndexerJob extends JobBasedOnLastSuccessfulRunTime {

  private static final Logger LOGGER = LogManager.getLogger(PersonIndexerJob.class);

  private static ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
  private static ObjectMapper MAPPER = new ObjectMapper();

  private PersonDao personDao;
  private ElasticsearchDao elasticsearchDao;

  public PersonIndexerJob(PersonDao personDao, ElasticsearchDao elasticsearchDao) {
    super();
    this.personDao = personDao;
    this.elasticsearchDao = elasticsearchDao;
  }

  public static void main(String... args) throws Exception {
    if (args.length != 1) {
      throw new Error("Usage: java gov.ca.cwds.jobs.PersonIndexerJob configFileLocation");
    }

    Injector injector = Guice.createInjector(new JobsGuiceInjector());
    SessionFactory sessionFactory = injector.getInstance(SessionFactory.class);
    PersonDao personDao = new PersonDao(sessionFactory);

    File file = new File(args[0]);
    ElasticsearchConfiguration configuration =
        YAML_MAPPER.readValue(file, ElasticsearchConfiguration.class);
    ElasticsearchDao elasticsearchDao = new ElasticsearchDao(configuration);


    PersonIndexerJob job = new PersonIndexerJob(personDao, elasticsearchDao);
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
  @Override
  public Date _run(Date lastSuccessfulRunTime) {
    try {
      List<Person> results = personDao.findAllUpdatedAfter(lastSuccessfulRunTime);
      Date currentTime = new Date();
      elasticsearchDao.start();
      for (Person person : results) {
        PostedPerson postedPerson = new PostedPerson(person);
        gov.ca.cwds.rest.api.elasticsearch.ns.Person esPerson =
            new gov.ca.cwds.rest.api.elasticsearch.ns.Person(postedPerson,
                DomainObject.cookTimestamp(person.getLastUpdatedTime()));
        esPerson.setId(person.getId().toString());


        indexDocument(esPerson);
      }
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

