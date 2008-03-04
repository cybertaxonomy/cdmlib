package eu.etaxonomy.cdm.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import eu.etaxonomy.cdm.api.application.CdmApplicationUtils;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.XmlHelp;

import static eu.etaxonomy.cdm.common.XmlHelp.getFirstAttributedChild;
import static eu.etaxonomy.cdm.common.XmlHelp.getOrAddChild;
import static eu.etaxonomy.cdm.common.XmlHelp.getRoot;
import static eu.etaxonomy.cdm.common.XmlHelp.insertXmlBean;
import static eu.etaxonomy.cdm.common.XmlHelp.insertXmlRefProperty;
import static eu.etaxonomy.cdm.common.XmlHelp.insertXmlValueProperty;
import static eu.etaxonomy.cdm.common.XmlHelp.saveToXml;


/**
 * class to access an CdmDataSource
 */
public class CdmDataSource {
	private static final Logger logger = Logger.getLogger(CdmDataSource.class);
	
	public static final String DATASOURCE_BEAN_POSTFIX = "DataSource";
	public final static String DATASOURCE_FILE_NAME = "cdm.datasource.xml";
	private final static Format format = Format.getPrettyFormat(); 
	
	//name
	protected String dataSourceName;

	
	/**
	 * Returns the default CdmDataSource
	 * @return the default CdmDataSource
	 */
	public final static CdmDataSource NewDefaultInstance(){
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
	public final static CdmDataSource NewLocalHsqlInstance(){
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
	public final static CdmDataSource NewInstance(String dataSourceName) 
				throws DataSourceNotFoundException{
		if (exists(dataSourceName)){
			return new CdmDataSource(dataSourceName);
		}else{
			throw new DataSourceNotFoundException("Datasource not found: " + dataSourceName);
		}
	}

	/**
	 * Private Constructor. Use NewXXX factory methods for creating a new instance of CdmDataSource!
	 * @param strDataSource
	 */
	private CdmDataSource(String strDataSource){
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
	
	public BeanDefinition getDatasourceBean(){
		AbstractBeanDefinition bd = new RootBeanDefinition(DriverManagerDataSource.class);
		DatabaseTypeEnum dbtype = getDatabaseType();
		//TODO: read real values
		MutablePropertyValues props = new MutablePropertyValues();
		props.addPropertyValue("driverClassName", dbtype.getDriverClassName());
		props.addPropertyValue("url", "jdbc:mysql://192.168.2.10/cdm_build");
		props.addPropertyValue("username", "edit");
		props.addPropertyValue("password", "wp5");
		bd.setPropertyValues(props);
		return bd;
	}
	public BeanDefinition getHibernatePropertiesBean(HBM2DDL hbm2dll, boolean showSql){
		DatabaseTypeEnum dbtype = getDatabaseType();
		AbstractBeanDefinition bd = new RootBeanDefinition(PropertiesFactoryBean.class);
		MutablePropertyValues hibernateProps = new MutablePropertyValues();

		Properties props = new Properties();
		props.setProperty("hibernate.hbm2ddl.auto", hbm2dll.getHibernateString());
		props.setProperty("hibernate.dialect", dbtype.getHibernateDialect());
		props.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
		props.setProperty("hibernate.show_sql", String.valueOf(showSql));
		props.setProperty("hibernate.format_sql", String.valueOf(false));

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
	 * Saves or updates the datasource to the datasource config file.
	 * Uses default port.
	 * @param strDataSourceName name of the datasource (without postfix DataSource)
	 * @param databaseTypeEnum
	 * @param server
	 * @param database
	 * @param username
	 * @param password
	 * @return the CdmDataSource, null if not successful.
	 */
	public static CdmDataSource save(String strDataSourceName, DatabaseTypeEnum databaseTypeEnum, String server, String database, 
			String username, String password){
		return save(strDataSourceName, databaseTypeEnum, server, database, 
				databaseTypeEnum.getDefaultPort(), username, password);
	}
	
	/**
	 * Saves or updates the datasource to the datasource config file.
	 * @param strDataSourceName name of the datasource (without postfix DataSource)
	 * @param databaseTypeEnum
	 * @param server
	 * @param database
	 * @param port
	 * @param username
	 * @param password
	 * @return the CdmDataSource, null if not successful.
	 */
	public static CdmDataSource save(String strDataSourceName, DatabaseTypeEnum databaseTypeEnum, String server, String database, 
				int port, String username, String password){
		Class<? extends DriverManagerDataSource> driverManagerDataSource =  DriverManagerDataSource.class;
		return save(strDataSourceName, databaseTypeEnum, server, database, port, username, password, driverManagerDataSource, null, null, null, null, null);
	}
	
	
	public static CdmDataSource saveLocalHsqlDb(String strDataSourceName, String databasePath, String databaseName, String username, String password){
		DatabaseTypeEnum databaseTypeEnum = DatabaseTypeEnum.HSqlDb;
		Class<? extends DriverManagerDataSource> driverManagerDataSource =  LocalHsqldb.class;
		String server = "localhost";
		int port = databaseTypeEnum.getDefaultPort();
		return save(strDataSourceName, databaseTypeEnum, server, databaseName, port, username, password, driverManagerDataSource, "init", "destroy", true, true, databasePath);
	}
	
	//
	private static CdmDataSource save(String strDataSourceName, 
			DatabaseTypeEnum databaseTypeEnum, 
			String server, 
			String database, 
			int port, 
			String username, 
			String password, 
			Class<? extends DriverManagerDataSource> driverManagerDataSource,
			String initMethod,
			String destroyMethod,
			Boolean startSilent,
			Boolean startServer, 
			String databasePath
		){
		//root
		Element root = getRoot(getDataSourceInputStream());
		if (root == null){
			return null;
		}
		//bean
		Element bean = XmlHelp.getFirstAttributedChild(root, "bean", "id", getBeanName(strDataSourceName));
		if (bean != null){
			bean.detach();  //delete old version if necessary
		}
		bean = insertXmlBean(root, getBeanName(strDataSourceName), driverManagerDataSource.getName());
		//attributes
		bean.setAttribute("lazy-init", "true");
		if (initMethod != null) {bean.setAttribute("init-method", initMethod);}
		if (destroyMethod != null) {bean.setAttribute("destroy-method", destroyMethod);}
		
		//set properties
		insertXmlValueProperty(bean, "driverClassName", databaseTypeEnum.getDriverClassName());
		insertXmlValueProperty(bean, "url", databaseTypeEnum.getConnectionString(server, database, port));
		if (username != null) {insertXmlValueProperty(bean, "username", username );}
		if (password != null) {insertXmlValueProperty(bean, "password", password );}
		if (startSilent != null) {insertXmlValueProperty(bean, "startSilent", startSilent.toString() );}
		if (startServer != null) {insertXmlValueProperty(bean, "startServer", startServer.toString() );}
		if (startServer != null) {insertXmlValueProperty(bean, "databasePath", databasePath );}
		
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
	 * Deletes a dataSource
	 * @param dataSource
	 */
	public static void delete (CdmDataSource dataSource){
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
	static public List<CdmDataSource> getAllDataSources(){
		List<CdmDataSource> dataSources = new ArrayList<CdmDataSource>();
		
		Element root = getRoot(getDataSourceInputStream());
		if (root == null){
			return null;
		}else{
	    	List<Element> lsChildren  = root.getChildren("bean", root.getNamespace());
	    	
	    	for (Element elBean : lsChildren){
	    		String strId = elBean.getAttributeValue("id");
	    		if (strId != null && strId.endsWith(DATASOURCE_BEAN_POSTFIX)){
	    			strId = strId.replace(DATASOURCE_BEAN_POSTFIX, "");
	    			dataSources.add(new CdmDataSource(strId));
	    		}
	    	}
		}
		return dataSources;
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
		Element root = getRoot(getDataSourceInputStream());
		if (root == null){
			return null;
		}else{
	    	Element xmlBean = XmlHelp.getFirstAttributedChild(root, "bean", "id", getBeanName(strDataSourceName));
			if (xmlBean == null){logger.warn("Unknown Element 'bean' ");};
			return xmlBean;
		}
	}
	
	// returns the directory containing the resources 
	private static String getResourceDirectory(){
		File f = CdmApplicationUtils.getWritableResourceDir();
		return f.getPath();
	}
	
//	/**
//	 * Returns the session factory config file input stream.
//	 * @return session factory config file
//	 */
//	private FileInputStream get22SessionFactoryInputStream(){
//		String dir = getResourceDirectory();
//		File file = new File(dir + File.separator +  SESSION_FACTORY_FILE);
//		return fileInputStream(file);
//	}
//	
//	/**
//	 * Returns the session factory output stream.
//	 * @return 
//	 */
//	private FileOutputStream get22SessionFactoryOutputStream(){
//		String dir = getResourceDirectory();
//		File file = new File(dir + File.separator +  SESSION_FACTORY_FILE);
//		return fileOutputStream(file);
//	}
//	
	
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
	
	
	/**
	 * Filter class to define datasource file format
	 */
	private static class DataSourceFileNameFilter implements FilenameFilter{
		public boolean accept(File dir, String name) {
	        return (name.endsWith(DATASOURCE_FILE_NAME));
	    }
	}
}