package gov.ca.cwds.jobs.cap.users.job;

import com.google.inject.Inject;
import gov.ca.cwds.jobs.cap.users.savepoint.CapUsersSavePoint;
import gov.ca.cwds.jobs.cap.users.service.CapUsersSavePointService;
import gov.ca.cwds.jobs.common.core.Job;
import gov.ca.cwds.jobs.common.timereport.JobTimeReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCapUsersJob implements Job {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCapUsersJob.class);

  @Inject
  private CapUsersSavePointService savePointService;

  @Override
  public void run() {
    JobTimeReport jobTimeReport = new JobTimeReport();
    CapUsersSavePoint savePoint = savePointService.createSavePoint();
    runJob();
    LOGGER.info("Creating save point savePoint {}", savePoint);
    savePointService.saveSavePoint(savePoint);
    if (LOGGER.isInfoEnabled()) {
      jobTimeReport.printTimeSpent();
    }
  }

  abstract void runJob();

}
