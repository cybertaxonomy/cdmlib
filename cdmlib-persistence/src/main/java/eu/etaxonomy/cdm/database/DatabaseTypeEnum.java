/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;

import eu.etaxonomy.cdm.database.types.H2DatabaseType;
import eu.etaxonomy.cdm.database.types.IDatabaseType;
import eu.etaxonomy.cdm.database.types.MariaDbDatabaseType;
import eu.etaxonomy.cdm.database.types.MySQLDatabaseType;
import eu.etaxonomy.cdm.database.types.OdbcDatabaseType;
import eu.etaxonomy.cdm.database.types.OracleDatabaseType;
import eu.etaxonomy.cdm.database.types.PostgreSQLDatabaseType;
import eu.etaxonomy.cdm.database.types.SqlServer2005DatabaseType;
import eu.etaxonomy.cdm.database.types.SqlServer2008DatabaseType;
import eu.etaxonomy.cdm.database.types.SqlServer2012DatabaseType;
import eu.etaxonomy.cdm.database.types.SybaseDatabaseType;

/**
 * @author a.mueller
 */
public enum DatabaseTypeEnum {
	MySQL(1),
	ODBC(2),
	PostgreSQL(3),
	Oracle(4),
	//SqlServer2000(5),
	SqlServer2005(6),
	Sybase(7),
	H2(8),
	SqlServer2008(9),
    SqlServer2012(10),
    MariaDB(11),  //not yet tested
    ;

    private static final Logger logger = LogManager.getLogger();

//	/**
//	 *
//	 */
//	private static final String P6SPY_DRIVER_CLASS_NAME = "com.p6spy.engine.spy.P6SpyDriver";

	private DatabaseTypeEnum(int i) {
		switch(i)
        {
        	case 1:
        		this.dbType = new MySQLDatabaseType(); break;
        	case 2:
        		this.dbType = new OdbcDatabaseType(); break;
        	case 3:
            	this.dbType = new PostgreSQLDatabaseType(); break;
        	case 4:
             	this.dbType = new OracleDatabaseType(); break;
            case 6:
            	this.dbType = new SqlServer2005DatabaseType(); break;
            case 7:
            	this.dbType = new SybaseDatabaseType(); break;
            case 8:
            	this.dbType = new H2DatabaseType(); break;
            case 9:
                this.dbType = new SqlServer2008DatabaseType(); break;
            case 10:
                this.dbType = new SqlServer2012DatabaseType(); break;
            case 11:
                this.dbType = new MariaDbDatabaseType(); break;
            default:
                throw new RuntimeException("Database type not handled");
        }
	}

	public IDatabaseType getDatabaseType(){
		return dbType;
	}

	protected IDatabaseType dbType;

    public String getName(){
    	return dbType.getName();
    }

	public String getDriverClassName(){
//		if(useP6Spy){
//			return P6SPY_DRIVER_CLASS_NAME;
//
//		} else {
			return dbType.getClassString();
//		}
	}

	/**
	 * Returns the DataSource class that the datasource needs to create a spring bean
	 * @return the DataSource class
	 */
	public Class<? extends DataSource> getDataSourceClass(){
		return dbType.getDataSourceClass();
	}

	public String getUrl(){
		return dbType.getUrlString();
	}

	public String getHibernateDialectCanonicalName(){
		return dbType.getHibernateDialectCanonicalName();
	}

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
     * Returns the {@link Dialect hibernate dialect} used for this database type.
	 * @return hibernate dialect
	 */
    public Dialect getHibernateDialect(){
    	Dialect result = dbType.getHibernateDialect();
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
    	List<DatabaseTypeEnum> result = new ArrayList<>();
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
    public static DatabaseTypeEnum byDriverClass(String strDriverClass){
    	if (strDriverClass == null){
    	    return null;
    	}
        for (DatabaseTypeEnum dbEnum : DatabaseTypeEnum.values()){
    		if (dbEnum.getDriverClassName().equals(strDriverClass)){
    			return dbEnum;
    		}
    	}
    	logger.info("Unknown driver class: " + strDriverClass);
    	return null;
    }

    public static DatabaseTypeEnum byDatabaseMetaData(DatabaseMetaData metaData) {
        if (metaData == null){
            return null;
        }

        //driver
        String driver = null;
        try {
            driver = metaData.getDriverName();
        } catch (SQLException e) {
            //do nothing
        }
        DatabaseTypeEnum result = byDriverClass(driver);
        if (result != null){
            return result;
        }

        //product
        String product = null;
        try {
            product = metaData.getDatabaseProductName();
        } catch (SQLException e) {
            //do nothing
        }
        if (product == null){
            return null;
        }
        if (product.toLowerCase().matches("\\.*mysql\\.*")){
            return MySQL;
        }else if (product.toLowerCase().matches("\\.*mariadb\\.*")) {
            return MariaDB;
        }else if (product.toLowerCase().matches("\\.*oracle\\.*")) {
            return Oracle;
        }else if (product.toLowerCase().matches("\\.*sybase\\.*")) {
            return Sybase;
        }else if (product.toLowerCase().matches("\\.*odbc\\.*")) {
            return ODBC;
        }else if (product.toLowerCase().matches("\\.*postgresql\\.*")) {
            return PostgreSQL;
        }else if (product.toLowerCase().matches("\\.*sqlserver\\.*")) {
            //TODO we need to distinguish versions here once we have sql server 2008 database enum
//            metaData.getDatabaseProductVersion()
            return SqlServer2005;
            //XX
        }else if (product.toLowerCase().matches("\\.*h2\\.*")) {
            return H2;
        }
        return null;
    }

    /**
     * Returns the database type evaluating the connection string.
     * To retrieves parts of the connection string (e.g. database name),
     * get the database type from the enum and call e.g.
     * type.getDatabaseType().getDatabaseNameByConnectionString(url);
     *
     * @param url the connection string
     * @return the database type
     */
    public static DatabaseTypeEnum byConnectionString(String url) {
        if (url == null){
            return null;
        }
        for (DatabaseTypeEnum type : values()){
            if (url.startsWith(type.getUrl())){
                type.getDatabaseType().getDatabaseNameByConnectionString(url);
                return type;
            }
        }
        return null;
    }
}