package gov.ca.cwds.jobs.common.savepoint;

import gov.ca.cwds.jobs.common.batch.JobBatch;

/**
 * Created by Alexander Serbin on 6/20/2018.
 */
public interface SavePointService<S extends SavePoint> {

  /**
   * Loads save point specific to job mode.
   *
   * @return save point
   */
  S loadSavePoint();

  /**
   * Builds save point
   */
  S defineSavepoint(JobBatch<S> jobBatch);

  /**
   * Saves save point spesific to job mode
   */
  void saveSavePoint(S savePoint);

  /**
   * Get savepoint container class.
   */
  Class<? extends SavePointContainer<? extends S>> getSavePointContainerClass();

}
