package gov.ca.cwds.jobs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.jobs.component.AtomJobControl;
import gov.ca.cwds.jobs.component.AtomShared;
import gov.ca.cwds.jobs.component.Job;
import gov.ca.cwds.jobs.component.JobProgressTrack;
import gov.ca.cwds.jobs.config.JobOptions;
import gov.ca.cwds.jobs.defaults.NeutronDateTimeFormat;
import gov.ca.cwds.jobs.defaults.NeutronIntegerDefaults;
import gov.ca.cwds.jobs.exception.NeutronException;
import gov.ca.cwds.jobs.schedule.JobRunner;
import gov.ca.cwds.jobs.util.JobLogs;

/**
 * Abstract base class for all batch jobs based on last successful run time.
 * 
 * @author CWDS API Team
 */
public abstract class LastSuccessfulRunJob implements Job, AtomShared, AtomJobControl {

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = LoggerFactory.getLogger(LastSuccessfulRunJob.class);

  /**
   * Command line options for this job.
   */
  protected JobOptions opts;

  private String lastRunTimeFilename;

  /**
   * Default constructor.
   * 
   * @param lastJobRunTimeFilename location of last run time file
   */
  public LastSuccessfulRunJob(String lastJobRunTimeFilename) {
    this.lastRunTimeFilename = lastJobRunTimeFilename;
  }

  @Override
  public final void run() {
    final JobProgressTrack track = getTrack();
    track.start();

    final Date lastRunTime = determineLastSuccessfulRunTime();
    track.setLastChangeSince(lastRunTime);

    try {
      final Date curentTimeRunTime = executeJob(lastRunTime);
      if (!isFailed()) {
        writeLastSuccessfulRunTime(curentTimeRunTime);
      }
    } catch (Exception e) {
      fail();
      LOGGER.error("FAIL JOB!", e);
    }

    finish(); // Close resources, notify listeners, or even close JVM in standalone mode.
    track.done();

    // SLF4J does not yet support conditional invocation.
    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn(track.toString());
    }

    JobRunner.getInstance().addTrack(getClass(), track);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void done() {
    this.getTrack().done();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void fail() {
    this.getTrack().fail();
    this.getTrack().done();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doneRetrieve() {
    getTrack().doneRetrieve();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doneTransform() {
    getTrack().doneTransform();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doneIndex() {
    getTrack().doneIndex();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRunning() {
    return getTrack().isRunning();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isFailed() {
    return getTrack().isFailed();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isRetrieveDone() {
    return getTrack().isRetrieveDone();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isTransformDone() {
    return getTrack().isTransformDone();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isIndexDone() {
    return getTrack().isIndexDone();
  }

  /**
   * If last run time is provide in options then use it, otherwise use provided
   * lastSuccessfulRunTime.
   * 
   * <p>
   * NOTE: make the look-back period configurable.
   * </p>
   * 
   * @param lastSuccessfulRunTime last successful run
   * @param opts command line job options
   * @return appropriate date to detect changes
   */
  protected Date calcLastRunDate(final Date lastSuccessfulRunTime, final JobOptions opts) {
    Date ret;
    final Date lastSuccessfulRunTimeOverride = opts.getLastRunTime();

    if (lastSuccessfulRunTimeOverride != null) {
      ret = lastSuccessfulRunTimeOverride;
    } else {
      final Calendar cal = Calendar.getInstance();
      cal.setTime(lastSuccessfulRunTime);
      cal.add(Calendar.MINUTE, NeutronIntegerDefaults.LOOKBACK_MINUTES.getValue());
      ret = cal.getTime();
    }

    return ret;
  }

  /**
   * Calculate last successful run date/time, per
   * {@link LastSuccessfulRunJob#calcLastRunDate(Date, JobOptions)}.
   * 
   * @param lastSuccessfulRunTime last successful run
   * @return appropriate date to detect changes
   */
  protected Date calcLastRunDate(final Date lastSuccessfulRunTime) {
    return calcLastRunDate(lastSuccessfulRunTime, getOpts());
  }

  /**
   * Reads the last run file and returns the last run date.
   * 
   * @return last successful run date/time as a Java Date.
   */
  protected Date determineLastSuccessfulRunTime() {
    Date ret = null;

    if (!StringUtils.isBlank(this.lastRunTimeFilename)) {
      try (BufferedReader br = new BufferedReader(new FileReader(lastRunTimeFilename))) { // NOSONAR
        ret = new SimpleDateFormat(NeutronDateTimeFormat.LAST_RUN_DATE_FORMAT.getFormat())
            .parse(br.readLine().trim()); // NOSONAR
      } catch (IOException e) {
        fail();
        JobLogs.raiseError(LOGGER, e, "Caught IOException: {}", e.getMessage());
      } catch (ParseException e) {
        fail();
        JobLogs.raiseError(LOGGER, e, "Caught ParseException: {}", e.getMessage());
      }
    }

    return ret;
  }

  /**
   * Write the time stamp IF the job succeeded.
   * 
   * @param datetime date and time to store
   */
  protected void writeLastSuccessfulRunTime(Date datetime) {
    if (datetime != null && !StringUtils.isBlank(this.lastRunTimeFilename) && !isFailed()) {
      try (BufferedWriter w = new BufferedWriter(new FileWriter(lastRunTimeFilename))) { // NOSONAR
        w.write(new SimpleDateFormat(NeutronDateTimeFormat.LAST_RUN_DATE_FORMAT.getFormat())
            .format(datetime));
      } catch (IOException e) {
        fail();
        JobLogs.raiseError(LOGGER, e, "Failed to write timestamp file: {}", e.getMessage());
      }
    }
  }

  /**
   * Execute the batch job. Child classes must provide an implementation.
   * 
   * @param lastSuccessfulRunTime The last successful run
   * @return The time of the latest run if successful.
   * @throws NeutronException if job fails
   */
  public abstract Date executeJob(Date lastSuccessfulRunTime) throws NeutronException;

  /**
   * Marks the job as completed. Close resources, notify listeners, or even close JVM.
   */
  protected abstract void finish();

  /**
   * Getter for last job run time.
   * 
   * @return last time the job ran successfully, in format
   *         {@link NeutronDateTimeFormat#LAST_RUN_DATE_FORMAT}
   */
  public String getLastJobRunTimeFilename() {
    return lastRunTimeFilename;
  }

  /**
   * Getter for this job's options.
   * 
   * @return this job's options
   */
  @Override
  public JobOptions getOpts() {
    return opts;
  }

  /**
   * Setter for this job's options.
   * 
   * @param opts this job's options
   */
  public void setOpts(JobOptions opts) {
    this.opts = opts;
  }

  @Override
  public Logger getLogger() {
    return LOGGER;
  }

}
