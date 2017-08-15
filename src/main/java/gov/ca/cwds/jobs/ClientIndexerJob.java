package gov.ca.cwds.jobs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import gov.ca.cwds.dao.cms.ReplicatedClientDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.persistence.cms.EsClientAddress;
import gov.ca.cwds.data.persistence.cms.rep.ReplicatedClient;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.inject.CmsSessionFactory;
import gov.ca.cwds.jobs.inject.LastRunFile;
import gov.ca.cwds.jobs.util.JobLogUtils;
import gov.ca.cwds.jobs.util.jdbc.JobResultSetAware;
import gov.ca.cwds.jobs.util.transform.EntityNormalizer;

/**
 * Job to load Clients from CMS into ElasticSearch.
 * 
 * @author CWDS API Team
 */
public class ClientIndexerJob extends BasePersonIndexerJob<ReplicatedClient, EsClientAddress>
    implements JobResultSetAware<EsClientAddress> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientIndexerJob.class);

  private AtomicInteger nextThreadNum = new AtomicInteger(0);

  /**
   * Construct batch job instance with all required dependencies.
   * 
   * @param clientDao Client DAO
   * @param esDao ElasticSearch DAO
   * @param lastJobRunTimeFilename last run date in format yyyy-MM-dd HH:mm:ss
   * @param mapper Jackson ObjectMapper
   * @param sessionFactory Hibernate session factory
   */
  @Inject
  public ClientIndexerJob(final ReplicatedClientDao clientDao, final ElasticsearchDao esDao,
      @LastRunFile final String lastJobRunTimeFilename, final ObjectMapper mapper,
      @CmsSessionFactory SessionFactory sessionFactory) {
    super(clientDao, esDao, lastJobRunTimeFilename, mapper, sessionFactory);
  }

  @Override
  public EsClientAddress extract(ResultSet rs) throws SQLException {
    return EsClientAddress.extract(rs);
  }

  @Override
  protected Class<? extends ApiGroupNormalizer<? extends PersistentObject>> getDenormalizedClass() {
    return EsClientAddress.class;
  }

  @Override
  public String getInitialLoadViewName() {
    return "MQT_CLIENT_ADDRESS";
  }

  @Override
  public String getJdbcOrderBy() {
    return " ORDER BY x.clt_identifier ";
  }

  @Override
  public String getInitialLoadQuery(String dbSchemaName) {
    StringBuilder buf = new StringBuilder();
    buf.append("SELECT x.* FROM ");
    buf.append(dbSchemaName);
    buf.append(".");
    buf.append(getInitialLoadViewName());
    buf.append(" x WHERE x.clt_identifier > ':fromId' AND ':toId' <= x.clt_identifier ");

    if (!getOpts().isLoadSealedAndSensitive()) {
      buf.append(" AND x.CLT_SENSTV_IND = 'N' ");
    }

    buf.append(getJdbcOrderBy()).append(" FOR READ ONLY WITH UR ");
    return buf.toString();
  }

  protected void handOff(List<EsClientAddress> grpRecs) {
    try {
      lock.readLock().unlock();
      lock.writeLock().lock();
      for (EsClientAddress cla : grpRecs) {
        queueTransform.putLast(cla);
      }
      lock.readLock().lock();
    } catch (InterruptedException ie) { // NOSONAR
      LOGGER.warn("interrupted: {}", ie.getMessage(), ie);
      fatalError = true;
      Thread.currentThread().interrupt();
    } finally {
      lock.writeLock().unlock();
    }
  }

  protected void extractPartitionRange(final Pair<String, String> p) {
    final int i = nextThreadNum.incrementAndGet();
    Thread.currentThread().setName("extract_" + i);
    LOGGER.info("BEGIN: Stage #1: extract " + i);

    try {
      Connection con = jobDao.getSessionFactory().getSessionFactoryOptions().getServiceRegistry()
          .getService(ConnectionProvider.class).getConnection();
      con.setSchema(getDBSchemaName());
      con.setAutoCommit(false);
      con.setReadOnly(true); // WARNING: fails with Postgres.

      // Linux MQT lacks ORDER BY clause. Must sort manually.
      // Either detect platform or force ORDER BY clause.
      final String query = getInitialLoadQuery(getDBSchemaName()).replaceAll(":fromId", p.getLeft())
          .replaceAll(":toId", p.getRight());
      enableParallelism(con);

      int cntr = 0;
      EsClientAddress m;
      Object lastId = new Object();
      List<EsClientAddress> grpRecs = new ArrayList<>();

      try (Statement stmt = con.createStatement()) {
        stmt.setFetchSize(5000); // faster
        stmt.setMaxRows(0);
        stmt.setQueryTimeout(100000);

        final ResultSet rs = stmt.executeQuery(query); // NOSONAR
        lock.readLock().lock();

        while (!fatalError && rs.next() && (m = extract(rs)) != null) {
          // Hand the baton to the next runner ...
          JobLogUtils.logEvery(++cntr, "Retrieved", "recs");
          // NOTE: Assumes that records are sorted by group key.
          if (!lastId.equals(m.getNormalizationGroupKey()) && cntr > 1) {
            handOff(grpRecs);
            grpRecs.clear(); // Single thread, re-use memory.
          }

          grpRecs.add(m);
          lastId = m.getNormalizationGroupKey();
        }

        con.commit();
      } finally {
        // Statement closes automatically.
        lock.readLock().unlock();

        // Close connection, return to pool?
        jobDao.getSessionFactory().getSessionFactoryOptions().getServiceRegistry()
            .getService(ConnectionProvider.class).closeConnection(con);
      }

    } catch (Exception e) {
      fatalError = true;
      JobLogUtils.raiseError(LOGGER, e, "BATCH ERROR! {}", e.getMessage());
    }

    LOGGER.info("DONE: Extract " + i);
  }

  /**
   * The "extract" part of ETL. Single producer, chained consumers.
   */
  @Override
  protected void threadExtractJdbc() {
    Thread.currentThread().setName("extract_main");
    LOGGER.info("BEGIN: main extract thread");

    try {
      final int maxThreads = Math.min(Runtime.getRuntime().availableProcessors(), 4);
      // System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
      // String.valueOf(maxThreads));
      LOGGER.info("JDBC processors={}", maxThreads);
      ForkJoinPool forkJoinPool = new ForkJoinPool(maxThreads);
      forkJoinPool.submit(
          () -> getPartitionRanges().stream().sequential().forEach(this::extractPartitionRange));
    } catch (Exception e) {
      fatalError = true;
      JobLogUtils.raiseError(LOGGER, e, "BATCH ERROR! {}", e.getMessage());
    } finally {
      doneExtract = true;
    }

    LOGGER.info("DONE: main extract thread");
  }

  @Override
  protected List<Pair<String, String>> getPartitionRanges() {
    List<Pair<String, String>> ret = new ArrayList<>();

    // z/OS:
    ret.add(Pair.of("aaaaaaaaaa", "B3bMRWu8NV"));
    ret.add(Pair.of("B3bMRWu8NV", "DW5GzxJ30A"));
    ret.add(Pair.of("DW5GzxJ30A", "FNOBbaG6qq"));
    ret.add(Pair.of("FNOBbaG6qq", "HJf1EJe25X"));
    ret.add(Pair.of("HJf1EJe25X", "JCoyq0Iz36"));
    ret.add(Pair.of("JCoyq0Iz36", "LvijYcj01S"));
    ret.add(Pair.of("LvijYcj01S", "Npf4LcB3Lr"));
    ret.add(Pair.of("Npf4LcB3Lr", "PiJ6a0H49S"));
    ret.add(Pair.of("PiJ6a0H49S", "RbL4aAL34A"));
    ret.add(Pair.of("RbL4aAL34A", "S3qiIdg0BN"));
    ret.add(Pair.of("S3qiIdg0BN", "0Ltok9y5Co"));
    ret.add(Pair.of("0Ltok9y5Co", "2CFeyJd49S"));
    ret.add(Pair.of("2CFeyJd49S", "4w3QDw136B"));
    ret.add(Pair.of("4w3QDw136B", "6p9XaHC10S"));
    ret.add(Pair.of("6p9XaHC10S", "8jw5J580MQ"));
    ret.add(Pair.of("8jw5J580MQ", "9999999999"));

    return ret;
  }

  @Override
  public boolean mustDeleteLimitedAccessRecords() {
    /**
     * If sealed or sensitive data must NOT be loaded then any records indexed with sealed or
     * sensitive flag must be deleted.
     */
    return !getOpts().isLoadSealedAndSensitive();
  }

  @Override
  protected List<ReplicatedClient> normalize(List<EsClientAddress> recs) {
    return EntityNormalizer.<ReplicatedClient, EsClientAddress>normalizeList(recs);
  }

  /**
   * Batch job entry point.
   * 
   * @param args command line arguments
   */
  public static void main(String... args) {
    runMain(ClientIndexerJob.class, args);
  }

}
