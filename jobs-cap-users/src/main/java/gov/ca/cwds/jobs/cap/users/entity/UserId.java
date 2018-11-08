package gov.ca.cwds.jobs.cap.users.entity;

import gov.ca.cwds.data.legacy.cms.CmsPersistentObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@NamedQueries({
    @NamedQuery(
        name = UserId.CWSCMS_INCREMENTAL_LOAD_QUERY_NAME,
        query = UserId.CWS_CMS_INCREMENTAL_LOAD_QUERY
    ),
    @NamedQuery(
        name = UserId.GET_MAX_LAST_UPDATED_TIME,
        query = UserId.GET_MAX_LAST_UPDATED_TIME_QUERY
    )
})
@Entity
@Table(name = "USERID_T")
public class UserId extends CmsPersistentObject {

  public static final String USERID_DATE_AFTER = "userIdDateAfter";
  public static final String OFFICE_DATE_AFTER = "officeDateAfter";
  public static final String STAFF_PERSON_DATE_AFTER = "staffPersonDateAfter";

  static final String CWS_CMS_INCREMENTAL_LOAD_QUERY =
          "select distinct u.logonId from UserId u left join StaffPerson s on u.staffPersonId = s.id " +
                  "left join CwsOffice o on s.cwsOffice = o.officeId " +
              " where u.lastUpdatedTime > :" + USERID_DATE_AFTER + " or s.lastUpdatedTime > :" +
              STAFF_PERSON_DATE_AFTER + " or o.lastUpdatedTime > :" + OFFICE_DATE_AFTER;

  static final String GET_MAX_LAST_UPDATED_TIME_QUERY = "select max(lastUpdatedTime) from UserId";

  private static final long serialVersionUID = 2128876585165704533L;

  public static final String CWSCMS_INCREMENTAL_LOAD_QUERY_NAME = "UserId.cwscmsIncrementalLoadQuery";
  public static final String GET_MAX_LAST_UPDATED_TIME = "UserId.getMaxLastUpdatedTime";


  @Id
  @Column(name = "IDENTIFIER")
  private String id;

  @Column(name = "FKSTFPERST")
  private String staffPersonId;

  @Column(name = "LOGON_ID")
  private String logonId;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getStaffPersonId() {
    return staffPersonId;
  }

  public void setStaffPersonId(String staffPersonId) {
    this.staffPersonId = staffPersonId;
  }

  public String getLogonId() {
    return logonId;
  }

  public void setLogonId(String logonId) {
    this.logonId = logonId;
  }

  @Override
  public String getPrimaryKey() {
    return this.getId();
  }
}
