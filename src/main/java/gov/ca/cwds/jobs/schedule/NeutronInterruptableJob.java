package gov.ca.cwds.jobs.schedule;

import java.util.concurrent.atomic.AtomicInteger;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.jobs.BasePersonIndexerJob;
import gov.ca.cwds.jobs.component.FlightRecord;

/**
 * Wrapper for scheduled jobs.
 * 
 * @author CWDS API Team
 * @see LaunchDirector
 */
@DisallowConcurrentExecution
public class NeutronInterruptableJob implements InterruptableJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronInterruptableJob.class);

  private static final AtomicInteger instanceCounter = new AtomicInteger(0);

  private final BasePersonIndexerJob rocket;

  private volatile FlightRecord track;

  /**
   * Constructor.
   */
  public NeutronInterruptableJob(final BasePersonIndexerJob rocket) {
    this.rocket = rocket;
  }

  /**
   * QUESTION: does Quartz allow injection? Is it safe to pass objects via the job data map??
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    final JobDataMap map = context.getJobDetail().getJobDataMap();
    final String jobName = context.getTrigger().getJobKey().getName();

    LOGGER.info("Execute {}, instance # ", rocket.getClass().getName(),
        instanceCounter.incrementAndGet());

    try (final BasePersonIndexerJob job = rocket) {
      track = new FlightRecord(); // fresh progress track
      track.setJobName(jobName);
      job.setTrack(track);

      map.put("opts", job.getOpts());
      map.put("track", track);
      context.setResult(track);

      job.run();
    } catch (Exception e) {
      LOGGER.error("FAILED TO LAUNCH! {}", rocket.getClass().getName(), e);
      throw new JobExecutionException("FAILED TO LAUNCH!", e);
    }

    LOGGER.info("Executed {}", rocket.getClass().getName());
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    LOGGER.warn("INTERRUPT RUNNING JOB!");
  }

  public FlightRecord getTrack() {
    return track;
  }

  public void setTrack(FlightRecord track) {
    this.track = track;
  }

}
