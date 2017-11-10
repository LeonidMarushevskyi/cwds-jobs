package gov.ca.cwds.jobs.util.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.ca.cwds.jobs.config.FlightPlan;
import gov.ca.cwds.neutron.enums.NeutronIntegerDefaults;

public final class NeutronThreadUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeutronThreadUtil.class);

  private NeutronThreadUtil() {
    // static methods only
  }

  /**
   * Calculate the number of reader threads to run from incoming job options and available
   * processors.
   * 
   * @param opts job options
   * @return number of reader threads to run
   */
  public static int calcReaderThreads(final FlightPlan opts) {
    final int ret = opts.getThreadCount() != 0L ? (int) opts.getThreadCount()
        : Math.max(Runtime.getRuntime().availableProcessors() - 4, 4);
    LOGGER.info(">>>>>>>> # OF READER THREADS: {} <<<<<<<<", ret);
    return ret;
  }

  /**
   * Set the name of the current thread. Simplifies logging when a rocket runs multiple threads or
   * when multiple rockets are in flight.
   * 
   * @param title title of thread
   * @param obj calling object
   */
  public static void nameThread(final String title, final Object obj) {
    Thread.currentThread().setName(obj.getClass().getSimpleName() + "_" + title);
  }

  public static void catchYourBreath() {
    try {
      Thread.sleep(NeutronIntegerDefaults.SLEEP_MILLIS.getValue()); // NOSONAR
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

}