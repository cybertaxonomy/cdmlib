// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.config;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

/**
 * @author a.kohlbecker
 * @date 20.07.2010
 * 
 */
@Configuration
public class LoggingConfigurer extends AbstractWebApplicationConfigurer implements InitializingBean  {

	private static final String CDM_LOGFILE = "cdm.logfile";

	protected void configureLogFile() {
		PatternLayout layout = new PatternLayout("%d %p [%c] - %m%n");
		String logFile = findProperty(CDM_LOGFILE, false);
		try {
			RollingFileAppender appender = new RollingFileAppender(layout, logFile);
			appender.setMaxBackupIndex(3);
			appender.setMaxFileSize("250MB");
			Logger.getRootLogger().addAppender(appender);
			logger.info("logging to :" + logFile);
		} catch (IOException e) {
			logger.error("Creating RollingFileAppender failed:", e);
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		configureLogFile();
	}
	
	

}
