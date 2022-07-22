/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * After changing from log4j1 to log4j2 some methods do not exist anymore.
 * Adaptations for these methods are handled in this class.
 *
 * Maybe also not log4j upgrade functionality will be added to this class in future.
 *
 * @author a.mueller
 * @date 09.06.2022
 */
/**
 * @author a.mueller
 * @date 22.07.2022
 *
 */
public class LogUtils {

    static final String TRACE = "trace_";
    static final String DEBUG = "debug_";
    static final String INFO = "info_";
    static final String WARN = "warn_";

    /**
     * Sets the level of logger anew.
     *
     * NOTE: This call is expensive as it recreates the full logging configuration each time
     *       it is called.
     */
    //copied from https://stackoverflow.com/questions/23434252/programmatically-change-log-level-in-log4j2/44678752#44678752
    public static void setLevel(String loggerName, Level level) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
        LoggerConfig specificConfig = loggerConfig;

        // We need a specific configuration for this logger,
        // otherwise we would change the level of all other loggers
        // having the original configuration as parent as well

        if (!loggerConfig.getName().equals(loggerName)) {
            specificConfig = new LoggerConfig(loggerName, level, true);
            specificConfig.setParent(loggerConfig);
            config.addLogger(loggerName, specificConfig);
        }
        specificConfig.setLevel(level);
        ctx.updateLoggers();
    }

    /**
     * Sets the level of logger anew.
     *
     * NOTE: This call is expensive as it recreates the full logging configuration each time
     *       it is called.
     */
    public static void setLevel(Logger logger, Level level) {
        setLevel(logger.getName(), level);
    }

    /**
     * Sets the level of class's logger anew.
     *
     * NOTE: This call is expensive as it recreates the full logging configuration each time
     *       it is called.
     */
    public static void setLevel(Class<?> clazz, Level level) {
        setLevel(clazz.getCanonicalName(), level);
    }

    /**
     * Forces logging as trace and uses a trace enabled logger
     * by prepending a "trace."
     */
    public static void logAsTrace(Class<?> clazz, String message) {
        logAs(clazz.getName(), message, TRACE, Level.TRACE);
    }
    /**
     * Forces logging as trace and uses a trace enabled logger
     * by prepending a "trace."
     */
    public static void logAsTrace(Logger logger, String message) {
        logAs(logger.getName(), message, TRACE, Level.TRACE);
    }

    /**
     * Forces logging as debug and uses a debug enabled logger
     * by prepending a "debug."
     */
    public static void logAsDebug(Class<?> clazz, String message) {
        logAs(clazz.getName(), message, DEBUG, Level.DEBUG);
    }
    public static void logAsDebug(Logger logger, String message) {
        logAs(logger.getName(), message, DEBUG, Level.DEBUG);
    }

    /**
     * Forces logging as info and uses an info enabled logger
     * by prepending "info."
     */
    public static void logAsInfo(Class<?> clazz, String message) {
        logAs(clazz.getName(), message, INFO, Level.INFO);
    }
    /**
     * Forces logging as info and uses an info enabled logger
     * by prepending "info."
     */
    public static void logAsInfo(Logger logger, String message) {
        logAs(logger.getName(), message, INFO, Level.INFO);
    }

    /**
     * Forces logging as warn and uses a warn enabled logger
     * by prepending "warn."
     */
    public static void logAsWarn(Class<?> clazz, String message) {
        logAs(clazz.getName(), message, WARN, Level.WARN);
    }
    /**
     * Forces logging as warn and uses a warn enabled logger
     * by prepending "warn."
     */
    public static void logAsWarn(Logger logger, String message) {
        logAs(logger.getName(), message, WARN, Level.WARN);
    }

    private static void logAs(String loggerName, String message, String levelStr, Level level) {
        if (LogManager.getLogger(levelStr).getLevel().isMoreSpecificThan(level)) {
            //initialize logger with expected level if it is not yet correctly set
            setLevel(LogManager.getLogger(levelStr), level);
        }
        LogManager.getLogger(levelStr + "." + loggerName).log(level, message);
    }

    /**
     * Logs as trace with a logger added a ".trace" postfix
     */
    public static void logWithTrace(Logger logger, String message) {
        logWith(logger, message, TRACE, Level.TRACE);
    }

    private static void logWith(Logger logger, String message, String levelStr, Level level) {
        LogManager.getLogger( logger.getName() + "." + levelStr).log(level, message);
    }
}