package gov.ca.cwds.jobs;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.ca.cwds.dao.cms.ReplicatedReporterDao;
import gov.ca.cwds.data.es.ElasticsearchDao;
import gov.ca.cwds.jobs.config.StaticSessionFactory;

/**
 * Test for {@link ReporterIndexerJob}.
 * 
 * @author CWDS API Team
 */
@SuppressWarnings("javadoc")
public class ReporterIndexerJobTest {

  @SuppressWarnings("unused")
  private static ReplicatedReporterDao reporterDao;
  private static SessionFactory sessionFactory;
  private Session session;

  @BeforeClass
  public static void beforeClass() {
    // sessionFactory =
    // new Configuration().configure("test-cms-hibernate.cfg.xml").buildSessionFactory();
    sessionFactory = StaticSessionFactory.getSessionFactory();
    reporterDao = new ReplicatedReporterDao(sessionFactory);
  }

  @AfterClass
  public static void afterClass() {
    sessionFactory.close();
  }

  @Before
  public void setup() {
    session = sessionFactory.getCurrentSession();
    session.beginTransaction();
  }

  @After
  public void teardown() {
    session.getTransaction().rollback();
  }

  @Test
  public void testType() throws Exception {
    assertThat(ReporterIndexerJob.class, notNullValue());
  }

  @Test
  public void testInstantiation() throws Exception {
    ReplicatedReporterDao reporterDao = null;
    ElasticsearchDao elasticsearchDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    ReporterIndexerJob target = new ReporterIndexerJob(reporterDao, elasticsearchDao,
        lastJobRunTimeFilename, mapper, sessionFactory);
    assertThat(target, notNullValue());
  }

  // @Test
  // public void testfindAllNamedQueryExists() throws Exception {
  // Query query =
  // session.getNamedQuery("gov.ca.cwds.data.persistence.cms.ReplicatedReporter.findAll");
  // assertThat(query, is(notNullValue()));
  // }
  //
  // @Test
  // public void testfindAllUpdatedAfterNamedQueryExists() throws Exception {
  // Query query = session
  // .getNamedQuery("gov.ca.cwds.data.persistence.cms.ReplicatedReporter.findAllUpdatedAfter");
  // assertThat(query, is(notNullValue()));
  // }

  // @Test
  // public void testFindAllByBucketNamedQueryExists() throws Exception {
  // Query query =
  // session.getNamedQuery("gov.ca.cwds.data.persistence.cms.Reporter.findAllByBucket");
  // assertThat(query, is(notNullValue()));
  // }

  @Test
  public void type() throws Exception {
    assertThat(ReporterIndexerJob.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    ReplicatedReporterDao dao = null;
    ElasticsearchDao esDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    ReporterIndexerJob target =
        new ReporterIndexerJob(dao, esDao, lastJobRunTimeFilename, mapper, sessionFactory);
    assertThat(target, notNullValue());
  }

  @Test
  public void getIdColumn_Args__() throws Exception {
    ReplicatedReporterDao dao = null;
    ElasticsearchDao esDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    final ReporterIndexerJob target =
        new ReporterIndexerJob(dao, esDao, lastJobRunTimeFilename, mapper, sessionFactory);
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    final String actual = target.getIdColumn();
    // then
    // e.g. : verify(mocked).called();
    final String expected = "FKREFERL_T";
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getLegacySourceTable_Args__() throws Exception {
    ReplicatedReporterDao dao = null;
    ElasticsearchDao esDao = null;
    String lastJobRunTimeFilename = null;
    ObjectMapper mapper = null;
    SessionFactory sessionFactory = null;
    ReporterIndexerJob target =
        new ReporterIndexerJob(dao, esDao, lastJobRunTimeFilename, mapper, sessionFactory);
    // given
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    final String actual = target.getLegacySourceTable();
    // then
    // e.g. : verify(mocked).called();
    String expected = "REPTR_T";
    assertThat(actual, is(equalTo(expected)));
  }

  // @Test
  // public void main_Args__StringArray() throws Exception {
  // // given
  // String[] args = new String[] {};
  // // e.g. : given(mocked.called()).willReturn(1);
  // // when
  // // ReporterIndexerJob.main(args);
  // // then
  // // e.g. : verify(mocked).called();
  // }

  // @Test
  public void main_Args__StringArray() throws Exception {
    // TODO auto-generated by JUnit Helper.
    // given
    String[] args = new String[] {};
    // e.g. : given(mocked.called()).willReturn(1);
    // when
    ReporterIndexerJob.main(args);
    // then
    // e.g. : verify(mocked).called();
  }

}
