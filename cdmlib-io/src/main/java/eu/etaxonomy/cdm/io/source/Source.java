package eu.etaxonomy.cdm.io.source;

/*
 * Created on 14.05.2005
 * @author Andreas Müller
 * Updated 20.08.2006
 */


import java.sql.*;
import java.io.*;

import org.apache.log4j.Logger;


/**
 *  
 *  Creates Cursors from extern relational DB.
 *  Used only for developpers convienence hence undocumented.
 *  You may create input cursors in any other way you want
 *  @author Andreas M&uuml;ller
 */
public class Source {
	static Logger logger = Logger.getLogger(Source.class);

/* ************ Constants **************************************/
    //Mode
	private final static boolean DEBUG_MODE = false;
    private final static boolean DEBUG_LOG_WRITER = false;
	
    //DB info
	public final static String SQL_SERVER = "SQLServer";
	public final static String ACCESS = "Access";
	public final static String EXCEL = "Excel";
	public final static String ODDBC = "ODBC";
	public final static String ORACLE = "Oracle";
	public final static String DB2 = "DB2";
	
	//coursor mode
	public final static String SELECT_DIRECT = "direct";
	public final static String SELECT_CURSOR = "cursor";
		
    //driver class
    private static String clsSQLServer = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    private static String clsODBC = "sun.jdbc.odbc.JdbcOdbcDriver";
    private static String clsOracle = "oracle.jdbc.driver.OracleDriver";
    private static String clsDB2 = "COM.ibm.db2.jdbc.net.DB2Driver";
    private static String clsSQLServerDdtek = "com.ddtek.jdbc.sqlserver.SQLServerDriver";
    private static String clsDefault = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    
    //url
    private static String urlSQLServer = "jdbc:microsoft:sqlserver://";
    private static String urlDB2 = "jdbc:db2://";
    private static String urlOracle = "jdbc:oracle:thin:@:1243:";
    private static String urlDataDirectSQLServer = "jdbc:datadirect:sqlserver://";
    private static String urlODBC = "jdbc:odbc:";
    private static String urlDefault = "jdbc:microsoft:sqlserver://LAPI:1433;DatabaseName=studienarbeit;SelectMethod=direct";
    
    
/* *************** VARIABLES *******************************/
    private Connection mConn;
    private Statement mStmt;
    private String mQuery;
    private String mUrl = null;
    private String mDb = null; 
    private int mPort = 1433; //default port TODO 2 derzeit nur für SQLServer, müsste auch für andere Datenbanken umgesetzt werden
    private String mDbms = null;
    private String mServer = null;
    private boolean isCursor;
    private boolean connExist = false; //does a Connection exist?
    private String mUserName; 
    private String mPwd; 
    
    private static String userNameDefault = "sa"; //default user
    private static String pwdDefault = "sa"; //default PWD
    
    
/* ******************** CONSTRUCTORS **********************************/
    
    
    /**
     * Creates a source and sets the according variables
     * @param dbms name of database management system (e.g. "SQLServer", "Access", "Excel", "Oracle"
	 * @param server name of server, if dbms is ODBC, this must be the ODBC name
	 * @param db name of database
	*/
    public Source(String dbms, String server, String db){
    	mDbms = dbms;
        mServer = server;
        mDb = db;
        this.mPwd = pwdDefault;
        this.mUserName = userNameDefault;
    }

    /**
     * Creates a source and sets the query string
     * @param dbms name of database management system (e.g. "SQLServer", "Access", "Excel", "Oracle"
	 * @param server name of server, if dbms is ODBC, this must be the ODBC name
	 * @param db name of database
     * @param query the query
     */
    public Source(String dbms, String server, String db, String query){
        this(dbms, server, db);
        this.setQuery(query);
    }

    /**
    * Creates a source and sets the parameter.
     * @param cursor access mode, if true 'cursor', if false 'direct'
    */
    public Source(String dbms, String server, String db, boolean cursor)   {
        this(dbms, server, db);
    	isCursor = cursor;
    }
    

    /**
    * Creates a source and sets the parameter. If port is -1 the databases default port is used.
    * @param dbms name of database management system (e.g. "SQLServer", "Access", "Excel", "Oracle"
    * @param server name of server, if dbms is ODBC, this must be the ODBC name
    * @param db name of database
    * @param port the databases port (-1: use default port)
    */
    public Source(String dbms, String server, String db, int port) {
        this(dbms, server, db);
        if (port != -1){
        	this.setPort(port);
        };
    }
    
//********************* METHODS *****************************************************/
   
    //******* RESULTSETS *******************//
    /**
     * Returns the Resultset for query 'query'. 
     * Does not change the Sources query-string!!
     * @return Resultset for the query.
     */
    public ResultSet getResultSet (String query){
    	ResultSet rs;
    	try {
            this.getConnection(); //establish connection
        	if (query == null){
        		return null;
        	}
            mStmt = mConn.createStatement();
            rs = mStmt.executeQuery(query);
            return rs;
        }catch(SQLException e){
            logger.error("Problems when creating Resultset for query \n  " + query + " \n" + "Exception: " + e);
            return null;
        }
    }

    
    /** 
     * Returns the Resultset for Sources query. 
     * @return Resultset for the Sources query
     */
    public ResultSet getResultSet (){
    	return getResultSet(mQuery);  
    }

    
    // ***************CONNECTION *************************************//
    /**
     * Returns the connection.
     * @return the Sources connection
     */
    public Connection getConnection(){
    	try {
			if (this.connExist == false){
				if (mConn != null){ 
					mConn.close();
				}
				this.makeConnection() ;
    		}
			return this.mConn;
		} catch (SourceConnectionException e) {
			//TODO error weitergabe des Fehlers nach oben?
			logger.error("Error when establishing Connection");
			return null;
		} catch (SQLException e) {
			logger.error("SQLException in getConnection.");
			return null;
		}
    }
    
    
    /**
     * Makes the connection.
     * @return true if connection created
     * @throws SourceConnectionException
     */
    private boolean makeConnection()throws SourceConnectionException {
    	try {
			if (makeConnectionString() == false){
				throw new SourceConnectionException ("Error in Connection String");
			}
			mConn = DriverManager.getConnection(mUrl, mUserName, mPwd);
			mConn.setCatalog(mDb);  //
			logger.info("Connected to " + mConn.getCatalog());
			mStmt = mConn.createStatement();
			this.connExist = true;
			return true;
		}catch (SQLException e){
            logger.error("Probleme beim Öffnen der Datenbank !!!\n" + 
                    "URL: " + mUrl  + "\n" +
                    "Exception: " + e);
            throw new SourceConnectionException ();
        } 
    }
    
    /**
     * Makes the connection string 
     * @return false if ClassNotFoundException, else true
     */
    private boolean makeConnectionString(){
    	String selectMethod; 
        String server;
        
    	if (isCursor)
	    	selectMethod = SELECT_CURSOR;
	    else
	    	selectMethod = SELECT_DIRECT;
	    try{
	        if (DEBUG_LOG_WRITER) DriverManager.setLogWriter((new PrintWriter(System.out)));
	        if (mDbms.equalsIgnoreCase("SQLServer")) {
	            Class.forName(clsSQLServer);
	            server = mServer + ":" + mPort;
	            mUrl = urlSQLServer + server + ";DataBase=" + mDb + ";SelectMethod="+ selectMethod; 
	        }
	        else if (mDbms.equalsIgnoreCase("Access")) {
	            Class.forName(clsODBC);
	            mUrl = urlODBC + mDb ; 
	        }
	        else if (mDbms.equalsIgnoreCase("Excel")) {
	            Class.forName(clsODBC);
	            mUrl = urlODBC + mDb ; 
	        }
	        else if (mDbms.equalsIgnoreCase("ODBC")) {
	            Class.forName(clsODBC);
	            mUrl = urlODBC + mServer ; 
	        }
	        else if (mDbms.equalsIgnoreCase("Oracle")) {
	            Class.forName(clsOracle);
	            mUrl = urlOracle + mDb ;
	        }
	        else if (mDbms.equalsIgnoreCase("DB2")) {
	            Class.forName(clsDB2);
	            mUrl = urlDB2 + mDb; 
	        }
	        else if (mDbms.equalsIgnoreCase("SQLServerDdtek")) {
	             Class.forName(clsSQLServerDdtek);
	             mUrl = urlDataDirectSQLServer + mServer;
	         }
	        else {
	            Class.forName(clsSQLServer);
	            mUrl = urlDefault; 
	        }
	        logger.debug("Connection String: " + mUrl);	
	        return true;
	    }catch (ClassNotFoundException e){
	        logger.error("Datenbank-Treiber-Klasse konnte nicht geladen werden\n" + "Exception: " + e.toString());
	        return false;
	    }
    }
 
    
/* ******************** SETTER *************************************/

    /**
     * @param query
     */
    public Source setQuery (String query){
        mQuery = query;
        return this;
    }
    
    /**
     * Sets the username.
     * @param userName
     */
    public Source setUsername (String userName){
    	if (userName == null){
    		this.mUserName = userNameDefault;
    	}else{
    		this.mUserName = userName;
    	}
    	this.connExist = false;
		return this;
    }
        
    /**
     * Sets the password.
     * @param pwd
     */
    public Source setPassword (String pwd){
    	if (pwd == null){
    		this.mPwd = pwdDefault;
    	}else{
    		this.mPwd = pwd;
    	}
    	this.connExist = false;
		return this;
	}
    
    /**
     * Sets the username and password.
     * @param userName
     * @param pwd
     */
    public Source setUserAndPwd (String userName, String pwd){
    	setUsername(userName);
    	setPassword(pwd);
    	return this;
	}
    
    /**
     * Sets the port.
     * @param userName
     * @param pwd
     */
    public Source setPort (int port){
    	this.mPort = port;
		this.connExist = false;
		return this;
	}
    
    public String getDatabase(){
    	return mDb;
    }
    
    public String getServer(){
    	return mServer;
    }
    

}