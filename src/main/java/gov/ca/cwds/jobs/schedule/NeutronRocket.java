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

import gov.ca.cwds.data.persistence.PersistentObject;
import gov.ca.cwds.data.std.ApiGroupNormalizer;
import gov.ca.cwds.jobs.BasePersonIndexerJob;
import gov.ca.cwds.jobs.component.FlightLog;

/**
 * Wrapper for scheduled jobs.
 * 
 * @author CWDS API Team
 * @see LaunchCommand
 */
@DisallowConcurrentExecution
public class NeutronRocket implements InterruptableJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronRocket.class);

  private static final AtomicInteger instanceCounter = new AtomicInteger(0);

  @SuppressWarnings("rawtypes")
  private final BasePersonIndexerJob rocket;

  private final FlightRecorder flightRecorder;

  private final DefaultFlightSchedule flightSchedule;

  private volatile FlightLog flightLog; // volatile shows changes immediately across threads

  /**
   * Constructor.
   * 
   * @param <T> ES replicated Person persistence class
   * @param <M> MQT entity class, if any, or T
   * @param rocket launch me!
   * @param flightSchedule flight schedule
   * @param flightRecorder common flight recorder
   */
  public <T extends PersistentObject, M extends ApiGroupNormalizer<?>> NeutronRocket(
      final BasePersonIndexerJob<T, M> rocket, final DefaultFlightSchedule flightSchedule,
      final FlightRecorder flightRecorder) {
    this.rocket = rocket;
    this.flightSchedule = flightSchedule;
    this.flightRecorder = flightRecorder;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    final JobDataMap map = context.getJobDetail().getJobDataMap();
    final String jobName = context.getTrigger().getJobKey().getName();

    LOGGER.info("Execute {}, instance # {}", rocket.getClass().getName(),
        instanceCounter.incrementAndGet());

    try (final BasePersonIndexerJob job = rocket) {
      flightLog = new FlightLog(); // fresh progress track
      flightLog.setRocketName(jobName);
      job.setTrack(flightLog);

      map.put("opts", job.getFlightPlan());
      map.put("track", flightLog);
      context.setResult(flightLog);

      job.run();
    } catch (Exception e) {
      LOGGER.error("FAILED TO LAUNCH! {}", e);
      throw new JobExecutionException("FAILED TO LAUNCH! {}", e);
    } finally {
      flightRecorder.summarizeFlight(flightSchedule, flightLog);
    }

    LOGGER.info("Executed {}", rocket.getClass().getName());
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    LOGGER.warn("INTERRUPT RUNNING JOB!");
  }

  public FlightLog getFlightLog() {
    return flightLog;
  }

  public void setFlightLog(FlightLog track) {
    this.flightLog = track;
  }

  public BasePersonIndexerJob getRocket() {
    return rocket;
  }

}
