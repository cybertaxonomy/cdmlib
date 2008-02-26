/**
 * 
 */
package eu.etaxonomy.cdm.database;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.types.HSqlDbDatabaseType;
import eu.etaxonomy.cdm.database.types.IDatabaseType;
import eu.etaxonomy.cdm.database.types.MySQLDatabaseType;
import eu.etaxonomy.cdm.database.types.OdbcDatabaseType;
import eu.etaxonomy.cdm.database.types.PostgreSQLDatabaseType;
import eu.etaxonomy.cdm.database.types.SqlServerDatabaseType;

/**
 * @author a.mueller
 *
 */
public enum DatabaseTypeEnum {
	HSqlDb(1),
	MySQL(2),
	ODBC(3),
	PostgreSQL(4),
	SqlServer(5)
	;

	/**
	 * Constructor
	 * @param i
	 */
	private DatabaseTypeEnum(int i) {
		switch(i)
        {
        	case 1:
        		this.dbType = new HSqlDbDatabaseType(); break;
        	case 2:
        		this.dbType = new MySQLDatabaseType(); break;
        	case 3:
        		this.dbType = new OdbcDatabaseType(); break;
        	case 4:
            	this.dbType = new PostgreSQLDatabaseType(); break;
            case 5:
            	this.dbType = new SqlServerDatabaseType(); break;
            default:
                //TODO Exception
        }
	}
	
 	//Logger
	private static final Logger logger = Logger.getLogger(DatabaseTypeEnum.class);
	protected IDatabaseType dbType;
	
	   
    /**
     * @return
     */
    public String getName(){
    	return dbType.getName();
    }
    
	/**
	 * @return
	 */
	public String getDriverClassName(){
		return dbType.getClassString();
	}
	
	/**
	 * @return
	 */
	public String getUrl(){
		return dbType.getUrlString();
	}
	
	/**
	 * @return
	 */
	public String getHibernateDialect(){
		return dbType.getHibernateDialect();
	}
	   
    /**
     * @return
     */
    public int getDefaultPort(){
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
    
    /**
     * Returns a List of all available DatabaseEnums.
     * @return List of DatabaseEnums
     */
    public static List<DatabaseTypeEnum> getAllTypes(){
    	List<DatabaseTypeEnum> result = new ArrayList<DatabaseTypeEnum>();
    	for (DatabaseTypeEnum dbEnum : DatabaseTypeEnum.values()){
    		result.add(dbEnum);
    	}
    	return result;
    }

    /**
     * Returns the DatabaseTypeEnum to a given DriverClass
     * @param strDriverClass
     * @return the according DatabaseTypeEnum. Null if the driver class does not exist.
     */
    public static DatabaseTypeEnum getDatabaseEnumByDriverClass(String strDriverClass){
    	for (DatabaseTypeEnum dbEnum : DatabaseTypeEnum.values()){
    		if (dbEnum.getDriverClassName().equals(strDriverClass)){
    			return dbEnum;
    		}
    	}
    	logger.warn("Unknown driver class " + strDriverClass==null ? "null" : strDriverClass);
    	return null;
    }
 
}

