/**
 * 
 */
package eu.etaxonomy.cdm.database;

/**
 * @author a.mueller
 *
 */
public enum DbType {
	MySQL(), 
	HsSqlDb(),
	SqlServer()
	;
	
	
    //driver class
	private static String clsMySQL = "com.mysql.jdbc.Driver";
    private static String clsHSqlDb = "org.hsqldb.jdbcDriver";
    private static String clsSQLServer = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    private static String clsODBC = "sun.jdbc.odbc.JdbcOdbcDriver";
    private static String clsOracle = "oracle.jdbc.driver.OracleDriver";
    private static String clsDB2 = "COM.ibm.db2.jdbc.net.DB2Driver";
    private static String clsSQLServerDdtek = "com.ddtek.jdbc.sqlserver.SQLServerDriver";
    
    //url
    private static String urlSQLServer = "jdbc:microsoft:sqlserver://";
    private static String urlDB2 = "jdbc:db2://";
    private static String urlOracle = "jdbc:oracle:thin:@:1243:";
    private static String urlDataDirectSQLServer = "jdbc:datadirect:sqlserver://";
    private static String urlODBC = "jdbc:odbc:";
    private static String urlDefault = "jdbc:microsoft:sqlserver://LAPI:1433;DatabaseName=studienarbeit;SelectMethod=direct";
	
	


	
	public String getDriverClassName(){
		if (this == this.MySQL){
			return clsMySQL;
		}else if (this == this.HsSqlDb){
			return clsHSqlDb;
		}else if (this == this.SqlServer){
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
     * Makes the connection string 
     * @return false if ClassNotFoundException, else true
     */
    private boolean makeConnectionString(){
    	String selectMethod; 
        String server;
        
    	try{
	        if (this == this.SqlServer) {
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
	
	
	
}

