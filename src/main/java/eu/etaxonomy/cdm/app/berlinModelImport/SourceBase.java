// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.app.berlinModelImport;

import eu.etaxonomy.cdm.common.AccountStore;
import eu.etaxonomy.cdm.io.common.Source;

/**
 * @author a.mueller
 * @date 14.11.2012
 *
 */
public abstract class SourceBase {

	
	/**
	 * Initializes the source.
	 * @param dbms
	 * @param strServer
	 * @param strDB
	 * @param port
	 * @param userName
	 * @param pwd
	 * @return the source
	 */
	public static Source makeSource(String dbms, String strServer, String strDB, int port, String userName, String pwd ){
		//establish connection
		Source source = null;
		source = new Source(dbms, strServer, strDB);
		source.setPort(port);
			
		pwd = AccountStore.readOrStorePassword(dbms, strServer, userName, pwd);
		source.setUserAndPwd(userName, pwd);
		// write pwd to account store
		return source;
	}
}
