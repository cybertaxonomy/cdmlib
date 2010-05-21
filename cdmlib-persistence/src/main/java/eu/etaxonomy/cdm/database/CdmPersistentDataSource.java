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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.NoCacheProvider;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import eu.etaxonomy.cdm.api.application.CdmApplicationUtils;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.database.types.IDatabaseType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;


/**
 * class to access an CdmDataSource
 */
public class CdmPersistentDataSource extends CdmDataSourceBase{
	private static final Logger logger = Logger.getLogger(CdmPersistentDataSource.class);
	
	public static final String DATASOURCE_BEAN_POSTFIX = "DataSource";
	public final static String DATASOURCE_FILE_NAME = "cdm.datasources.xml";
	public final static String DATASOURCE_PATH = "/eu/etaxonomy/cdm/";
	
	private final static Format format = Format.getPrettyFormat(); 

	public enum DbProperties{
		DRIVER_CLASS,
		URL,
		SERVER, 
		DATABASE,
		PORT,
		MODE,
		FILEPATH,
		USERNAME,
		PASSWORD, 
		NOMENCLATURAL_CODE;

		@Override
		public String toString(){
			switch (this){
				case DRIVER_CLASS:
					return "driverClassName";
				case URL:
					return "url";
				case SERVER:
					return "server";
				case DATABASE:
					return "database";
				case PORT:
					return "port";
				case MODE:
					return "mode";
				case FILEPATH:
					return "filePath";
				case USERNAME:
					return "username";
				case PASSWORD:
					return "password";
				case NOMENCLATURAL_CODE:
					return "nomenclaturalCode";
				default: 
					throw new IllegalArgumentException( "Unknown enumeration type" );
			}
		}
	}

	/**
	 * The Datasource class that Spring will use to set up the connection to the database
	 */
	private static String dataSourceClassName = ComboPooledDataSource.class.getName();
	// we used dbcps BasicDataSource before
//	private static String dataSourceClassName = BasicDataSource.class.getName();
	
	//name
	protected String dataSourceName;

	
	/**
	 * Returns the default CdmDataSource
	 * @return the default CdmDataSource
	 */
	public final static CdmPersistentDataSource NewDefaultInstance(){
		try {
			return NewInstance("default");
		} catch (DataSourceNotFoundException e) {
			logger.error("Default datasource does not exist in config file");
			return null;
		}
	}
	
	
	/**
	 * Returns the default CdmDataSource
	 * @return the default CdmDataSource
	 */
	public final static CdmPersistentDataSource NewLocalHsqlInstance(){
		try {
			return NewInstance("localDefaultHsql");
		} catch (DataSourceNotFoundException e) {
			logger.error("Local datasource does not exist in config file");
			return null;
		}
	}
	
	/**
	 * Returns the CdmDataSource named by strDataSource
	 * @param strDataSource
	 * @return
	 */
	public final static CdmPersistentDataSource NewInstance(String dataSourceName) 
				throws DataSourceNotFoundException{
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
		dataSourceName = strDataSource;
	}
	
	/**
	 * Returns the name of the bean.
	 * @return
	 */
	public String getName(){
		return dataSourceName;
	}
	
	
	/**
	 * Returns the name of the bean Element in the xml config file.
	 * @return bean name
	 */
	private static String getBeanName(String name){
		return name == null? null : name + DATASOURCE_BEAN_POSTFIX;
	}


	
	public String getDatabase() {
		return getDatabaseProperty(DbProperties.DATABASE);
	}


	public String getFilePath() {
		//TODO null
		return getDatabaseProperty(DbProperties.FILEPATH);
	}


	public H2Mode getMode() {
		//TODO null
		return H2Mode.fromString(getDatabaseProperty(DbProperties.MODE));
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.ICdmDataSource#getNomenclaturalCode()
	 */
	public NomenclaturalCode getNomenclaturalCode() {
		// TODO null
		return NomenclaturalCode.fromString(getDatabaseProperty(DbProperties.NOMENCLATURAL_CODE));
	}

	public int getPort() {
		String port = CdmUtils.Nz(getDatabaseProperty(DbProperties.PORT));
		if ("".equals(port)){
			return -1;
		}else{
			//TODO exception if non integer
			return Integer.valueOf(port);
		}
	}


	public String getServer() {
		return getDatabaseProperty(DbProperties.SERVER);
	}

	/**
	 * Returns the database type of the data source. 
	 * @return the database type of the data source. Null if the bean or the driver class property does not exist or the driver class is unknown.
	 */
	public DatabaseTypeEnum getDatabaseType(){
		Element bean = getDatasourceBeanXml(this.dataSourceName);
		if (bean == null){
			return null;
		}else{
			Element driverProp = XmlHelp.getFirstAttributedChild(bean, "property", "name", "driverClassName");
			if (driverProp == null){
				logger.warn("Unknown property driverClass");
		    	return null;
			}else{
				String strDriverClass = driverProp.getAttributeValue("value");
				DatabaseTypeEnum dbType = DatabaseTypeEnum.getDatabaseEnumByDriverClass(strDriverClass);
				return dbType;
			}
		}
	}
	
	
	/**
	 * Returns the database type of the data source. 
	 * @return the database type of the data source. Null if the bean or the driver class property does not exist or the driver class is unknown.
	 */
	protected String getDatabaseProperty(DbProperties property){
		Element bean = getDatasourceBeanXml(this.dataSourceName);
		String url;
		String result = null;
		if (bean != null){
			result = getPropertyValue(bean, property.toString());
			if (result == null){  //test if property is database, server or port which are included in the url
				url = getPropertyValue(bean, DbProperties.URL.toString());
				DatabaseTypeEnum dbTypeEnum = getDatabaseType();
				if (dbTypeEnum != null){
					IDatabaseType dbType = dbTypeEnum.getDatabaseType();
					if (property.equals(DbProperties.DATABASE)){
						result = dbType.getDatabaseNameByConnectionString(url);
					}else if(property.equals(DbProperties.SERVER)){
						result = dbType.getServerNameByConnectionString(url);
					}else if(property.equals(DbProperties.PORT)){
						result = String.valueOf(dbType.getPortByConnectionString(url));
					}else{
						logger.debug("Unknown property: " + property);
					}
				}
			}
		}
		return result;	
	}
	
	private String getPropertyValue(Element bean, String property){
		Element driverProp = XmlHelp.getFirstAttributedChild(bean, "property", "name", property);
		if (driverProp == null){
			logger.debug("Unknown property: " + property);
	    	return null;
		}else{
			String strProperty = driverProp.getAttributeValue("value");
			return strProperty;
		}
	}
	


	/**
	 * Returns the list of properties that are defined in the datasource    
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public List<Attribute> getDatasourceAttributes(){
		List<Attribute> result = new ArrayList<Attribute>();
		Element bean = getDatasourceBeanXml(this.dataSourceName);
		if (bean == null){
			return null;
		}else{
			result = bean.getAttributes();
		}
		return result;
	}	

	/**
	 * Returns a defined property of the datasource
	 * @return the property of the data source. NULL if the datasource bean or the property does not exist.
	 */
	public String getDatasourceProperty(DbProperties dbProp){
		Element bean = getDatasourceBeanXml(this.dataSourceName);
		if (bean == null){
			return null;
		}else{
			Element elProperty = XmlHelp.getFirstAttributedChild(bean, "property", "name", dbProp.toString());
			if (elProperty == null){
				logger.warn("Unknown property: " + dbProp.toString());
		    	return null;
			}else{
				String strValue = elProperty.getAttributeValue("value");
				return strValue;
			}
		}
	}

	
	/**
	 * Returns the list of properties that are defined in the datasource    
	 * @return 
	 */
	public Properties getDatasourceProperties(){
		Properties result = new Properties();
		Element bean = getDatasourceBeanXml(this.dataSourceName);
		if (bean == null){
			return null;
		}else{
			List<Element> elProperties = XmlHelp.getAttributedChildList(bean, "property", "name");
			Iterator<Element> iterator = elProperties.iterator();
			while(iterator.hasNext()){
				Element next = iterator.next();
				String strName = next.getAttributeValue("name");
				String strValue = next.getAttributeValue("value");
				result.put(strName, strValue);
			}
		}
		return result;
	}
	
	/**
	 * Returns a BeanDefinition object of type DataSource that contains
	 * datsource properties (url, username, password, ...)
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public BeanDefinition getDatasourceBean(){
		DatabaseTypeEnum dbtype = DatabaseTypeEnum.getDatabaseEnumByDriverClass(getDatasourceProperty(DbProperties.DRIVER_CLASS));
		
		AbstractBeanDefinition bd = new RootBeanDefinition(dbtype.getDataSourceClass());
		//attributes
		Iterator<Attribute> iterator = getDatasourceAttributes().iterator();
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
		Properties persistentProperties = getDatasourceProperties();
		Enumeration<String> keys = (Enumeration)persistentProperties.keys(); 
		while (keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			props.addPropertyValue(key, persistentProperties.getProperty(key));
		}

		bd.setPropertyValues(props);
		return bd;
	}
	
	/**
	 * @param hbm2dll
	 * @param showSql
	 * @return
	 */
	public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll){
		boolean showSql = false;
		boolean formatSql = false;
		boolean registerSearchListener = false;
		Class<? extends CacheProvider> cacheProviderClass = NoCacheProvider.class;
		return getHibernatePropertiesBean(hbm2dll, showSql, formatSql, registerSearchListener, cacheProviderClass);
	}
	
	
	/**
	 * @param hbm2dll
	 * @param showSql
	 * @return
	 */
	public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll, Boolean showSql, Boolean formatSql, Boolean registerSearchListener, Class<? extends CacheProvider> cacheProviderClass){
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
			cacheProviderClass = NoCacheProvider.class;
		}
		if(registerSearchListener == null){
			registerSearchListener = false;
		}
				
		DatabaseTypeEnum dbtype = getDatabaseType();
		AbstractBeanDefinition bd = new RootBeanDefinition(PropertiesFactoryBean.class);
		MutablePropertyValues hibernateProps = new MutablePropertyValues();

		Properties props = new Properties();
		props.setProperty("hibernate.hbm2ddl.auto", hbm2dll.toString());
		props.setProperty("hibernate.dialect", dbtype.getHibernateDialect());
		props.setProperty("hibernate.cache.provider_class", cacheProviderClass.getName());
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
		Element bean = getDatasourceBeanXml(strDataSourceName);
		return (bean != null);
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
			H2Mode mode,
			NomenclaturalCode code
		){
		
		int portNumber = "".equals(port) ? databaseTypeEnum.getDefaultPort() : Integer.valueOf(port);
		
		ICdmDataSource dataSource = new CdmDataSource(databaseTypeEnum, server, database, portNumber, username, password, filePath, mode, code);
				
		//root
		Element root = getBeansRoot(getDataSourceInputStream());
		if (root == null){
			return null;
		}
		//bean
		Element bean = XmlHelp.getFirstAttributedChild(root, "bean", "id", getBeanName(strDataSourceName));
		if (bean != null){
			bean.detach();  //delete old version if necessary
		}
		bean = insertXmlBean(root, getBeanName(strDataSourceName), dataSourceClass.getName());
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
		if (code != null) {insertXmlValueProperty(bean, "nomenclaturalCode", code.name());}
		
		//save
		saveToXml(root.getDocument(), getResourceDirectory(), DATASOURCE_FILE_NAME, format );
		try {
			return NewInstance(strDataSourceName) ;
		} catch (DataSourceNotFoundException e) {
			logger.error("Error when saving datasource");
			return null;
		}
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
		delete(CdmPersistentDataSource.NewInstance(strDataSourceName));
		return save(strDataSourceName, dataSource);
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
			new IllegalArgumentException("Database type not specified");
		}
		
		if(dataSource.getDatabaseType().equals(DatabaseTypeEnum.H2)){
			Class<? extends DataSource> dataSourceClass =  LocalH2.class;
			if(dataSource.getMode() == null){
				new IllegalArgumentException("H2 mode not specified");
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
					getCheckedDataSourceParameter(dataSource.getFilePath()), 
					dataSource.getMode(),
					dataSource.getNomenclaturalCode());
		}else{
			
			Class<? extends DataSource> dataSourceClass;
			try {
				dataSourceClass = (Class<? extends DataSource>) Class.forName(dataSourceClassName);
				
				CdmPersistentDataSource persistendDatasource =  save(
					strDataSourceName, 
					dataSource.getDatabaseType(), 
					getCheckedDataSourceParameter(dataSource.getServer()), 
					getCheckedDataSourceParameter(dataSource.getDatabase()), 
					dataSource.getPort() + "", 
					getCheckedDataSourceParameter(dataSource.getUsername()), 
					getCheckedDataSourceParameter(dataSource.getPassword()), 
					dataSourceClass, 
					null, null, null, null, null, null,
					dataSource.getNomenclaturalCode());
				
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
		if(parameter != null){
			return parameter;
		}else{
			new IllegalArgumentException("Non obsolete paramater was assigned a null value: " + parameter);
			return null;
		}
	}

	/**
	 * Deletes a dataSource
	 * @param dataSource
	 */
	public static void delete (CdmPersistentDataSource dataSource){
		Element bean = getDatasourceBeanXml(dataSource.getName());
		if (bean != null){
			Document doc = bean.getDocument();
			bean.detach();
			saveToXml(doc, getDataSourceOutputStream(), format );
		}
	}
	
	
	/**
	 * Returns a list of all datasources stored in the datasource config file
	 * @return all existing data sources
	 */
	@SuppressWarnings("unchecked")
	static public List<CdmPersistentDataSource> getAllDataSources(){
		List<CdmPersistentDataSource> dataSources = new ArrayList<CdmPersistentDataSource>();
		
		Element root = getBeansRoot(getDataSourceInputStream());
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
	
	public String getUsername(){
		return getDatasourceProperty(DbProperties.USERNAME);
	}
	
	public String getPassword(){
		return getDatasourceProperty(DbProperties.PASSWORD);
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		if (this.dataSourceName != null){
			return dataSourceName;
		}else{
			return null;
		}
	}


	
	/**
	 * Returns the datasource config file input stream.
	 * @return data source config file input stream
	 */
	static protected FileInputStream getDataSourceInputStream(){
		String dir = getResourceDirectory();
		File file = new File(dir + File.separator +  DATASOURCE_FILE_NAME);
		return fileInputStream(file);
	}
	
	
	/**
	 * Returns the datasource config file outputStream.
	 * @return data source config file outputStream
	 */
	static protected FileOutputStream getDataSourceOutputStream(){
		String dir = getResourceDirectory();
		File file = new File(dir + File.separator +  DATASOURCE_FILE_NAME);
		return fileOutputStream(file);
	}

	/**
	 * Returns the jdom Element representing the data source bean in the config file.
	 * @return
	 */
	private static Element getDatasourceBeanXml(String strDataSourceName){
		FileInputStream inStream = getDataSourceInputStream();
		Element root = getBeansRoot(inStream);
		if (root == null){
			return null;
		}else{
	    	Element xmlBean = XmlHelp.getFirstAttributedChild(root, "bean", "id", getBeanName(strDataSourceName));
			if (xmlBean == null){
				//TODO warn or info
				logger.debug("Unknown Element 'bean id=" +strDataSourceName + "' ");
			};
			return xmlBean;
		}
	}
	
	// returns the directory containing the resources 
	private static String getResourceDirectory(){
		try {
			File f = CdmApplicationUtils.getWritableResourceDir();
			return f.getPath();
		} catch (IOException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	static private FileInputStream fileInputStream(File file){
		try {
			FileInputStream fis = new FileInputStream(file);
			return fis;
		} catch (FileNotFoundException e) {
			logger.warn("File " + file == null?"null":file.getAbsolutePath() + " does not exist in the file system");
			return null;
		}
	}
	
	static private FileOutputStream fileOutputStream(File file){
		try {
			FileOutputStream fos = new FileOutputStream(file);
			return fos;
		} catch (FileNotFoundException e) {
			logger.warn("File " + (file == null?"null":file.getAbsolutePath()) + " does not exist in the file system");
			return null;
		}
	}
	
	public boolean equals(Object obj){
		if (obj == null){
			return false;
		}else if (! CdmPersistentDataSource.class.isAssignableFrom(obj.getClass())){
			return false;
		}else{
			CdmPersistentDataSource dataSource = (CdmPersistentDataSource)obj;
			return (this.dataSourceName == dataSource.dataSourceName);
		}

	}
}