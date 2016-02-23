/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

/*
 * Created on 14.05.2005
 * @author Andreas Müller
 * Updated 20.08.2006
 */


import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.http.MethodNotSupportedException;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.DatabaseTypeNotSupportedException;


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
//	public final static String SQL_SERVER_2000 = "SQLServer2000";
	public final static String SQL_SERVER_2005 = "SQLServer2005";
	public final static String SQL_SERVER_2008 = "SQLServer2008";
	public final static String ACCESS = "Access";
	public final static String EXCEL = "Excel";
	public final static String ODDBC = "ODBC";
	public final static String ORACLE = "Oracle";
	public final static String DB2 = "DB2";
	public final static String POSTGRESQL9 = "PostgreSQL9";

	//coursor mode
	public final static String SELECT_DIRECT = "direct";
	public final static String SELECT_CURSOR = "cursor";

    //driver class
//    private static String clsSQLServer2000 = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    private static String clsSQLServer2005 = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static String clsSQLServer2008 = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static String clsODBC = "sun.jdbc.odbc.JdbcOdbcDriver";
    private static String clsOracle = "oracle.jdbc.driver.OracleDriver";
    private static String clsDB2 = "COM.ibm.db2.jdbc.net.DB2Driver";
    private static String clsSQLServerDdtek = "com.ddtek.jdbc.sqlserver.SQLServerDriver";
    private static String clsDefault = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    private static String clsPostgreSQL = "org.postgresql.Driver";

    //url
//    private static String urlSQLServer = "jdbc:microsoft:sqlserver://";
    private static String urlSQLServer2005 = "jdbc:sqlserver://";
    private static String urlSQLServer2008 = "jdbc:sqlserver://";
    private static String urlDB2 = "jdbc:db2://";
    private static String urlOracle = "jdbc:oracle:thin:@:1243:";
    private static String urlDataDirectSQLServer = "jdbc:datadirect:sqlserver://";
    private static String urlODBC = "jdbc:odbc:";
    private static String urlPostgreSQL = "jdbc:postgresql://";

/* *************** VARIABLES *******************************/
    private Connection mConn;
    private Statement mStmt;
    private String mQuery;
    private String mUrl = null;
    private String mDb = null;
    private int mPort = 1433; //default port TODO 2 currently only for SQLServer, needs to be implemented also for othe DBMS
    private String mDbms = null;
    private String mServer = null;
    private boolean isCursor;
    private boolean connExist = false; //does a Connection exist?
    private String mUserName;
    private String mPwd;

	private boolean doLog = false;


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

    /**
     * Creates a source with parameters of a ICdmDataSource instance
     *
     * @param cdmDataSource
     */
    public Source(ICdmDataSource cdmDataSource){
    	mDbms = cdmDataSource.getDatabaseType().getName();
        mServer = cdmDataSource.getServer();
        mDb = cdmDataSource.getDatabase();
        mPwd = cdmDataSource.getPassword();
        mUserName = cdmDataSource.getUsername();
        this.setPort(cdmDataSource.getPort());
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
        }catch(Exception e){
            logger.error("Problems when creating Resultset for query \n  " + query + " \n" + "Exception: " + e);
            throw new RuntimeException(e);
        }
    }

    //******* INSERT, UPDATE, DELETE *******************//
    /**
     * Executes an insert, update or delete statement.
     * Returns the number of rows changed or -1 if updatedStatement was 0 or and error occurred.
     * Does not change the Sources query-string!!
     * @return Resultset for the query.
     */
    public int update (String updateStatement){
    	int result;
    	try {
            this.getConnection(); //establish connection
        	if (updateStatement == null){
        		return -1;
        	}
            mStmt = mConn.createStatement();
            result = mStmt.executeUpdate(updateStatement);
            return result;
        }catch(SQLException e){
            logger.error("Problems when creating Resultset for query \n  " + updateStatement + " \n" + "Exception: " + e);
            return -1;
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
			throw new RuntimeException("Error when establishing Connection.", e);
		} catch (SQLException e) {
			throw new RuntimeException("SQLException in getConnection.", e);
		}
    }


    /**
     * Makes the connection.
     * @return true if connection created
     * @throws SourceConnectionException
     */
    private boolean makeConnection()throws SourceConnectionException {
    	if (doLog ){
    		DriverManager.setLogWriter(new PrintWriter(System.out));
    	}
    	try {
			if (makeConnectionString() == false){
				throw new SourceConnectionException ("Error in Connection String");
			}
			if (mDbms.equalsIgnoreCase(ODDBC) ){
				//not necessarily limited to ODBC
				java.util.Properties prop = new java.util.Properties();
//			    prop.put("charSet", "Big5");
			    prop.put("user", mUserName);
			    prop.put("password", mPwd);
//			    DriverManager.setLogWriter(new PrintWriter(System.out));
			    mConn = DriverManager.getConnection(mUrl, prop);
			}else{
				mConn = DriverManager.getConnection(mUrl, mUserName, mPwd);
			}



			mConn.setCatalog(mDb);  //
			logger.info("Connected to " + mConn.getCatalog());
			mStmt = mConn.createStatement();
			this.connExist = true;
			return true;
		}catch (SQLException e){
            logger.error("Problems when trying to open the database !!!\n" +
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

    	if (isCursor) {
            selectMethod = SELECT_CURSOR;
        } else {
            selectMethod = SELECT_DIRECT;
        }
	    try{
	        if (DEBUG_LOG_WRITER) {
                DriverManager.setLogWriter((new PrintWriter(System.out)));
            } else if (mDbms.equalsIgnoreCase(SQL_SERVER_2005)) {
	            Class.forName(clsSQLServer2005);
	            server = mServer + ":" + mPort;
	            mUrl = urlSQLServer2005 + server + ";databaseName=" + mDb +";SelectMethod="+ selectMethod;
	        }
	        else if (mDbms.equalsIgnoreCase(SQL_SERVER_2008)) {
	            Class.forName(clsSQLServer2008);
	            server = mServer + ":" + mPort;
	            mUrl = urlSQLServer2008 + server + ";databaseName=" + mDb +";SelectMethod="+ selectMethod;
	        }
	        else if (mDbms.equalsIgnoreCase(ACCESS)) {
	        	Class.forName(clsODBC);

	        	//mDb must be the file path
	        	mUrl = urlODBC + "Driver={Microsoft Access Driver (*.mdb)};DBQ=";
	        	mUrl += mDb.trim() + ";DriverID=22;READONLY=false}";
	        }
	        else if (mDbms.equalsIgnoreCase(EXCEL)) {
	            Class.forName(clsODBC);
	            mUrl = urlODBC + "jdbc:odbc:Driver={Microsoft Excel Driver (*.xls)};DBQ=";
	            mUrl += mDb.trim() + ";DriverID=22;READONLY=false";
	        }
	        else if (mDbms.equalsIgnoreCase(ODDBC)) {
	            //mDb must be the System DNS name
	        	Class.forName(clsODBC);
	            mUrl = urlODBC + mDb ;
	        }
	        else if (mDbms.equalsIgnoreCase(ORACLE)) {
	            Class.forName(clsOracle);
	            mUrl = urlOracle + mDb ;
	        }
	        else if (mDbms.equalsIgnoreCase(DB2)) {
	            Class.forName(clsDB2);
	            mUrl = urlDB2 + mDb;
	        }
	        else if (mDbms.equalsIgnoreCase("SQLServerDdtek")) {
	             Class.forName(clsSQLServerDdtek);
	             mUrl = urlDataDirectSQLServer + mServer;
	        }
	        else if (mDbms.equalsIgnoreCase(POSTGRESQL9)) {
	            Class.forName(clsPostgreSQL);
	            server = mServer + ":" + mPort;
	            mUrl = urlPostgreSQL + server+ "/" + mDb;
	        }
	        else {
	            throw new RuntimeException("Unsupported Database type");
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


    public boolean isDoLog() {
		return doLog;
	}

	public void setDoLog(boolean doLog) {
		this.doLog = doLog;
	}

    /**
     * Checks if an attribute exists in the database schema. At the moment only supported
     * for SQL Server.
     * TODO implement for others.
     * @param tableName
     * @param dbAttribute
     * @return
     * @throws MethodNotSupportedException
     */
    public boolean checkColumnExists(String tableName, String dbAttribute) throws DatabaseTypeNotSupportedException{
    	if (mDbms.equalsIgnoreCase(SQL_SERVER_2005)|| mDbms.equalsIgnoreCase(SQL_SERVER_2008) ){
    		String strQuery = "SELECT  Count(t.id) as n " +
				" FROM sysobjects AS t " +
				" INNER JOIN syscolumns AS c ON t.id = c.id " +
				" WHERE (t.xtype = 'U') AND " +
				" (t.name = '" + tableName + "') AND " +
				" (c.name = '" + dbAttribute + "')";
			ResultSet rs = getResultSet(strQuery) ;
			int n;
			try {
				rs.next();
				n = rs.getInt("n");
				return n>0;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
    	}else{
    		throw new DatabaseTypeNotSupportedException("Check column exist is not supported by the database system");
    	}
    }

    @Override
    public String toString(){
    	if (mDb != null){
    		return mDb;
    	}else if (mUrl == null){
    		return super.toString();
    	}else{
        	return mUrl;
    	}
    }


}