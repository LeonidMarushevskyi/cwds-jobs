package gov.ca.cwds.jobs.schedule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import gov.ca.cwds.jobs.BasePersonIndexerJob;
import gov.ca.cwds.jobs.config.FlightPlan;
import gov.ca.cwds.jobs.exception.NeutronException;
import gov.ca.cwds.neutron.atom.AtomFlightPlanManager;
import gov.ca.cwds.neutron.atom.AtomLaunchPad;
import gov.ca.cwds.neutron.atom.AtomLaunchScheduler;
import gov.ca.cwds.neutron.atom.AtomRocketFactory;
import gov.ca.cwds.neutron.flight.FlightLog;
import gov.ca.cwds.neutron.jetpack.JobLogs;
import gov.ca.cwds.neutron.launch.LaunchPad;

@Singleton
public class LaunchScheduler implements AtomLaunchScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(LaunchScheduler.class);

  private Scheduler scheduler;

  private final FlightRecorder flightRecorder;

  private final AtomRocketFactory rocketFactory;

  private final AtomFlightPlanManager flightPlanManger;

  private FlightPlan flightPlan;

  /**
   * Scheduled jobs.
   */
  private final Map<Class<?>, AtomLaunchPad> scheduleRegistry = new ConcurrentHashMap<>();

  /**
   * Possibly not necessary. Listeners and running jobs should handle this, but we still need a
   * single place to track rockets in flight.
   * 
   * <p>
   * OPTION: Quartz scheduler can track this too. Obsolete implementation?
   * </p>
   */
  private final Map<TriggerKey, NeutronRocket> rocketsInFlight = new ConcurrentHashMap<>();

  @Inject
  public LaunchScheduler(final FlightRecorder flightRecorder, final AtomRocketFactory rocketFactory,
      final AtomFlightPlanManager flightPlanMgr) {
    this.flightRecorder = flightRecorder;
    this.rocketFactory = rocketFactory;
    this.flightPlanManger = flightPlanMgr;
  }

  /**
   * Build a registered job.
   * 
   * @param klass batch job class
   * @param flightPlan command line arguments
   * @return the job
   * @throws NeutronException unexpected runtime error
   */
  @SuppressWarnings("rawtypes")
  public BasePersonIndexerJob fuelRocket(final Class<?> klass, final FlightPlan flightPlan)
      throws NeutronException {
    return this.rocketFactory.createJob(klass, flightPlan);
  }

  /**
   * Create a registered rocket.
   * 
   * @param jobName batch job class
   * @param flightPlan command line arguments
   * @return the job
   * @throws NeutronException unexpected runtime error
   */
  @SuppressWarnings("rawtypes")
  public BasePersonIndexerJob fuelRocket(final String jobName, final FlightPlan flightPlan)
      throws NeutronException {
    return this.rocketFactory.createJob(jobName, flightPlan);
  }

  @Override
  public FlightLog launchScheduledFlight(Class<?> klass, FlightPlan flightPlan)
      throws NeutronException {
    try {
      LOGGER.info("Run scheduled rocket: {}", klass.getName());
      final BasePersonIndexerJob<?, ?> rocket = fuelRocket(klass, flightPlan);
      rocket.run();
      return rocket.getFlightLog();
    } catch (Exception e) {
      throw JobLogs.checked(LOGGER, e, "SCHEDULED LAUNCH FAILED!: {}", e.getMessage());
    }
  }

  @Override
  public FlightLog launchScheduledFlight(String jobName, FlightPlan flightPlan)
      throws NeutronException {
    try {
      final Class<?> klass = Class.forName(jobName);
      return launchScheduledFlight(klass, flightPlan);
    } catch (ClassNotFoundException e) {
      throw JobLogs.checked(LOGGER, e, "SCHEDULED LAUNCH FAILED!: {}", e.getMessage());
    }
  }

  @Override
  public LaunchPad scheduleLaunch(Class<?> klazz, StandardFlightSchedule sched,
      FlightPlan flightPlan) {
    LOGGER.debug("LAUNCH COORDINATOR: LAST CHANGE LOCATION: {}", flightPlan.getLastRunLoc());
    final LaunchPad nj = new LaunchPad(this, sched, flightRecorder, flightPlan);
    flightPlanManger.addFlightPlan(klazz, flightPlan);
    scheduleRegistry.put(klazz, nj);
    return nj;
  }

  @Override
  public void stopScheduler(boolean waitForJobsToComplete) throws NeutronException {
    LOGGER.warn("STOP SCHEDULER! wait for jobs to complete: {}", waitForJobsToComplete);
    try {
      this.getScheduler().shutdown(waitForJobsToComplete);
    } catch (SchedulerException e) {
      throw JobLogs.checked(LOGGER, e, "FAILED TO STOP SCHEDULER! {}", e.getMessage());
    }
  }

  @Override
  public void startScheduler() throws NeutronException {
    LOGGER.warn("START SCHEDULER!");
    try {
      this.getScheduler().start();
    } catch (SchedulerException e) {
      LOGGER.error("FAILED TO START SCHEDULER! {}", e.getMessage(), e);
      throw JobLogs.checked(LOGGER, e, "FAILED TO START SCHEDULER! {}", e.getMessage());
    }
  }

  @Override
  public void trackInFlightRocket(final TriggerKey key, NeutronRocket job) {
    rocketsInFlight.put(key, job);
  }

  public void removeExecutingJob(final TriggerKey key) {
    if (rocketsInFlight.containsKey(key)) {
      rocketsInFlight.remove(key);
    }
  }

  public Map<TriggerKey, NeutronRocket> getRocketsInFlight() {
    return rocketsInFlight;
  }

  public AtomRocketFactory getRocketFactory() {
    return rocketFactory;
  }

  public FlightPlan getFlightPlan() {
    return flightPlan;
  }

  public void setFlightPlan(FlightPlan opts) {
    this.flightPlan = opts;
  }

  @Override
  public Scheduler getScheduler() {
    return scheduler;
  }

  public void setScheduler(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Override
  public Map<Class<?>, AtomLaunchPad> getScheduleRegistry() {
    return scheduleRegistry;
  }

  @Override
  public boolean isLaunchVetoed(String className) throws NeutronException {
    Class<?> klazz = null;
    try {
      klazz = Class.forName(className);
    } catch (Exception e) {
      throw JobLogs.checked(LOGGER, e, "UNKNOWN JOB CLASS! {}", className, e);
    }
    return this.getScheduleRegistry().get(klazz).isVetoExecution();
  }

  @Override
  public AtomFlightPlanManager getFlightPlanManger() {
    return flightPlanManger;
  }

}
