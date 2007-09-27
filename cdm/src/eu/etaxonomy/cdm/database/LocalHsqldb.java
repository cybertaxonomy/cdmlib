/**
 * 
 */
package eu.etaxonomy.cdm.database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hsqldb.Server;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author a.mueller
 *
 */

public class LocalHsqldb extends DriverManagerDataSource {
	private static final Logger logger = Logger.getLogger(LocalHsqldb.class);
	
	private String sep = System.getProperty("file.separator");
	
	/** url without database name */
	protected String pureUrl = "jdbc:hsqldb:hsql://localhost/";
	/** database name */
	protected String dbName = "cdm";
	/** path, where database should be stored in the file system */
	protected String dbPath = getDefaultPath();
	/** Server instance */
	protected Server hsqldbServer;
	/** if true starts server on init() */
	protected boolean isStartServer = true;
	/** makes the Server silent (no messages) */
	protected boolean isSilent = true;
	/** default driver class name */
	protected String DEFAULT_DRIVER_CLASS_NAME = "org.hsqldb.jdbcDriver";
	
	
	/**
	 * 
	 */
	public LocalHsqldb() {
		setDriverClassName(DEFAULT_DRIVER_CLASS_NAME);
		setComposedUrl();
	}

	/**
	 * @param url
	 * @throws CannotGetJdbcConnectionException
	 */
	public LocalHsqldb(String url) throws CannotGetJdbcConnectionException {
		super(url);
		setDriverClassName(DEFAULT_DRIVER_CLASS_NAME);
	}

	/**
	 * @param url
	 * @param username
	 * @param password
	 * @throws CannotGetJdbcConnectionException
	 */
	public LocalHsqldb(String url, String username, String password)
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
	public LocalHsqldb(String driverClassName, String url, String username,
			String password) throws CannotGetJdbcConnectionException {
		super(driverClassName, url, username, password);
	}

	public void init(){
		if (isStartServer){
			this.startHsqldbServer();
		}
	}
	
	public void destroy(){
		this.stopHsqldbServer();
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
	
	/*checked ob die hsqldb gestartet ist, wenn dies nicht der Fall ist, wird sie gestartet
	TODO status: in work
	*/
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
				//server is probably not runing on the url (or login is wrong !!)
				logger.warn("Start HsqldbServer"); //TODO make it .info
				hsqldbServer = new Server();
				hsqldbServer.setSilent(this.isSilent);
				hsqldbServer.setDatabaseName(0, getDbName());
				hsqldbServer.setDatabasePath(0,  getDbPath());
				hsqldbServer.start();
				hsqldbServer.checkRunning(true);
				//String[] args = {"org.hsqldb.Server"};
				// Server.main(args);
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
		//hsqldbServer.shutdown();
	}
	
	private static final String getDefaultPath(){
		String path = System.getProperty("user.dir");
		String sep = System.getProperty("file.separator");
		return  path + sep + "db" +sep + "hsqldb" + sep + "localCdm";
	}

	/**
	 * @return the dbPath
	 */
	public String getDbPath() {
		return dbPath;
	}

	/**
	 * @param dbPath the dbPath to set
	 */
	public void setDbPath(String dbPath) {
		if (dbPath.endsWith(sep)){
			dbPath = dbPath + "localCdm";
		}
		this.dbPath = dbPath;
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
		if (this.hsqldbServer != null){
			this.hsqldbServer.setSilent(isSilent);
		}
		this.isSilent = isSilent;
	}
	
	

}
