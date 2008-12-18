/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.types.IDatabaseType;

/**
 * @author a.mueller
 * @created 18.12.2008
 * @version 1.0
 */
abstract class CdmDataSourceBase implements ICdmDataSource {
	private static final Logger logger = Logger.getLogger(CdmDataSourceBase.class);
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.ICdmDataSource#testConnection()
	 */
	public boolean testConnection() {
		try {
			IDatabaseType dbType = getDatabaseType().getDatabaseType();
			String classString = dbType.getClassString();
			Class.forName(classString);
			
			String mUrl = dbType.getConnectionString(this);
			Connection mConn = DriverManager.getConnection(mUrl, getUserName(), getPassword());
			if (mConn != null){
				return true;
			}else{
				return false;
			}
		} catch (Exception e) {
			logger.warn(e.getMessage());
			return false;
		}
	}
}
