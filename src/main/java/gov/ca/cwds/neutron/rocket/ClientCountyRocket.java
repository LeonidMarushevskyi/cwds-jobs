package gov.ca.cwds.neutron.rocket;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.ParameterMode;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.procedure.ProcedureCall;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.EsClientAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.inject.CmsSessionFactory;
import gov.ca.cwds.jobs.config.FlightPlan;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.jobs.util.jdbc.NeutronJdbcUtil;
import gov.ca.cwds.jobs.util.jdbc.NeutronRowMapper;
import gov.ca.cwds.neutron.atom.AtomValidateDocument;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;
import gov.ca.cwds.neutron.inject.annotation.LastRunFile;
import gov.ca.cwds.neutron.jetpack.ConditionalLogger;
import gov.ca.cwds.neutron.jetpack.JetPackLogger;
import gov.ca.cwds.neutron.jetpack.JobLogs;

/**
 * Job to load Clients from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class ClientCountyRocket extends InitialLoadJdbcRocket<ReplicatedClient, EsClientAddress>
    implements NeutronRowMapper<EsClientAddress>, AtomValidateDocument {

  private static final long serialVersionUID = 1L;

  private static final ConditionalLogger LOGGER = new JetPackLogger(ClientCountyRocket.class);

  private static final String INSERT_CLIENT_INITIAL_LOAD =
      "INSERT INTO GT_ID (IDENTIFIER)\n" + "SELECT x.IDENTIFIER \nFROM CLIENT_T x\n"
          + "WHERE x.IDENTIFIER BETWEEN ':fromId' AND ':toId' ";

  private AtomicInteger nextThreadNum = new AtomicInteger(0);

  /**
   * Construct batch job instance with all required dependencies.
   * 
   * @param dao Client DAO
   * @param esDao ElasticSearch DAO
   * @param lastRunFile last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param sessionFactory Hibernate session factory
   * @param flightPlan command line options
   */
  @Inject
  public ClientCountyRocket(final ReplicatedClientDao dao, final ElasticsearchDao esDao,
      @LastRunFile final String lastRunFile, final ObjectMapper mapper,
      @CmsSessionFactory SessionFactory sessionFactory, FlightPlan flightPlan) {
    super(dao, esDao, lastRunFile, mapper, sessionFactory, flightPlan);
  }

  @Override
  public EsClientAddress extract(ResultSet rs) throws SQLException {
    return EsClientAddress.extract(rs);
  }

  /**
   * NEXT: turn into a fixed rocket setting, not override method.
   */
  @Override
  public Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return EsClientAddress.class;
  }

  /**
   * NEXT: turn into a fixed rocket setting, not override method.
   */
  @Override
  public String getInitialLoadViewName() {
    return "CLIENT_T";
  }

  /**
   * NEXT: turn into a fixed rocket setting, not override method.
   */
  @Override
  public String getMQTName() {
    return getInitialLoadViewName();
  }

  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    return INSERT_CLIENT_INITIAL_LOAD;
  }

  /**
   * NEXT: turn into a fixed rocket setting, not override method.
   */
  @Override
  public String getJdbcOrderBy() {
    return " ORDER BY x.IDENTIFIER ";
  }

  /**
   * NEXT: Turn this method into a rocket setting.
   */
  @Override
  public boolean useTransformThread() {
    return false;
  }

  /**
   * NEXT: Turn this method into a rocket setting.
   */
  @Override
  public boolean isInitialLoadJdbc() {
    return true;
  }

  /**
   * Read records from the given key range, typically within a single partition on large tables.
   * 
   * @param p partition range to read
   */
  @Override
  public void pullRange(final Pair<String, String> p) {
    final String threadName =
        "extract_" + nextThreadNumber() + "_" + p.getLeft() + "_" + p.getRight();
    nameThread(threadName);
    getLogger().info("BEGIN: extract thread {}", threadName);
    getFlightLog().trackRangeStart(p);
    final String query = getInitialLoadQuery(getDBSchemaName()).replaceAll(":fromId", p.getLeft())
        .replaceAll(":toId", p.getRight());
    getLogger().info("query: {}", query);

    try (Connection con = NeutronJdbcUtil.prepConnection(getJobDao().getSessionFactory())) {
      try (Statement stmt = con.createStatement()) {
        stmt.setFetchSize(NeutronIntegerDefaults.FETCH_SIZE.getValue()); // faster
        stmt.setMaxRows(0);
        stmt.setQueryTimeout(0);

        getOrCreateTransaction(); // HACK: fix Hibernate DAO.
        stmt.executeUpdate(query); // NOSONAR

        callProc();
        con.commit();
      } catch (SQLException e) {
        LOGGER.error("ERROR CALLING CLIENT COUNTY PROC! SQL state: {}, error code: {}, msg: {}",
            e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
        con.rollback(); // Clear cursors.
        throw e;
      }
    } catch (Exception e) {
      fail();
      throw JobLogs.runtime(LOGGER, e, "PROC ERROR ON RANGE! {}-{} : {}", p.getLeft(), p.getRight(),
          e.getMessage());
    }
  }

  /**
   * Proc call for initial load: {@code CALL CWSRSQ.PRCCLNCNTY ('O', '', '', ?);}
   * 
   * <p>
   * Incremental changes are called by table trigger, not by batch.
   * </p>
   */
  protected void callProc() {
    if (LaunchCommand.isInitialMode()) { // initial mode only
      LOGGER.info("Call stored proc");
      final Session session = getJobDao().getSessionFactory().getCurrentSession();
      getOrCreateTransaction(); // HACK. move to base DAO.
      final String schema =
          (String) session.getSessionFactory().getProperties().get("hibernate.default_schema");

      final ProcedureCall proc = session.createStoredProcedureCall(schema + ".PRCCLNCNTY");
      proc.registerStoredProcedureParameter("PARM_CRUD", String.class, ParameterMode.IN);
      proc.registerStoredProcedureParameter("PARM_ID", String.class, ParameterMode.IN);
      proc.registerStoredProcedureParameter("PARM_TRIGTBL", String.class, ParameterMode.IN);
      proc.registerStoredProcedureParameter("RETCODE", Integer.class, ParameterMode.OUT);

      proc.setParameter("PARM_CRUD", "0");
      proc.setParameter("PARM_ID", "");
      proc.setParameter("PARM_TRIGTBL", "");
      proc.execute();

      final int retcode = ((Integer) proc.getOutputParameterValue("RETCODE")).intValue();
      LOGGER.info("Client county proc: retcode: {}", retcode);

      if (retcode != 0) {
        throw JobLogs.runtime(getLogger(), "PROC FAILED! {}", retcode);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void threadRetrieveByJdbc() {
    bigRetrieveByJdbc();
  }

  @Override
  public List<Pair<String, String>> getPartitionRanges() {
    return NeutronJdbcUtil.getCommonPartitionRanges64(this);
  }

  @Override
  public int nextThreadNumber() {
    return nextThreadNum.incrementAndGet();
  }

  /**
   * Batch job entry point.
   * 
   * @param args command line arguments
   */
  public static void main(String... args) {
    LaunchCommand.runStandalone(ClientCountyRocket.class, args);
  }

}
