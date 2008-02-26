/**
 * 
 */
package eu.etaxonomy.cdm.database.types;


/**
 * @author a.mueller
 *
 */
public class SqlServer2000DatabaseType extends AbstractDatabaseType {

	//name
	protected String typeName = "SQL Server";
	
	//driver class
	protected String classString = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    
	//url
	protected String urlString = "jdbc:microsoft:sqlserver://";
    
    //default port
    private int defaultPort = 1433;
    
    //hibernate dialect
    private String hibernateDialect = "SQLServerDialect";
 
    public String getConnectionString(String server, String database, int port){
		return urlString + server + ":" + port + ";databaseName=" + database+";SelectMethod=cursor";
    }
	
	//Constructor
    public SqlServer2000DatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}

}