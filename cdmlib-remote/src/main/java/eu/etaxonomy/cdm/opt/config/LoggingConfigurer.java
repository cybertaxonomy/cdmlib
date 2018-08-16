/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.opt.config;

import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import eu.etaxonomy.cdm.remote.config.AbstractWebApplicationConfigurer;

/**
 * @author a.kohlbecker
 * @since 20.07.2010
 *
 */
@Configuration
public class LoggingConfigurer extends AbstractWebApplicationConfigurer implements InitializingBean  {

    @Autowired
    private DataSourceProperties dataSourceProperties = null;

    /**
     *
     */
    private static final String ROLLING_FILE_APPENDER = "rollingFileAppender";

    /**
     * see also <code>eu.etaxonomy.cdm.server.instance.SharedAttributes</code>
     */
    private static final String CDM_LOGFILE = "cdm.logfile";

    private void configureLogFile() {
        PatternLayout layout = new PatternLayout("%d %p [%c] - %m%n");
        String logFile = findProperty(CDM_LOGFILE, false);
        if (logFile == null) {
            logger.info("No logfile specified, running without.");
            return;
        }
        try {
            RollingFileAppender appender = new RollingFileAppender(layout, logFile);
            appender.setName(ROLLING_FILE_APPENDER);
            appender.setMaxBackupIndex(3);
            appender.setMaxFileSize("2MB");
            Logger.getRootLogger().addAppender(appender);
            logger.info("logging to :" + logFile);
        } catch (IOException e) {
            logger.error("Creating RollingFileAppender failed:", e);
        }
    }

    /**
     *
     */
    private void configureInstanceNamePrefix() {
        String instanceName = dataSourceProperties.getCurrentDataSourceId();
        String patternPrefix = "[" + instanceName + "] ";

        @SuppressWarnings("unchecked")
        Enumeration<Appender> appenders = Logger.getRootLogger().getAllAppenders();
        while(appenders.hasMoreElements()){
            Appender appender = appenders.nextElement();
            if(appender != null){
                if(appender.getLayout() instanceof PatternLayout){
                    PatternLayout layout = (PatternLayout)appender.getLayout();
                    String conversionPattern = layout.getConversionPattern();
                    if(!conversionPattern.startsWith(patternPrefix)){
                        layout.setConversionPattern(patternPrefix + conversionPattern);
                    }
                }
                if(appender.getLayout() instanceof EnhancedPatternLayout){
                    EnhancedPatternLayout layout = (EnhancedPatternLayout)appender.getLayout();
                    String conversionPattern = layout.getConversionPattern();
                    if(!conversionPattern.startsWith(patternPrefix)){
                        layout.setConversionPattern(patternPrefix + conversionPattern);
                    }
                }
            }
        }

    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // per instance logfiles disabled, see #6249
        // configureLogFile();
        configureInstanceNamePrefix();
    }


}
