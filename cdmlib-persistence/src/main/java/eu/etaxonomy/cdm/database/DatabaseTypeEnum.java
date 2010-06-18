/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.types.H2DatabaseType;
import eu.etaxonomy.cdm.database.types.HSqlDbDatabaseType;
import eu.etaxonomy.cdm.database.types.IDatabaseType;
import eu.etaxonomy.cdm.database.types.MySQLDatabaseType;
import eu.etaxonomy.cdm.database.types.OdbcDatabaseType;
import eu.etaxonomy.cdm.database.types.OracleDatabaseType;
import eu.etaxonomy.cdm.database.types.PostgreSQLDatabaseType;
import eu.etaxonomy.cdm.database.types.SqlServer2005DatabaseType;
import eu.etaxonomy.cdm.database.types.SybaseDatabaseType;

/**
 * @author a.mueller
 *
 */
public enum DatabaseTypeEnum {
	HSqlDb(1),
	MySQL(2),
	ODBC(3),
	PostgreSQL(4),
	Oracle(5),
	//SqlServer2000(6),
	SqlServer2005(7),
	Sybase(8),
	H2(9)
	;

	/**
	 * 
	 */
	private static final String P6SPY_DRIVER_CLASS_NAME = "com.p6spy.engine.spy.P6SpyDriver";
	private boolean useP6Spy = false;
	
	
	/**
	 * @return the useP6Spy
	 */
	public boolean isUseP6Spy() {
		return useP6Spy;
	}

	/**
	 * @param useP6Spy the useP6Spy to set
	 */
	public void setUseP6Spy(boolean useP6Spy) {
		this.useP6Spy = useP6Spy;
	}

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
             	this.dbType = new OracleDatabaseType(); break;
//            case 6:
//            	this.dbType = new SqlServer2000DatabaseType(); break;
            case 7:
            	this.dbType = new SqlServer2005DatabaseType(); break;
            case 8:
            	this.dbType = new SybaseDatabaseType(); break;
            case 9:
            	this.dbType = new H2DatabaseType(); break;
            default:
                //TODO Exception
        }
	}
	
	public IDatabaseType getDatabaseType(){
		return dbType;
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
		if(useP6Spy){
			return P6SPY_DRIVER_CLASS_NAME;
			
		} else {
			return dbType.getClassString();			
		}
	}
    
	/**
	 * Returns the DataSource class that that the datasource needs to create a spring bean
	 * @return the DataSource class
	 */
	public Class<? extends DataSource> getDataSourceClass(){
		return dbType.getDataSourceClass();
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
    public String getConnectionString(ICdmDataSource cdmDataSource){
    	String result = dbType.getConnectionString(cdmDataSource);
    	logger.debug("Connection String: " + result);	
        return result;
    }
    


	/**
     * Returns the Name of the initialization method to be used when a hibernate datasource is created for this database
	 * @return String name of the init method
	 */
    public String getInitMethod(){
    	String result = dbType.getInitMethod();
    	logger.debug("InitMethod: " + result);	
        return result;
    }
    
	/**
	 * Returns the Name of the destroying method to be used when a hibernate datasource representing this database is destroyed
	 * @return String name of the destroy method
	 */
    public String getDestroyMethod(){
    	String result = dbType.getDestroyMethod();
    	logger.debug("DestroyMethod: " + result);	
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

