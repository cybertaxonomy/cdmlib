/**
 * 
 */
package eu.etaxonomy.cdm.database.types;


/**
 * TODO not tested yet !!
 * 
 * @author a.mueller
 *
 */
public class OdbcDatabaseType extends AbstractDatabaseType {

	//typeName
	protected String typeName = "ODBC";
   
	//class
	protected String classString = "sun.jdbc.odbc.JdbcOdbcDriver";
    
	//url
    protected String urlString = "jdbc:odbc:";
    
    //port
    private int defaultPort = 0;
    
    //hibernate dialect
    //TODO
    private String hibernateDialect = "xxx";
    
    //connection String
	public String getConnectionString(String server, String database, int port){
        return urlString + server ;
    }  
	
	//constructor
    public OdbcDatabaseType() {
    	init (typeName, classString, urlString, defaultPort,  hibernateDialect );
    }




}
