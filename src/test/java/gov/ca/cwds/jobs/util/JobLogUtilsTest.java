package gov.ca.cwds.jobs.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import gov.ca.cwds.jobs.exception.JobsException;

public class JobLogUtilsTest {

  @Test
  public void type() throws Exception {
    assertThat(JobLogUtils.class, notNullValue());
  }

  @Test
  public void logEvery_Args__Logger__int__String__StringArray() throws Exception {
    Logger log = mock(Logger.class);
    int cntr = 0;
    String action = null;
    Object[] args = new String[] {};
    JobLogUtils.logEvery(log, cntr, action, args);
  }

  @Test
  public void logEvery_Args__int__String__StringArray() throws Exception {
    int cntr = 0;
    String action = null;
    Object[] args = new String[] {};
    JobLogUtils.logEvery(cntr, action, args);
  }

  @Test(expected = JobsException.class)
  public void throwFatalError_Args__Logger__Throwable__String__ObjectArray() throws Exception {
    Logger log = mock(Logger.class);
    Throwable e = new IllegalStateException("hello world");
    String pattern = null;
    Object[] args = new Object[] {};
    JobLogUtils.raiseError(log, e, pattern, args);
  }

  @Test(expected = JobsException.class)
  public void throwFatalError_Args__Logger__Throwable__String() throws Exception {
    Logger log = mock(Logger.class);
    Throwable e = new IllegalStateException("error message");
    String message = "hello world";
    JobLogUtils.raiseError(log, e, message);
  }

  @Test(expected = JobsException.class)
  public void logEvery_Args__Logger__int__String__ObjectArray() throws Exception {
    Exception e = new Exception();
    JobLogUtils.raiseError(JobLogUtils.LOGGER, e, "BATCH ERROR! {}", e.getMessage());
  }

  @Test
  public void logEvery_Args__int__String__ObjectArray() throws Exception {
    int cntr = 0;
    String action = null;
    Object[] args = new Object[] {};
    JobLogUtils.logEvery(cntr, action, args);
  }

  @Test(expected = JobsException.class)
  public void raiseError_Args__Logger__Throwable__String__ObjectArray() throws Exception {
    Logger log = mock(Logger.class);
    Throwable e = null;
    String pattern = null;
    Object[] args = new Object[] {};
    JobLogUtils.raiseError(log, e, pattern, args);
  }

  // @Test
  public void buildException_Args__Logger__Throwable__String__ObjectArray() throws Exception {
    Logger log = mock(Logger.class);
    Throwable e = null;
    String pattern = null;
    Object[] args = new Object[] {};
    JobsException actual = JobLogUtils.buildException(log, e, pattern, args);
    JobsException expected = new JobsException("", null);
    assertThat(actual, is(equalTo(expected)));
  }

  @Test(expected = JobsException.class)
  public void raiseError_Args__Logger__Throwable__ObjectArray() throws Exception {
    Logger log = mock(Logger.class);
    Throwable e = null;
    Object[] args = new Object[] {};
    JobLogUtils.raiseError(log, e, args);
  }

}

