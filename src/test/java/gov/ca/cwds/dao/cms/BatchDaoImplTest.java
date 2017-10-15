package gov.ca.cwds.dao.cms;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.type.StringType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.ca.cwds.jobs.PersonJobTester;
import gov.ca.cwds.jobs.test.TestDenormalizedEntity;
import gov.ca.cwds.jobs.test.TestNormalizedEntity;

public class BatchDaoImplTest
    extends PersonJobTester<TestNormalizedEntity, TestDenormalizedEntity> {

  private static final class TestBatchDaoImpl extends BatchDaoImpl<TestDenormalizedEntity> {

    public TestBatchDaoImpl(SessionFactory sessionFactory) {
      super(sessionFactory);
    }

  }

  BatchDaoImpl target;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();
    target = new TestBatchDaoImpl(sessionFactory);
  }

  @Test
  public void type() throws Exception {
    assertThat(BatchDaoImpl.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void findAll_Args__() throws Exception {
    final Query query = Mockito.mock(Query.class);

    when(sessionFactory.getCurrentSession()).thenReturn(session);
    when(session.getNamedQuery(any())).thenReturn(query);
    when(query.list()).thenReturn(new ArrayList<>());

    final List<Object> actual = target.findAll();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void findAllUpdatedAfter_Args__Date() throws Exception {
    final Query q = Mockito.mock(Query.class);

    when(sessionFactory.getCurrentSession()).thenReturn(session);
    when(session.getNamedQuery(any())).thenReturn(q);
    when(q.list()).thenReturn(new ArrayList<>());

    when(q.setString(any(String.class), any(String.class))).thenReturn(q);
    when(q.setParameter(any(String.class), any(String.class), any(StringType.class))).thenReturn(q);
    when(q.setHibernateFlushMode(any(FlushMode.class))).thenReturn(q);
    when(q.setReadOnly(any(Boolean.class))).thenReturn(q);
    when(q.setCacheMode(any(CacheMode.class))).thenReturn(q);
    when(q.setFetchSize(any(Integer.class))).thenReturn(q);
    when(q.setCacheable(any(Boolean.class))).thenReturn(q);
    when(q.setTimestamp(any(String.class), any(Timestamp.class))).thenReturn(q);

    final ScrollableResults results = mock(ScrollableResults.class);
    when(q.scroll(any(ScrollMode.class))).thenReturn(results);
    when(results.next()).thenReturn(true).thenReturn(false);
    when(results.get()).thenReturn(new Object[0]);

    final Date datetime = new Date();
    final List<Object> actual = target.findAllUpdatedAfter(datetime);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void partitionedBucketList_Args__long__long__String__String() throws Exception {
    long bucketNum = 0L;
    long totalBuckets = 0L;
    String minId = "1";
    String maxId = "2";
    final List<Object> actual = target.partitionedBucketList(bucketNum, totalBuckets, minId, maxId);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void bucketList_Args__long__long() throws Exception {
    long bucketNum = 0L;
    long totalBuckets = 0L;
    final List<Object> actual = target.bucketList(bucketNum, totalBuckets);
    assertThat(actual, is(notNullValue()));
  }

}

