package gov.ca.cwds.jobs.common;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import gov.ca.cwds.jobs.common.api.JobModeImplementor;
import gov.ca.cwds.jobs.common.api.JobModeService;
import gov.ca.cwds.jobs.common.config.JobOptions;
import gov.ca.cwds.jobs.common.core.Job;
import gov.ca.cwds.jobs.common.core.JobImpl;
import gov.ca.cwds.jobs.common.entity.ChangedEntityService;
import gov.ca.cwds.jobs.common.identifier.ChangedEntitiesIdentifiersService;
import gov.ca.cwds.jobs.common.inject.AbstractBaseJobModule;
import gov.ca.cwds.jobs.common.mode.DefaultJobMode;
import gov.ca.cwds.jobs.common.mode.LocalDateTimeDefaultJobModeService;
import gov.ca.cwds.jobs.common.mode.TimestampIncrementalModeImplementor;
import gov.ca.cwds.jobs.common.mode.TimestampInitialModeImplementor;
import gov.ca.cwds.jobs.common.mode.TimestampInitialResumeModeImplementor;
import gov.ca.cwds.jobs.common.savepoint.LocalDateTimeSavePointContainerService;
import gov.ca.cwds.jobs.common.savepoint.LocalDateTimeSavePointService;
import gov.ca.cwds.jobs.common.savepoint.SavePointContainerService;
import gov.ca.cwds.jobs.common.savepoint.SavePointService;
import gov.ca.cwds.jobs.common.savepoint.TimestampSavePoint;
import java.time.LocalDateTime;

/**
 * Created by Alexander Serbin on 3/5/2018.
 */
public class TestModule extends AbstractBaseJobModule {

  private ChangedEntityService<TestEntity> changedEntityService;
  private ChangedEntitiesIdentifiersService<TimestampSavePoint<LocalDateTime>> changedEntitiesIdentifiers;

  public TestModule(String[] args) {
    super(args);
    initDefaults();
  }

  private void initDefaults() {
    setElasticSearchModule(new AbstractModule() {
      @Override
      protected void configure() {
      }
    });
  }

  @Override
  protected BaseJobConfiguration getJobsConfiguration(JobOptions jobsOptions) {
    TestJobConfiguration testJobConfiguration = new TestJobConfiguration();
    testJobConfiguration.setBatchSize(1);
    return testJobConfiguration;
  }

  @Override
  protected void configure() {
    super.configure();
    bind(Job.class).to(TestJobImpl.class);
    bind(new TypeLiteral<JobModeService<DefaultJobMode>>() {
    }).to(LocalDateTimeDefaultJobModeService.class);
    bindJobModeImplementor();
    bind(
        new TypeLiteral<SavePointContainerService<TimestampSavePoint<LocalDateTime>, DefaultJobMode>>() {
        }).to(LocalDateTimeSavePointContainerService.class);
    bind(new TypeLiteral<SavePointService<TimestampSavePoint<LocalDateTime>, DefaultJobMode>>() {
    }).to(LocalDateTimeSavePointService.class);
    bind(new TypeLiteral<ChangedEntityService<TestEntity>>() {
    }).toInstance(changedEntityService);
    bind(
        new TypeLiteral<ChangedEntitiesIdentifiersService<TimestampSavePoint<LocalDateTime>>>() {
        }).toInstance(changedEntitiesIdentifiers);
    bind(new TypeLiteral<BulkWriter<TestEntity>>() {
    }).to(TestEntityWriter.class);
  }

  private void bindJobModeImplementor() {
    Class<? extends JobModeImplementor<TestEntity, TimestampSavePoint<LocalDateTime>, DefaultJobMode>> clazz = null;

    LocalDateTimeDefaultJobModeService timestampDefaultJobModeService =
        new LocalDateTimeDefaultJobModeService();
    LocalDateTimeSavePointContainerService savePointContainerService =
        new LocalDateTimeSavePointContainerService(getJobOptions().getLastRunLoc());
    timestampDefaultJobModeService.setSavePointContainerService(savePointContainerService);
    switch (timestampDefaultJobModeService.getCurrentJobMode()) {
      case INITIAL_LOAD:
        clazz = TestInitialModeImplementor.class;
        break;
      case INITIAL_LOAD_RESUME:
        clazz = TestInitialResumeModeImplementor.class;
        break;
      case INCREMENTAL_LOAD:
        clazz = TestIncrementalModeImplementor.class;
        break;
    }
    bind(
        new TypeLiteral<JobModeImplementor<TestEntity, TimestampSavePoint<LocalDateTime>, DefaultJobMode>>() {
        }).to(clazz);
  }

  private static class TestJobImpl extends
      JobImpl<TestEntity, TimestampSavePoint<LocalDateTime>, DefaultJobMode> {

  }

  private static class TestInitialModeImplementor extends
      TimestampInitialModeImplementor<TestEntity> {

  }

  private static class TestInitialResumeModeImplementor extends
      TimestampInitialResumeModeImplementor<TestEntity> {

  }

  private static class TestIncrementalModeImplementor extends
      TimestampIncrementalModeImplementor<TestEntity> {

  }

  static class TestEntityWriter extends TestWriter<TestEntity> {

  }

  public void setChangedEntityService(ChangedEntityService<TestEntity> changedEntityService) {
    this.changedEntityService = changedEntityService;
  }

  public void setChangedEntitiesIdentifiers(
      ChangedEntitiesIdentifiersService<TimestampSavePoint<LocalDateTime>> changedEntitiesIdentifiers) {
    this.changedEntitiesIdentifiers = changedEntitiesIdentifiers;
  }

}
