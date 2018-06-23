package gov.ca.cwds.jobs.cals.facility;

import gov.ca.cwds.cals.service.dto.FacilityDTO;
import gov.ca.cwds.dto.BaseDTO;
import gov.ca.cwds.jobs.common.ChangedDTO;
import gov.ca.cwds.jobs.common.RecordChangeOperation;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author CWDS TPT-2
 */
public class ChangedFacilityDto extends BaseDTO implements ChangedDTO<FacilityDTO>, Serializable {

  private static final long serialVersionUID = 1L;

  private FacilityDTO facilityDTO;

  private RecordChangeOperation recordChangeOperation;

  public ChangedFacilityDto(FacilityDTO facilityDTO, RecordChangeOperation recordChangeOperation) {
    this.facilityDTO = facilityDTO;
    this.recordChangeOperation = recordChangeOperation;
  }

  public ChangedFacilityDto() {
    //default constructor
  }

  @Override
  public RecordChangeOperation getRecordChangeOperation() {
    return recordChangeOperation;
  }

  @Override
  public FacilityDTO getDTO() {
    return facilityDTO;
  }

  @Override
  public String getId() {
    return facilityDTO.getId();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChangedFacilityDto that = (ChangedFacilityDto) o;
    return recordChangeOperation == that.recordChangeOperation && Objects
        .equals(facilityDTO, that.facilityDTO);
  }

  @Override
  public int hashCode() {
    return Objects.hash(facilityDTO, recordChangeOperation);
  }
}
