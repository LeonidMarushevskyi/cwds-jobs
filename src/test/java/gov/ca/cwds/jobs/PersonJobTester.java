package gov.ca.cwds.jobs;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StringType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.db2.jcc.am.DatabaseMetaData;

import gov.ca.cwds.ObjectMapperUtils;
import gov.ca.cwds.data.cms.SystemCodeDao;
import gov.ca.cwds.data.cms.SystemMetaDao;
import gov.ca.cwds.data.es.ElasticSearchPerson;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.component.FlightRecord;
import gov.ca.cwds.jobs.config.FlightPlan;
import gov.ca.cwds.jobs.schedule.FlightRecorder;
import gov.ca.cwds.jobs.schedule.LaunchCommand;
import gov.ca.cwds.jobs.schedule.LaunchScheduler;
import gov.ca.cwds.jobs.schedule.RocketFactory;
import gov.ca.cwds.jobs.test.Mach1TestRocket;
import gov.ca.cwds.jobs.test.SimpleTestSystemCodeCache;
import gov.ca.cwds.jobs.util.transform.ElasticTransformer;
import gov.ca.cwds.rest.ElasticsearchConfiguration;

public class PersonJobTester<T extends PersistentObject, M extends ApiGroupNormalizer<?>> {

  protected static final ObjectMapper MAPPER = ObjectMapperUtils.createObjectMapper();

  protected static final ObjectMapper mapper = MAPPER;

  public static final String DEFAULT_CLIENT_ID = "abc1234567";

  public final AtomicBoolean isRunwayClear = new AtomicBoolean(false);

  public final Lock lock = new ReentrantLock();

  @BeforeClass
  public static void setupClass() {
    LaunchCommand.setTestMode(true);
    SimpleTestSystemCodeCache.init();
    ElasticTransformer.setMapper(MAPPER);
  }

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  public ElasticsearchConfiguration esConfig;
  public ElasticsearchDao esDao;
  public Client client;
  public ElasticSearchPerson esp;

  public File tempFile;
  public File jobConfigFile;
  public File esConfileFile;
  public String lastJobRunTimeFilename;
  public java.util.Date lastRunTime = new java.util.Date();

  public SessionFactory sessionFactory;
  public Session session;
  public EntityManager em;
  public SessionFactoryOptions sfo;
  public Transaction transaction;
  public StandardServiceRegistry reg;
  public ConnectionProvider cp;
  public Connection con;
  public Statement stmt;
  public ResultSet rs;
  public DatabaseMetaData meta;
  public NativeQuery<M> nq;

  public SystemCodeDao systemCodeDao;
  public SystemMetaDao systemMetaDao;

  public Configuration hibernationConfiguration;

  public FlightPlan opts;
  public FlightRecord flightRecord;
  public FlightRecorder flightRecorder;

  public LaunchScheduler launchScheduler;
  public RocketFactory rocketFactory;
  public BasePersonIndexerJob mach1Rocket;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    System.setProperty("DB_CMS_SCHEMA", "CWSRS1");

    // Last run time:
    tempFile = tempFolder.newFile("tempFile.txt");
    jobConfigFile = tempFolder.newFile("jobConfigFile.yml");
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
    em = mock(EntityManager.class);
    client = mock(Client.class);
    hibernationConfiguration = mock(Configuration.class);
    rocketFactory = mock(RocketFactory.class);
    mach1Rocket = new Mach1TestRocket(esDao, lastJobRunTimeFilename, MAPPER, flightRecorder);

    when(sessionFactory.getCurrentSession()).thenReturn(session);
    when(sessionFactory.createEntityManager()).thenReturn(em);
    when(session.beginTransaction()).thenReturn(transaction);
    when(session.getTransaction()).thenReturn(transaction);
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

    // Elasticsearch basics:
    esp = new ElasticSearchPerson();
    esDao = mock(ElasticsearchDao.class);
    esConfig = mock(ElasticsearchConfiguration.class);

    when(esDao.getConfig()).thenReturn(esConfig);
    when(esDao.getClient()).thenReturn(client);

    final Settings settings = Settings.builder().put(new HashMap<String, String>()).build();
    when(client.settings()).thenReturn(settings);

    when(esConfig.getElasticsearchAlias()).thenReturn("people");
    when(esConfig.getElasticsearchDocType()).thenReturn("person");

    // Job options:
    esConfileFile = tempFolder.newFile("es.yml");
    opts = mock(FlightPlan.class);

    when(opts.isLoadSealedAndSensitive()).thenReturn(false);
    when(opts.getEsConfigLoc()).thenReturn(esConfileFile.getAbsolutePath());

    // Queries.
    nq = mock(NativeQuery.class);
    when(session.getNamedNativeQuery(any(String.class))).thenReturn(nq);
    when(nq.setString(any(String.class), any(String.class))).thenReturn(nq);
    when(nq.setParameter(any(String.class), any(String.class), any(StringType.class)))
        .thenReturn(nq);
    when(nq.setFlushMode(any(FlushMode.class))).thenReturn(nq);
    when(nq.setHibernateFlushMode(any(FlushMode.class))).thenReturn(nq);
    when(nq.setReadOnly(any(Boolean.class))).thenReturn(nq);
    when(nq.setCacheMode(any(CacheMode.class))).thenReturn(nq);
    when(nq.setFetchSize(any(Integer.class))).thenReturn(nq);
    when(nq.setCacheable(any(Boolean.class))).thenReturn(nq);

    // Job track:
    flightRecord = new FlightRecord();
    flightRecorder = new FlightRecorder();
    launchScheduler = mock(LaunchScheduler.class);

    // Elasticsearch msearch.
    final MultiSearchRequestBuilder mBuilder = mock(MultiSearchRequestBuilder.class);
    final MultiSearchResponse multiResponse = mock(MultiSearchResponse.class);
    final SearchRequestBuilder sBuilder = mock(SearchRequestBuilder.class);
    final MultiSearchResponse.Item item = mock(MultiSearchResponse.Item.class);
    final MultiSearchResponse.Item[] items = new MultiSearchResponse.Item[1];
    items[0] = item;

    final SearchHits hits = mock(SearchHits.class);
    final SearchHit hit = mock(SearchHit.class);
    final SearchHit[] hitArray = {hit};
    final SearchResponse sr = mock(SearchResponse.class);

    when(client.prepareMultiSearch()).thenReturn(mBuilder);
    when(client.prepareSearch()).thenReturn(sBuilder);

    when(mBuilder.add(any(SearchRequestBuilder.class))).thenReturn(mBuilder);
    when(mBuilder.get()).thenReturn(multiResponse);
    when(sBuilder.setQuery(any())).thenReturn(sBuilder);
    when(multiResponse.getResponses()).thenReturn(items);
    when(item.getResponse()).thenReturn(sr);
    when(sr.getHits()).thenReturn(hits);
    when(hits.getHits()).thenReturn(hitArray);

    when(hit.docId()).thenReturn(12345);
    when(hit.getSourceAsString())
        .thenReturn(IOUtils.toString(getClass().getResourceAsStream("/fixtures/es_person.json")));

    systemCodeDao = mock(SystemCodeDao.class);
    systemMetaDao = mock(SystemMetaDao.class);

    // Command and control.
    when(launchScheduler.createJob(any(String.class), any(FlightPlan.class)))
        .thenReturn(mach1Rocket);
    when(launchScheduler.runScheduledJob(any(Class.class), any(FlightPlan.class)))
        .thenReturn(flightRecord);
    when(rocketFactory.createJob(any(Class.class), any(FlightPlan.class))).thenReturn(mach1Rocket);
    when(rocketFactory.createJob(any(String.class), any(FlightPlan.class))).thenReturn(mach1Rocket);

    markTestDone();
  }

  public Thread runKillThread(final BasePersonIndexerJob<T, M> target, long sleepMillis) {
    final Thread t = new Thread(() -> {
      try {
        lock.lockInterruptibly();
        await("kill thread").atMost(sleepMillis, TimeUnit.MILLISECONDS).untilTrue(isRunwayClear);
        target.done();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        lock.unlock();
      }
    });

    t.start();
    return t;
  }

  public Thread runKillThread(final BasePersonIndexerJob<T, M> target) {
    return runKillThread(target, 1100L);
  }

  public void markTestDone() {
    isRunwayClear.set(true);
  }

}
