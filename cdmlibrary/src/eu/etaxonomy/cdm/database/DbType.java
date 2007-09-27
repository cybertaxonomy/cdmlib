/**
 * 
 */
package eu.etaxonomy.cdm.database;

/**
 * @author a.mueller
 *
 */
public enum DbType {
	MySQL, 
	HsSqlDb,
	SqlServer
	;
	
	public String getDriverClass(){
		//TODO
		return null;
	}
	
	public String getUrl(){
		//TODO 
		return null;
	}
	
	
	
}
