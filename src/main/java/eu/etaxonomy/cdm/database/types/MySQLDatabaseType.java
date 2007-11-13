/**
 * 
 */
package eu.etaxonomy.cdm.database.types;


/**
 * @author a.mueller
 *
 */
public class MySQLDatabaseType extends AbstractDatabaseType {

	//typeName
	protected String typeName = "MySQL";
   
	//class
	protected String classString = "com.mysql.jdbc.Driver";
    
	//url
    protected String urlString = "jdbc:mysql://";
    
    //port
    private int defaultPort = 3306;
    
    //connection String
	public String getConnectionString(String server, String database, int port){
        return urlString + server + ":" + port + "/" + database;
    }  
    
    public MySQLDatabaseType() {
		super();
		super.typeName = typeName;
		super.classString = classString;
		super.urlString = urlString;
		super.defaultPort = defaultPort;
	}




}
