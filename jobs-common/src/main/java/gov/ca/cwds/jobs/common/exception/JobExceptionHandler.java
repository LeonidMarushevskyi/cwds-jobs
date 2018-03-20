package gov.ca.cwds.jobs.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Alexander Serbin on 2/15/2018.
 */

public class JobExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobExceptionHandler.class);

    private static AtomicBoolean exceptionHappened = new AtomicBoolean(false) ;

    public static void handleException(String message, Throwable e) {
       LOGGER.error(message, e);
       exceptionHappened.set(true);
    }

    public static void handleException(Throwable e) {
        handleException("Exception occured ", e);
    }

    public static boolean isExceptionHappened() {
        return exceptionHappened.get();
    }

    public static void reset() {
        exceptionHappened.set(false);
    }
}
