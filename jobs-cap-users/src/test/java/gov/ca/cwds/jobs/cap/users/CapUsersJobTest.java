package gov.ca.cwds.jobs.cap.users;

import static gov.ca.cwds.jobs.common.mode.DefaultJobMode.INCREMENTAL_LOAD;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import gov.ca.cwds.jobs.cap.users.savepoint.CapUsersSavePoint;
import gov.ca.cwds.jobs.cap.users.savepoint.CapUsersSavePointContainer;
import gov.ca.cwds.jobs.cap.users.service.CapUsersSavePointContainerService;
import gov.ca.cwds.jobs.common.configuration.JobConfiguration;
import gov.ca.cwds.jobs.common.configuration.JobOptions;
import gov.ca.cwds.jobs.common.core.JobRunner;
import gov.ca.cwds.jobs.common.inject.JobModule;
import gov.ca.cwds.jobs.common.util.LastRunDirHelper;
import gov.ca.cwds.test.support.DatabaseHelper;
import io.dropwizard.db.DataSourceFactory;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import liquibase.exception.LiquibaseException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CapUsersJobTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(CapUsersJobTest.class);
  private static final String SCHEMA_NAME = "CWSCMS";

  private static final LocalDateTime INITIAL_USERID_TIMESTAMP = LocalDateTime
      .of(2018, 2, 6, 7, 4, 36, 471000000);
  private static final LocalDateTime INITIAL_CWS_OFFICE_TIMESTAMP = LocalDateTime
      .of(2018, 2, 5, 10, 52, 34, 55000000);
  private static final LocalDateTime INITIAL_STAFF_PERSON_TIMESTAMP = LocalDateTime
      .of(2017, 9, 25, 11, 52, 3, 699000000);

  private static LastRunDirHelper lastRunDirHelper = new LastRunDirHelper("cap_job_temp");
  private CapUsersSavePointContainerService savePointContainerService =
      new CapUsersSavePointContainerService(
          lastRunDirHelper.getSavepointContainerFolder().toString());

  private DatabaseHelper databaseHelper;

  @Before
  public void init() {

    LOGGER.info("Setup database has been started!!!");
    try {
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("schema.name", SCHEMA_NAME);
      getDataBaseHelper().runScript("liquibase/cws_init_load.xml", parameters, SCHEMA_NAME);
    } catch (LiquibaseException e) {
      LOGGER.error(e.getMessage(), e);
    }
    LOGGER.info("Database setup has been finished!!!");
  }

  @Test
  public void capUsersJobTest() throws IOException, LiquibaseException {
    try {
      lastRunDirHelper.deleteSavePointContainerFolder();
      testInitialLoad();
      testIncrementalLoad();
    } finally {
      lastRunDirHelper.deleteSavePointContainerFolder();
      TestCapUserWriter.reset();
    }
  }

  private void testIncrementalLoad() throws LiquibaseException {
    runJob();
    assertEquals(0, TestCapUserWriter.getItems().size());
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime userIdTimestamp = now.plusSeconds(1);
    LocalDateTime cwsOfficeTimestamp = now.plusSeconds(2);
    LocalDateTime staffPersonAndOfficeTimestamp = now.plusSeconds(3);

    addCwsDataForIncrementalLoad(1, userIdTimestamp);
    runJob();
    assertEquals(4, TestCapUserWriter.getItems().size());
    SavePointCheckRequest savePointCheckRequest = new SavePointCheckRequest();
    savePointCheckRequest.timeBeforeStart = now;
    savePointCheckRequest.userIdTimestamp = userIdTimestamp;
    savePointCheckRequest.cwsOfficeTimestamp = INITIAL_CWS_OFFICE_TIMESTAMP;
    savePointCheckRequest.staffPersonTimeStamp = INITIAL_STAFF_PERSON_TIMESTAMP;
    assertSavePoint(savePointCheckRequest);

    addCwsDataForIncrementalLoad(2, cwsOfficeTimestamp);
    runJob();
    assertEquals(1, TestCapUserWriter.getItems().size());
    savePointCheckRequest.timeBeforeStart = now;
    savePointCheckRequest.userIdTimestamp = userIdTimestamp;
    savePointCheckRequest.cwsOfficeTimestamp = cwsOfficeTimestamp;
    savePointCheckRequest.staffPersonTimeStamp = INITIAL_STAFF_PERSON_TIMESTAMP;
    assertSavePoint(savePointCheckRequest);

    addCwsDataForIncrementalLoad(3, staffPersonAndOfficeTimestamp);
    runJob();
    assertEquals(3, TestCapUserWriter.getItems().size());
    savePointCheckRequest.timeBeforeStart = now;
    savePointCheckRequest.userIdTimestamp = userIdTimestamp;
    savePointCheckRequest.cwsOfficeTimestamp = staffPersonAndOfficeTimestamp;
    savePointCheckRequest.staffPersonTimeStamp = staffPersonAndOfficeTimestamp;
    assertSavePoint(savePointCheckRequest);

    MockedIdmService.capChanges = true;
    runJob();
    assertEquals(2, TestCapUserWriter.getItems().size());

  }

  private void testInitialLoad() {
    LocalDateTime timestampBeforeStart = LocalDateTime.now();
    assertEquals(0, TestCapUserWriter.getItems().size());
    runJob();
    assertEquals(MockedIdmService.NUMBER_OF_USERS, TestCapUserWriter.getItems().size());
    SavePointCheckRequest savePointCheckRequest = new SavePointCheckRequest();
    savePointCheckRequest.timeBeforeStart = timestampBeforeStart;
    savePointCheckRequest.userIdTimestamp = INITIAL_USERID_TIMESTAMP;
    savePointCheckRequest.cwsOfficeTimestamp = INITIAL_CWS_OFFICE_TIMESTAMP;
    savePointCheckRequest.staffPersonTimeStamp = INITIAL_STAFF_PERSON_TIMESTAMP;
    assertSavePoint(savePointCheckRequest);
  }

  private void runJob() {
    JobOptions jobOptions = JobOptions.parseCommandLine(getModuleArgs());
    CapUsersJobConfiguration jobConfiguration = JobConfiguration
        .getJobsConfiguration(CapUsersJobConfiguration.class, jobOptions.getConfigFileLocation());
    JobModule jobModule = new JobModule(jobOptions.getLastRunLoc());
    CapUsersJobModule capUsersJobModule = new CapUsersJobModule(jobConfiguration,
        jobOptions.getLastRunLoc());
    capUsersJobModule.setIdmService(MockedIdmService.class);
    jobModule.addModule(capUsersJobModule);
    TestCapUserWriter.reset();
    capUsersJobModule.setCapElasticWriterClass(TestCapUserWriter.class);
    JobRunner.run(jobModule);
  }

  private void addCwsDataForIncrementalLoad(int i, LocalDateTime timestamp)
      throws LiquibaseException {
    DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("now", datetimeFormatter.format(timestamp));
    String scriptName;
    switch (i) {
      case 1:
        scriptName = "liquibase/cws_userid_changes.xml";
        break;
      case 2:
        scriptName = "liquibase/cws_office_changes.xml";
        break;
      case 3:
        scriptName = "liquibase/cws_staffperson_and_office_changes.xml";
        break;
      default:
        scriptName = "not valid name";
    }
    getDataBaseHelper()
        .runScript(scriptName, parameters, SCHEMA_NAME);
  }

  private DatabaseHelper getDataBaseHelper() {
    if (databaseHelper == null) {
      DataSourceFactory cwsDataSourceFactory = getCapUsersJobConfiguration()
          .getCmsDataSourceFactory();
      databaseHelper = new DatabaseHelper(cwsDataSourceFactory.getUrl(),
          cwsDataSourceFactory.getUser(), cwsDataSourceFactory.getPassword());
    }
    return databaseHelper;
  }

  private String[] getModuleArgs() {
    return new String[]{"-c", getConfigFilePath(), "-l",
        lastRunDirHelper.getSavepointContainerFolder().toString()};
  }

  private static String getConfigFilePath() {
    return Paths.get("src", "test", "resources", "cap-users-job-test.yaml")
        .normalize().toAbsolutePath().toString();
  }

  private static CapUsersJobConfiguration getCapUsersJobConfiguration() {
    CapUsersJobConfiguration capUsersJobConfiguration =
        JobConfiguration.getJobsConfiguration(CapUsersJobConfiguration.class, getConfigFilePath());
    DataSourceFactory dataSourceFactory = capUsersJobConfiguration.getCmsDataSourceFactory();
    dataSourceFactory.setUrl(dataSourceFactory.getProperties().get("hibernate.connection.url"));
    dataSourceFactory
        .setUser(dataSourceFactory.getProperties().get("hibernate.connection.username"));
    dataSourceFactory
        .setPassword(dataSourceFactory.getProperties().get("hibernate.connection.password"));

    return capUsersJobConfiguration;
  }

  private void assertSavePoint(SavePointCheckRequest savePointCheckRequest) {
    CapUsersSavePointContainer savePointContainer = (CapUsersSavePointContainer) savePointContainerService
        .readSavePointContainer(CapUsersSavePointContainer.class);
    CapUsersSavePoint savePoint = savePointContainer.getSavePoint();
    assertTrue(savePoint.getCognitoTimestamp().isAfter(savePointCheckRequest.timeBeforeStart));
    assertTrue(savePoint.getCognitoTimestamp().isBefore(LocalDateTime.now()));
    assertEquals(savePointCheckRequest.userIdTimestamp, savePoint.getUserIdTimestamp());
    assertEquals(savePointCheckRequest.cwsOfficeTimestamp, savePoint.getCwsOfficeTimestamp());
    assertEquals(savePointCheckRequest.staffPersonTimeStamp, savePoint.getStaffPersonTimestamp());
    assertEquals(INCREMENTAL_LOAD, savePointContainer.getJobMode());
  }

  private class SavePointCheckRequest {

    private LocalDateTime timeBeforeStart;
    private LocalDateTime userIdTimestamp;
    private LocalDateTime cwsOfficeTimestamp;
    private LocalDateTime staffPersonTimeStamp;

  }

}
