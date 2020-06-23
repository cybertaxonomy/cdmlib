/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.opt.config;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import eu.etaxonomy.cdm.remote.config.AbstractWebApplicationConfigurer;

@Configuration
public class LoggingConfigurer extends AbstractWebApplicationConfigurer implements InitializingBean  {


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

    @Override
    public void afterPropertiesSet() throws Exception {
        configureInstanceNamePrefix();
    }

}
