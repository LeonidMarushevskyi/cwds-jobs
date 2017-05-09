package gov.ca.cwds.data.persistence.ns;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Id;

import gov.ca.cwds.dao.ApiMultiplePersonAware;
import gov.ca.cwds.dao.ApiScreeningAware;
import gov.ca.cwds.data.es.ElasticSearchPerson.ElasticSearchPersonAny;
import gov.ca.cwds.data.es.ElasticSearchPerson.ElasticSearchPersonScreening;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.ns.IntakeParticipant.EsPersonType;
import gov.ca.cwds.data.std.ApiPersonAware;
import gov.ca.cwds.rest.api.domain.DomainChef;

/**
 * NS Persistence class for Intake Screenings.
 * 
 * @author CWDS API Team
 */
@SuppressWarnings("serial")
// @Entity
// @Table(name = "screenings")
public class IntakeScreening
    implements PersistentObject, ApiMultiplePersonAware, ApiScreeningAware {

  private static final Set<String> EMPTY_SET_STRING = new LinkedHashSet<>();

  @Id
  // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "screenings_id_seq")
  // @SequenceGenerator(name = "screenings_id_seq", sequenceName = "screenings_id_seq",
  // allocationSize = 10)
  @Column(name = "SCREENING_ID")
  private String id;

  @Column(name = "REFERENCE")
  private String reference;

  @Column(name = "STARTED_AT")
  private String startedAt;

  @Column(name = "ENDED_AT")
  private String endedAt;

  @Column(name = "INCIDENT_DATE")
  private String incidentDate;

  @Column(name = "LOCATION_TYPE")
  private String locationType;

  @Column(name = "COMMUNICATION_METHOD")
  private String communicationMethod;

  @Column(name = "SCREENING_NAME")
  private String screeningName;

  @Column(name = "SCREENING_DECISION")
  private String screeningDecision;

  @Column(name = "INCIDENT_COUNTY")
  private String incidentCounty;

  @Column(name = "REPORT_NARRATIVE")
  private String reportNarrative;

  @Column(name = "ASSIGNEE")
  private String assignee;

  @Column(name = "ADDITIONAL_INFORMATION")
  private String additionalInformation;

  @Column(name = "SCREENING_DECISION_DETAIL")
  private String screeningDecisionDetail;

  private Map<String, IntakeParticipant> participants = new LinkedHashMap<>();

  private Map<String, Set<String>> participantRoles = new LinkedHashMap<>();

  private Map<String, IntakeAllegation> allegations = new LinkedHashMap<>();

  private IntakeParticipant socialWorker = new IntakeParticipant();

  private IntakeParticipant reporter = new IntakeParticipant();

  /**
   * Default constructor, required for Hibernate.
   */
  public IntakeScreening() {
    super();
  }

  /**
   * Constructor
   * 
   * @param reference The reference
   */
  public IntakeScreening(String reference) {
    this.reference = reference;
  }

  /**
   * {@inheritDoc}
   * 
   * @see gov.ca.cwds.data.persistence.PersistentObject#getPrimaryKey()
   */
  @Override
  public String getPrimaryKey() {
    return getId();
  }

  /**
   * Convert this Intake screening object to an Elasticsearch screening element.
   * 
   * @return ES screening document object
   */
  public ElasticSearchPersonScreening toEsScreening() {
    ElasticSearchPersonScreening ret = new ElasticSearchPersonScreening();

    ret.id = this.id;
    ret.countyName = this.incidentCounty;
    ret.decision = this.screeningDecision;
    ret.endDate = DomainChef.uncookDateString(this.endedAt);
    ret.startDate = DomainChef.uncookDateString(this.startedAt);
    ret.responseTime = this.screeningDecisionDetail;

    ret.reporter.firstName = getReporter().getFirstName();
    ret.reporter.lastName = getReporter().getLastName();
    ret.reporter.id = getReporter().getId();
    ret.reporter.legacyClientId = getReporter().getLegacyId();

    ret.assignedSocialWorker.firstName = getSocialWorker().getFirstName();
    ret.assignedSocialWorker.id = getSocialWorker().getId();
    ret.assignedSocialWorker.lastName = getSocialWorker().getLastName();
    ret.assignedSocialWorker.legacyClientId = getSocialWorker().getLegacyId();

    for (IntakeAllegation alg : this.allegations.values()) {
      ret.allegations.add(alg.toEsAllegation());
    }

    for (IntakeParticipant p : this.participants.values()) {
      ret.allPeople.add((ElasticSearchPersonAny) p.toEsPerson(EsPersonType.All, this));
    }

    return ret;
  }

  @Override
  public ApiPersonAware[] getPersons() {
    return getParticipants().values().toArray(new ApiPersonAware[0]);
  }

  @Override
  public ElasticSearchPersonScreening[] getEsScreenings() {
    List<ElasticSearchPersonScreening> esScreenings = new ArrayList<>();
    esScreenings.add(toEsScreening());
    return esScreenings.toArray(new ElasticSearchPersonScreening[0]);
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(String startedAt) {
    this.startedAt = startedAt;
  }

  public String getEndedAt() {
    return endedAt;
  }

  public void setEndedAt(String endedAt) {
    this.endedAt = endedAt;
  }

  public String getIncidentDate() {
    return incidentDate;
  }

  public void setIncidentDate(String incidentDate) {
    this.incidentDate = incidentDate;
  }

  public String getLocationType() {
    return locationType;
  }

  public void setLocationType(String locationType) {
    this.locationType = locationType;
  }

  public String getCommunicationMethod() {
    return communicationMethod;
  }

  public void setCommunicationMethod(String communicationMethod) {
    this.communicationMethod = communicationMethod;
  }

  public String getScreeningName() {
    return screeningName;
  }

  public void setScreeningName(String screeningName) {
    this.screeningName = screeningName;
  }

  public String getScreeningDecision() {
    return screeningDecision;
  }

  public void setScreeningDecision(String screeningDecision) {
    this.screeningDecision = screeningDecision;
  }

  public String getIncidentCounty() {
    return incidentCounty;
  }

  public void setIncidentCounty(String incidentCounty) {
    this.incidentCounty = incidentCounty;
  }

  public String getReportNarrative() {
    return reportNarrative;
  }

  public void setReportNarrative(String reportNarrative) {
    this.reportNarrative = reportNarrative;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public String getAdditionalInformation() {
    return additionalInformation;
  }

  public void setAdditionalInformation(String additionalInformation) {
    this.additionalInformation = additionalInformation;
  }

  public String getScreeningDecisionDetail() {
    return screeningDecisionDetail;
  }

  public void setScreeningDecisionDetail(String screeningDecisionDetail) {
    this.screeningDecisionDetail = screeningDecisionDetail;
  }

  public IntakeParticipant getSocialWorker() {
    return socialWorker;
  }

  public void setSocialWorker(IntakeParticipant assignedSocialWorker) {
    this.socialWorker = assignedSocialWorker;
  }

  public void addParticipant(IntakeParticipant prt) {
    this.participants.put(prt.getId(), prt);
  }

  public void addAllegation(IntakeAllegation alg) {
    this.allegations.put(alg.getId(), alg);
  }

  public void addParticipantRole(String partcId, String role) {
    Set<String> roles;
    if (this.participantRoles.containsKey(partcId)) {
      roles = this.participantRoles.get(partcId);
    } else {
      roles = new LinkedHashSet<>();
      this.participantRoles.put(partcId, roles);
    }

    roles.add(role);
  }

  public Set<String> findParticipantRoles(String partcId) {
    return this.participantRoles.containsKey(partcId) ? this.participantRoles.get(partcId)
        : EMPTY_SET_STRING;
  }

  public Map<String, IntakeParticipant> getParticipants() {
    return participants;
  }

  public Map<String, IntakeAllegation> getAllegations() {
    return allegations;
  }

  public IntakeParticipant getReporter() {
    return reporter;
  }

  public void setReporter(IntakeParticipant reporter) {
    this.reporter = reporter;
  }

  public Map<String, Set<String>> getParticipantRoles() {
    return participantRoles;
  }

}
