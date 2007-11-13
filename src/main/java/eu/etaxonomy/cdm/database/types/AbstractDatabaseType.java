/**
 * 
 */
package eu.etaxonomy.cdm.database.types;


/**
 * @author a.mueller
 *
 */
abstract class AbstractDatabaseType implements IDatabaseType {
	//typeName
	protected String typeName;
	//String for DriverClass
	protected String classString;
	//url
	protected String urlString;
	//defaultPort
	protected int defaultPort;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.IDatabaseType#getName()
	 */
	public String getName(){
		return typeName;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.IDatabaseType#getClassString()
	 */
	public String getClassString(){
		return classString;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.IDatabaseType#getUrlString()
	 */
	public String getUrlString(){
		return urlString;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.IDatabaseType#getDefaultPort()
	 */
	public int getDefaultPort(){
		return defaultPort;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.IDatabaseType#getConnectionString(java.lang.String, java.lang.String)
	 */
	public String getConnectionString(String server, String database){
		return getConnectionString(server, database, defaultPort);
    } 
	
	
}
