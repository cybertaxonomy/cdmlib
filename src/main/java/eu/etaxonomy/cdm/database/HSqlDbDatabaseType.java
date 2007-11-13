/**
 * 
 */
package eu.etaxonomy.cdm.database;

/**
 * @author a.mueller
 *
 */
class HSqlDbDatabaseType extends AbstractDatabaseType {

	//typeName
	protected String typeName = "Hypersonic SQL DB (HSqlDb)";
   
	//class
	protected String classString = "org.hsqldb.jdbcDriver";
    
	//url
    protected String urlString = "jdbc:hsqldb:hsql://";
    
    //port
    private int defaultPort = 9001;
    
    //connection String
	public String getConnectionString(String server, String database, int port){
        return urlString + server + ":" + port + "/" + database;
    }
	
    
    public HSqlDbDatabaseType() {
		super();
		super.typeName = typeName;
		super.classString = classString;
		super.urlString = urlString;
		super.defaultPort = defaultPort;
	}




}
