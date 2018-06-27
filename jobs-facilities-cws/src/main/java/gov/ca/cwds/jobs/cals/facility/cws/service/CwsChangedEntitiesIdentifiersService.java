package gov.ca.cwds.jobs.cals.facility.cws.service;

import static gov.ca.cwds.cals.Constants.UnitOfWork.CMS;

import com.google.inject.Inject;
import gov.ca.cwds.jobs.cals.facility.ChangedFacilitiesIdentifiers;
import gov.ca.cwds.jobs.cals.facility.cws.CwsRecordChange;
import gov.ca.cwds.jobs.cals.facility.cws.dao.RecordChangeCwsCmsDao;
import gov.ca.cwds.jobs.common.batch.PageRequest;
import gov.ca.cwds.jobs.common.identifier.ChangedEntitiesIdentifiersService;
import gov.ca.cwds.jobs.common.identifier.ChangedEntityIdentifier;
import gov.ca.cwds.jobs.common.savepoint.TimestampSavePoint;
import io.dropwizard.hibernate.UnitOfWork;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Alexander Serbin on 3/6/2018.
 */

public class CwsChangedEntitiesIdentifiersService implements
    ChangedEntitiesIdentifiersService<TimestampSavePoint, TimestampSavePoint> {

  @Inject
  private RecordChangeCwsCmsDao recordChangeCwsCmsDao;

  @Override
  public List<ChangedEntityIdentifier<TimestampSavePoint>> getIdentifiersForInitialLoad(
      PageRequest pageRequest) {
    return getCwsCmsInitialLoadIdentifiers(pageRequest).sorted().collect(Collectors.toList());
  }

  @Override
  public List<ChangedEntityIdentifier<TimestampSavePoint>> getIdentifiersForResumingInitialLoad(
      TimestampSavePoint timeStampAfter, PageRequest pageRequest) {
    return getCwsCmsResumingInitialLoadIdentifiers(timeStampAfter, pageRequest);
  }

  @Override
  public List<ChangedEntityIdentifier<TimestampSavePoint>> getIdentifiersForIncrementalLoad(
      TimestampSavePoint savePoint,
      PageRequest pageRequest) {
    return getCwsCmsIncrementalLoadIdentifiers(savePoint, pageRequest);
  }

  @UnitOfWork(CMS)
  protected List<ChangedEntityIdentifier<TimestampSavePoint>> getCwsCmsResumingInitialLoadIdentifiers(
      TimestampSavePoint timeStampAfter, PageRequest pageRequest) {
    ChangedFacilitiesIdentifiers<TimestampSavePoint> changedEntityIdentifiers =
        new ChangedFacilitiesIdentifiers<>();
    recordChangeCwsCmsDao.getResumeInitialLoadStream(timeStampAfter.getTimestamp(), pageRequest).
        map(CwsRecordChange::valueOf).forEach(changedEntityIdentifiers::add);
    return changedEntityIdentifiers.newStream().filter(Objects::nonNull).sorted()
        .collect(Collectors.toList());
  }

  @UnitOfWork(CMS)
  protected Stream<ChangedEntityIdentifier<TimestampSavePoint>> getCwsCmsInitialLoadIdentifiers(
      PageRequest pageRequest) {
    ChangedFacilitiesIdentifiers<TimestampSavePoint> changedEntityIdentifiers =
        new ChangedFacilitiesIdentifiers<>();
    recordChangeCwsCmsDao.getInitialLoadStream(pageRequest).
        map(CwsRecordChange::valueOf).forEach(changedEntityIdentifiers::add);
    return changedEntityIdentifiers.newStream().filter(Objects::nonNull);
  }

  @UnitOfWork(CMS)
  protected List<ChangedEntityIdentifier<TimestampSavePoint>> getCwsCmsIncrementalLoadIdentifiers(
      TimestampSavePoint dateAfter, PageRequest pageRequest) {
    ChangedFacilitiesIdentifiers<TimestampSavePoint> changedEntityIdentifiers =
        new ChangedFacilitiesIdentifiers<>();
    recordChangeCwsCmsDao.getIncrementalLoadStream(dateAfter.getTimestamp(), pageRequest).
        map(CwsRecordChange::valueOf).forEach(changedEntityIdentifiers::add);
    return changedEntityIdentifiers.newStream().filter(Objects::nonNull).sorted()
        .collect(Collectors.toList());
  }

}
