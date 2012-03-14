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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmMetaData;
import eu.etaxonomy.cdm.model.common.CdmMetaData.MetaDataPropertyName;

/**
 * The <code>DataSourceConfigurer</code> can be used as a replacement for a xml configuration in the application context.
 * Enter the following in your application context configuration in order to enable the <code>DataSourceConfigurer</code>:
 *
<pre>
&lt;!-- enable processing of annotations such as @Autowired and @Configuration --&gt;
&lt;context:annotation-config/&gt;

&lt;bean class="eu.etaxonomy.cdm.remote.config.DataSourceConfigurer" &gt;
&lt;/bean&gt;
</pre>
 * The <code>DataSourceConfigurer</code> allows alternative ways to specify a data source:
 *
 * <ol>
 * <li>Specify the data source bean to use in the Java environment properties:
 * <code>-Dcdm.datasource={dataSourceName}</code> ({@link #ATTRIBUTE_DATASOURCE_NAME}).
 * The data source bean with the given name will then be loaded from the <code>cdm.beanDefinitionFile</code>
 * ({@link #CDM_BEAN_DEFINITION_FILE}), which must be a valid Spring bean definition file.
 * </li>
 * <li>
 * Use a JDBC data source which is bound into the JNDI context. In this case the JNDI name is specified
 * via the {@link #ATTRIBUTE_JDBC_JNDI_NAME} as attribute to the ServletContext.
 * This scenario usually being used by the cdm-server application.
 * </li>
 * </ol>
 * The attributes used in (1) and (2) are in a first step being searched in the ServletContext
 * if not found search in a second step in the environment variables of the OS, see:{@link #findProperty(String, boolean)}.
 *
 * @author a.kohlbecker
 * @date 04.02.2011
 *
 */
@Configuration
public class DataSourceConfigurer extends AbstractWebApplicationConfigurer {

    public static final Logger logger = Logger.getLogger(DataSourceConfigurer.class);

    private static final String ATTRIBUTE_JDBC_JNDI_NAME = "cdm.jdbcJndiName";
    private static final String HIBERNATE_DIALECT = "hibernate.dialect";
    private static final String HIBERNATE_SEARCH_DEFAULT_INDEX_BASE = "hibernate.search.default.indexBase";
    private static final String CDM_BEAN_DEFINITION_FILE = "cdm.beanDefinitionFile";
    private static final String ATTRIBUTE_DATASOURCE_NAME = "cdm.datasource";

    private static final String DATASOURCE_BEANDEF_DEFAULT = CdmUtils.getCdmHomeDir().getPath() + File.separator + "datasources.xml";

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

        String beanName = findProperty(ATTRIBUTE_DATASOURCE_NAME, true);
        String jndiName = null;
        if(this.dataSource == null){
            jndiName = findProperty(ATTRIBUTE_JDBC_JNDI_NAME, false);

            if(jndiName != null){
                dataSource = useJndiDataSource(jndiName);
            } else {
                dataSource = loadDataSourceBean(beanName);
            }
        }

        if(dataSource == null){
            return null;
        }

        // validate correct schema version
        try {

            Connection connection = dataSource.getConnection();

            ResultSet resultSet = connection.createStatement().executeQuery(MetaDataPropertyName.DB_SCHEMA_VERSION.getSqlQuery());
            String version = null;
            if(resultSet.next()){
                version = resultSet.getString(1);
            } else {
                throw new RuntimeException("Unable to retrieve version info from data source " + dataSource.toString());
            }

            connection.close();

            if(!CdmMetaData.isDbSchemaVersionCompatible(version)){
                /*
                 * any exception thrown here would be nested into a spring
                 * BeanException which can not be caught in the servlet
                 * container, so we post the information into the
                 * ServletContext
                 */
                String errorMessage = "Incompatible version [" + (beanName != null ? beanName : jndiName) + "] expected version: " + CdmMetaData.getDbSchemaVersion() + ",  data base version  " + version;
                addErrorMessageToServletContextAttributes(errorMessage);
            }


        } catch (SQLException e) {
            RuntimeException re =   new RuntimeException("Unable to connect or to retrieve version info from data source " + dataSource.toString() , e);
            addErrorMessageToServletContextAttributes(re.getMessage());
            throw re;

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
            logger.info("DataSourceBean '" + beanName + "' is a ComboPooledDataSource [URL:" + ((ComboPooledDataSource)dataSource).getJdbcUrl()+ "]");
        } else {
            logger.error("DataSourceBean '" + beanName + "' IS NOT a ComboPooledDataSource");
        }
        return dataSource;
    }

    @Bean
    public Properties hibernateProperties(){
        Properties props = getHibernateProperties();
        props.setProperty(HIBERNATE_DIALECT, inferHibernateDialectName());
        props.setProperty(HIBERNATE_SEARCH_DEFAULT_INDEX_BASE, CdmUtils.getCdmHomeDir().getPath() + "/remote-webapp/index/".replace("/", File.separator) + findProperty(ATTRIBUTE_DATASOURCE_NAME, true));
        logger.debug("hibernateProperties: " + props.toString());
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
