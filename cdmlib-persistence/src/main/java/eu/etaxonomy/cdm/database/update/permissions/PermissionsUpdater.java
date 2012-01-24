package eu.etaxonomy.cdm.database.update.permissions;

import java.sql.SQLException;
import java.util.List;

import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.v30_31.LanguageLabelUpdater;

public class PermissionsUpdater extends SchemaUpdaterStepBase implements
		ISchemaUpdaterStep {

	private static final String stepName = "Update granted authorities and grantedAuthorities_useraccount";
	
	public static final PermissionsUpdater NewInstance(){
		return new PermissionsUpdater(stepName);	
	}

	protected PermissionsUpdater(String stepName) {
		super(stepName);
	}
	
	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor)
			throws SQLException {
		String sql;
		
		//insert admin into useraccount if not available
		sql = "INSERT INTO useraccount "+
		"(id, uuid, accountnonexpired, accountnonlocked, credentialsnonexpired, enabled, password, username) "+
		"SELECT (SELECT MAX(id)+1 FROM useraccount), '2b78fd58-1179-4e93-a8cb-ff5d2ba50e07', 1, 1, 1, 1, '6d54445d1b1cdc44e668a1e07ee4ab4a', 'admin2' "+
		"FROM useraccount "+
		"WHERE (SELECT COUNT(*) FROM useraccount WHERE username LIKE 'admin')=0";
		
		datasource.executeUpdate(sql);
		
		//insert granted authorities
		
		sql = "INSERT INTO grantedauthorityimpl (id,uuid, authority) VALUES (1,'889f9961-8d0f-41a9-95ec-59905b3941bf', 'TAXONBASE.ADMIN')";
		datasource.executeUpdate(sql);
		
		sql = "INSERT INTO grantedauthorityimpl (id,uuid, authority) VALUES (2,'841a1711-20f1-4209-82df-7944ad2050da', 'TAXONNODE.ADMIN')";
		datasource.executeUpdate(sql);
		
		sql = "INSERT INTO grantedauthorityimpl (id,uuid, authority) VALUES (3,'bb9e2547-1e28-45fd-8c35-d1ceffbfcb36', 'DESCRIPTIONBASE.ADMIN')";
		datasource.executeUpdate(sql);
		
		sql = "INSERT INTO grantedauthorityimpl (id,uuid, authority) VALUES (6,'8e3a8b36-1c9e-43cb-9887-83b117cf7566', 'CLASSIFICATION.UPDATE')";
		datasource.executeUpdate(sql);
		
		sql = "INSERT INTO grantedauthorityimpl (id,uuid, authority) VALUES (4,'8a61c102-4643-4e81-a3b6-c40d60d2ba99', 'USER.ADMIN')";
		datasource.executeUpdate(sql);
		
		sql = "INSERT INTO grantedauthorityimpl (id,uuid, authority) VALUES (5,'216a56bf-cd1a-43ff-9cb1-039c823c38af', 'ALL.ADMIN')";
		datasource.executeUpdate(sql);
		
		//insert useraccount_grantedauthority for user admin
		
		/*sql = "INSERT INTO useraccount_grantedauthorityimpl (UserAccount_id, grantedauthorities_id) VALUES ((SELECT id FROM useraccount WHERE username LIKE 'admin'), 4)";
		datasource.executeUpdate(sql);
		
		sql = "INSERT INTO useraccount_grantedauthorityimpl (UserAccount_id, grantedauthorities_id) VALUES ((SELECT id FROM useraccount WHERE username LIKE 'admin'), 3)";
		datasource.executeUpdate(sql);
		
		sql = "INSERT INTO useraccount_grantedauthorityimpl (UserAccount_id, grantedauthorities_id) VALUES ((SELECT id FROM useraccount WHERE username LIKE 'admin'), 2)";
		datasource.executeUpdate(sql);
		
		sql = "INSERT INTO useraccount_grantedauthorityimpl (UserAccount_id, grantedauthorities_id) VALUES ((SELECT id FROM useraccount WHERE username LIKE 'admin'), 1)";
		datasource.executeUpdate(sql);
		*/
		sql = "INSERT INTO useraccount_grantedauthorityimpl (UserAccount_id, grantedauthorities_id) VALUES ((SELECT id FROM useraccount WHERE username LIKE 'admin'), 5)";
		datasource.executeUpdate(sql);
		
		sql = "INSERT INTO permissiongroup (id, created, uuid, name) VALUES (1, NOW(), '6f9238e1-c6cb-4bdf-8495-ccbe32c655f0', 'taxon editor')";
		datasource.executeUpdate(sql);
		
		sql = "INSERT INTO permissiongroup_grantedauthorityimpl (PermissionGroup_id, grantedauthorities_id) VALUES (1,1)";
		datasource.executeUpdate(sql);
		
		sql = "INSERT INTO permissiongroup_grantedauthorityimpl (PermissionGroup_id, grantedauthorities_id) VALUES (1,2)";
		datasource.executeUpdate(sql);
		
		sql = "INSERT INTO permissiongroup_grantedauthorityimpl (PermissionGroup_id, grantedauthorities_id) VALUES (1,3)";
		datasource.executeUpdate(sql);
		
		sql = "INSERT INTO permissiongroup_grantedauthorityimpl (PermissionGroup_id, grantedauthorities_id) VALUES (1,6)";
		datasource.executeUpdate(sql);
		
		
		
		return null;
	}

	

}
