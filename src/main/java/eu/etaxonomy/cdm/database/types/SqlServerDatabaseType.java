/**
 * 
 */
package eu.etaxonomy.cdm.database.types;


/**
 * @author a.mueller
 *
 */
public class SqlServerDatabaseType extends AbstractDatabaseType {

	protected String typeName = "SQL Server";
	protected String classString = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
    protected String urlString = "jdbc:microsoft:sqlserver://";
    private int defaultPort = 1433;
	public String getConnectionString(String server, String database, int port){
		return urlString + server + ":" + port + ";databaseName=" + database+";SelectMethod=cursor";
    }
	
	//Constructor
    public SqlServerDatabaseType() {
		super();
		super.typeName = typeName;
		super.classString = classString;
		super.urlString = urlString;
		super.defaultPort = defaultPort;
	}

}