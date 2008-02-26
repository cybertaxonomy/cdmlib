/**
 * 
 */
package eu.etaxonomy.cdm.database.types;


/**
 * @author a.mueller
 *
 */
public class PostgreSQLDatabaseType extends AbstractDatabaseType {

	//typeName
	protected String typeName = "PostgreSQL";
	//class
	protected String classString = "org.postgresql.Driver";
	//url
    protected String urlString = "jdbc:postgresql://";
    //port
    private int defaultPort = 5432;
    //hibernate dialect
    private String hibernateDialect = "PostgreSQLDialect";

    
    //connection String
	public String getConnectionString(String server, String database, int port){
        return urlString + server + ":" + port + "/" + database;
    }  
    
    public PostgreSQLDatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}




}
