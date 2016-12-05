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
import org.hibernate.dialect.Dialect;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.DbcpSaveDataSource;


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
	private Dialect hibernateDialect;
	//init method
	private String initMethod = null;
	//init method
	private String destroyMethod = null;
	
	
	//init
	protected void init(String typeName, String classString, String urlString, int defaultPort, Dialect hibernateDialect) {
		this.typeName = typeName;
		this.classString = classString;
		this.urlString = urlString;
		this.defaultPort = defaultPort;
		this.hibernateDialect = hibernateDialect;
	}
	
	@Override
	public String getName(){
		return typeName;
	}

	@Override
	public String getClassString(){
		return classString;
	}
	
	@Override
	public String getUrlString(){
		return urlString;
	}
	
	@Override
	public int getDefaultPort(){
		return defaultPort;
	}
	
	@Override
	public Dialect getHibernateDialect(){
		return hibernateDialect;
	}

	@Override
	public String getHibernateDialectCanonicalName(){
		return hibernateDialect.getClass().getCanonicalName();
	}

	@Override
	public String getConnectionString(ICdmDataSource cdmDataSource){
		int port = cdmDataSource.getPort();
		if (port< 1){
			port = defaultPort;
		}
		return getConnectionString(cdmDataSource, port);
    }
	
	abstract protected String getConnectionString(ICdmDataSource cdmDataSource, int port);

	@Override
	public Class<? extends DataSource> getDataSourceClass() {
		return DbcpSaveDataSource.class;
	} 
	
	@Override
	public String getInitMethod() {
		return initMethod;
	}
	
	@Override
	public String getDestroyMethod() {
		return destroyMethod;
	}
	
	@Override
	public String getServerNameByConnectionString(String connectionString){
    	return getServerNameByConnectionString(connectionString, urlString, "/");
    }
	
	protected String getServerNameByConnectionString(String connectionString, String strUrl, String dbSeparator){
    	String result;
    	if (connectionString == null || !connectionString.startsWith(urlString)){
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
	
	@Override
	public int getPortByConnectionString(String connectionString){
		String dbSeparator = "/";
    	return getPortByConnectionString(connectionString, urlString, dbSeparator);
    }
	
	protected int getPortByConnectionString(String connectionString, String strUrl, String dbSeparator){
		if (connectionString == null || !connectionString.startsWith(urlString)){
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
    	if (connectionString == null || !connectionString.startsWith(urlString)){
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
	
	@Override
	public abstract String getDatabaseNameByConnectionString(String connectionString);

	
}
