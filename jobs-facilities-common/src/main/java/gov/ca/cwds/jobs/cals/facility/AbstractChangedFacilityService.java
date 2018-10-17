package gov.ca.cwds.jobs.cals.facility;

import gov.ca.cwds.cals.service.dto.FacilityDTO;
import gov.ca.cwds.jobs.common.entity.ChangedEntityService;
import gov.ca.cwds.jobs.common.identifier.ChangedEntityIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Alexander Serbin on 3/28/2018.
 */
public abstract class AbstractChangedFacilityService implements
    ChangedEntityService<ChangedFacilityDto> {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractChangedFacilityService.class);

  @Override
  public ChangedFacilityDto loadEntity(ChangedEntityIdentifier identifier) {
    String facilityId = null;
    try {
      facilityId = identifier.getId();
      LOG.info("Loading entity by id {}", identifier.getId());
      FacilityDTO facilityDTO = loadEntityById(identifier);
      if (facilityDTO == null) {
        LOG.error("Can't get facility by id {}", facilityId);
        throw new IllegalStateException("FacilityDTO must not be null!!!");
      }
      return new ChangedFacilityDto(facilityDTO, identifier.getRecordChangeOperation());
    } catch (Exception e) {
      LOG.error("Can't get facility by id {}", facilityId, e);
      throw new IllegalStateException(
          String.format("Can't get facility by id %s", facilityId), e);
    }
  }

  protected abstract FacilityDTO loadEntityById(ChangedEntityIdentifier identifier);

}

