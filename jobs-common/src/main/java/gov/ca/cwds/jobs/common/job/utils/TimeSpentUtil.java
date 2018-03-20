package gov.ca.cwds.jobs.common.job.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Created by Alexander Serbin on 3/19/2018.
 */
public final class TimeSpentUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeSpentUtil.class);

    private TimeSpentUtil() {}

    public static void printTimeSpent(String workDescription, LocalDateTime startTime) {
        long hours = startTime.until(LocalDateTime.now(), ChronoUnit.HOURS);
        long minutes = startTime.until(LocalDateTime.now(), ChronoUnit.MINUTES);
        long seconds = startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS);
        long milliseconds = startTime.until(LocalDateTime.now(), ChronoUnit.MILLIS);
        LOGGER.info(workDescription + " time spent in milliseconds - {} ms", milliseconds);
        if (hours > 0) {
            LOGGER.info(workDescription + " time spent in hours - {} hr", hours);
        } else if (minutes > 0) {
            LOGGER.info(workDescription + " time spent in minutes - {} min", minutes);
        } else if (seconds > 0) {
            LOGGER.info(workDescription + " time spent in seconds - {} sec", seconds);
        }
    }

}
