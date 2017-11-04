package gov.ca.cwds.jobs.schedule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.TriggerFiredBundle;

import com.google.inject.Injector;

import gov.ca.cwds.jobs.BasePersonIndexerJob;
import gov.ca.cwds.jobs.PersonJobTester;
import gov.ca.cwds.jobs.config.FlightPlan;
import gov.ca.cwds.jobs.test.TestJob;

public class RocketFactoryTest extends PersonJobTester {

  RocketFactory target;
  Injector injector;
  FlightPlan opts;
  FlightPlanLog rocketOptions;

  JobDetail jd;
  JobDataMap jobDataMap;
  TriggerFiredBundle bundle = mock(TriggerFiredBundle.class);
  Scheduler scheduler = mock(Scheduler.class);
  BasePersonIndexerJob rocket;

  @Override
  @Before
  public void setup() throws Exception {
    super.setup();

    injector = mock(Injector.class);
    rocketOptions = mock(FlightPlanLog.class);
    jobDataMap = mock(JobDataMap.class);
    jd = mock(JobDetail.class);
    bundle = mock(TriggerFiredBundle.class);
    scheduler = mock(Scheduler.class);

    when(bundle.getJobDetail()).thenReturn(jd);
    when(jd.getJobDataMap()).thenReturn(jobDataMap);
    when(jobDataMap.getString(any())).thenReturn(TestJob.class.getName());

    rocket = new TestJob(esDao, lastJobRunTimeFilename, MAPPER, jobHistory);
    when(injector.getInstance(any(Class.class))).thenReturn(rocket);
    // opts = new FlightPlan();

    target = new RocketFactory(injector, opts, rocketOptions);
  }

  @Test
  public void type() throws Exception {
    assertThat(RocketFactory.class, notNullValue());
  }

  @Test
  public void instantiation() throws Exception {
    RocketFactory target = new RocketFactory(injector, opts, rocketOptions);
    assertThat(target, notNullValue());
  }

  @Test
  public void createJob_Args__Class__FlightPlan() throws Exception {
    Class<?> klass = TestJob.class;
    FlightPlan opts_ = mock(FlightPlan.class);
    BasePersonIndexerJob actual = target.createJob(klass, opts_);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  public void createJob_Args__String__FlightPlan() throws Exception {
    String jobName = TestJob.class.getName();
    FlightPlan opts_ = mock(FlightPlan.class);
    BasePersonIndexerJob actual = target.createJob(jobName, opts_);
    assertThat(actual, is(notNullValue()));
  }

  @Test
  @Ignore
  public void newJob_Args__TriggerFiredBundle__Scheduler() throws Exception {
    Job actual = target.newJob(bundle, scheduler);
    Job expected = null;
    assertThat(actual, is(equalTo(expected)));
  }

  @Test
  public void newJob_Args__TriggerFiredBundle__Scheduler_T__SchedulerException() throws Exception {
    try {
      target.newJob(bundle, scheduler);
      fail("Expected exception was not thrown!");
    } catch (SchedulerException e) {
    }
  }

  @Test
  public void getBaseOpts_Args__() throws Exception {
    FlightPlan actual = target.getBaseOpts();
    // assertThat(actual, is(notNullValue()));
  }

  @Test
  public void getRocketOptions_Args__() throws Exception {
    FlightPlanLog actual = target.getRocketOptions();
    assertThat(actual, is(notNullValue()));
  }

}
