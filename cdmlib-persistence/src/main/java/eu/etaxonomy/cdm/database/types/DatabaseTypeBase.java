/**
 * 
 */
package eu.etaxonomy.cdm.database.types;

import org.springframework.jdbc.datasource.DriverManagerDataSource;


/**
 * @author a.mueller
 *
 */
abstract class DatabaseTypeBase implements IDatabaseType {
	//typeName
	private String typeName;
	//String for DriverClass
	private String classString;
	//url
	private String urlString;
	//defaultPort
	private int defaultPort;
	//hibernate dialect
	private String hibernateDialect;
	
	//init
	protected void init(String typeName, String classString,
			String urlString, int defaultPort, String hibernateDialect) {
		this.typeName = typeName;
		this.classString = classString;
		this.urlString = urlString;
		this.defaultPort = defaultPort;
		this.hibernateDialect = hibernateDialect;
	}
	
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
	 * @see eu.etaxonomy.cdm.database.IDatabaseType#getHibernateDialect()
	 */
	public String getHibernateDialect(){
		return "org.hibernate.dialect." + hibernateDialect;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.IDatabaseType#getConnectionString(java.lang.String, java.lang.String)
	 */
	public String getConnectionString(String server, String database){
		return getConnectionString(server, database, defaultPort);
    }

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getDriverManagerDataSourceClass()
	 */
	public Class<? extends DriverManagerDataSource> getDriverManagerDataSourceClass() {
		return DriverManagerDataSource.class;
	} 
	
	
}
