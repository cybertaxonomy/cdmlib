/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.opt.config;

//import org.apache.log4j.Appender;
//import org.apache.log4j.EnhancedPatternLayout;
//import org.apache.log4j.Logger;
//import org.apache.log4j.PatternLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import eu.etaxonomy.cdm.remote.config.AbstractWebApplicationConfigurer;

@Configuration
public class LoggingConfigurer extends AbstractWebApplicationConfigurer  {

    @Autowired
    private DataSourceProperties dataSourceProperties = null;

    /**
     * As we have changed the logging in the cdm-server (see https://dev.e-taxonomy.eu/redmine/issues/7085)
     * adding the instance name to the log is no longer required for server systems but it is
     * still a very nice feature for developers, so we keep this method.
     */
    private void configureInstanceNamePrefix() {
        String instanceName = dataSourceProperties.getCurrentDataSourceId();
        String patternPrefix = "[" + instanceName + "] ";


        //still not succesfully tried to update to log4j2. The problem is that the conversion
        //pattern can not be set explicitly in PatternLayout anymore
        //for updating configuration see also https://stackoverflow.com/questions/23434252/programmatically-change-log-level-in-log4j2/44678752#44678752
        //Also we may want to move the pattern/prefix setting to LogUtils (cdmlib-commons). This class here
        //we only need to read the patternPrefix from dataSourceProperties

//        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
//        org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
//        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
//        Map<String, Appender> apps = loggerConfig.getAppenders();
//
//        Map<String, Appender> appenders = ((org.apache.logging.log4j.core.Logger)LogManager.getRootLogger()).getAppenders();
//        for(Appender appender : appenders.values()) {
//            if(appender != null){
//                if(appender.getLayout() instanceof PatternLayout && appender instanceof ConsoleAppender){
//                    PatternLayout layout = (PatternLayout)appender.getLayout();
//                    ConsoleAppender consoleAppender = (ConsoleAppender)appender;
//                    PatternParser parser = PatternLayout.createPatternParser(config);
//                    parser.
//                    consoleAppender = ConsoleAppender.newBuilder().asBuilder()
//                            .setLayout(appender.getLayout())
//                            .setName(appender.getName())
//                            .build();
//                    String conversionPattern = layout.getConversionPattern();
//                    if(!conversionPattern.startsWith(patternPrefix)){
//                        ((ConsoleAppender)appender).setLayout(new PatternLayout(patternPrefix + conversionPattern));
//                        layout.setConversionPattern(patternPrefix + conversionPattern);
//                    }
//                }
        // ... ctx.updateLoggers();

//        @SuppressWarnings("unchecked")
//        Enumeration<Appender> appenders = Logger.getRootLogger().getAllAppenders();
//        while(appenders.hasMoreElements()){
//            Appender appender = appenders.nextElement();
//            if(appender != null){
//                if(appender.getLayout() instanceof PatternLayout){
//                    PatternLayout layout = (PatternLayout)appender.getLayout();
//                    String conversionPattern = layout.getConversionPattern();
//                    if(!conversionPattern.startsWith(patternPrefix)){
//                        layout.setConversionPattern(patternPrefix + conversionPattern);
//                    }
//                }
//                if(appender.getLayout() instanceof EnhancedPatternLayout){
//                    EnhancedPatternLayout layout = (EnhancedPatternLayout)appender.getLayout();
//                    String conversionPattern = layout.getConversionPattern();
//                    if(!conversionPattern.startsWith(patternPrefix)){
//                        layout.setConversionPattern(patternPrefix + conversionPattern);
//                    }
//                }
//            }
//        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        configureInstanceNamePrefix();
    }

}
