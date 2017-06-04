/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.permissions;

import java.sql.SQLException;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.config.Configuration;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * @author k.luther
 *
 */
public class PermissionsUpdater extends SchemaUpdaterStepBase  {

	private static final String stepName = "Update granted authorities and grantedAuthorities_useraccount";

	public static final PermissionsUpdater NewInstance(){
		return new PermissionsUpdater(stepName);
	}

	protected PermissionsUpdater(String stepName) {
		super(stepName);
	}

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

        String sql;

		//insert admin into useraccount if not available
		sql = "INSERT INTO @@UserAccount@@ "+
					"(id, uuid, accountnonexpired, accountnonlocked, credentialsnonexpired, enabled, password, username) "+
				" SELECT (SELECT MAX(id)+1 FROM @@UserAccount@@), '2b78fd58-1179-4e93-a8cb-ff5d2ba50e07', 1, 1, 1, 1, '6d54445d1b1cdc44e668a1e07ee4ab4a', 'admin2' "+
				" FROM @@UserAccount@@ "+
				" WHERE (SELECT COUNT(*) FROM @@UserAccount@@ WHERE username LIKE '" + Configuration.adminLogin + "')=0";

		datasource.executeUpdate(caseType.replaceTableNames(sql));

		//insert granted authorities

		sql = "INSERT INTO @@GrantedAuthorityImpl@@ (id,uuid, authority) VALUES (1,'889f9961-8d0f-41a9-95ec-59905b3941bf', 'USER.EDIT')";
		datasource.executeUpdate(caseType.replaceTableNames(sql));

		sql = "INSERT INTO @@GrantedAuthorityImpl@@ (id,uuid, authority) VALUES (2,'841a1711-20f1-4209-82df-7944ad2050da', 'USER.CREATE')";
		datasource.executeUpdate(caseType.replaceTableNames(sql));

		sql = "INSERT INTO @@GrantedAuthorityImpl@@ (id,uuid, authority) VALUES (3,'bb9e2547-1e28-45fd-8c35-d1ceffbfcb36', 'USER.DELETE')";
		datasource.executeUpdate(caseType.replaceTableNames(sql));

		sql = "INSERT INTO @@GrantedAuthorityImpl@@ (id,uuid, authority) VALUES (4,'8a61c102-4643-4e81-a3b6-c40d60d2ba99', 'USER.ADMIN')";
		datasource.executeUpdate(caseType.replaceTableNames(sql));

		sql = "INSERT INTO @@GrantedAuthorityImpl@@ (id,uuid, authority) VALUES (5,'216a56bf-cd1a-43ff-9cb1-039c823c38af', 'ALL.ADMIN')";
		datasource.executeUpdate(caseType.replaceTableNames(sql));

		//insert useraccount_grantedauthority for user admin

		/*sql = "INSERT INTO useraccount_grantedauthorityimpl (UserAccount_id, grantedauthorities_id) VALUES ((SELECT id FROM useraccount WHERE username LIKE '" + Configuration.adminLogin + "'), 4)";
		datasource.executeUpdate(sql);

		sql = "INSERT INTO useraccount_grantedauthorityimpl (UserAccount_id, grantedauthorities_id) VALUES ((SELECT id FROM useraccount WHERE username LIKE '" + Configuration.adminLogin + "'), 3)";
		datasource.executeUpdate(sql);

		sql = "INSERT INTO useraccount_grantedauthorityimpl (UserAccount_id, grantedauthorities_id) VALUES ((SELECT id FROM useraccount WHERE username LIKE '" + Configuration.adminLogin + "'), 2)";
		datasource.executeUpdate(sql);

		sql = "INSERT INTO useraccount_grantedauthorityimpl (UserAccount_id, grantedauthorities_id) VALUES ((SELECT id FROM useraccount WHERE username LIKE '" + Configuration.adminLogin + "'), 1)";
		datasource.executeUpdate(sql);
		*/
		sql = "INSERT INTO @@UserAccount_GrantedAuthorityImpl@@ (UserAccount_id, grantedauthorities_id) VALUES ((SELECT id FROM @@UserAccount@@ WHERE username LIKE '" + Configuration.adminLogin + "'), 5)";
		datasource.executeUpdate(caseType.replaceTableNames(sql));

		return;
	}



}
