package gov.ca.cwds.jobs;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.db2.jcc.DB2SystemMonitor;
import com.ibm.db2.jcc.am.DatabaseMetaData;

import gov.ca.cwds.ObjectMapperUtils;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.jobs.config.JobOptions;
import gov.ca.cwds.jobs.test.SimpleTestSystemCodeCache;
import gov.ca.cwds.rest.ElasticsearchConfiguration;

public class PersonJobTester {

  public static class TestDB2SystemMonitor implements DB2SystemMonitor {

    @Override
    public void enable(boolean paramBoolean) throws SQLException {}

    @Override
    public void start(int paramInt) throws SQLException {}

    @Override
    public void stop() throws SQLException {}

    @Override
    public long getServerTimeMicros() throws SQLException {
      return 0;
    }

    @Override
    public long getNetworkIOTimeMicros() throws SQLException {
      return 0;
    }

    @Override
    public long getCoreDriverTimeMicros() throws SQLException {
      return 0;
    }

    @Override
    public long getApplicationTimeMillis() throws SQLException {
      return 0;
    }

    @Override
    public Object moreData(int paramInt) throws SQLException {
      return "nothin";
    }

  }

  protected static final ObjectMapper mapper = ObjectMapperUtils.createObjectMapper();

  @BeforeClass
  public static void setupClass() {
    BasePersonIndexerJob.setTestMode(true);
    SimpleTestSystemCodeCache.init();
  }

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  ElasticsearchConfiguration esConfig;
  ElasticsearchDao esDao;
  ElasticSearchPerson esp = new ElasticSearchPerson();

  JobOptions opts;
  File tempFile;
  String lastJobRunTimeFilename;

  SessionFactory sessionFactory;
  Session session;
  SessionFactoryOptions sfo;
  Transaction transaction;
  StandardServiceRegistry reg;
  ConnectionProvider cp;
  Connection con;
  Statement stmt;
  ResultSet rs;
  DatabaseMetaData meta;

  @Before
  public void setup() throws Exception {
    System.setProperty("DB_CMS_SCHEMA", "CWSRS1");

    // Last run time:
    tempFile = tempFolder.newFile("tempFile.txt");
    lastJobRunTimeFilename = tempFile.getAbsolutePath();

    // JDBC:
    sessionFactory = mock(SessionFactory.class);
    session = mock(Session.class);
    transaction = mock(Transaction.class);
    sfo = mock(SessionFactoryOptions.class);
    reg = mock(StandardServiceRegistry.class);
    cp = mock(ConnectionProvider.class);
    con = mock(Connection.class);
    rs = mock(ResultSet.class);
    meta = mock(DatabaseMetaData.class);
    stmt = mock(Statement.class);

    when(sessionFactory.getCurrentSession()).thenReturn(session);
    when(session.beginTransaction()).thenReturn(transaction);
    when(sessionFactory.getSessionFactoryOptions()).thenReturn(sfo);
    when(sfo.getServiceRegistry()).thenReturn(reg);
    when(reg.getService(ConnectionProvider.class)).thenReturn(cp);
    when(cp.getConnection()).thenReturn(con);
    when(con.getMetaData()).thenReturn(meta);
    when(con.createStatement()).thenReturn(stmt);
    when(stmt.executeQuery(any())).thenReturn(rs);

    // Result set:
    when(rs.next()).thenReturn(true).thenReturn(false);
    when(rs.getString(any())).thenReturn("abc123456789");
    when(rs.getString(contains("IBMSNAP_OPERATION"))).thenReturn("I");
    when(rs.getString("LIMITED_ACCESS_CODE")).thenReturn("N");
    when(rs.getInt(any())).thenReturn(0);

    final java.util.Date date = new java.util.Date();
    final Timestamp ts = new Timestamp(date.getTime());
    when(rs.getDate(any())).thenReturn(new Date(date.getTime()));
    when(rs.getTimestamp("LIMITED_ACCESS_CODE")).thenReturn(ts);
    when(rs.getTimestamp(any())).thenReturn(ts);

    // DB2 platform and version:
    when(meta.getDatabaseMajorVersion()).thenReturn(11);
    when(meta.getDatabaseMinorVersion()).thenReturn(2);
    when(meta.getDatabaseProductName()).thenReturn("DB2");
    when(meta.getDatabaseProductVersion()).thenReturn("DSN11010");

    // Elasticsearch:
    esDao = mock(ElasticsearchDao.class);
    esConfig = mock(ElasticsearchConfiguration.class);

    when(esDao.getConfig()).thenReturn(esConfig);
    when(esConfig.getElasticsearchAlias()).thenReturn("people");
    when(esConfig.getElasticsearchDocType()).thenReturn("person");

    // Job options:
    opts = mock(JobOptions.class);
    when(opts.isLoadSealedAndSensitive()).thenReturn(false);
  }

}
