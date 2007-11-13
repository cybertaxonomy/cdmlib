/**
 * 
 */
package eu.etaxonomy.cdm.database;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.types.HSqlDbDatabaseType;
import eu.etaxonomy.cdm.database.types.IDatabaseType;
import eu.etaxonomy.cdm.database.types.MySQLDatabaseType;
import eu.etaxonomy.cdm.database.types.SqlServerDatabaseType;

/**
 * @author a.mueller
 *
 */
public enum DatabaseEnum {
	MySQL(1), 
	HSqlDb(2),
	SqlServer(3)
	;

	/**
	 * Constructor
	 * @param i
	 */
	private DatabaseEnum(int i) {
		if (i == 1){
			this.dbType = new MySQLDatabaseType();
		}else if (i == 2){
			this.dbType = new HSqlDbDatabaseType();
		}else if (i == 3){
			this.dbType = new SqlServerDatabaseType();
		}
	}
	
	
	//Logger
	private static final Logger logger = Logger.getLogger(DatabaseEnum.class);
	protected IDatabaseType dbType;
	
	public String getDriverClassName(){
		return dbType.getClassString();
	}
	
	public String getUrl(){
		return dbType.getUrlString();
	}
	   
    private int getDefaultPort(){
    	return dbType.getDefaultPort();
    }

	/**
     * returns the connection string 
     * @param server the server, e.g. IP-Address
     * @param database the database name on the server (e.g. "testDB")
     * @param port the port number
     * @return the connection string
     */
    public String getConnectionString(String server, String database, int port){
    	String result = dbType.getConnectionString(server, database, port);
    	logger.debug("Connection String: " + result);	
        return result;
    }
    

	/**
     * returns the connection string (using the default port)
     * @param server the server, e.g. IP-Address
     * @param database the database name on the server (e.g. "testDB")
      * @return the connection string
     */
    public String getConnectionString(String server, String database){
    	String result = dbType.getConnectionString(server, database);
    	logger.debug("Connection String: " + result);	
        return result;
    }
 
	
	
}

