/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.config;

import java.sql.SQLException;

import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

public interface ICdmSource {
	
	/**
	 * The name representation of thie Datasource.
	 * @return
	 */
	public String getName();
	
	/**
	 * @return
	 */
	public String getServer();
	
	/**
	 * @return
	 */
	public int getPort();
		
	
	public NomenclaturalCode getNomenclaturalCode();
	
	public String getDbSchemaVersion() throws CdmSourceException;
	
	public boolean isDbEmpty() throws CdmSourceException;
	
	/**
	 * Tests, if a database connection can be established.
	 * @return true if test was successful, false otherwise
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws Exception 
	 */
	public boolean checkConnection() throws CdmSourceException;
	
	public String getConnectionMessage();

	public void closeOpenConnections();

}
