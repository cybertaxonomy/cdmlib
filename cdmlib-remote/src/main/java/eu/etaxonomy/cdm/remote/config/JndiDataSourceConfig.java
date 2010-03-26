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

import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.web.context.WebApplicationContext;

@Configuration
public class JndiDataSourceConfig {
	
	public static final Logger logger = Logger.getLogger(JndiDataSourceConfig.class);

    private static final String ATTRIBUTE_JDBC_JNDI_NAME = "cdm.jdbcJndiName";
    private static final String ATTRIBUTE_HIBERNATE_DIALECT = "hibernate.dialect";
	
	private WebApplicationContext webApplicationContext;
	
	private Properties getHibernateProperties() {
		Properties hibernateProperties = webApplicationContext.getBean("jndiHibernateProperties", Properties.class);
		return hibernateProperties;
	}

	@Autowired
	public void setApplicationContext(ApplicationContext applicationContext){

		if(WebApplicationContext.class.isAssignableFrom(applicationContext.getClass())) {
			this.webApplicationContext = (WebApplicationContext)applicationContext;
		} else {
			logger.error("The DataSourceConfig class only can be used within a WebApplicationContext");
		}
	}
	
	@Bean
	public DataSource dataSource() {
		
		String jndiName = findProperty(ATTRIBUTE_JDBC_JNDI_NAME, true);
		
		logger.info("attaching to jndi datasource'" + jndiName);

		JndiObjectFactoryBean jndiFactory = new JndiObjectFactoryBean();
		/*
		JndiTemplate jndiTemplate = new JndiTemplate();
		jndiFactory.setJndiTemplate(jndiTemplate); no need to use a JndiTemplate 
		if I try using JndiTemplate I get an org.hibernate.AnnotationException: "Unknown Id.generator: system-increment" 
		when running multiple intances via the Bootloader
		*/
		jndiFactory.setResourceRef(true);
		jndiFactory.setJndiName(jndiName);
		try {
			jndiFactory.afterPropertiesSet();
		} catch (IllegalArgumentException e) {
			logger.error(e);
		} catch (NamingException e) {
			logger.error(e);
		}
		Object obj = jndiFactory.getObject();
		return (DataSource)obj;	
		
	}
	
	@Bean
	public Properties hibernateProperties(){
		Properties props = getHibernateProperties();
		String beanDefinitionFileFromProperty = findProperty(ATTRIBUTE_HIBERNATE_DIALECT, true);
		props.setProperty(ATTRIBUTE_HIBERNATE_DIALECT, beanDefinitionFileFromProperty);
		return props;
	}

	private String findProperty(String property, boolean required) {
		// 1. look for the dataSource beanName in the ServletContext
		Object obj = webApplicationContext.getServletContext().getAttribute(property);
		String value = (String)obj;
		// 2. look for the dataSource beanName in environment variables of the OS
		if(value == null){
			value = System.getProperty(property);
		}
		if(value == null && required){
			logger.error("property {" + property + "} not found.");
			logger.error("--> This property can be set in two ways:");
			logger.error("--> 		1. as attribute to the ServletContext");
			logger.error("--> 		2. as system property e.g. -D" + property);
			logger.error("Stopping application ...");
			System.exit(-1);
		}
		return value;
	}

}
