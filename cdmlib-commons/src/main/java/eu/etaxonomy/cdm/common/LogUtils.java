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
public class LogUtils {

    //copied from https://stackoverflow.com/questions/23434252/programmatically-change-log-level-in-log4j2/44678752#44678752
    //TODO: not yet tested
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

    public static void setLevel(Logger logger, Level level) {
        setLevel(logger.getName(), level);
    }

    public static void setLevel(Class<?> clazz, Level level) {
        setLevel(clazz.getCanonicalName(), level);
    }
}