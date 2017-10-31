package gov.ca.cwds.jobs.listener;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerKey;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.jobs.exception.NeutronException;
import gov.ca.cwds.jobs.schedule.LaunchDirector;
import gov.ca.cwds.jobs.schedule.NeutronInterruptableJob;
import gov.ca.cwds.jobs.util.JobLogs;

public class NeutronTriggerListener implements TriggerListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronTriggerListener.class);

  @Override
  public String getName() {
    return "neutron_trigger_listener";
  }

  @Override
  public void triggerFired(Trigger trigger, JobExecutionContext context) {
    final TriggerKey key = trigger.getKey();
    LOGGER.debug("trigger fired: key: {}", key);
    LaunchDirector.getInstance().getExecutingJobs().put(key,
        (NeutronInterruptableJob) context.getJobInstance());
  }

  /**
   * Job instance type is {@link NeutronInterruptableJob}.
   */
  @Override
  public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
    final JobDataMap map = context.getJobDetail().getJobDataMap();
    final String className = map.getString("job_class");
    boolean answer = true;

    try {
      answer = LaunchDirector.getInstance().isJobVetoed(className);
    } catch (NeutronException e) {
      throw JobLogs.buildRuntimeException(LOGGER, e, "ERROR FINDING JOB FACADE! job class: {}",
          className, e);
    }

    LOGGER.info("veto job execution: {}", answer);
    return answer;
  }

  @Override
  public void triggerMisfired(Trigger trigger) {
    final TriggerKey key = trigger.getKey();
    LOGGER.warn("TRIGGER MISFIRED! key: {}", key);
    LaunchDirector.getInstance().removeExecutingJob(key);
  }

  @Override
  public void triggerComplete(Trigger trigger, JobExecutionContext context,
      CompletedExecutionInstruction triggerInstructionCode) {
    final TriggerKey key = trigger.getKey();
    LOGGER.debug("trigger complete: key: {}", key);
    LaunchDirector.getInstance().removeExecutingJob(key);
  }

}
