/**
 * 
 */
package eu.etaxonomy.cdm.database.types;


/**
 * @author a.mueller
 *
 */
public class HSqlDbDatabaseType extends AbstractDatabaseType {

	//typeName
	private String typeName = "Hypersonic SQL DB (HSqlDb)";
   
	//class
	private String classString = "org.hsqldb.jdbcDriver";
    
	//url
	private String urlString = "jdbc:hsqldb:hsql://";
    
    //port
    private int defaultPort = 9001;
    
    //hibernate dialect
    private String hibernateDialect = "HSQLDialect";
    
    
    //connection String
	public String getConnectionString(String server, String database, int port){
        return urlString + server + ":" + port + "/" + database;
    }
	
    
    public HSqlDbDatabaseType() {
		init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}




}
