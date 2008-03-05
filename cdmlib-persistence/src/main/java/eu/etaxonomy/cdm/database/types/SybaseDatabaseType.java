/**
 * 
 */
package eu.etaxonomy.cdm.database.types;


/**
 * !! UNTESTED !!
 * @author a.mueller
 *
 */
public class SybaseDatabaseType extends DatabaseTypeBase {

	//typeName
	protected String typeName = "Sybase";
	//class
	protected String classString = "com.sybase.jdbc2.jdbc.SybDriver";
	//url
    protected String urlString = "jdbc:sybase:Tds:";
    //port
    private int defaultPort = 4100;
    //hibernate dialect
    private String hibernateDialect = "SybaseDialect";

    
    //connection String
	public String getConnectionString(String server, String database, int port){
        return urlString + server + ":" + port + "/" + database;
    }  
    
    public SybaseDatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
	}




}
