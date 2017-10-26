package gov.ca.cwds.jobs.schedule;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.JobKey;
import org.quartz.listeners.JobChainingJobListener;

import gov.ca.cwds.jobs.ChildCaseHistoryIndexerJob;
import gov.ca.cwds.jobs.ClientIndexerJob;
import gov.ca.cwds.jobs.CollateralIndividualIndexerJob;
import gov.ca.cwds.jobs.EducationProviderContactIndexerJob;
import gov.ca.cwds.jobs.IntakeScreeningJob;
import gov.ca.cwds.jobs.MSearchJob;
import gov.ca.cwds.jobs.OtherAdultInPlacemtHomeIndexerJob;
import gov.ca.cwds.jobs.OtherChildInPlacemtHomeIndexerJob;
import gov.ca.cwds.jobs.OtherClientNameIndexerJob;
import gov.ca.cwds.jobs.ParentCaseHistoryIndexerJob;
import gov.ca.cwds.jobs.ReferralHistoryIndexerJob;
import gov.ca.cwds.jobs.RelationshipIndexerJob;
import gov.ca.cwds.jobs.ReporterIndexerJob;
import gov.ca.cwds.jobs.SafetyAlertIndexerJob;
import gov.ca.cwds.jobs.ServiceProviderIndexerJob;
import gov.ca.cwds.jobs.SubstituteCareProviderIndexJob;

public enum NeutronDefaultJobSchedule {

  //
  // Person document roots.
  //

  /**
   * Client. Essential document root.
   */
  CLIENT(ClientIndexerJob.class, true, "client", 1, 5, 20, 1000, null),

  REPORTER(ReporterIndexerJob.class, true, "reporter", 2, 10, 30, 950, null),

  COLLATERAL_INDIVIDUAL(CollateralIndividualIndexerJob.class, true, "collateral_individual", 3, 20,
      30, 90, null),

  SERVICE_PROVIDER(ServiceProviderIndexerJob.class, true, "service_provider", 4, 25, 120, 85, null),

  SUBSTITUTE_CARE_PROVIDER(SubstituteCareProviderIndexJob.class, true, "substitute_care_provider",
      5, 30, 120, 80, null),

  EDUCATION_PROVIDER(EducationProviderContactIndexerJob.class, true, "education_provider", 6, 42,
      120, 75, null),

  OTHER_ADULT_IN_HOME(OtherAdultInPlacemtHomeIndexerJob.class, true, "other_adult", 7, 50, 120, 70,
      null),

  OTHER_CHILD_IN_HOME(OtherChildInPlacemtHomeIndexerJob.class, true, "other_child", 8, 55, 120, 65,
      null),

  //
  // JSON elements inside document.
  //

  /**
   * Client name aliases.
   */
  OTHER_CLIENT_NAME(OtherClientNameIndexerJob.class, false, "other_client_name", 20, 90, 45, 300, "akas"),

  /**
   * Child cases.
   */
  CHILD_CASE(ChildCaseHistoryIndexerJob.class, false, "child_case", 25, 70, 30, 550, "cases"),

  /**
   * Parent cases.
   */
  PARENT_CASE(ParentCaseHistoryIndexerJob.class, false, "parent_case", 30, 90, 30, 500, "cases"),

  /**
   * Relationships.
   */
  RELATIONSHIP(RelationshipIndexerJob.class, false, "relationship", 40, 90, 30, 600, "relationships"),

  /**
   * Referrals.
   */
  REFERRAL(ReferralHistoryIndexerJob.class, false, "referral", 50, 45, 30, 700, "referrals"),

  /**
   * Safety alerts.
   */
  SAFETY_ALERT(SafetyAlertIndexerJob.class, false, "safety_alert", 60, 90, 45, 350, "safety_alerts"),

  /**
   * Screenings.
   */
  INTAKE_SCREENING(IntakeScreeningJob.class, false, "intake_screening", 70, 90, 20, 800, "screenings"),

  /**
   * Validation.
   */
  VALIDATE_LAST_RUN(MSearchJob.class, false, "validate_last_run", 90, 90, 10, 100, null)

  ;

  private final Class<?> klazz;

  private final String name;

  private final boolean newDocument;

  private final int initialLoadOrder;

  private final int startDelaySeconds;

  private final int periodSeconds;

  private final int lastRunPriority;

  private final String jsonElement;

  private static final Map<String, NeutronDefaultJobSchedule> mapName = new ConcurrentHashMap<>();

  static {
    for (NeutronDefaultJobSchedule sched : NeutronDefaultJobSchedule.values()) {
      mapName.put(sched.name, sched);
    }
  }

  private NeutronDefaultJobSchedule(Class<?> klazz, boolean newDocument, String name,
      int initialLoadOrder, int startDelaySeconds, int periodSeconds, int lastRunPriority,
      String jsonElement) {
    this.klazz = klazz;
    this.newDocument = newDocument;
    this.name = name;
    this.initialLoadOrder = initialLoadOrder;
    this.startDelaySeconds = startDelaySeconds;
    this.periodSeconds = periodSeconds;
    this.lastRunPriority = lastRunPriority;
    this.jsonElement = jsonElement;
  }

  public static JobChainingJobListener fullLoadJobChainListener() {
    final JobChainingJobListener ret = new JobChainingJobListener("initial_load");

    final NeutronDefaultJobSchedule[] arr = Arrays.copyOf(NeutronDefaultJobSchedule.values(),
        NeutronDefaultJobSchedule.values().length);
    Arrays.sort(arr, (o1, o2) -> Integer.compare(o1.initialLoadOrder, o2.initialLoadOrder));

    NeutronDefaultJobSchedule sched;
    final int len = arr.length;
    for (int i = 0; i < len; i++) {
      sched = arr[i];
      ret.addJobChainLink(new JobKey(sched.name, NeutronSchedulerConstants.GRP_FULL_LOAD),
          i != (len - 1) ? new JobKey(arr[i + 1].name, NeutronSchedulerConstants.GRP_FULL_LOAD)
              : new JobKey("verify", NeutronSchedulerConstants.GRP_FULL_LOAD));
    }

    return ret;
  }

  public Class<?> getKlazz() {
    return klazz;
  }

  public String getName() {
    return name;
  }

  public boolean isNewDocument() {
    return newDocument;
  }

  public int getStartDelaySeconds() {
    return startDelaySeconds;
  }

  public int getPeriodSeconds() {
    return periodSeconds;
  }

  public int getLastRunPriority() {
    return lastRunPriority;
  }

  public String getJsonElement() {
    return jsonElement;
  }

  public static NeutronDefaultJobSchedule lookupByJobName(String key) {
    return mapName.get(key);
  }

  public int getInitialLoadOrder() {
    return initialLoadOrder;
  }

}
