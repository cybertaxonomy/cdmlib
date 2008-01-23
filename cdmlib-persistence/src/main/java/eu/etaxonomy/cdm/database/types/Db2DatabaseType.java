/**
 * 
 */
package eu.etaxonomy.cdm.database.types;


/**
 * !! UNTESTED !!
 * Db2 in use with Universal Driver (db2jcc.jar)
 * 
 * @author a.mueller
 *
 */
public class Db2DatabaseType extends AbstractDatabaseType {

	//typeName
	protected String typeName = "DB2 Universal";
	//class
	protected String classString = "com.ibm.db2.jcc.DB2Driver"; 
	//protected String classString = "COM.ibm.db2.jdbc.app.DB2Driver";
	//protected String classString = "COM.ibm.db2.jdbc.net.DB2Driver";
	
	//url
    protected String urlString = "jdbc:db2://";
    //port
    private int defaultPort = 50000;
    //hibernate dialect
    private String hibernateDialect = "DB2Dialect";

    
    //connection String
	public String getConnectionString(String server, String database, int port){
        return urlString + server + ":" + port + database;
    }  
    
    public Db2DatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}




}
