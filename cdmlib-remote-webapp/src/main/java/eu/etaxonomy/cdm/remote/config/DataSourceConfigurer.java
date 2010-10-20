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
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jndi.JndiObjectFactoryBean;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
public class DataSourceConfigurer extends AbstractWebApplicationConfigurer {
	
	public static final Logger logger = Logger.getLogger(DataSourceConfigurer.class);

    private static final String ATTRIBUTE_JDBC_JNDI_NAME = "cdm.jdbcJndiName";
    private static final String ATTRIBUTE_HIBERNATE_DIALECT = "hibernate.dialect";
    private static final String CDM_BEAN_DEFINITION_FILE = "cdm.beanDefinitionFile";
    private static final String ATTRIBUTE_DATASOURCE_NAME = "cdm.datasource";

    private static final String DATASOURCE_BEANDEF_DEFAULT = System.getProperty("user.home")+File.separator+".cdmLibrary"+File.separator+"datasources.xml";

	private static String beanDefinitionFile = DATASOURCE_BEANDEF_DEFAULT;
	
	public void setBeanDefinitionFile(String filename){
		beanDefinitionFile = filename;
	}
	
	
	private DataSource dataSource;
	
	private Properties getHibernateProperties() {
		Properties hibernateProperties = webApplicationContext.getBean("jndiHibernateProperties", Properties.class);
		return hibernateProperties;
	}


	
	@Bean
	public DataSource dataSource() {
// TODO		
//		if(!CdmMetaData.isDbSchemaVersionCompatible(CdmMetaData.getDbSchemaVersion())){
//			logger.error("Schema version of the database ");
//		}
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
		
		String beanDefinitionFileFromProperty = findProperty(CDM_BEAN_DEFINITION_FILE, false);
		String path = (beanDefinitionFileFromProperty != null ? beanDefinitionFileFromProperty : beanDefinitionFile);
		logger.info("loading DataSourceBean '" + beanName + "' from: " + path);
		FileSystemResource file = new FileSystemResource(path);
		XmlBeanFactory beanFactory  = new XmlBeanFactory(file);
		DataSource dataSource = beanFactory.getBean(beanName, DataSource.class);
		if(dataSource instanceof ComboPooledDataSource){
			logger.info("DataSourceBean '" + beanName + "is a ComboPooledDataSource [URL:" + ((ComboPooledDataSource)dataSource).getJdbcUrl()+ "]");
		} else {
			logger.error("DataSourceBean '" + beanName + "IS NOT a ComboPooledDataSource");
		}
		return dataSource;
	}
	
	@Bean
	public Properties hibernateProperties(){
		Properties props = getHibernateProperties();
		props.setProperty(ATTRIBUTE_HIBERNATE_DIALECT, inferHibernateDialectName());
		return props;
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
