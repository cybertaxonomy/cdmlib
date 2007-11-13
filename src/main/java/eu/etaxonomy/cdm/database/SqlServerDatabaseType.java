/**
 * 
 */
package eu.etaxonomy.cdm.database;

/**
 * @author a.mueller
 *
 */
class SqlServerDatabaseType extends AbstractDatabaseType {

	//typeName
	protected String typeName = "SQL Server";
   
	//class
	protected String classString = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    
	//url
    protected String urlString = "jdbc:microsoft:sqlserver://";
    
    //port
    private int defaultPort = 1433;
    
    //connection String
	public String getConnectionString(String server, String database, int port){
		return urlString + server + ":" + port + ";databaseName=" + database+";SelectMethod=cursor";
    }
	
  
 
    
    public SqlServerDatabaseType() {
		super();
		super.typeName = typeName;
		super.classString = classString;
		super.urlString = urlString;
		super.defaultPort = defaultPort;
	}




}
