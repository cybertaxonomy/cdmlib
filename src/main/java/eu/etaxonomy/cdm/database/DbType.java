/**
 * 
 */
package eu.etaxonomy.cdm.database;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 *
 */
public enum DbType {
	MySQL(), 
	HSqlDb(),
	SqlServer()
	;
	
	//Logger
	private static final Logger logger = Logger.getLogger(DbType.class);
	
    //driver class
	private static String clsMySQL = "com.mysql.jdbc.Driver";
    private static String clsHSqlDb = "org.hsqldb.jdbcDriver";
    private static String clsSQLServer = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    private static String clsODBC = "sun.jdbc.odbc.JdbcOdbcDriver";
    private static String clsOracle = "oracle.jdbc.driver.OracleDriver";
    private static String clsDB2 = "COM.ibm.db2.jdbc.net.DB2Driver";
    
    //url
    private static String urlSQLServer = "jdbc:microsoft:sqlserver://";
    private static String urlMySQL = "jdbc:mysql://";
    private static String urlHSqlDb = "jdbc:hsqldb:hsql://";
    private static String urlDB2 = "jdbc:db2://";
    private static String urlOracle = "jdbc:oracle:thin:@:1243:";
    private static String urlODBC = "jdbc:odbc:";
	
    //port
    private static String portSQLServer = "jdbc:microsoft:sqlserver://";
    private static String portMySQL = "jdbc:mysql://";
    private static String portHSqlDb = "jdbc:hsqldb:hsql://";
    


	
	public String getDriverClassName(){
		if (this == MySQL){
			return clsMySQL;
		}else if (this == HSqlDb){
			return clsHSqlDb;
		}else if (this == SqlServer){
			return clsSQLServer;
		}else{
			//TODO Exception
		}
		return null;
	}
	
	public String getUrl(){
		//TODO 
		return null;
	}
	
    /**
     * returns the connection string 
     * @param server the server, e.g. IP-Address
     * @param database the database name on the server (e.g. "testDB")
     * @param port the port number (0 for default port)
     * @return the connection string
     */
    public String getConnectionString(String server, String database, int port){
    	String result;
    	String fullServerString;
    	
    	//get port
    	if (port == 0){
			port = getDefaultPort();
		}
		
    	//switch servertypes
    	if (this == SqlServer) {
    		fullServerString = server + ":" + port;
            result = urlSQLServer + fullServerString + ";DataBase=" + database+";SelectMethod=cursor";
        }else if (this == MySQL) {
            server = server + ":" + port + "/" + database; 
        	result = urlMySQL + server + "/" + database;
        }else if (this == HSqlDb) {
             result = this.urlHSqlDb + ":" + port +  server + "/" + database; 
        }else {
        	logger.error("unknown database type");
        	result = null; 
        }
        logger.debug("Connection String: " + result);	
        return result;
    }
    
    private int getDefaultPort(){
    	if (this == SqlServer) {
    		return 1433;
    	}else if (this == MySQL) {
        	return  3306;
        }else if (this == HSqlDb) {
            return 9001;
        }else {
            return 0; 
        }
    }
	
	
	
}

