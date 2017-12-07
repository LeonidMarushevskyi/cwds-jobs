package gov.ca.cwds.neutron.rocket;

import static gov.ca.cwds.neutron.util.transform.JobTransformUtils.ifNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.action.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.dao.cms.ReplicatedPersonCasesDao;
import gov.ca.cwds.dao.cms.StaffPersonDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.CaseSQLResource;
import gov.ca.cwds.data.persistence.cms.EsChildPersonCase;
import gov.ca.cwds.data.persistence.cms.EsPersonCase;
import gov.ca.cwds.data.persistence.cms.ReplicatedPersonCases;
import gov.ca.cwds.data.persistence.cms.StaffPerson;
import gov.ca.cwds.data.persistence.cms.rep.EmbeddableStaffWorker;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.config.FlightPlan;
import gov.ca.cwds.jobs.exception.NeutronException;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.jobs.util.jdbc.NeutronDB2Util;
import gov.ca.cwds.jobs.util.jdbc.NeutronRowMapper;
import gov.ca.cwds.jobs.util.jdbc.NeutronThreadUtil;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.JobLogs;
import gov.ca.cwds.neutron.util.jdbc.NeutronJdbcUtil;
import gov.ca.cwds.neutron.util.transform.EntityNormalizer;

/**
 * Rocket to index person cases from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class CaseRocket extends InitialLoadJdbcRocket<ReplicatedPersonCases, EsPersonCase>
    implements NeutronRowMapper<EsPersonCase> {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(CaseRocket.class);

  /**
   * Allocate memory once for each thread and reuse per key range.
   * 
   * <p>
   * Use thread local variables <strong>sparingly</strong> because they stick to the thread. This
   * Neutron rocket reuses threads for performance, since thread creation is expensive.
   * </p>
   */
  protected transient ThreadLocal<List<EsChildPersonCase>> allocCases = new ThreadLocal<>();

  protected transient ThreadLocal<Map<String, EsChildPersonCase>> allocMapCases =
      new ThreadLocal<>();

  protected transient ThreadLocal<Map<String, ReplicatedClient>> allocMapClients =
      new ThreadLocal<>();

  protected transient ThreadLocal<List<EsChildPersonCase>> allocReadyToNorm = new ThreadLocal<>();

  protected final AtomicInteger rowsReadCases = new AtomicInteger(0);

  protected final AtomicInteger nextThreadNum = new AtomicInteger(0);

  private StaffPersonDao staffPersonDao;

  private ReplicatedClientDao clientDao;

  private Map<String, StaffPerson> staffWorkers = new HashMap<>();

  /**
   * Construct rocket with all required dependencies.
   * 
   * @param dao DAO for {@link ReplicatedPersonCases}
   * @param esDao ElasticSearch DAO
   * @param clientDao client DAO
   * @param staffPersonDao staff worker DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param flightPlan command line options
   */
  @Inject
  public CaseRocket(ReplicatedPersonCasesDao dao, ElasticsearchDao esDao,
      ReplicatedClientDao clientDao, StaffPersonDao staffPersonDao, @LastRunFile String lastRunFile,
      ObjectMapper mapper, FlightPlan flightPlan) {
    super(dao, esDao, lastRunFile, mapper, flightPlan);

    this.clientDao = clientDao;
    this.staffPersonDao = staffPersonDao;
  }

  // =====================
  // FIXED SPECS:
  // =====================

  /**
   * This rocket normalizes <strong>without</strong> the transform thread.
   */
  @Override
  public boolean useTransformThread() {
    return false;
  }

  @Override
  public String getPrepLastChangeSQL() {
    return CaseSQLResource.INSERT_CLIENT_LAST_CHG;
  }

  @Override
  public String getInitialLoadViewName() {
    return "VW_MQT_REFRL_ONLY";
  }

  @Override
  public boolean isInitialLoadJdbc() {
    return true;
  }

  @Override
  public List<Pair<String, String>> getPartitionRanges() throws NeutronException {
    return NeutronJdbcUtil.getCommonPartitionRanges64(this);
  }

  @Override
  public String getOptionalElementName() {
    return "cases";
  }

  /**
   * If sealed or sensitive data must NOT be loaded then any records indexed with sealed or
   * sensitive flag must be deleted.
   */
  @Override
  public boolean mustDeleteLimitedAccessRecords() {
    return !getFlightPlan().isLoadSealedAndSensitive();
  }

  @Override
  public String getJdbcOrderBy() {
    return ""; // sort manually since DB2 might not optimize the sort.
  }

  /**
   * Roll your own SQL.
   * <p>
   * This approach requires sorted results. Either sort on the database side or here in the
   * application.
   */
  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    final StringBuilder buf = new StringBuilder();
    buf.append(CaseSQLResource.SELECT_CASES_FULL);

    if (!getFlightPlan().isLoadSealedAndSensitive()) {
      buf.append(" WHERE CAS.LMT_ACSSCD = 'N' ");
    }

    final String ret = buf.toString().trim();
    LOGGER.info("CASE SQL: {}", ret);
    return ret;
  }

  // =====================
  // NORMALIZATION:
  // =====================

  @Override
  protected UpdateRequest prepareUpsertRequest(ElasticSearchPerson esp, ReplicatedPersonCases p)
      throws NeutronException {
    return prepareUpdateRequest(esp, p, p.getCases(), true);
  }

  @Override
  public Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return EsPersonCase.class;
  }

  @Override
  public List<ReplicatedPersonCases> normalize(List<EsPersonCase> recs) {
    return EntityNormalizer.<ReplicatedPersonCases, EsPersonCase>normalizeList(recs);
  }

  // =====================
  // JDBC:
  // =====================

  protected void prepClientBundle(final PreparedStatement stmtInsClient,
      final Pair<String, String> p) throws SQLException {
    stmtInsClient.setMaxRows(0);
    stmtInsClient.setQueryTimeout(0);
    stmtInsClient.setString(1, p.getLeft());
    stmtInsClient.setString(2, p.getRight());

    final int countInsClientCases = stmtInsClient.executeUpdate();
    LOGGER.info("bundle client/cases: {}", countInsClientCases);
  }

  protected EsPersonCase mapRows(ResultSet rs) throws SQLException {
    return extract(rs);
  }

  protected void readCases(final PreparedStatement stmtSelCase) throws SQLException {
    final List<EsChildPersonCase> cases = this.allocCases.get();
    stmtSelCase.setMaxRows(0);
    stmtSelCase.setQueryTimeout(0);
    stmtSelCase.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue());

    int cntr = 0;
    EsPersonCase m;
    LOGGER.info("pull cases");
    final ResultSet rs = stmtSelCase.executeQuery(); // NOSONAR
    while (!isFailed() && rs.next() && (m = mapRows(rs)) != null) {
      JobLogs.logEvery(++cntr, "read", "case bundle");
      JobLogs.logEvery(LOGGER, 10000, rowsReadCases.incrementAndGet(), "Total read", "cases");
    }
  }

  /**
   * Pour cases, and client/case keys into the caldron and brew into a cases JSON array element per
   * client.
   * 
   * @param listCases cases bundle
   * @param mapCases k=referral id, v=EsPersonCase
   * @param listClientCaseKeys client/referral key pairs
   * @param listReadyToNorm denormalized records
   * @return normalized record count
   */
  protected int mapReduce(final List<EsChildPersonCase> listCases,
      final Map<String, EsChildPersonCase> mapCases,
      final List<EsChildPersonCase> listReadyToNorm) {
    int countNormalized = 0;

    try {
      final Map<String, List<EsPersonCase>> mapCasesByClient =
          listCases.stream().sorted((e1, e2) -> e1.getCaseId().compareTo(e2.getCaseId()))
              .collect(Collectors.groupingBy(EsPersonCase::getCaseId));
      listCases.clear(); // release objects for garbage collection
    } finally {
      releaseThreadMemory();
    }

    return countNormalized;
  }

  /**
   * Read all records from a single partition (key range), sort results, and normalize.
   * 
   * <p>
   * Each call to this method should run in its own thread.
   * </p>
   * 
   * @param p partition (key) range to read
   * @return number of client documents affected
   */
  protected int pullNextRange(final Pair<String, String> p) {
    final String threadName =
        "extract_" + nextThreadNum.incrementAndGet() + "_" + p.getLeft() + "_" + p.getRight();
    nameThread(threadName);
    LOGGER.info("BEGIN");
    getFlightLog().markRangeStart(p);

    allocateThreadMemory(); // allocate thread local memory, if not done prior.
    final List<EsChildPersonCase> listCases = allocCases.get();
    final Map<String, EsChildPersonCase> mapCases = allocMapCases.get();
    final List<EsChildPersonCase> listReadyToNorm = allocReadyToNorm.get();

    listCases.clear();
    mapCases.clear();
    listReadyToNorm.clear();

    try (final Connection con = getConnection()) {
      final String schema = getDBSchemaName();
      con.setSchema(schema);
      con.setAutoCommit(false);
      NeutronDB2Util.enableParallelism(con);

      try (
          final PreparedStatement stmtInsClient =
              con.prepareStatement(CaseSQLResource.INSERT_CLIENT_FULL);
          final PreparedStatement stmtSelCase = con.prepareStatement(getInitialLoadQuery(schema))) {
        prepClientBundle(stmtInsClient, p);
        allocMapClients.set(readClients());
        readCases(stmtSelCase);
      } finally {
        con.commit();
      }
    } catch (Exception e) {
      fail();
      throw JobLogs.runtime(LOGGER, e, "ERROR HANDLING RANGE {} - {}: {}", p.getLeft(),
          p.getRight(), e.getMessage());
    }

    int cntr = 0;
    // int cntr = mapReduce(listCases, mapCases, listReadyToNorm);
    getFlightLog().markRangeComplete(p);
    LOGGER.info("DONE");
    return cntr;
  }

  /**
   * Initial load only. The "extract" part of ETL. Processes key ranges in separate threads.
   * 
   * <p>
   * Note that this rocket normalizes <strong>without</strong> the transform thread.
   * </p>
   */
  @Override
  protected void threadRetrieveByJdbc() {
    nameThread("case_main");
    LOGGER.info("BEGIN: main read thread");
    doneTransform(); // normalize in place **WITHOUT** the transform thread

    try {
      staffWorkers = readStaffWorkers();
      final List<Pair<String, String>> ranges = getPartitionRanges();
      LOGGER.info(">>>>>>>> # OF RANGES: {} <<<<<<<<", ranges);
      final List<ForkJoinTask<?>> tasks = new ArrayList<>(ranges.size());
      final ForkJoinPool threadPool =
          new ForkJoinPool(NeutronThreadUtil.calcReaderThreads(getFlightPlan()));

      // Queue execution.
      for (Pair<String, String> p : ranges) {
        // Pull each range independently on the next available thread.
        tasks.add(threadPool.submit(() -> pullNextRange(p)));
      }

      // Join threads. Don't return from method until they complete.
      for (ForkJoinTask<?> task : tasks) {
        task.get();
      }
    } catch (Exception e) {
      fail();
      throw JobLogs.runtime(LOGGER, e, "ERROR! {}", e.getMessage());
    } finally {
      doneRetrieve();
    }

    LOGGER.info("DONE: read {} ES case rows", this.rowsReadCases.get());
  }

  /**
   * @return complete list of potential case workers
   * @throws NeutronException on database error
   */
  protected Map<String, StaffPerson> readStaffWorkers() throws NeutronException {
    try {
      return staffPersonDao.findAll().stream()
          .collect(Collectors.toMap(StaffPerson::getId, w -> w));
    } catch (Exception e) {
      fail();
      throw new NeutronException("ERROR READING STAFF WORKERS", e);
    }
  }

  protected Map<String, ReplicatedClient> readClients() throws NeutronException {
    try {
      return clientDao.findByTemp().stream()
          .collect(Collectors.toMap(ReplicatedClient::getId, c -> c));
    } catch (Exception e) {
      fail();
      throw new NeutronException("ERROR READING CLIENTS", e);
    }
  }

  @Override
  public EsPersonCase extract(final ResultSet rs) throws SQLException {
    final String caseId = rs.getString("CASE_ID");
    String focusChildId = rs.getString("FOCUS_CHILD_ID");

    if (focusChildId == null) {
      LOGGER.warn("FOCUS_CHILD_ID is null for CASE_ID: {}", caseId); // NOSONAR
      return null;
    }

    final EsChildPersonCase ret = new EsChildPersonCase();

    //
    // Case:
    //
    ret.setCaseId(caseId);
    ret.setStartDate(rs.getDate("START_DATE"));
    ret.setEndDate(rs.getDate("END_DATE"));
    ret.setCaseLastUpdated(rs.getTimestamp("CASE_LAST_UPDATED"));
    ret.setCounty(rs.getInt("COUNTY"));
    ret.setServiceComponent(rs.getInt("SERVICE_COMP"));

    //
    // Child (client):
    //
    ret.setFocusChildId(focusChildId);
    // ret.setFocusChildFirstName(ifNull(rs.getString("FOCUS_CHLD_FIRST_NM")));
    // ret.setFocusChildLastName(ifNull(rs.getString("FOCUS_CHLD_LAST_NM")));
    // ret.setFocusChildLastUpdated(rs.getTimestamp("FOCUS_CHILD_LAST_UPDATED"));
    // ret.setFocusChildSensitivityIndicator(rs.getString("FOCUS_CHILD_SENSITIVITY_IND"));

    //
    // Relative (client):
    //

    // FOCUS_IND,
    // 0 AS REL_FOCUS_TO_OTHER,
    // 0 AS REL_OTHER_TO_FOCUS,

    final String focusInd = rs.getString("FOCUS_IND");

    if (StringUtils.isBlank(focusInd) || !"Y".equalsIgnoreCase(focusInd)) {
      ret.setParentId(ifNull(rs.getString("THIS_CLIENT_ID")));

      // TODO: read from Client map.



      // ret.setParentFirstName(ifNull(rs.getString("OTHER_FIRST_NM")));
      // ret.setParentLastName(ifNull(rs.getString("OTHER_LAST_NM")));
      // ret.setParentRelationship(rs.getInt("OTHER_RELATIONSHIP"));
      // ret.setParentLastUpdated(rs.getTimestamp("OTHER_LAST_UPDATED"));
      // ret.setParentSourceTable(rs.getString("OTHER_SOURCE_TABLE"));
      // ret.setParentSensitivityIndicator(rs.getString("OTHER_SENSITIVITY_IND"));
    }

    //
    // Worker (staff):
    //
    final String workerId = ifNull(rs.getString("WORKER_ID"));
    if (StringUtils.isNotBlank(workerId) && staffWorkers.containsKey(workerId)) {
      final StaffPerson staffPerson = staffWorkers.get(workerId);
      final EmbeddableStaffWorker worker = ret.getWorker();
      worker.setWorkerId(workerId);
      worker.setWorkerFirstName(staffPerson.getFirstName());
      worker.setWorkerLastName(staffPerson.getLastName());
      worker.setWorkerLastUpdated(staffPerson.getLastUpdatedTime());
    }

    //
    // Access Limitation:
    //
    ret.setLimitedAccessCode(ifNull(rs.getString("LIMITED_ACCESS_CODE")));
    ret.setLimitedAccessDate(rs.getDate("LIMITED_ACCESS_DATE"));
    ret.setLimitedAccessDescription(ifNull(rs.getString("LIMITED_ACCESS_DESCRIPTION")));
    ret.setLimitedAccessGovernmentEntityId(rs.getInt("LIMITED_ACCESS_GOVERNMENT_ENT"));
    return ret;
  }

  // =====================
  // THREAD MEMORY:
  // =====================

  protected void releaseThreadMemory() {
    clearThreadContainers();
    System.gc(); // NOSONAR
  }

  protected void clearThreadContainers() {
    if (allocCases.get() == null) {
      this.allocCases.get().clear();
      this.allocMapCases.get().clear();
      this.allocMapClients.get().clear();
      this.allocReadyToNorm.get().clear();
    }
  }

  /**
   * Initial mode only. Allocate memory once per thread and reuse it.
   * 
   * <p>
   * NEXT: calculate container sizes by bundle size.
   * </p>
   */
  protected void allocateThreadMemory() {
    if (allocCases.get() == null) {
      allocCases.set(new ArrayList<>(150000));
      allocReadyToNorm.set(new ArrayList<>(150000));
      allocMapCases.set(new HashMap<>(99881)); // Prime
      allocReadyToNorm.set(new ArrayList<>(150000));
      clearThreadContainers();
    }
  }

  /**
   * Rocket entry point.
   * 
   * @param args command line arguments
   * @throws Exception on launch error
   */
  public static void main(String... args) throws Exception {
    LaunchCommand.launchOneWayTrip(CaseRocket.class, args);
  }

}
