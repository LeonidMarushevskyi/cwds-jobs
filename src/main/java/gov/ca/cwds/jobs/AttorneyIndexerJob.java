package gov.ca.cwds.jobs;

import gov.ca.cwds.jobs.schedule.MasterJobRunner;

/**
 * Original Attorney job now in class {@link OrigAttorneyIndexerJob}.
 * 
 * <p>
 * This job is a temporary facade to test {@link MasterJobRunner}, until DevOps adds a new job.
 * </p>
 * 
 * @author CWDS API Team
 */
public class AttorneyIndexerJob {

  private AttorneyIndexerJob() {
    // no-op
  }

  /**
   * Batch job entry point.
   * 
   * @param args command line arguments
   */
  public static void main(String... args) {
    MasterJobRunner.main(args);
  }

}
