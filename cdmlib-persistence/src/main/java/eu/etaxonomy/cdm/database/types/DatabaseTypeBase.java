/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.types;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.NomenclaturalCodeAwareDataSource;


/**
 * @author a.mueller
 *
 */
abstract class DatabaseTypeBase implements IDatabaseType {
	private static final Logger logger = Logger.getLogger(DatabaseTypeBase.class);
	
	//typeName
	private String typeName;
	//String for DriverClass
	private String classString;
	//url
	private String urlString;
	//defaultPort
	private int defaultPort;
	//hibernate dialect
	private String hibernateDialect;
	//init method
	private String initMethod = null;
	//init method
	private String destroyMethod = null;
	
	
	//init
	protected void init(String typeName, String classString, String urlString, int defaultPort, String hibernateDialect) {
		this.typeName = typeName;
		this.classString = classString;
		this.urlString = urlString;
		this.defaultPort = defaultPort;
		this.hibernateDialect = hibernateDialect;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.IDatabaseType#getName()
	 */
	public String getName(){
		return typeName;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.IDatabaseType#getClassString()
	 */
	public String getClassString(){
		return classString;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.IDatabaseType#getUrlString()
	 */
	public String getUrlString(){
		return urlString;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.IDatabaseType#getDefaultPort()
	 */
	public int getDefaultPort(){
		return defaultPort;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.IDatabaseType#getHibernateDialect()
	 */
	public String getHibernateDialect(){
		return "org.hibernate.dialect." + hibernateDialect;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.IDatabaseType#getConnectionString(java.lang.String, java.lang.String)
	 */
	public String getConnectionString(ICdmDataSource cdmDataSource){
		int port = cdmDataSource.getPort();
		if (port< 1){
			port = defaultPort;
		}
		return getConnectionString(cdmDataSource, port);
    }
	
	abstract protected String getConnectionString(ICdmDataSource cdmDataSource, int port);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getDataSourceClass()
	 */
	public Class<? extends DataSource> getDataSourceClass() {
		return NomenclaturalCodeAwareDataSource.class;
	} 
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getInitMethod()
	 */
	public String getInitMethod() {
		return initMethod;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getDestroyMethod()
	 */
	public String getDestroyMethod() {
		return destroyMethod;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getServerNameByConnectionString(java.lang.String)
	 */
	public String getServerNameByConnectionString(String connectionString){
    	return getServerNameByConnectionString(connectionString, urlString, "/");
    }
	
	protected String getServerNameByConnectionString(String connectionString, String strUrl, String dbSeparator){
    	String result;
    	if (connectionString == null){
    		return null;
    	}
    	connectionString = connectionString.substring(strUrl.length());
    	int posPort = connectionString.indexOf(":");
    	int posDb = connectionString.indexOf(dbSeparator);
    	if (posPort != -1 && posPort < posDb){
    		result = connectionString.substring(0, posPort);
    	}else if (posDb != -1){
    		result = connectionString.substring(0, posDb);
    	}else{
    		logger.warn("No database defined or override for getServerNameByConnectionString() needed for this database type");
    		result = connectionString;
    	}
    	return result;
    }
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getPortByConnectionString(java.lang.String)
	 */
	public int getPortByConnectionString(String connectionString){
		String dbSeparator = "/";
    	return getPortByConnectionString(connectionString, urlString, dbSeparator);
    }
	
	protected int getPortByConnectionString(String connectionString, String strUrl, String dbSeparator){
		if (connectionString == null){
    		return -1;
    	}
		int result;
    	connectionString = connectionString.substring(strUrl.length());
    	int posPort = connectionString.indexOf(":");
    	int posDb = connectionString.indexOf(dbSeparator, posPort+1);
    	if (posPort == -1){
    		result = defaultPort;
    	}else{
    		if (posDb != -1){
    			connectionString = connectionString.substring(posPort + 1, posDb);
    			try {
					result = Integer.valueOf(connectionString);
				} catch (NumberFormatException e) {
					logger.warn("Port is not an Integer in connection string: " + connectionString);
					return -1;
				}
        	}else{
        		logger.warn("No database defined or override for getPortByConnectionString() needed for this database type");	
        		result = -1;
        	}
    	}
     	return result;
	}
	
	
	protected String getDatabasePartOfConnectionString(String connectionString, String dbSeparator){
    	String result;
    	if (connectionString == null){
    		return null;
    	}
    	connectionString = connectionString.substring(urlString.length()); //delete prefix
    	int posDb = connectionString.indexOf(dbSeparator);
    	if (posDb != -1){
    		result = connectionString.substring(posDb + dbSeparator.length() );
    	}else{
    		logger.warn("No database defined or override for getServerNameByConnectionString() needed for this database type");
    		result = connectionString;
    	}
    	return result;
    }
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getDatabaseNameByConnectionString(java.lang.String)
	 */
	public abstract String getDatabaseNameByConnectionString(String connectionString);

	
}
