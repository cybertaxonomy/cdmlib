/**
 * 
 */
package eu.etaxonomy.cdm.database;

/**
 * @author a.mueller
 *
 */
public enum DbType {
	MySQL(), 
	HsSqlDb(),
	SqlServer()
	;
	
	private String typeStrings;
	
	
	DbType(){
		if (this == this.MySQL){
			typeStrings = "";
		}
	}
	
	private class MySQLdata{
		String name = "MySQL";
		String url = "";
	}

	
	private class HsSqlDb{
		String name = "MySQL";
		String url = "";
	}

	
	public String getDriverClass(){
		//TODO
		return null;
	}
	
	public String getUrl(){
		//TODO 
		return null;
	}
	
	
	
}

