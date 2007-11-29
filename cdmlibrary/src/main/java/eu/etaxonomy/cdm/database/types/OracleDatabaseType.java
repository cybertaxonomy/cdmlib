/**
 * 
 */
package eu.etaxonomy.cdm.database.types;


/**
 * @author a.mueller
 *
 */
public class OracleDatabaseType extends AbstractDatabaseType {

	//typeName
	protected String typeName = "Oracle";
	//class
	protected String classString = "oracle.jdbc.driver.OracleDriver";
	//url
    protected String urlString = "jdbc:oracle:thin:@";
    //port
    private int defaultPort = 1521;
    //hibernate dialect
    private String hibernateDialect = "org.hibernate.dialect.OracleDialect";

    
    //connection String
	public String getConnectionString(String server, String database, int port){
        return urlString + server + ":" + port + ":" + database;
    }  
    
    public OracleDatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}




}
