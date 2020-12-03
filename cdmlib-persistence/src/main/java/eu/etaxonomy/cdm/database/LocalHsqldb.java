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
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.springframework.jdbc.CannotGetJdbcConnectionException;

import eu.etaxonomy.cdm.api.application.CdmApplicationUtils;

/**
 * @author a.mueller
 */
public class LocalHsqldb extends BasicDataSource {
	private static final Logger logger = Logger.getLogger(LocalHsqldb.class);

	private String sep = System.getProperty("file.separator");

	/** url without database name */
	protected String pureUrl = "jdbc:hsqldb:hsql://localhost/";
	/** database name */
	protected String dbName = "cdm";
	/** path, where database should be stored in the file system */
	protected String databasePath = getDefaultPath();
	/** Server instance */
	protected Server hsqldbServer;
	/** if true starts server on init() */
	protected boolean isStartServer = true;
	/** makes the Server silent (no messages) */
	protected boolean isSilent = true;
	/** default driver class name */
	protected String DEFAULT_DRIVER_CLASS_NAME = "org.hsqldb.jdbcDriver";

	public LocalHsqldb() {
		setDriverClassName(DEFAULT_DRIVER_CLASS_NAME);
		setComposedUrl();
	}

	public LocalHsqldb(String url) throws CannotGetJdbcConnectionException {
		super();
		this.setUrl(url);
		setDriverClassName(DEFAULT_DRIVER_CLASS_NAME);
	}

	public LocalHsqldb(String url, String username, String password)
			throws CannotGetJdbcConnectionException {
		super();
		this.setUrl(url);
		this.setUsername(username);
		this.setPassword(password);
		this.setDriverClassName(DEFAULT_DRIVER_CLASS_NAME);
	}

	public LocalHsqldb(String driverClassName, String url, String username,
			String password) throws CannotGetJdbcConnectionException {
		super();
		this.setUrl(url);
		this.setUsername(username);
		this.setPassword(password);
		this.setDriverClassName(driverClassName);
	}

	public void init(){
		if (isStartServer){
			this.startHsqldbServer();
		}
	}

	public void destroy(){
		this.stopHsqldbServer();
	}

	@Override
	public String getUrl() {
		return super.getUrl();
	}
	@Override
	public void setUrl(String url) {
		super.setUrl(url);
	}

	public String getPureUrl() {
		return pureUrl;
	}
	public void setPureUrl(String pureUrl) {
		this.pureUrl = pureUrl;
		if (dbName != null){
			setComposedUrl();
		}
	}

	public String getDbName() {
		return dbName;
	}
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
	private void startHsqldbServer(){
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
				//server is probably not running on the url (or login is wrong !!)
				logger.info("Start HsqldbServer");
				hsqldbServer = new Server();
				hsqldbServer.setSilent(this.isSilent);
				if (logger.isDebugEnabled()){
					for (int i = 0; i < 10; i++){
						logger.info("DatabaseName " + i + ": " + hsqldbServer.getDatabaseName(i, true));
						logger.info("DatabaseName " + i + ": " + hsqldbServer.getDatabaseName(i, false));
						logger.info("DatabasePath " + i + ": " + hsqldbServer.getDatabasePath(i, true));
						logger.info("DatabasePath " + i + ": " + hsqldbServer.getDatabasePath(i, false));
						logger.info("DatabaseType " + i + ": " + hsqldbServer.getDatabaseType(i));
					}
				}
				hsqldbServer.setDatabaseName(0, getDbName());
				hsqldbServer.setDatabasePath(0,  getFilePath());
				hsqldbServer.start();
				hsqldbServer.checkRunning(true);
			} catch (RuntimeException e1) {
				logger.error("Local hsqlServer could not be started or connection to existing server could not be established.");
			}
		}
	}


	/**
	 * stops the Hsqldb Server
	 */
	private void stopHsqldbServer(){
		if (hsqldbServer != null){
			logger.info("stop HsqldbServer");
			hsqldbServer.stop();
		}
	}

	private static final String getDefaultPath(){
		try {
			File path = CdmApplicationUtils.getWritableResourceDir();
			String subPath = File.separator + "hsqlDb" + File.separator + "LocalHsqldb";
			return  path + subPath;
		} catch (IOException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}

	public String getFilePath() {
		return databasePath;
	}
	public void setFilePath(String filePath) {
		if (databasePath.endsWith(sep)){
			databasePath = databasePath + "localCdm";
		}
		this.databasePath = filePath;
	}

	public boolean isStartServer() {
		return isStartServer;
	}
	public void setStartServer(boolean isStartServer) {
		this.isStartServer = isStartServer;
	}

	public boolean isSilent() {
		return isSilent;
	}
	public void setSilent(boolean isSilent) {
		if (this.hsqldbServer != null){
			this.hsqldbServer.setSilent(isSilent);
		}
		this.isSilent = isSilent;
	}
}
