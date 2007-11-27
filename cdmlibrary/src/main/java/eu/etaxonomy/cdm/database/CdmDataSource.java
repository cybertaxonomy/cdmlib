package eu.etaxonomy.cdm.database;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.xpath.XPath;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;

import static eu.etaxonomy.cdm.database.XmlHelp.insertXmlBean; 
import static eu.etaxonomy.cdm.database.XmlHelp.insertXmlRefProperty;
import static eu.etaxonomy.cdm.database.XmlHelp.insertXmlValueProperty; 
import static eu.etaxonomy.cdm.database.XmlHelp.saveToXml; 
import static eu.etaxonomy.cdm.database.XmlHelp.getFirstAttributedChild;
import static eu.etaxonomy.cdm.database.XmlHelp.getOrAddChild;
import static eu.etaxonomy.cdm.database.XmlHelp.getRoot;;


/**
 * class to access an CdmDataSource
 */
public class CdmDataSource {
	private static final Logger logger = Logger.getLogger(CdmDataSource.class);
	
	public static final String BEAN_POSTFIX = "DataSource";
	public static final String SESSION_FACTORY_FILE = "sessionfactory.xml";
	public final static String DATASOURCE_FILE_NAME = "cdm.datasource.xml";
	private final static Format format = Format.getPrettyFormat(); 
	
	
	/**
	 * Returns the default CdmDataSource
	 * @return the default CdmDataSource
	 */
	public final static CdmDataSource getDefaultDataSource(){
		return getDataSource("default");
	}
	/**
	 * Returns the CdmDataSource named by strDataSource
	 * @param strDataSource
	 * @return
	 */
	public final static CdmDataSource getDataSource(String strDataSource){
		if (exists(strDataSource)){
			return new CdmDataSource(strDataSource);
		}else{
			logger.warn("CdmDataSource" + strDataSource == null? "(null)": strDataSource + " does not exist."  );
			return null;
		}
	}
	
	//name
	protected String dataSourceName;

	/**
	 * private Constructor.
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
		return name == null? null : name + BEAN_POSTFIX;
	}


	
	/**
	 * Updates the session factory config file for using this database.
	 * Writes the datasource property and the dialect property into the session factory.
	 * @return true if successful.
	 */
	public boolean updateSessionFactory(){
		Element root = getRoot(getSessionFactoryFile());
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
		
		//save
		saveToXml(root.getDocument(), getResourceDirectory(), getSessionFactoryFile().getName(), format );
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
		//root
		Element root = getRoot(getDataSourceFile());
		if (root == null){
			return null;
		}
		//bean
		Element bean = XmlHelp.getFirstAttributedChild(root, "bean", "id", getBeanName(strDataSourceName));
		if (bean != null){
			bean.detach();  //getParentElement().removeChildren("mysqlDataSource");//delete old version if necessary
		}
		bean = insertXmlBean(root, getBeanName(strDataSourceName), "org.springframework.jdbc.datasource.DriverManagerDataSource");
		//set properties
		insertXmlValueProperty(bean, "driverClassName", databaseTypeEnum.getDriverClassName());
		insertXmlValueProperty(bean, "url", databaseTypeEnum.getConnectionString(server, database, port));
		insertXmlValueProperty(bean, "username", username );
		insertXmlValueProperty(bean, "password", password );
		//save
		saveToXml(root.getDocument(), getResourceDirectory(), DATASOURCE_FILE_NAME, format );
		return getDataSource(strDataSourceName) ;
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
			saveToXml(doc, getResourceDirectory(), DATASOURCE_FILE_NAME, format );
		}
	}
	
	
	/**
	 * Returns a list of all datasources stored in the datasource config file
	 * @return all existing data sources
	 */
	static public List<CdmDataSource> getAllDataSources(){
		List<CdmDataSource> dataSources = new ArrayList<CdmDataSource>();
		
		Element root = getRoot(getDataSourceFile());
		if (root == null){
			return null;
		}else{
	    	List<Element> lsChildren  = root.getChildren("bean", root.getNamespace());
	    	
	    	for (Element elBean : lsChildren){
	    		String strId = elBean.getAttributeValue("id");
	    		if (strId != null && strId.endsWith(BEAN_POSTFIX)){
	    			strId = strId.replace(BEAN_POSTFIX, "");
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
	 * Returns the session factory config file.
	 * @return session factory config file
	 */
	private File getSessionFactoryFile(){
		String dir = getResourceDirectory();
		File file = new File(dir + File.separator +  SESSION_FACTORY_FILE);
		return file;
	}
	
	/**
	 * Returns the datasource config file.
	 * @return data source config file
	 */
	static public File getDataSourceFile(){
		String dir = getResourceDirectory();
		File file = new File(dir + File.separator +  DATASOURCE_FILE_NAME);
		if (! file.exists()) logger.warn("Datasource file does not exist in the file system");
		return file;
	}
	
	
	// returns the directory containing the resources 
	private static String getResourceDirectory(){
		File f = CdmUtils.getResourceDir();
		return f.getPath();
	}
	
	
	/**
	 * Returns the jdom Element representing the data source bean in the config file.
	 * @return
	 */
	private static Element getDatasourceBeanXml(String strDataSourceName){
		Element root = getRoot(getDataSourceFile());
		if (root == null){
			return null;
		}else{
	    	Element xmlBean = XmlHelp.getFirstAttributedChild(root, "bean", "id", getBeanName(strDataSourceName));
			if (xmlBean == null){logger.warn("Unknown Element 'bean' ");};
			return xmlBean;
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