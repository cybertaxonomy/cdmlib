package eu.etaxonomy.cdm.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
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
	public static final String SESSION_FACTORY_FILE = "sessionfactory.xml";
	public final static String DATASOURCE_FILE_NAME = "cdm.datasource.xml";
	public final static String APPLICATION_CONTEXT_FILE_NAME = "applicationContext.xml";
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
	 * Updates the session factory config file for using this database.
	 * Writes the datasource property and the dialect property into the session factory.
	 * @param hibernateHbm2ddlAuto value for the hibernate property hibernate.hbm2dll.auto . If null the properties is not changed. Possible values are 'validate', 'create', 'update' and 'create-drop'.
	 * @return true if successful.
	 */
	public boolean updateSessionFactory(String hibernateHbm2ddlAuto){
		Element root = getRoot(getSessionFactoryInputStream());
		if (root == null){
			return false;
		}
		//get sessionFactory bean
		Element sessionFactoryBean = getFirstAttributedChild(root, "bean", "id", "sessionFactory");
		//sessionFactory must exist 
		if  (sessionFactoryBean == null){
			return false;
		}
		
		//set dataSource property
		Element dataSourceProperty = getFirstAttributedChild(sessionFactoryBean, "property", "name", "dataSource");
		if (dataSourceProperty == null){
			dataSourceProperty = insertXmlRefProperty(sessionFactoryBean, "dataSource", getBeanName(this.dataSourceName));
		}
		Attribute attrRef = dataSourceProperty.getAttribute("ref");
		if (attrRef == null){
			dataSourceProperty.setAttribute("ref", getBeanName(this.dataSourceName));
		}else{
			attrRef.setValue(getBeanName(this.dataSourceName));
		}
		
		//set dialect
		Element elHibernateProperties = getOrAddChild(sessionFactoryBean, "property", "name", "hibernateProperties");
		Element props = getOrAddChild(elHibernateProperties, "props", null, null);
		Element elDialectProp = getOrAddChild(props, "prop", "key", "hibernate.dialect");
		elDialectProp.setText(this.getDatabaseType().getHibernateDialect());
		
		//set hibernateHbm2ddlAuto
		if (hibernateHbm2ddlAuto != null){
			if (hibernateHbm2ddlAuto != "validate" && hibernateHbm2ddlAuto != "create"  && hibernateHbm2ddlAuto != "update "  && hibernateHbm2ddlAuto != "create-drop"  ){
				logger.warn("Invalid value " + hibernateHbm2ddlAuto + " for property hibernate.hbm2ddl.auto");
			}
			Element elHbm2ddlAutoProp = getOrAddChild(props, "prop", "key", "hibernate.hbm2ddl.auto");
			elHbm2ddlAutoProp.setText(hibernateHbm2ddlAuto);
		}
		
		//save
		saveToXml(root.getDocument(), getSessionFactoryOutputStream() , format );
		return true;
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
	
	/**
	 * Returns the session factory config file input stream.
	 * @return session factory config file
	 */
	private FileInputStream getSessionFactoryInputStream(){
		String dir = getResourceDirectory();
		File file = new File(dir + File.separator +  SESSION_FACTORY_FILE);
		return fileInputStream(file);
	}
	
	/**
	 * Returns the session factory output stream.
	 * @return 
	 */
	private FileOutputStream getSessionFactoryOutputStream(){
		String dir = getResourceDirectory();
		File file = new File(dir + File.separator +  SESSION_FACTORY_FILE);
		return fileOutputStream(file);
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
	
	
	/**
	 * Filter class to define datasource file format
	 */
	private static class DataSourceFileNameFilter implements FilenameFilter{
		public boolean accept(File dir, String name) {
	        return (name.endsWith(DATASOURCE_FILE_NAME));
	    }
	}
}