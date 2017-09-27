package gov.ca.cwds.data.persistence.cms;

import static gov.ca.cwds.jobs.util.transform.JobTransformUtils.ifNull;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.NamedNativeQueries;
import org.hibernate.annotations.NamedNativeQuery;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.data.es.ElasticSearchAccessLimitation;
import gov.ca.cwds.data.es.ElasticSearchPerson.ElasticSearchPersonSocialWorker;
import gov.ca.cwds.data.es.ElasticSearchPersonAllegation;
import gov.ca.cwds.data.es.ElasticSearchPersonNestedPerson;
import gov.ca.cwds.data.es.ElasticSearchPersonReferral;
import gov.ca.cwds.data.es.ElasticSearchPersonReporter;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.data.std.ApiObjectIdentity;
import gov.ca.cwds.jobs.config.JobOptions;
import gov.ca.cwds.jobs.util.transform.ElasticTransformer;
import gov.ca.cwds.jobs.util.transform.JobTransformUtils;
import gov.ca.cwds.rest.api.domain.DomainChef;
import gov.ca.cwds.rest.api.domain.cms.LegacyTable;
import gov.ca.cwds.rest.api.domain.cms.SystemCodeCache;

/**
 * Entity bean for view VW_LST_REFERRAL_HIST.
 * 
 * <p>
 * Implements {@link ApiGroupNormalizer} and converts to {@link ReplicatedPersonReferrals}.
 * </p>
 * 
 * @author CWDS API Team
 */
@Entity
@Table(name = "VW_LST_REFERRAL_HIST")
@NamedNativeQueries({@NamedNativeQuery(
    name = "gov.ca.cwds.data.persistence.cms.EsPersonReferral.findAllUpdatedAfter",
    query = "SELECT r.* FROM {h-schema}VW_LST_REFERRAL_HIST r "
        + "WHERE (1=1 OR current timestamp < :after)" + "ORDER BY CLIENT_ID FOR READ ONLY WITH UR ",
    resultClass = EsPersonReferral.class, readOnly = true),

    @NamedNativeQuery(
        name = "gov.ca.cwds.data.persistence.cms.EsPersonReferral.findAllUpdatedAfterWithUnlimitedAccess",
        query = "SELECT r.* FROM {h-schema}VW_LST_REFERRAL_HIST r "
            + "WHERE (1=1 OR current timestamp < :after)"
            + "AND r.LIMITED_ACCESS_CODE = 'N' ORDER BY CLIENT_ID FOR READ ONLY WITH UR ",
        resultClass = EsPersonReferral.class, readOnly = true),

    @NamedNativeQuery(
        name = "gov.ca.cwds.data.persistence.cms.EsPersonReferral.findAllUpdatedAfterWithLimitedAccess",
        query = "SELECT r.* FROM {h-schema}VW_LST_REFERRAL_HIST r "
            + "WHERE (1=1 OR current timestamp < :after)"
            + "AND r.LIMITED_ACCESS_CODE != 'N' ORDER BY CLIENT_ID FOR READ ONLY WITH UR ",
        resultClass = EsPersonReferral.class, readOnly = true)})
public class EsPersonReferral extends ApiObjectIdentity
    implements PersistentObject, ApiGroupNormalizer<ReplicatedPersonReferrals>,
    Comparable<EsPersonReferral>, Comparator<EsPersonReferral> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EsPersonReferral.class);

  private static final long serialVersionUID = -2265057057202257108L;

  private static JobOptions opts;

  @Type(type = "timestamp")
  @Column(name = "LAST_CHG", updatable = false)
  private Date lastChange;

  // ================
  // KEYS:
  // ================

  @Id
  @Column(name = "CLIENT_ID")
  private String clientId;

  @Transient
  private transient String clientSensitivity;

  @Id
  @Column(name = "REFERRAL_ID")
  private String referralId;

  // ================
  // REFERRAL:
  // ================

  @Column(name = "START_DATE")
  @Type(type = "date")
  private Date startDate;

  @Column(name = "END_DATE")
  @Type(type = "date")
  private Date endDate;

  @Column(name = "REFERRAL_RESPONSE_TYPE")
  @Type(type = "integer")
  private Integer referralResponseType;

  @Column(name = "REFERRAL_COUNTY")
  @Type(type = "integer")
  private Integer county;

  @Column(name = "REFERRAL_LAST_UPDATED")
  @Type(type = "timestamp")
  private Date referralLastUpdated;

  // ==============
  // REPORTER:
  // ==============

  @Column(name = "REPORTER_ID")
  private String reporterId;

  @Column(name = "REPORTER_FIRST_NM")
  private String reporterFirstName;

  @Column(name = "REPORTER_LAST_NM")
  private String reporterLastName;

  @Column(name = "REPORTER_LAST_UPDATED")
  @Type(type = "timestamp")
  private Date reporterLastUpdated;

  // ==============
  // SOCIAL WORKER:
  // ==============

  @Column(name = "WORKER_ID")
  private String workerId;

  @Column(name = "WORKER_FIRST_NM")
  private String workerFirstName;

  @Column(name = "WORKER_LAST_NM")
  private String workerLastName;

  @Column(name = "WORKER_LAST_UPDATED")
  @Type(type = "timestamp")
  private Date workerLastUpdated;

  // =============
  // ALLEGATION:
  // =============

  @Column(name = "ALLEGATION_ID")
  private String allegationId;

  @Column(name = "ALLEGATION_DISPOSITION")
  @Type(type = "integer")
  private Integer allegationDisposition;

  @Column(name = "ALLEGATION_TYPE")
  @Type(type = "integer")
  private Integer allegationType;

  @Column(name = "ALLEGATION_LAST_UPDATED")
  @Type(type = "timestamp")
  private Date allegationLastUpdated;

  // =============
  // VICTIM:
  // =============

  @Column(name = "VICTIM_ID")
  private String victimId;

  @Column(name = "VICTIM_FIRST_NM")
  private String victimFirstName;

  @Column(name = "VICTIM_LAST_NM")
  private String victimLastName;

  @Column(name = "VICTIM_LAST_UPDATED")
  @Type(type = "timestamp")
  private Date victimLastUpdated;

  @Column(name = "VICTIM_SENSITIVITY_IND")
  private String victimSensitivityIndicator;

  // ==================
  // ACCESS LIMITATION:
  // ==================

  @Column(name = "LIMITED_ACCESS_CODE")
  private String limitedAccessCode;

  @Column(name = "LIMITED_ACCESS_DATE")
  @Type(type = "date")
  private Date limitedAccessDate;

  @Column(name = "LIMITED_ACCESS_DESCRIPTION")
  private String limitedAccessDescription;

  @Column(name = "LIMITED_ACCESS_GOVERNMENT_ENT")
  @Type(type = "integer")
  private Integer limitedAccessGovernmentEntityId;

  // =============
  // PERPETRATOR:
  // =============

  @Column(name = "PERPETRATOR_ID")
  private String perpetratorId;

  @Column(name = "PERPETRATOR_FIRST_NM")
  private String perpetratorFirstName;

  @Column(name = "PERPETRATOR_LAST_NM")
  private String perpetratorLastName;

  @Column(name = "PERPETRATOR_LAST_UPDATED")
  @Type(type = "timestamp")
  private Date perpetratorLastUpdated;

  @Column(name = "PERPETRATOR_SENSITIVITY_IND")
  private String perpetratorSensitivityIndicator;

  public EsPersonReferral() {
    // Default no-op.
  }

  public EsPersonReferral(final ResultSet rs) throws SQLException {
    this.setReferralId(JobTransformUtils.ifNull(rs.getString("REFERRAL_ID")));
    this.setStartDate(rs.getDate("START_DATE"));
    this.setEndDate(rs.getDate("END_DATE"));
    this.setReferralResponseType(rs.getInt("REFERRAL_RESPONSE_TYPE"));
    this.setLimitedAccessCode(ifNull(rs.getString("LIMITED_ACCESS_CODE")));
    this.setLimitedAccessDate(rs.getDate("LIMITED_ACCESS_DATE"));
    this.setLimitedAccessDescription(ifNull(rs.getString("LIMITED_ACCESS_DESCRIPTION")));
    this.setLimitedAccessGovernmentEntityId(rs.getInt("LIMITED_ACCESS_GOVERNMENT_ENT"));
    this.setReferralLastUpdated(rs.getTimestamp("REFERRAL_LAST_UPDATED"));

    this.setAllegationId(ifNull(rs.getString("ALLEGATION_ID")));
    this.setAllegationType(rs.getInt("ALLEGATION_TYPE"));
    this.setAllegationDisposition(rs.getInt("ALLEGATION_DISPOSITION"));
    this.setAllegationLastUpdated(rs.getTimestamp("ALLEGATION_LAST_UPDATED"));

    this.setPerpetratorId(ifNull(rs.getString("PERPETRATOR_ID")));
    this.setPerpetratorFirstName(ifNull(rs.getString("PERPETRATOR_FIRST_NM")));
    this.setPerpetratorLastName(ifNull(rs.getString("PERPETRATOR_LAST_NM")));
    this.setPerpetratorLastUpdated(rs.getTimestamp("PERPETRATOR_LAST_UPDATED"));
    this.setPerpetratorSensitivityIndicator(rs.getString("PERPETRATOR_SENSITIVITY_IND"));

    this.setReporterId(ifNull(rs.getString("REPORTER_ID")));
    this.setReporterFirstName(ifNull(rs.getString("REPORTER_FIRST_NM")));
    this.setReporterLastName(ifNull(rs.getString("REPORTER_LAST_NM")));
    this.setReporterLastUpdated(rs.getTimestamp("REPORTER_LAST_UPDATED"));

    this.setVictimId(ifNull(rs.getString("VICTIM_ID")));
    this.setVictimFirstName(ifNull(rs.getString("VICTIM_FIRST_NM")));
    this.setVictimLastName(ifNull(rs.getString("VICTIM_LAST_NM")));
    this.setVictimLastUpdated(rs.getTimestamp("VICTIM_LAST_UPDATED"));
    this.setVictimSensitivityIndicator(rs.getString("VICTIM_SENSITIVITY_IND"));

    this.setWorkerId(ifNull(rs.getString("WORKER_ID")));
    this.setWorkerFirstName(ifNull(rs.getString("WORKER_FIRST_NM")));
    this.setWorkerLastName(ifNull(rs.getString("WORKER_LAST_NM")));
    this.setWorkerLastUpdated(rs.getTimestamp("WORKER_LAST_UPDATED"));

    this.setCounty(rs.getInt("REFERRAL_COUNTY"));
    this.setLastChange(rs.getDate("LAST_CHG"));
  }

  @Override
  public Class<ReplicatedPersonReferrals> getNormalizationClass() {
    return ReplicatedPersonReferrals.class;
  }

  /**
   * Merge common Referral fields into this Allegation before normalizing. Very silly, yes, but it
   * works with existing code without refactoring.
   * 
   * @param clientId target client id
   * @param ref parent referral to merge
   */
  public void mergeClientReferralInfo(String clientId, EsPersonReferral ref) {
    this.clientId = clientId;
    this.referralId = ref.referralId;
    this.county = ref.county;
    this.startDate = ref.startDate;
    this.endDate = ref.endDate;
    this.referralResponseType = ref.referralResponseType;
    this.referralLastUpdated = ref.referralLastUpdated;
    this.limitedAccessCode = ref.limitedAccessCode;
    this.limitedAccessDate = ref.limitedAccessDate;
    this.limitedAccessDescription = ref.limitedAccessDescription;
    this.limitedAccessGovernmentEntityId = ref.limitedAccessGovernmentEntityId;
    this.reporterFirstName = ref.reporterFirstName;
    this.reporterId = ref.reporterId;
    this.reporterLastName = ref.reporterLastName;
    this.reporterLastUpdated = ref.reporterLastUpdated;
    this.workerFirstName = ref.workerFirstName;
    this.workerId = ref.workerId;
    this.workerLastName = ref.workerLastName;
    this.workerLastUpdated = ref.workerLastUpdated;
  }

  @Override
  public ReplicatedPersonReferrals normalize(Map<Object, ReplicatedPersonReferrals> map) {
    ReplicatedPersonReferrals ret = map.get(this.clientId);
    if (ret == null) {
      ret = new ReplicatedPersonReferrals(this.clientId);
      map.put(this.clientId, ret);
    }

    final boolean isNewReferral = !ret.hasReferral(this.referralId);
    final ElasticSearchPersonReferral r =
        !isNewReferral ? ret.getReferral(referralId) : new ElasticSearchPersonReferral();

    if (isNewReferral) {
      r.setId(this.referralId);
      r.setLegacyId(this.referralId);
      r.setLegacyLastUpdated(DomainChef.cookStrictTimestamp(this.referralLastUpdated));
      r.setStartDate(DomainChef.cookDate(this.startDate));
      r.setEndDate(DomainChef.cookDate(this.endDate));
      r.setCountyId(this.county == null ? null : this.county.toString());
      r.setCountyName(SystemCodeCache.global().getSystemCodeShortDescription(this.county));
      r.setResponseTimeId(
          this.referralResponseType == null ? null : this.referralResponseType.toString());
      r.setResponseTime(
          SystemCodeCache.global().getSystemCodeShortDescription(this.referralResponseType));
      r.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.referralId,
          this.referralLastUpdated, LegacyTable.REFERRAL));

      //
      // Reporter:
      //
      ElasticSearchPersonReporter reporter = new ElasticSearchPersonReporter();
      reporter.setId(this.reporterId);
      reporter.setLegacyClientId(this.reporterId);
      reporter.setFirstName(this.reporterFirstName);
      reporter.setLastName(this.reporterLastName);
      reporter.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.reporterId,
          this.reporterLastUpdated, LegacyTable.REPORTER));
      r.setReporter(reporter);

      //
      // Assigned Worker:
      //
      ElasticSearchPersonSocialWorker assignedWorker = new ElasticSearchPersonSocialWorker();
      assignedWorker.setId(this.workerId);
      assignedWorker.setLegacyClientId(this.workerId);
      assignedWorker.setFirstName(this.workerFirstName);
      assignedWorker.setLastName(this.workerLastName);
      assignedWorker.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.workerId,
          this.workerLastUpdated, LegacyTable.STAFF_PERSON));
      r.setAssignedSocialWorker(assignedWorker);

      //
      // Access Limitation:
      //
      ElasticSearchAccessLimitation accessLimit = new ElasticSearchAccessLimitation();
      accessLimit.setLimitedAccessCode(this.limitedAccessCode);
      accessLimit.setLimitedAccessDate(DomainChef.cookDate(this.limitedAccessDate));
      accessLimit.setLimitedAccessDescription(this.limitedAccessDescription);
      accessLimit.setLimitedAccessGovernmentEntityId(this.limitedAccessGovernmentEntityId == null
          ? null : this.limitedAccessGovernmentEntityId.toString());
      accessLimit.setLimitedAccessGovernmentEntityName(SystemCodeCache.global()
          .getSystemCodeShortDescription(this.limitedAccessGovernmentEntityId));
      r.setAccessLimitation(accessLimit);
    }

    //
    // A referral may have multiple allegations.
    //
    ElasticSearchPersonAllegation allegation = new ElasticSearchPersonAllegation();
    allegation.setId(this.allegationId);
    allegation.setLegacyId(this.allegationId);
    allegation.setAllegationDescription(
        SystemCodeCache.global().getSystemCodeShortDescription(this.allegationType));
    allegation.setDispositionId(
        this.allegationDisposition == null ? null : this.allegationDisposition.toString());
    allegation.setDispositionDescription(
        SystemCodeCache.global().getSystemCodeShortDescription(this.allegationDisposition));
    allegation.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.allegationId,
        this.allegationLastUpdated, LegacyTable.ALLEGATION));

    if (opts.isLoadSealedAndSensitive() || (perpetratorSensitivityIndicator == null
        || "N".equalsIgnoreCase(perpetratorSensitivityIndicator))) {
      ElasticSearchPersonNestedPerson perpetrator = new ElasticSearchPersonNestedPerson();
      perpetrator.setId(this.perpetratorId);
      perpetrator.setFirstName(this.perpetratorFirstName);
      perpetrator.setLastName(this.perpetratorLastName);
      perpetrator.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.perpetratorId,
          this.perpetratorLastUpdated, LegacyTable.CLIENT));
      perpetrator.setSensitivityIndicator(perpetratorSensitivityIndicator);
      allegation.setPerpetrator(perpetrator);

      // TODO: #148091785: deprecated person fields.
      allegation.setPerpetratorId(this.perpetratorId);
      allegation.setPerpetratorLegacyClientId(this.perpetratorId);
      allegation.setPerpetratorFirstName(this.perpetratorFirstName);
      allegation.setPerpetratorLastName(this.perpetratorLastName);
    } else {
      LOGGER.trace("OMIT sealed or sensitive perpetrator: id={}", this.perpetratorId);
    }

    if (opts.isLoadSealedAndSensitive() || (victimSensitivityIndicator == null
        || "N".equalsIgnoreCase(victimSensitivityIndicator))) {
      ElasticSearchPersonNestedPerson victim = new ElasticSearchPersonNestedPerson();
      victim.setId(this.victimId);
      victim.setFirstName(this.victimFirstName);
      victim.setLastName(this.victimLastName);
      victim.setLegacyDescriptor(ElasticTransformer.createLegacyDescriptor(this.victimId,
          this.victimLastUpdated, LegacyTable.CLIENT));
      victim.setSensitivityIndicator(victimSensitivityIndicator);
      allegation.setVictim(victim);

      // TODO: #148091785: deprecated person fields.
      allegation.setVictimId(this.victimId);
      allegation.setVictimLegacyClientId(this.victimId);
      allegation.setVictimFirstName(this.victimFirstName);
      allegation.setVictimLastName(this.victimLastName);
    } else {
      LOGGER.trace("OMIT sealed or sensitive victim: id={}", this.victimId);
    }

    ret.addReferral(r, allegation);
    return ret;
  }

  @Override
  public String getNormalizationGroupKey() {
    return this.clientId;
  }

  @Override
  public Serializable getPrimaryKey() {
    return null;
  }

  /**
   * Getter for composite "last change", the latest time that any associated record was created or
   * updated.
   * 
   * @return last change date
   */
  public Date getLastChange() {
    return lastChange;
  }

  /**
   * Setter for composite "last change", the latest time that any associated record was created or
   * updated.
   * 
   * @param lastChange last change date
   */
  public void setLastChange(Date lastChange) {
    this.lastChange = lastChange;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getReferralId() {
    return referralId;
  }

  public void setReferralId(String referralId) {
    this.referralId = referralId;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Integer getReferralResponseType() {
    return referralResponseType;
  }

  public void setReferralResponseType(Integer referralResponseType) {
    this.referralResponseType = referralResponseType;
  }

  public Integer getCounty() {
    return county;
  }

  public void setCounty(Integer county) {
    this.county = county;
  }

  public String getReporterId() {
    return reporterId;
  }

  public void setReporterId(String reporterId) {
    this.reporterId = reporterId;
  }

  public String getReporterFirstName() {
    return reporterFirstName;
  }

  public void setReporterFirstName(String reporterFirstName) {
    this.reporterFirstName = reporterFirstName;
  }

  public String getReporterLastName() {
    return reporterLastName;
  }

  public void setReporterLastName(String reporterLastName) {
    this.reporterLastName = reporterLastName;
  }

  public String getWorkerId() {
    return workerId;
  }

  public void setWorkerId(String workerId) {
    this.workerId = workerId;
  }

  public String getWorkerFirstName() {
    return workerFirstName;
  }

  public void setWorkerFirstName(String workerFirstName) {
    this.workerFirstName = workerFirstName;
  }

  public String getWorkerLastName() {
    return workerLastName;
  }

  public void setWorkerLastName(String workerLastName) {
    this.workerLastName = workerLastName;
  }

  public String getAllegationId() {
    return allegationId;
  }

  public void setAllegationId(String allegationId) {
    this.allegationId = allegationId;
  }

  public Integer getAllegationDisposition() {
    return allegationDisposition;
  }

  public void setAllegationDisposition(Integer allegationDisposition) {
    this.allegationDisposition = allegationDisposition;
  }

  public Integer getAllegationType() {
    return allegationType;
  }

  public void setAllegationType(Integer allegationType) {
    this.allegationType = allegationType;
  }

  public String getVictimId() {
    return victimId;
  }

  public void setVictimId(String victimId) {
    this.victimId = victimId;
  }

  public String getVictimFirstName() {
    return victimFirstName;
  }

  public void setVictimFirstName(String victimFirstName) {
    this.victimFirstName = victimFirstName;
  }

  public String getVictimLastName() {
    return victimLastName;
  }

  public void setVictimLastName(String victimLastName) {
    this.victimLastName = victimLastName;
  }

  public String getPerpetratorId() {
    return perpetratorId;
  }

  public void setPerpetratorId(String perpetratorId) {
    this.perpetratorId = perpetratorId;
  }

  public String getPerpetratorFirstName() {
    return perpetratorFirstName;
  }

  public void setPerpetratorFirstName(String perpetratorFirstName) {
    this.perpetratorFirstName = perpetratorFirstName;
  }

  public String getPerpetratorLastName() {
    return perpetratorLastName;
  }

  public void setPerpetratorLastName(String perpetratorLastName) {
    this.perpetratorLastName = perpetratorLastName;
  }

  public String getLimitedAccessCode() {
    return limitedAccessCode;
  }

  public void setLimitedAccessCode(String limitedAccessCode) {
    this.limitedAccessCode = limitedAccessCode;
  }

  public Date getLimitedAccessDate() {
    return limitedAccessDate;
  }

  public void setLimitedAccessDate(Date limitedAccessDate) {
    this.limitedAccessDate = limitedAccessDate;
  }

  public String getLimitedAccessDescription() {
    return limitedAccessDescription;
  }

  public void setLimitedAccessDescription(String limitedAccessDescription) {
    this.limitedAccessDescription = limitedAccessDescription;
  }

  public Integer getLimitedAccessGovernmentEntityId() {
    return limitedAccessGovernmentEntityId;
  }

  public void setLimitedAccessGovernmentEntityId(Integer limitedAccessGovernmentEntityId) {
    this.limitedAccessGovernmentEntityId = limitedAccessGovernmentEntityId;
  }

  public Date getReferralLastUpdated() {
    return referralLastUpdated;
  }

  public void setReferralLastUpdated(Date referralLastUpdated) {
    this.referralLastUpdated = referralLastUpdated;
  }

  public Date getReporterLastUpdated() {
    return reporterLastUpdated;
  }

  public void setReporterLastUpdated(Date reporterLastUpdated) {
    this.reporterLastUpdated = reporterLastUpdated;
  }

  public Date getWorkerLastUpdated() {
    return workerLastUpdated;
  }

  public void setWorkerLastUpdated(Date workerLastUpdated) {
    this.workerLastUpdated = workerLastUpdated;
  }

  public Date getAllegationLastUpdated() {
    return allegationLastUpdated;
  }

  public void setAllegationLastUpdated(Date allegationLastUpdated) {
    this.allegationLastUpdated = allegationLastUpdated;
  }

  public Date getVictimLastUpdated() {
    return victimLastUpdated;
  }

  public void setVictimLastUpdated(Date victimLastUpdated) {
    this.victimLastUpdated = victimLastUpdated;
  }

  public Date getPerpetratorLastUpdated() {
    return perpetratorLastUpdated;
  }

  public void setPerpetratorLastUpdated(Date perpetratorLastUpdated) {
    this.perpetratorLastUpdated = perpetratorLastUpdated;
  }

  public String getVictimSensitivityIndicator() {
    return victimSensitivityIndicator;
  }

  public void setVictimSensitivityIndicator(String victimSensitivityIndicator) {
    this.victimSensitivityIndicator = victimSensitivityIndicator;
  }

  public String getPerpetratorSensitivityIndicator() {
    return perpetratorSensitivityIndicator;
  }

  public void setPerpetratorSensitivityIndicator(String perpetratorSensitivityIndicator) {
    this.perpetratorSensitivityIndicator = perpetratorSensitivityIndicator;
  }

  public String getClientSensitivity() {
    return clientSensitivity;
  }

  public void setClientSensitivity(String clientSensitivity) {
    this.clientSensitivity = clientSensitivity;
  }

  public static void setOpts(JobOptions opts) {
    EsPersonReferral.opts = opts;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE, false);
  }

  @Override
  public int compare(EsPersonReferral o1, EsPersonReferral o2) {
    return o1.getClientId().compareTo(o2.getClientId());
  }

  @Override
  public int compareTo(EsPersonReferral o) {
    return compare(this, o);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
    result = prime * result + ((referralId == null) ? 0 : referralId.hashCode());
    result = prime * result + ((allegationId == null) ? 0 : allegationId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    EsPersonReferral other = (EsPersonReferral) obj;

    if (clientId == null) {
      if (other.clientId != null)
        return false;
    } else if (!clientId.equals(other.clientId))
      return false;

    if (referralId == null) {
      if (other.referralId != null)
        return false;
    } else if (!referralId.equals(other.referralId))
      return false;

    if (allegationId == null) {
      if (other.allegationId != null)
        return false;
    } else if (!allegationId.equals(other.allegationId))
      return false;

    return true;
  }

}
