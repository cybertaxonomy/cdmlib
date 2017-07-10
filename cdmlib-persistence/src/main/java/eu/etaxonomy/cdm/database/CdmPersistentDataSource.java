/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import static eu.etaxonomy.cdm.common.XmlHelp.getBeansRoot;
import static eu.etaxonomy.cdm.common.XmlHelp.insertXmlBean;
import static eu.etaxonomy.cdm.common.XmlHelp.insertXmlValueProperty;
import static eu.etaxonomy.cdm.common.XmlHelp.saveToXml;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.hibernate.cache.internal.NoCachingRegionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.jdom.Attribute;
import org.jdom.Element;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.config.CdmPersistentSourceUtils;
import eu.etaxonomy.cdm.config.CdmPersistentXMLSource;
import eu.etaxonomy.cdm.config.CdmPersistentXMLSource.CdmSourceProperties;
import eu.etaxonomy.cdm.config.ICdmPersistentSource;
import eu.etaxonomy.cdm.database.types.IDatabaseType;


/**
 * class to access an CdmDataSource
 */
public class CdmPersistentDataSource extends CdmDataSourceBase implements ICdmPersistentSource {
	private static final Logger logger = Logger.getLogger(CdmPersistentDataSource.class);

	public static final String DATASOURCE_BEAN_POSTFIX = "DataSource";


	private String beanName;

	private String database;

	/**
	 * This is strictly a <String, String> list of properties
	 */
	private Properties cdmSourceProperties;

	private List<Attribute> cdmSourceAttributes;


	/**
	 * The Datasource class that Spring will use to set up the connection to the database
	 */
	private static String dataSourceClassName = ComboPooledDataSource.class.getName();


	/**
	 * Returns the default CdmDataSource
	 * @return the default CdmDataSource
	 * @throws DataSourceNotFoundException
	 */
	public final static CdmPersistentDataSource NewDefaultInstance() throws DataSourceNotFoundException {
		return NewInstance("default");
	}


	/**
	 * Returns the default CdmDataSource
	 * @return the default CdmDataSource
	 * @throws DataSourceNotFoundException
	 */
	public final static CdmPersistentDataSource NewLocalHsqlInstance() throws DataSourceNotFoundException{
		return NewInstance("localDefaultHsql");
	}

	/**
	 * Returns the CdmDataSource named by strDataSource
	 * @param strDataSource
	 * @return
	 */
	public final static CdmPersistentDataSource NewInstance(String dataSourceName) throws DataSourceNotFoundException{
		if (exists(dataSourceName)){
			return new CdmPersistentDataSource(dataSourceName);
		}else{
			throw new DataSourceNotFoundException("Datasource not found: " + dataSourceName);
		}
	}

	/**
	 * Private Constructor. Use NewXXX factory methods for creating a new instance of CdmDataSource!
	 * @param strDataSource
	 */
	private CdmPersistentDataSource(String strDataSource){
		setName(strDataSource);
		loadSource(strDataSource);
	}

	private void loadSource(String strDataSource) {
		CdmPersistentXMLSource cdmPersistentXMLSource = CdmPersistentXMLSource.NewInstance(strDataSource, DATASOURCE_BEAN_POSTFIX);
		if(cdmPersistentXMLSource.getElement() != null) {
			beanName = cdmPersistentXMLSource.getBeanName();
			// properties from the persistent xml file
			cdmSourceProperties = cdmPersistentXMLSource.getCdmSourceProperties();
			cdmSourceAttributes = cdmPersistentXMLSource.getCdmSourceAttributes();

			// added database specific properties if they are null
			String url = getCdmSourceProperty(CdmSourceProperties.URL);
			DatabaseTypeEnum dbTypeEnum = getDatabaseType();
			if (dbTypeEnum != null && url != null){
				IDatabaseType dbType = dbTypeEnum.getDatabaseType();
				if (getCdmSourceProperty(CdmSourceProperties.DATABASE) == null){
					String database = dbType.getDatabaseNameByConnectionString(url);
					if(database != null) {
						setDatabase(database);
					}
				}
				if(getCdmSourceProperty(CdmSourceProperties.SERVER) == null){
					String server = dbType.getServerNameByConnectionString(url);
					if(server != null) {
						setServer(server);
					}
				}
				if(getCdmSourceProperty(CdmSourceProperties.PORT) == null){
					int port = dbType.getPortByConnectionString(url);
						if(port > 0) {
							setPort(port);
						} else {
							setPort(NULL_PORT);
						}
				}
			}
		}
	}

	@Override
    public String getBeanName() {
		return beanName;
	}

	@Override
	public String getDatabase() {
		return database;
	}


	@Override
	public void setDatabase(String database) {
		this.database = database;
		//update url string
		cdmSourceProperties.put(CdmSourceProperties.URL.toString(), getDatabaseType().getConnectionString(this));

	}

	@Override
	public void setServer(String server) {
		super.setServer(server);
		//update url string
		cdmSourceProperties.put(CdmSourceProperties.URL.toString(), getDatabaseType().getConnectionString(this));
	}

	@Override
	public void setPort(int port) {
		super.setPort(port);
		if(port != NULL_PORT) {
			//update url string
			cdmSourceProperties.put(CdmSourceProperties.URL.toString(), getDatabaseType().getConnectionString(this));
		}
	}
	@Override
	public String getFilePath() {
		return getCdmSourceProperty(CdmSourceProperties.FILEPATH);
	}


	@Override
	public H2Mode getMode() {
		return H2Mode.fromString(getCdmSourceProperty(CdmSourceProperties.MODE));
	}

	@Override
	public void setMode(H2Mode h2Mode) {
		cdmSourceProperties.put(CdmSourceProperties.MODE.toString(), h2Mode.name());

	}

	@Override
	public String getUsername(){
		return getCdmSourceProperty(CdmSourceProperties.USERNAME);
	}

	@Override
	public void setUsername(String username) {
		cdmSourceProperties.put(CdmSourceProperties.USERNAME.toString(), username);

	}

	@Override
	public String getPassword(){
		return getCdmSourceProperty(CdmSourceProperties.PASSWORD);
	}

	@Override
	public void setPassword(String password) {
		cdmSourceProperties.put(CdmSourceProperties.PASSWORD.toString(), password);

	}

	@Override
	public DatabaseTypeEnum getDatabaseType(){
		String strDriverClass = getCdmSourceProperty(CdmSourceProperties.DRIVER_CLASS);
		DatabaseTypeEnum dbType = DatabaseTypeEnum.byDriverClass(strDriverClass);
		return dbType;
	}


	public String getCdmSourceProperty(CdmSourceProperties property){
		return cdmSourceProperties.getProperty(property.toString(),null);
	}

	/**
	 * Returns a BeanDefinition object of type DataSource that contains
	 * datsource properties (url, username, password, ...)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public BeanDefinition getDatasourceBean(){
		DatabaseTypeEnum dbtype =
				DatabaseTypeEnum.byDriverClass(getCdmSourceProperty(CdmSourceProperties.DRIVER_CLASS));

		AbstractBeanDefinition bd = new RootBeanDefinition(dbtype.getDataSourceClass());
		//attributes
		Iterator<Attribute> iterator = cdmSourceAttributes.iterator();
		while(iterator.hasNext()){
			Attribute attribute = iterator.next();
			if (attribute.getName().equals("lazy-init")){
				bd.setLazyInit(Boolean.valueOf(attribute.getValue()));
			}
			if (attribute.getName().equals("init-method")){
				bd.setInitMethodName(attribute.getValue());
			}
			if (attribute.getName().equals("destroy-method")){
				bd.setDestroyMethodName(attribute.getValue());
			}
			//Attribute attribute = iterator.next();
			//bd.setAttribute(attribute.getName(), attribute.getValue());
		}

		//properties
		MutablePropertyValues props = new MutablePropertyValues();

		Enumeration<String> keys = (Enumeration)cdmSourceProperties.keys();
		while (keys.hasMoreElements()){
			String key = keys.nextElement();

			props.addPropertyValue(key, cdmSourceProperties.getProperty(key));
		}

		bd.setPropertyValues(props);
		return bd;
	}

	/**
	 * @param hbm2dll
	 * @param showSql
	 * @return
	 */
	@Override
	public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll){
		boolean showSql = false;
		boolean formatSql = false;
		boolean registerSearchListener = false;
		Class<? extends RegionFactory> cacheProviderClass = NoCachingRegionFactory.class;
		return getHibernatePropertiesBean(hbm2dll, showSql, formatSql, registerSearchListener, cacheProviderClass);
	}


	/**
	 * @param hbm2dll
	 * @param showSql
	 * @return
	 */
	@Override
	public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll, Boolean showSql, Boolean formatSql, Boolean registerSearchListener, Class<? extends RegionFactory> cacheProviderClass){
		//Hibernate default values
		if (hbm2dll == null){
			hbm2dll = DbSchemaValidation.VALIDATE;
		}
		if (showSql == null){
			showSql = false;
		}
		if (formatSql == null){
			formatSql = false;
		}
		if (cacheProviderClass == null){
			cacheProviderClass = NoCachingRegionFactory.class;
		}
		if(registerSearchListener == null){
			registerSearchListener = false;
		}

		DatabaseTypeEnum dbtype = getDatabaseType();
		AbstractBeanDefinition bd = new RootBeanDefinition(PropertiesFactoryBean.class);
		MutablePropertyValues hibernateProps = new MutablePropertyValues();

		Properties props = new Properties();
		props.setProperty("hibernate.hbm2ddl.auto", hbm2dll.toString());
		props.setProperty("hibernate.dialect", dbtype.getHibernateDialectCanonicalName());
		props.setProperty("hibernate.cache.region.factory_class", cacheProviderClass.getName());
		props.setProperty("hibernate.show_sql", String.valueOf(showSql));
		props.setProperty("hibernate.format_sql", String.valueOf(formatSql));
		props.setProperty("hibernate.search.autoregister_listeners", String.valueOf(registerSearchListener));

		hibernateProps.addPropertyValue("properties",props);
		bd.setPropertyValues(hibernateProps);
		return bd;
	}


	/**
	 * Tests existing of the datsource in the according config  file.
	 * @return true if a datasource with the given name exists in the according datasource config file.
	 */
	public static boolean exists(String strDataSourceName){
		Element bean = CdmPersistentSourceUtils.getCdmSourceBeanXml(strDataSourceName, DATASOURCE_BEAN_POSTFIX);
		return (bean != null);
	}

	/**
	 * @param strDataSourceName
	 * @param dataSource
	 * @param code
	 * @return
	 * 			the updated dataSource, null if not succesful
	 */
	public static CdmPersistentDataSource update(String strDataSourceName,
			ICdmDataSource dataSource) throws DataSourceNotFoundException, IllegalArgumentException{
		CdmPersistentSourceUtils.delete(CdmPersistentSourceUtils.getBeanName(strDataSourceName,DATASOURCE_BEAN_POSTFIX));
		return save(strDataSourceName, dataSource);
	}

	/**
	 * Replace the persisted datasource with another one.
	 * Used primarily for renaming a datasource.
	 *
	 * @param strDataSourceName
	 * @param dataSource
	 * @return
	 * @throws DataSourceNotFoundException
	 * @throws IllegalArgumentException
	 */
	public static CdmPersistentDataSource replace(String strDataSourceName,
			ICdmDataSource dataSource) throws DataSourceNotFoundException, IllegalArgumentException{
		CdmPersistentSourceUtils.delete(CdmPersistentSourceUtils.getBeanName(strDataSourceName,DATASOURCE_BEAN_POSTFIX));
		return save(dataSource);
	}

	/**
	 * @param dataSource
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static CdmPersistentDataSource save(ICdmDataSource dataSource)  throws IllegalArgumentException {
		return save(dataSource.getName(),dataSource);
	}

	/**
	 *
	 * @param strDataSourceName
	 * @param databaseTypeEnum
	 * @param server
	 * @param database
	 * @param port
	 * @param username
	 * @param password
	 * @param dataSourceClass
	 * @param initMethod
	 * @param destroyMethod
	 * @param startSilent
	 * @param startServer
	 * @param filePath
	 * @param mode
	 * @return
	 */
	private static CdmPersistentDataSource save(String strDataSourceName,
			DatabaseTypeEnum databaseTypeEnum,
			String server,
			String database,
			String port,
			String username,
			String password,
			Class<? extends DataSource> dataSourceClass,
			String initMethod,
			String destroyMethod,
			Boolean startSilent,
			Boolean startServer,
			String filePath,
			H2Mode mode
		){

		int portNumber = "".equals(port) ? databaseTypeEnum.getDefaultPort() : Integer.valueOf(port);

		ICdmDataSource dataSource = new CdmDataSource(databaseTypeEnum, server, database, portNumber, username, password, filePath, mode);

		//root
		Element root = getBeansRoot(CdmPersistentSourceUtils.getCdmSourceInputStream());
		if (root == null){
			return null;
		}
		//bean
		Element bean = XmlHelp.getFirstAttributedChild(root, "bean", "id", CdmPersistentSourceUtils.getBeanName(strDataSourceName, DATASOURCE_BEAN_POSTFIX));
		if (bean != null){
			bean.detach();  //delete old version if necessary
		}
		bean = insertXmlBean(root, CdmPersistentSourceUtils.getBeanName(strDataSourceName, DATASOURCE_BEAN_POSTFIX), dataSourceClass.getName());
		//attributes
		bean.setAttribute("lazy-init", "true");
		if (initMethod != null) {bean.setAttribute("init-method", initMethod);}
		if (destroyMethod != null) {bean.setAttribute("destroy-method", destroyMethod);}

		//set properties
		insertXmlValueProperty(bean, "driverClassName", databaseTypeEnum.getDriverClassName());

		insertXmlValueProperty(bean, "url", databaseTypeEnum.getConnectionString(dataSource));
		if (username != null) {insertXmlValueProperty(bean, "username", username );}
		if (password != null) {insertXmlValueProperty(bean, "password", password );}
		if (startSilent != null) {insertXmlValueProperty(bean, "startSilent", startSilent.toString() );}
		if (startServer != null) {insertXmlValueProperty(bean, "startServer", startServer.toString() );}
		if (filePath != null) {insertXmlValueProperty(bean, "filePath", filePath );}
		if (mode != null) {insertXmlValueProperty(bean, "mode", mode.toString() );}

		//save
		saveToXml(root.getDocument(),
				CdmPersistentSourceUtils.getResourceDirectory(),
				CdmPersistentXMLSource.CDMSOURCE_FILE_NAME,
				XmlHelp.prettyFormat );
		try {
			return NewInstance(strDataSourceName) ;
		} catch (DataSourceNotFoundException e) {
			logger.error("Error when saving datasource");
			return null;
		}
	}


	/**
	 * Saves a datasource to the datasource config file. If strDataSourceName differs a new dataSource
	 * will be created in config file. Use update() of real update functionality.
	 *
	 * @param strDataSourceName
	 * @param dataSource
	 * @return
	 */
	public static CdmPersistentDataSource save(String strDataSourceName,
			ICdmDataSource dataSource)  throws IllegalArgumentException{

		if(dataSource.getDatabaseType() == null){
			throw new IllegalArgumentException("Database type not specified");
		}

		if(dataSource.getDatabaseType().equals(DatabaseTypeEnum.H2)){
			Class<? extends DataSource> dataSourceClass =  LocalH2.class;
			if(dataSource.getMode() == null) {
			    throw new IllegalArgumentException("H2 mode not specified");
			}
			return save(
					strDataSourceName,
					dataSource.getDatabaseType(),
					"localhost",
					getCheckedDataSourceParameter(dataSource.getDatabase()),
					dataSource.getDatabaseType().getDefaultPort() + "",
					getCheckedDataSourceParameter(dataSource.getUsername()),
					getCheckedDataSourceParameter(dataSource.getPassword()),
					dataSourceClass,
					null, null, null, null,
					dataSource.getFilePath(),
					dataSource.getMode()
					);
		}else{

			Class<? extends DataSource> dataSourceClass;
			try {
				dataSourceClass = (Class<? extends DataSource>) Class.forName(dataSourceClassName);
				String server = getCheckedDataSourceParameter(dataSource.getServer());
				CdmPersistentDataSource persistendDatasource =  save(
					strDataSourceName,
					dataSource.getDatabaseType(),
					getCheckedDataSourceParameter(dataSource.getServer()),
					getCheckedDataSourceParameter(dataSource.getDatabase()),
					dataSource.getPort() + "",
					getCheckedDataSourceParameter(dataSource.getUsername()),
					getCheckedDataSourceParameter(dataSource.getPassword()),
					dataSourceClass,
					null, null, null, null, null, null
				);

				return persistendDatasource;
			} catch (ClassNotFoundException e) {
				logger.error("DataSourceClass not found - stopping application", e);
				System.exit(-1);
			}
			// will never be reached
			return null;
		}
	}

	private static String getCheckedDataSourceParameter(String parameter) throws IllegalArgumentException{
		if(parameter != null) {
			return parameter;
		} else {
			throw new IllegalArgumentException("Non obsolete paramater was assigned a null value: " + parameter);
		}
	}


	/**
	 * Returns a list of all datasources stored in the datasource config file
	 * @return all existing data sources
	 */
	@SuppressWarnings("unchecked")
	static public List<CdmPersistentDataSource> getAllDataSources(){
		List<CdmPersistentDataSource> dataSources = new ArrayList<>();

		Element root = getBeansRoot(CdmPersistentSourceUtils.getCdmSourceInputStream());
		if (root == null){
			return null;
		}else{
	    	List<Element> lsChildren  = root.getChildren("bean", root.getNamespace());

	    	for (Element elBean : lsChildren){
	    		String strId = elBean.getAttributeValue("id");
	    		if (strId != null && strId.endsWith(DATASOURCE_BEAN_POSTFIX)){
	    			strId = strId.replace(DATASOURCE_BEAN_POSTFIX, "");
	    			dataSources.add(new CdmPersistentDataSource(strId));
	    		}
	    	}
		}
		return dataSources;
	}


	@Override
	public boolean equals(Object obj){
		if (obj == null){
			return false;
		}else if (! CdmPersistentDataSource.class.isAssignableFrom(obj.getClass())){
			return false;
		}else{
			CdmPersistentDataSource dataSource = (CdmPersistentDataSource)obj;
			return (getName() == dataSource.getName());
		}
	}

	@Override
	public String toString(){
		if (getName() != null){
			return getName();
		}else{
			return super.toString();
		}
	}
}
