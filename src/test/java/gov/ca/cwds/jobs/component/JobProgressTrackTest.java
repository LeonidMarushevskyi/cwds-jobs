package gov.ca.cwds.jobs.component;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import gov.ca.cwds.jobs.schedule.NeutronJobExecutionStatus;
import gov.ca.cwds.rest.api.domain.DomainChef;

public class JobProgressTrackTest {

  JobProgressTrack target;

  @Before
  public void setup() throws Exception {
    target = new JobProgressTrack();
  }

  @Test
  public void type() throws Exception {
    assertThat(JobProgressTrack.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void trackQueuedToIndex_Args__() throws Exception {
    int actual = target.trackQueuedToIndex();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void trackNormalized_Args__() throws Exception {
    int actual = target.trackNormalized();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void trackBulkDeleted_Args__() throws Exception {
    int actual = target.trackBulkDeleted();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void trackBulkPrepared_Args__() throws Exception {
    int actual = target.trackBulkPrepared();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void trackBulkError_Args__() throws Exception {
    int actual = target.trackBulkError();
    int expected = 1;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void trackRangeStart_Args__Pair() throws Exception {
    Pair<String, String> pair = Pair.of("1", "2");
    target.trackRangeStart(pair);
  }

  @Test
  public void trackRangeComplete_Args__Pair() throws Exception {
    Pair<String, String> pair = Pair.of("1", "2");
    target.trackRangeComplete(pair);
  }

  @Test
  public void start_Args__() throws Exception {
    target.start();
  }

  @Test
  public void fail_Args__() throws Exception {
    target.fail();
  }

  @Test
  public void done_Args__() throws Exception {
    target.done();
  }

  @Test
  public void hashCode_Args__() throws Exception {
    int actual = target.hashCode();
    assertThat(actual, not(equalTo(0)));
  }

  @Test
  public void equals_Args__Object() throws Exception {
    Object obj = null;
    boolean actual = target.equals(obj);
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadRangesStarted_Args__() throws Exception {
    List<Pair<String, String>> actual = target.getInitialLoadRangesStarted();
    List<Pair<String, String>> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getInitialLoadRangesCompleted_Args__() throws Exception {
    List<Pair<String, String>> actual = target.getInitialLoadRangesCompleted();
    List<Pair<String, String>> expected = new ArrayList<>();
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void toString_Args__initial_load() throws Exception {
    target.setLastChangeSince(DomainChef.uncookTimestampString("2017-12-25-08.32.05.123"));
    target.done();
    target.setInitialLoad(true);
    String actual = target.toString();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void toString_Args__last_chg() throws Exception {
    target.setLastChangeSince(DomainChef.uncookTimestampString("2017-12-25-08.32.05.123"));
    target.done();
    target.setInitialLoad(false);
    String actual = target.toString();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void isInitialLoad_Args__() throws Exception {
    boolean actual = target.isInitialLoad();
    boolean expected = false;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setInitialLoad_Args__boolean() throws Exception {
    boolean initialLoad = false;
    target.setInitialLoad(initialLoad);
  }

  @Test
  public void getLastChangeSince_Args__() throws Exception {
    Date actual = target.getLastChangeSince();
    Date expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setLastChangeSince_Args__Date() throws Exception {
    Date lastChangeSince = mock(Date.class);
    target.setLastChangeSince(lastChangeSince);
  }

  @Test
  public void toString_Args__() throws Exception {
    String actual = target.toString();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void addAffectedDocumentId_Args__String() throws Exception {
    String docId = "abc1234567";
    target.addAffectedDocumentId(docId);
  }

  @Test
  public void getJobName_Args__() throws Exception {
    String actual = target.getJobName();
    String expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setJobName_Args__String() throws Exception {
    String jobName = null;
    target.setJobName(jobName);
  }

  @Test
  public void getStartTime_Args__() throws Exception {
    long actual = target.getStartTime();
    long expected = 0L;
    assertThat(actual, is(not(expected)));
  }

  @Test
  public void getEndTime_Args__() throws Exception {
    long actual = target.getEndTime();
    long expected = 0L;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getStatus_Args__() throws Exception {
    NeutronJobExecutionStatus actual = target.getStatus();
    NeutronJobExecutionStatus expected = NeutronJobExecutionStatus.NOT_STARTED;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void getAffectedDocuments_Args__() throws Exception {
    Queue<String> actual = target.getSampleDocumentKeys();
    assertThat(actual, is(notNullValue()));
  }

}
