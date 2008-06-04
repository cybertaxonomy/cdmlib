/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.h2.tools.Server;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
//import org.springframework.jdbc.datasource.DriverManagerDataSource;

import eu.etaxonomy.cdm.api.application.CdmApplicationUtils;


/**
 * @author a.mueller
 *
 */

public class LocalH2 extends DriverManagerDataSource {
	private static final Logger logger = Logger.getLogger(LocalH2.class);
	
	private String sep = System.getProperty("file.separator");
	
	/** url without database name */
	protected String pureUrl = "jdbc:h2:tcp://localhost/";
	/** database name */
	protected String dbName = "cdm";
	/** path, where database should be stored in the file system */
	protected String databasePath = getDefaultPath();
	/** Server instance */
	protected Server h2Server;
	/** if true starts server on init() */
	protected boolean isStartServer = true;
	/** makes the Server silent (no messages) */
	protected boolean isSilent = true;
	/** default driver class name */
	protected String DEFAULT_DRIVER_CLASS_NAME = "org.h2.Driver";
	
	
	/**
	 * 
	 */
	public LocalH2() {
		setDriverClassName(DEFAULT_DRIVER_CLASS_NAME);
		setComposedUrl();
	}

	/**
	 * @param url
	 * @throws CannotGetJdbcConnectionException
	 */
	public LocalH2(String url) throws CannotGetJdbcConnectionException {
		super(url);
		setDriverClassName(DEFAULT_DRIVER_CLASS_NAME);
	}

	/**
	 * @param url
	 * @param username
	 * @param password
	 * @throws CannotGetJdbcConnectionException
	 */
	public LocalH2(String url, String username, String password)
			throws CannotGetJdbcConnectionException {
		super(url, username, password);
		this.setDriverClassName(DEFAULT_DRIVER_CLASS_NAME);
	}

	/**
	 * @param driverClassName
	 * @param url
	 * @param username
	 * @param password
	 * @throws CannotGetJdbcConnectionException
	 */
	public LocalH2(String driverClassName, String url, String username,
			String password) throws CannotGetJdbcConnectionException {
		super(driverClassName, url, username, password);
	}

	public void init(){
		if (isStartServer){
			this.startH2Server();
		}
	}
	
	public void destroy(){
		this.stopH2Server();
	}
	

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.datasource.DriverManagerDataSource#getUrl()
	 */
	@Override
	public String getUrl() {
		return super.getUrl();
	}

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.datasource.DriverManagerDataSource#setUrl(java.lang.String)
	 */
	@Override
	public void setUrl(String url) {
		super.setUrl(url);
	}

	/**
	 * @return the pureUrl
	 */
	public String getPureUrl() {
		return pureUrl;
	}

	/**
	 * @param pureUrl the pureUrl to set
	 */
	public void setPureUrl(String pureUrl) {
		this.pureUrl = pureUrl;
		if (dbName != null){
			setComposedUrl();
		}
	}

	/**
	 * @return the dbName
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * @param dbName the dbName to set
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName;
		if (pureUrl != null){
			setComposedUrl();
		}
	}
	
	private void setComposedUrl(){
		setUrl(getPureUrl() + getDbName());
	}
	
	//checks if hsqldb-server is started, if not it will be started	
	private void startH2Server(){
		try {
			Driver driver = DriverManager.getDriver(getUrl());
			Properties prop = new Properties();
			prop.setProperty("user", this.getUsername());
			prop.setProperty("password", this.getPassword());
			Connection con = driver.connect(getUrl(),  prop);
			if (con == null) {
				logger.warn("Connection to URL " +  getUrl() +  " could not be established");
				throw new SQLException();
			}
		} catch (SQLException e) {
			try {
				//server is probably not runing on the url (or login is wrong !!)
				logger.info("Start H2Server"); 
				String[] args = new String[] { "-trace" };
				h2Server = Server.createTcpServer(args).start();
				if (logger.isDebugEnabled()){
					for (int i = 0; i < 10; i++){
//						logger.info("DatabaseName " + i + ": " + h2Server.getDatabaseName(i, true));
//						logger.info("DatabaseName " + i + ": " + h2Server.getDatabaseName(i, false));
//						logger.info("DatabasePath " + i + ": " + h2Server.getDatabasePath(i, true));
//						logger.info("DatabasePath " + i + ": " + h2Server.getDatabasePath(i, false));
//						logger.info("DatabaseType " + i + ": " + h2Server.getDatabaseType(i));
					}
				}
//				h2Server.setDatabaseName(0, getDbName());
//				h2Server.setDatabasePath(0,  getDatabasePath());
				h2Server.start();
//				h2Server.checkRunning(true);
			} catch (SQLException sqle1) {
				logger.error("SQL Exception when starting Local H2Server.");
			} catch (RuntimeException e1) {
				logger.error("Local H2Server could not be started or connection to existing server could not be established.");
			}
		}
	}
	
	
	/**
	 * stops the Hsqldb Server
	 */
	private void stopH2Server(){
		if (h2Server != null){
			logger.info("stop H2Server");
			h2Server.stop();
		}
	}
	
	private static final String getDefaultPath(){
		//String path = System.getProperty("user.dir");
		File path = CdmApplicationUtils.getWritableResourceDir();
		String subPath = File.separator + "h2" + File.separator + "LocalH2"; 
		return  path + subPath;
	}

	/**
	 * @return the dbPath
	 */
	public String getDatabasePath() {
		return databasePath;
	}

	/**
	 * @param dbPath the dbPath to set
	 */
	public void setDatabasePath(String databasePath) {
		if (databasePath.endsWith(sep)){
			databasePath = databasePath + "localCdm";
		}
		this.databasePath = databasePath;
	}

	/**
	 * @return the isStartServer
	 */
	public boolean isStartServer() {
		return isStartServer;
	}

	/**
	 * @param isStartServer the isStartServer to set
	 */
	public void setStartServer(boolean isStartServer) {
		this.isStartServer = isStartServer;
	}

	/**
	 * @return the isSilent
	 */
	public boolean isSilent() {
		return isSilent;
	}

	/**
	 * @param isSilent the isSilent to set
	 */
	public void setSilent(boolean isSilent) {
		if (this.h2Server != null){
//			this.h2Server.setSilent(isSilent);
		}
		this.isSilent = isSilent;
	}
	
	

}
