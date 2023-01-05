/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.opt.config;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.H2CorrectedDialect;
import org.hibernate.dialect.MySQL5MyISAMUtf8Dialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jndi.JndiObjectFactoryBean;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import eu.etaxonomy.cdm.api.config.CdmConfigurationKeys;
import eu.etaxonomy.cdm.config.ConfigFileUtil;
import eu.etaxonomy.cdm.database.CdmDatabaseException;
import eu.etaxonomy.cdm.database.WrappedCdmDataSource;
import eu.etaxonomy.cdm.database.update.CdmUpdater;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;
import eu.etaxonomy.cdm.model.metadata.CdmMetaDataPropertyName;
import eu.etaxonomy.cdm.remote.config.AbstractWebApplicationConfigurer;

/**
 * The <code>DataSourceConfigurer</code> can be used as a replacement for a xml configuration in the application context.
 * <p>
 * The id of the loaded data source bean aka the <b>cdm instance name</b> is put into the <b>Spring environment</b> from
 * where it can be retrieved using the key {@link CDM_DATA_SOURCE_ID}.
 * <p>
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
 * @since 04.02.2011
 */
@Configuration
// cdmlib-remote.properties is used by developers to define the datasource bean to use from
// the datasources.xml. It is not relevant for production systems.
@PropertySource(value="file:${user.home}/.cdmLibrary/cdmlib-remote.properties", ignoreResourceNotFound=true)
public class DataSourceConfigurer extends AbstractWebApplicationConfigurer {

    private static final Logger logger = LogManager.getLogger();

    protected static final String HIBERNATE_DIALECT = "hibernate.dialect";
    protected static final String HIBERNATE_SEARCH_DEFAULT_INDEX_BASE = "hibernate.search.default.indexBase";
    protected static final String CDM_BEAN_DEFINITION_FILE = "cdm.beanDefinitionFile";

    @Autowired // Important!!!!
    private ConfigFileUtil configFileUtil;

    /**
     * Attribute to configure the name of the data source as set as bean name in the datasources.xml.
     * This name usually is used as the prefix for the webapplication root path.
     * <br>
     * <b>This is a required attribute!</b>
     *
     * @see AbstractWebApplicationConfigurer#findProperty(String, boolean)
     *
     * see also :
     *
     * <ul>
     * <li><code>eu.etaxonomy.cdm.server.instance.SharedAttributes</code></li>
     * <li>{@link CdmConfigurationKeys#CDM_DATA_SOURCE_ID}</li>
     * </ul>
     *
     */
    protected static final String ATTRIBUTE_DATASOURCE_NAME = "cdm.datasource";

    /**
     * see also <code>eu.etaxonomy.cdm.server.instance.SharedAttributes</code>
     */
    public static final String ATTRIBUTE_JDBC_JNDI_NAME = "cdm.jdbcJndiName";

    /**
     * Force a schema update when the cdmlib-remote-webapp instance is starting up
     * see also <code>eu.etaxonomy.cdm.server.instance.SharedAttributes.ATTRIBUTE_FORCE_SCHEMA_UPDATE</code>
     */
    public static final String ATTRIBUTE_FORCE_SCHEMA_UPDATE = "cdm.forceSchemaUpdate";

    /**
     * <b>WARNING!!!!!!!!!!!!!!!</b> Using this option will delete the existing data base followed by database creation.
     * <p>
     * Force a schema creation when the cdmlib-remote-webapp instance is starting up. Will set the hibernate property
     * {@code hibernate.hbm2ddl.auto} to {@code create}
     * <p>
     * This option will inactivate {@link #ATTRIBUTE_FORCE_SCHEMA_UPDATE} even if this is set.
     * <p>
     * This attribute has no equivalent in <code>eu.etaxonomy.cdm.server.instance.SharedAttributes</code> and never must
     * have any since this setting this attribute for any database listed for a server is far too dangerous. It must only
     * be possible to set this option per data base individually.
     */
    public static final String ATTRIBUTE_FORCE_SCHEMA_CREATE = "cdm.forceSchemaCreate";

    private static String beanDefinitionFile = null;


    /**
     * The file to load the {@link DataSource} beans from.
     * This file is usually {@code ${user.home}/.cdmLibrary/datasources.xml}
     * The variable <code>${user.home}</code>is determined by {@link ConfigFileUtil.getCdmHomeDir()}
     *
     *
     * @param filename
     */
    public void setBeanDefinitionFile(String filename){
        beanDefinitionFile = filename;
    }

    public String getBeanDefinitionFile(){
        if(beanDefinitionFile == null){
            beanDefinitionFile = configFileUtil.getCdmHomeDir().getPath() + File.separator + "datasources.xml";
        }
        return beanDefinitionFile;
    }

    private String dataSourceId = null;

    private DataSource dataSource;

    private DataSourceProperties dataSourceProperties;

    private Properties getHibernateProperties() {
        Properties hibernateProperties = webApplicationContext.getBean("jndiHibernateProperties", Properties.class);
        return hibernateProperties;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public DataSource dataSource() {

        String beanName = findProperty(ATTRIBUTE_DATASOURCE_NAME, true);
        String jndiName = null;
        if(this.dataSource == null){
            jndiName = findProperty(ATTRIBUTE_JDBC_JNDI_NAME, false);

            if(jndiName != null){
                try {
                    dataSource = useJndiDataSource(jndiName);
                    dataSourceId = FilenameUtils.getName(jndiName);
                } catch (NamingException e) {
                   throw new DataSourceException("JNDI data source (" + jndiName + ") not found. Jdbc URI correct? Does the database exist?", e);
                }
            } else {
                dataSource = loadDataSourceBean(beanName);
                dataSourceId = beanName;
            }
        }

        if(dataSource == null){
            return null;
        }

        MutablePropertySources sources = env.getPropertySources();
        Properties props = new Properties();
        props.setProperty(CdmConfigurationKeys.CDM_DATA_SOURCE_ID, dataSourceId);
        sources.addFirst(new PropertiesPropertySource("cdm-datasource",  props));

        if(!isForceSchemaCreate()) {
            // validate correct schema version
            Connection connection  = null;
            try {
                connection = dataSource.getConnection();
                String metadataTableName = "CdmMetaData";
                if(inferHibernateDialectName(dataSource).equals(H2CorrectedDialect.class.getName())){
                    metadataTableName = metadataTableName.toUpperCase();
                }
                ResultSet tables = connection.getMetaData().getTables(connection.getCatalog(), null, metadataTableName, null);
                if(tables.first()){
                    ResultSet resultSet;
                    try {
                        resultSet = connection.createStatement().executeQuery(CdmMetaDataPropertyName.DB_SCHEMA_VERSION.getSqlQuery());
                    } catch (Exception e) {
                        try {
                            resultSet = connection.createStatement().executeQuery(CdmMetaDataPropertyName.DB_SCHEMA_VERSION.getSqlQueryOld());
                        } catch (Exception e1) {
                            throw e1;
                        }
                    }
                    String version = null;
                    if(resultSet.next()){
                        version = resultSet.getString(1);
                    } else {
                        CdmDatabaseException cde = new CdmDatabaseException("Unable to retrieve version info from data source " + dataSource.toString()
                        + " -  the database may have been corrupted or is not a cdm database");
                        addErrorMessageToServletContextAttributes(cde.getMessage());
                        throw cde;
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
                } else {
                    CdmDatabaseException cde = new CdmDatabaseException("database " + dataSource.toString() + " is empty or not a cdm database");
                    logger.error(cde.getMessage());
                    // throw cde; // TODO: No exception was thrown here before. Is this correct behavior or
                }
            } catch (SQLException e) {
                CdmDatabaseException re = new CdmDatabaseException("Unable to connect or to retrieve version info from data source " + dataSource.toString() , e);
                addErrorMessageToServletContextAttributes(re.getMessage());
                throw re;
            } finally {
                if(connection != null){
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        // IGNORE //
                    }
                }
            } // END // validate correct schema version
        } // END !isForceSchemaCreate()


        String forceSchemaUpdate = findProperty(ATTRIBUTE_FORCE_SCHEMA_UPDATE, false);
        if(forceSchemaUpdate != null){
            if(!isForceSchemaCreate()) {
                logger.info("Update of data source requested by property '" + ATTRIBUTE_FORCE_SCHEMA_UPDATE + "'");
                CdmUpdater updater = CdmUpdater.NewInstance();
                WrappedCdmDataSource cdmDataSource = new WrappedCdmDataSource(dataSource);
                updater.updateToCurrentVersion(cdmDataSource, null);
                cdmDataSource.closeOpenConnections();
            } else {
                logger.info("Update of data source requested by property '" + ATTRIBUTE_FORCE_SCHEMA_UPDATE + "' but overwritten by " + ATTRIBUTE_FORCE_SCHEMA_CREATE);
            }
        }

        return dataSource;
    }

    @Bean
    public DataSourceProperties dataSourceProperties(){
        if(this.dataSourceProperties == null){
            dataSourceProperties = loadDataSourceProperties();
            if(dataSourceId == null){
                dataSource();
            }
            dataSourceProperties.setCurrentDataSourceId(dataSourceId);
        }
        return dataSourceProperties;
    }


    private DataSource useJndiDataSource(String jndiName) throws NamingException {
        logger.info("using jndi datasource '" + jndiName + "'");

        JndiObjectFactoryBean jndiFactory = new JndiObjectFactoryBean();
        /*
        JndiTemplate jndiTemplate = new JndiTemplate();
        jndiFactory.setJndiTemplate(jndiTemplate); no need to use a JndiTemplate
        if I try using JndiTemplate I get an org.hibernate.AnnotationException: "Unknown Id.generator: system-increment"
        when running multiple instances via the Bootloader
        */
        jndiFactory.setResourceRef(true);
        jndiFactory.setJndiName(jndiName);
        try {
            jndiFactory.afterPropertiesSet();
        } catch (IllegalArgumentException e) {
            logger.error(e, e);
        } catch (NamingException e) {
            logger.error(e, e);
            throw e;
        }
        Object obj = jndiFactory.getObject();
        return (DataSource)obj;
    }

    /**
     * Loads the {@link DataSource} bean from the cdm bean definition file.
     * This file is usually {@code ./.cdmLibrary/datasources.xml}
     *
     * @param beanName
     * @return
     */
    private DataSource loadDataSourceBean(String beanName) {

        String beanDefinitionFileFromProperty = findProperty(CDM_BEAN_DEFINITION_FILE, false);
        String path = (beanDefinitionFileFromProperty != null ? beanDefinitionFileFromProperty : getBeanDefinitionFile());
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


    /**
     * Loads the <code>dataSourceProperties</code> bean from the cdm bean
     * definition file.
     * This file is usually {@code ./.cdmLibrary/datasources.xml}
     *
     * @return the DataSourceProperties bean or an empty instance if the bean is not found
     */
    private DataSourceProperties loadDataSourceProperties() {

        String beanDefinitionFileFromProperty = findProperty(CDM_BEAN_DEFINITION_FILE, false);
        String path = (beanDefinitionFileFromProperty != null ? beanDefinitionFileFromProperty : getBeanDefinitionFile());
        logger.info("loading dataSourceProperties from: " + path);
        FileSystemResource file = new FileSystemResource(path);
        XmlBeanFactory beanFactory  = new XmlBeanFactory(file);
        DataSourceProperties properties = null;
        try {
            properties = beanFactory.getBean("dataSourceProperties", DataSourceProperties.class);
        } catch (BeansException e) {
            logger.warn("bean 'dataSourceProperties' not found");
            properties = new DataSourceProperties();
        }
        return properties;
    }

    @Bean
    public Properties hibernateProperties(){
        Properties props = getHibernateProperties();
        props.setProperty(HIBERNATE_DIALECT, inferHibernateDialectName());
        String searchPath = configFileUtil.getCdmHomeSubDir(ConfigFileUtil.SUBFOLDER_WEBAPP).getPath();
        props.setProperty(HIBERNATE_SEARCH_DEFAULT_INDEX_BASE,
                searchPath +
                "/index/".replace("/", File.separator) +
                findProperty(ATTRIBUTE_DATASOURCE_NAME, true));
        if(isForceSchemaCreate()) {
            props.setProperty("hibernate.hbm2ddl.auto", "create");
        }
        logger.debug("hibernateProperties: " + props.toString());
        return props;
    }

    private boolean isForceSchemaCreate() {
        String propVal = findProperty(ATTRIBUTE_FORCE_SCHEMA_CREATE, false);
        logger.debug("System property " + ATTRIBUTE_FORCE_SCHEMA_CREATE +  " = " + Objects.toString(propVal, "[NULL]"));
        return propVal != null && !(propVal.toLowerCase().equals("false") || propVal.equals("0"));
    }

    /**
     * Returns the full class name of the according {@link org.hibernate.dialect.Dialect} implementation
     *
     * @param ds the DataSource
     * @return the name
     */
    public String inferHibernateDialectName() {
        DataSource ds = dataSource();
        return inferHibernateDialectName(ds);
    }



    /**
     * Returns the full class name of the according {@link org.hibernate.dialect.Dialect} implementation
     *
     * @param ds the DataSource
     * @return the name
     */
    public String inferHibernateDialectName(DataSource ds) {
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

        if(url != null){
            if(url.contains(":mysql:")){
                // TODO we should switch all databases to InnoDB !
                // TODO open jdbc connection to check engine and choose between
                // MySQL5MyISAMUtf8Dialect and MySQL5MyISAMUtf8Dialect
                // see #3371 (switch cdm to MySQL InnoDB)
                return MySQL5MyISAMUtf8Dialect.class.getName();
            }
            if(url.contains(":h2:")){
                return H2CorrectedDialect.class.getName();
            }
            if(url.contains(":postgresql:")){
                return PostgreSQL82Dialect.class.getName();
            }
        }

        logger.error("hibernate dialect mapping for "+url+ " not yet implemented or unavailable");
        return null;
    }
}