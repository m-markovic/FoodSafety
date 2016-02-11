package uk.ac.abdn.foodsafety.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author nhc
 *
 * Utilities for logging. Use pom.xml to control which logging framework to use.
 */
final class Logging {
    /** All application-specific logging will be associated with this class. */
    private static final Logger logger = LoggerFactory.getLogger(Logging.class);

    /**
     * Log msg on level warn
     * @param msg Message to log
     */
    static void warn(final String msg) {
      logger.warn(msg);
    }

    /**
     * Log msg on level debug
     * @param msg Message to log
     */
    static void debug(final String msg) {
        logger.debug(msg);
    }
}
