/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.types;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.ICdmDataSource;


/**
 * @author a.mueller
 *
 */
abstract class DatabaseTypeBase implements IDatabaseType {
	private static final Logger logger = Logger.getLogger(DatabaseTypeBase.class);
	
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
	//init method
	private String initMethod = null;
	//init method
	private String destroyMethod = null;
	
	
	//init
	protected void init(String typeName, String classString, String urlString, int defaultPort, String hibernateDialect) {
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
	public String getConnectionString(ICdmDataSource cdmDataSource){
		int port = cdmDataSource.getPort();
		if (port< 1){
			port = defaultPort;
		}
		return getConnectionString(cdmDataSource, port);
    }
	
	abstract protected String getConnectionString(ICdmDataSource cdmDataSource, int port);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getDriverManagerDataSourceClass()
	 */
	public Class<? extends DriverManagerDataSource> getDriverManagerDataSourceClass() {
		return DriverManagerDataSource.class;
	} 
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getInitMethod()
	 */
	public String getInitMethod() {
		return initMethod;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.types.IDatabaseType#getDestroyMethod()
	 */
	public String getDestroyMethod() {
		return destroyMethod;
	}
}
