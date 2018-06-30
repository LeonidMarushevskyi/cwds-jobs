package gov.ca.cwds.jobs.cals.facility.lisfas.savepoint;

import com.google.inject.Inject;
import gov.ca.cwds.jobs.common.inject.LastRunDir;
import gov.ca.cwds.jobs.common.mode.DefaultJobMode;
import gov.ca.cwds.jobs.common.savepoint.SavePointContainerServiceImpl;

/**
 * Created by Alexander Serbin on 6/29/2018.
 */
public class LisTimestampSavePointContainerService extends
    SavePointContainerServiceImpl<LisTimestampSavePoint, DefaultJobMode> {

  @Inject
  public LisTimestampSavePointContainerService(@LastRunDir String outputDir) {
    super(outputDir);
  }
}
