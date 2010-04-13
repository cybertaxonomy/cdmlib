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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.jndi.JndiTemplate;
import org.springframework.web.context.WebApplicationContext;

@Configuration
public class DataSourceConfig {
	
	public static final Logger logger = Logger.getLogger(DataSourceConfig.class);

    private static final String ATTRIBUTE_JDBC_JNDI_NAME = "cdm.jdbcJndiName";
    private static final String ATTRIBUTE_HIBERNATE_DIALECT = "hibernate.dialect";
    private static final String CDM_BEAN_DEFINITION_FILE = "cdm.beanDefinitionFile";
    private static final String ATTRIBUTE_DATASOURCE_NAME = "cdm.datasource";

    private static final String DATASOURCE_BEANDEF_DEFAULT = System.getProperty("user.home")+File.separator+".cdmLibrary"+File.separator+"datasources.xml";

	private static String beanDefinitionFile = DATASOURCE_BEANDEF_DEFAULT;
	
	public void setBeanDefinitionFile(String filename){
		beanDefinitionFile = filename;
	}
	
	private WebApplicationContext webApplicationContext;
	private DataSource dataSource;
	
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
		if(this.dataSource == null){
			String jndiName = findProperty(ATTRIBUTE_JDBC_JNDI_NAME, false);
			
			if(jndiName != null){
				dataSource = useJndiDataSource(jndiName);
			} else {
				String beanName = findProperty(ATTRIBUTE_DATASOURCE_NAME, true);
				dataSource = loadDataSourceBean(beanName);
			}			
		}
		return dataSource; 
	}

	private DataSource useJndiDataSource(String jndiName) {
		logger.info("using jndi datasource '" + jndiName + "'");

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
	
	private DataSource loadDataSourceBean(String beanName) {
		logger.info("using datasource '"+beanName);

		String beanDefinitionFileFromProperty = findProperty(CDM_BEAN_DEFINITION_FILE, false);
		String path = (beanDefinitionFileFromProperty != null ? beanDefinitionFileFromProperty : beanDefinitionFile);
		logger.info("loading DataSourceBean '" + beanName + "' from: " + path);
		FileSystemResource file = new FileSystemResource(path);
		XmlBeanFactory beanFactory  = new XmlBeanFactory(file);
	
		return beanFactory.getBean(beanName, DataSource.class);
	}
	
	@Bean
	public Properties hibernateProperties(){
		Properties props = getHibernateProperties();
		props.setProperty(ATTRIBUTE_HIBERNATE_DIALECT, inferHibernateDialectName());
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
	
	public String inferHibernateDialectName() {
		DataSource ds = dataSource();
		String url = "<SEE PRIOR REFLECTION ERROR>";
		Method m = null;
		try {
			m = ds.getClass().getMethod("getUrl");
		} catch (SecurityException e) {
			logger.error(e);
		} catch (NoSuchMethodException e) {
			try {
				m = ds.getClass().getMethod("getJdbcUrl");
			} catch (SecurityException e2) {
				logger.error(e2);
			} catch (NoSuchMethodException e2) {
				logger.error(e2);
			}
		}
		try {
			url = (String)m.invoke(ds);
		} catch (IllegalArgumentException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		} catch (InvocationTargetException e) {
			logger.error(e);
		} catch (SecurityException e) {
			logger.error(e);
		} 
		
		if(url != null && url.contains("mysql")){
			return "org.hibernate.dialect.MySQLDialect";
		}
		
		logger.error("hibernate dialect mapping for "+url+ " not jet implemented or unavailable");
		return null;
	}

}
