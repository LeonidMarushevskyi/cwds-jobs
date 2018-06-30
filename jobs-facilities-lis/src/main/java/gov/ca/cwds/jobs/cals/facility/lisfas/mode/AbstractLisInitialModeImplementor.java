package gov.ca.cwds.jobs.cals.facility.lisfas.mode;

import com.google.inject.Inject;
import gov.ca.cwds.jobs.cals.facility.ChangedFacilityDto;
import gov.ca.cwds.jobs.cals.facility.lisfas.identifier.LisChangedEntitiesIdentifiersService;
import gov.ca.cwds.jobs.cals.facility.lisfas.savepoint.LicenseNumberSavePoint;
import gov.ca.cwds.jobs.cals.facility.lisfas.savepoint.LicenseNumberSavePointService;
import gov.ca.cwds.jobs.cals.facility.lisfas.savepoint.LisTimestampSavePoint;
import gov.ca.cwds.jobs.cals.facility.lisfas.savepoint.LisTimestampSavePointContainer;
import gov.ca.cwds.jobs.cals.facility.lisfas.savepoint.LisTimestampSavePointContainerService;
import gov.ca.cwds.jobs.common.batch.JobBatch;
import gov.ca.cwds.jobs.common.batch.JobBatchSize;
import gov.ca.cwds.jobs.common.identifier.ChangedEntityIdentifier;
import gov.ca.cwds.jobs.common.mode.AbstractJobModeImplementor;
import gov.ca.cwds.jobs.common.mode.DefaultJobMode;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLisInitialModeImplementor extends
    AbstractJobModeImplementor<ChangedFacilityDto, LicenseNumberSavePoint, DefaultJobMode> {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(AbstractLisInitialModeImplementor.class);

  @Inject
  @JobBatchSize
  private int batchSize;

  protected int lastId;

  @Inject
  private LicenseNumberSavePointService savePointService;

  @Inject
  protected LisChangedEntitiesIdentifiersService changedEntitiesIdentifiersService;

  @Inject
  private LisTimestampSavePointContainerService savePointContainerService;

  @Override
  public List<JobBatch<LicenseNumberSavePoint>> getNextPortion() {
    List<ChangedEntityIdentifier<LicenseNumberSavePoint>> identifiers = getNextPage();
    if (identifiers.isEmpty()) {
      return Collections.emptyList();
    }
    lastId = getLastId(identifiers);
    LOGGER.info("Next page prepared. List size: {}. Last Id: {}", identifiers.size(), lastId);
    //TODO: remove after testing of bug hypothesis
    if (identifiers.size() > batchSize) {
      identifiers = identifiers.subList(0, batchSize);
      lastId = getLastId(identifiers);
      LOGGER.info("Next page cut to the batch size. Adjusted list size: {}. Last Id: {}",
          identifiers.size(), lastId);
    }
    JobBatch<LicenseNumberSavePoint> jobBatch = new JobBatch<>(identifiers);
    return Collections.singletonList(jobBatch);
  }

  private List<ChangedEntityIdentifier<LicenseNumberSavePoint>> getNextPage() {
    return changedEntitiesIdentifiersService
        .getIdentifiersForInitialLoad(lastId);
  }

  protected static int getLastId(
      List<ChangedEntityIdentifier<LicenseNumberSavePoint>> identifiers) {
    identifiers.sort(Comparator.comparing(ChangedEntityIdentifier::getIntId));
    return identifiers.get(identifiers.size() - 1).getIntId();
  }

  @Override
  public LicenseNumberSavePoint defineSavepoint(JobBatch<LicenseNumberSavePoint> jobBatch) {
    return savePointService.defineSavepoint(jobBatch);
  }

  @Override
  protected void doFinalizeJob() {
    LisTimestampSavePoint lisTimestampSavePoint = new LisTimestampSavePoint(
        changedEntitiesIdentifiersService.findMaxTimestamp());
    LOGGER.info("Updating job save point to the last batch save point {}", lisTimestampSavePoint);
    DefaultJobMode nextJobMode = DefaultJobMode.INCREMENTAL_LOAD;
    LOGGER.info("Updating next job mode to the {}", nextJobMode);
    LisTimestampSavePointContainer savePointContainer = new LisTimestampSavePointContainer();
    savePointContainer.setJobMode(nextJobMode);
    savePointContainer.setSavePoint(lisTimestampSavePoint);
    savePointContainerService.writeSavePointContainer(savePointContainer);
  }

}
