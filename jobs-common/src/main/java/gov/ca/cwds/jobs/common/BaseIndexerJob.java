package gov.ca.cwds.jobs.common;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import gov.ca.cwds.jobs.common.config.JobOptions;
import gov.ca.cwds.jobs.common.exception.JobExceptionHandler;
import gov.ca.cwds.jobs.common.exception.JobsException;
import gov.ca.cwds.jobs.common.inject.LastRunDir;
import gov.ca.cwds.jobs.common.job.Job;
import gov.ca.cwds.jobs.common.job.timestamp.FilesystemTimestampOperator;
import gov.ca.cwds.jobs.common.job.timestamp.TimestampOperator;
import gov.ca.cwds.jobs.common.job.utils.ConsumerCounter;
import gov.ca.cwds.rest.ElasticsearchConfiguration;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static gov.ca.cwds.jobs.common.elastic.ElasticUtils.createAndConfigureESClient;

/**
 * @author CWDS TPT-2
 */
public abstract class BaseIndexerJob<T extends ElasticsearchConfiguration> extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseIndexerJob.class);

  private JobOptions jobOptions;

  protected abstract T getJobsConfiguration();

  @Override
  protected void configure() {
    bind(JobOptions.class).toInstance(jobOptions);
    bindConstant().annotatedWith(LastRunDir.class).to(jobOptions.getLastRunLoc());
    bind(TimestampOperator.class).to(FilesystemTimestampOperator.class).asEagerSingleton();
  }

  private JobOptions validateJobOptions(JobOptions jobOptions) {
    // check option: -c
    File configFile = new File(jobOptions.getEsConfigLoc());
    if (!configFile.exists()) {
      throw new JobsException(
          "job arguments error: specified config file " + configFile.getPath() + " not found");
    }

    // check option: -l
    File timeFilesDir = new File(jobOptions.getLastRunLoc());
    if (!timeFilesDir.exists()) {
      if (timeFilesDir.mkdir() && (LOGGER.isInfoEnabled())) {
        LOGGER.info(getPathToOutputDirectory() + " was created in file system");
      }
    }

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Using " + getPathToOutputDirectory() + " as output folder");
    }
    return jobOptions;
  }

  private String getPathToOutputDirectory() {
    return Paths.get(jobOptions.getLastRunLoc()).normalize().toAbsolutePath().toString();
  }

  protected void run(String[] args) {
    Injector injector = null;
    try {
      final JobOptions jobOptions = JobOptions.parseCommandLine(args);
      setJobOptions(jobOptions);
      validateJobOptions(jobOptions);
      injector = Guice.createInjector(this);
      injector.getInstance(Job.class).run();
      if (!JobExceptionHandler.isExceptionHappened()) {
        injector.getInstance(TimestampOperator.class).writeTimestamp(LocalDateTime.now());
      }
      System.out.println(
          String.format("Added %s entities to ES bulk uploader", ConsumerCounter.getCounter()));
    } catch (RuntimeException e) {
      LOGGER.error("ERROR: ", e.getMessage(), e);
      System.exit(1);
    } finally {
      JobExceptionHandler.reset();
      close(injector);
    }
  }

  void setJobOptions(JobOptions jobOptions) {
    this.jobOptions = jobOptions;
  }

  public JobOptions getJobOptions() {
    return jobOptions;
  }

  @Provides
  @Inject
  // the client should not be closed here, it is closed when job is done
  @SuppressWarnings("squid:S2095")
  public Client elasticsearchClient(BaseJobConfiguration config) {
    return createAndConfigureESClient(config);
  }

  @Provides
  @Singleton
  @Inject
  public ElasticsearchIndexerDao elasticsearchDao(Client client,
      BaseJobConfiguration configuration) {

    ElasticsearchIndexerDao esIndexerDao = new ElasticsearchIndexerDao(client,
        configuration);
    esIndexerDao.createIndexIfMissing();

    return esIndexerDao;
  }

  protected void close(Injector inject) {
    //empty by default
  }

}
