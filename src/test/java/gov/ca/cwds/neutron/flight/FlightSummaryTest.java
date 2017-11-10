package gov.ca.cwds.neutron.flight;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.jobs.Goddard;
import gov.ca.cwds.jobs.schedule.StandardFlightSchedule;
import gov.ca.cwds.neutron.enums.FlightStatus;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.flight.FlightSummary;

public class FlightSummaryTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlightSummaryTest.class);

  FlightSummary target;

  @Before
  public void setup() throws Exception {
    target = new FlightSummary(StandardFlightSchedule.CLIENT);
  }

  @Test
  public void type() throws Exception {
    assertThat(FlightSummary.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    assertThat(target, notNullValue());
  }

  @Test
  public void accumulate_Args__FlightLog() throws Exception {
    final FlightLog flightLog = new FlightLog();
    flightLog.addAffectedDocumentId(Goddard.DEFAULT_CLIENT_ID);
    flightLog.addToQueuedToIndex(35);
    flightLog.addToBulkAfter(60);
    flightLog.addToBulkError(22);
    flightLog.addToBulkPrepared(36);
    flightLog.addToNormalized(45);
    flightLog.addToBulkDeleted(4);

    flightLog.trackQueuedToIndex();
    flightLog.trackQueuedToIndex();
    flightLog.trackQueuedToIndex();
    flightLog.trackQueuedToIndex();

    flightLog.trackBulkError();
    flightLog.trackBulkError();
    flightLog.trackBulkError();

    flightLog.trackBulkDeleted();
    flightLog.trackBulkDeleted();
    flightLog.trackBulkDeleted();
    flightLog.trackBulkDeleted();
    flightLog.done();

    target.accumulate(flightLog);
    LOGGER.info("summary: {}", target);

    assertThat(target.getRecsBulkPrepared(), is(not(0)));
  }

  @Test
  public void getStatus_Args__() throws Exception {
    Map<FlightStatus, Integer> actual = target.getStatus();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setStatus_Args__EnumMap() throws Exception {
    EnumMap<FlightStatus, Integer> status = mock(EnumMap.class);
    target.setStatus(status);
  }

  @Test
  public void getTotalRuns_Args__() throws Exception {
    int actual = target.getTotalRuns();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setTotalRuns_Args__int() throws Exception {
    int totalRuns = 0;
    target.setTotalRuns(totalRuns);
  }

  @Test
  public void getRecsSentToIndexQueue_Args__() throws Exception {
    int actual = target.getRecsSentToIndexQueue();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsSentToIndexQueue_Args__int() throws Exception {
    int recsSentToIndexQueue = 0;
    target.setRecsSentToIndexQueue(recsSentToIndexQueue);
  }

  @Test
  public void getRecsSentToBulkProcessor_Args__() throws Exception {
    int actual = target.getRecsSentToBulkProcessor();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsSentToBulkProcessor_Args__int() throws Exception {
    int recsSentToBulkProcessor = 0;
    target.setRecsSentToBulkProcessor(recsSentToBulkProcessor);
  }

  @Test
  public void getRowsNormalized_Args__() throws Exception {
    int actual = target.getRowsNormalized();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRowsNormalized_Args__int() throws Exception {
    int rowsNormalized = 0;
    target.setRowsNormalized(rowsNormalized);
  }

  @Test
  public void getRecsBulkPrepared_Args__() throws Exception {
    int actual = target.getRecsBulkPrepared();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsBulkPrepared_Args__int() throws Exception {
    int recsBulkPrepared = 0;
    target.setRecsBulkPrepared(recsBulkPrepared);
  }

  @Test
  public void getRecsBulkDeleted_Args__() throws Exception {
    int actual = target.getRecsBulkDeleted();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsBulkDeleted_Args__int() throws Exception {
    int recsBulkDeleted = 0;
    target.setRecsBulkDeleted(recsBulkDeleted);
  }

  @Test
  public void getRecsBulkBefore_Args__() throws Exception {
    int actual = target.getRecsBulkBefore();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsBulkBefore_Args__int() throws Exception {
    int recsBulkBefore = 0;
    target.setRecsBulkBefore(recsBulkBefore);
  }

  @Test
  public void getRecsBulkAfter_Args__() throws Exception {
    int actual = target.getRecsBulkAfter();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsBulkAfter_Args__int() throws Exception {
    int recsBulkAfter = 0;
    target.setRecsBulkAfter(recsBulkAfter);
  }

  @Test
  public void getRecsBulkError_Args__() throws Exception {
    int actual = target.getRecsBulkError();
    int expected = 0;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void setRecsBulkError_Args__int() throws Exception {
    int recsBulkError = 0;
    target.setRecsBulkError(recsBulkError);
  }

  @Test
  public void getFirstStart_Args__() throws Exception {
    Date actual = target.getFirstStart();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setFirstStart_Args__Date() throws Exception {
    Date firstStart = new Date();
    target.setFirstStart(firstStart);
  }

  @Test
  public void getLastEnd_Args__() throws Exception {
    Date actual = target.getLastEnd();
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void setLastEnd_Args__Date() throws Exception {
    Date lastEnd = new Date();
    target.setLastEnd(lastEnd);
  }

}
