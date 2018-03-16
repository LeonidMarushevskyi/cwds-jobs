package gov.ca.cwds.jobs.cals.facility;

import static gov.ca.cwds.cals.Constants.UnitOfWork.CMS;

import com.google.inject.Inject;
import com.jcabi.aspects.Loggable;
import gov.ca.cwds.cals.service.FacilityService;
import gov.ca.cwds.cals.service.builder.FacilityParameterObjectBuilder;
import gov.ca.cwds.cals.service.dto.FacilityChildDTO;
import gov.ca.cwds.cals.service.dto.FacilityDTO;
import gov.ca.cwds.cals.web.rest.parameter.FacilityParameterObject;
import gov.ca.cwds.jobs.common.identifier.ChangedEntityIdentifier;
import gov.ca.cwds.jobs.common.job.ChangedEntitiesService;
import io.dropwizard.hibernate.UnitOfWork;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author CWDS TPT-2
 */
public class ChangedFacilityService extends FacilityService implements
    ChangedEntitiesService<ChangedFacilityDTO> {

  private static final Logger LOG = LoggerFactory.getLogger(ChangedFacilityService.class);

  @Inject
  private FacilityParameterObjectBuilder facilityParameterObjectBuilder;

  @Override
  public Stream<ChangedFacilityDTO> loadEntities(List<ChangedEntityIdentifier> identifiers) {
    return identifiers.stream().map(this::findFacilityById);
  }


  @Loggable(Loggable.DEBUG)
  @UnitOfWork(CMS)
  protected FacilityParameterObject createFacilityParameterObject(String id) {
    return facilityParameterObjectBuilder.createFacilityParameterObject(id);
  }

  @Loggable(Loggable.DEBUG)
  protected ChangedFacilityDTO findFacilityById(ChangedEntityIdentifier identifier) {
    try {
    //
      FacilityDTO facilityDTO = findByParameterObject(
          createFacilityParameterObject(identifier.getId()));
      List<FacilityChildDTO> childrenList = findFacilityChildredByFacilityId(identifier.getId());
     if (LOG.isInfoEnabled() && !childrenList.isEmpty()) {
       LOG.info("Found {} children for facility with ID: {}", childrenList.size(), facilityDTO.getId());
       LOG.info(Arrays.toString(childrenList.toArray()));
     }
     //
      if (facilityDTO == null) {
        LOG.error("Can't get facility by id " + identifier.getId());
        throw new IllegalStateException("FacilityDTO must not be null!!!");
      } else {
        if (LOG.isInfoEnabled()) {
          LOG.info("Found facility by ID {}", facilityDTO.getId());
        }
      }
      return new ChangedFacilityDTO(facilityDTO, identifier.getRecordChangeOperation());
    } catch (Exception e) {
      LOG.error("Can't get facility by id " + identifier.getId(), e);
      throw new IllegalStateException(
          String.format("Can't get facility by id %s", identifier.getId()), e);
    }
  }

}
