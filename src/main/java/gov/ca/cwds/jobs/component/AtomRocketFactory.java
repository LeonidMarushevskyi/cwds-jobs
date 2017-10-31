package gov.ca.cwds.jobs.component;

import org.quartz.spi.JobFactory;

import gov.ca.cwds.jobs.BasePersonIndexerJob;
import gov.ca.cwds.jobs.exception.NeutronException;

public interface AtomRocketFactory extends JobFactory {

  /**
   * Build a registered job.
   * 
   * @param klass batch job class
   * @param args command line arguments
   * @return the job
   * @throws NeutronException unexpected runtime error
   */
  @SuppressWarnings("rawtypes")
  BasePersonIndexerJob createJob(final Class<?> klass, String... args) throws NeutronException;

  /**
   * Build a registered job.
   * 
   * @param jobName batch job class
   * @param args command line arguments
   * @return the job
   * @throws NeutronException unexpected runtime error
   */
  @SuppressWarnings("rawtypes")
  public BasePersonIndexerJob createJob(final String jobName, String... args)
      throws NeutronException;

}
