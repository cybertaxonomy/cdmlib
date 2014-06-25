// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v33_34;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.v31_33.SchemaUpdater_33_331;

/**
 * @author a.mueller
 * @created Jan 14, 2014
 */
public class SchemaUpdater_331_34 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_331_34.class);
	private static final String startSchemaVersion = "3.3.1.0.201401140000";
	private static final String endSchemaVersion = "3.4.0.0.201407010000";

	// ********************** FACTORY METHOD
	// *******************************************

	public static SchemaUpdater_331_34 NewInstance() {
		return new SchemaUpdater_331_34();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_331_34() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		ISchemaUpdaterStep step;
		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();

		//TODO test and PostGreSQL / SQL Server
		//UserAccount unique
		stepName = "Update User unique indexes";
		tableName = "UserAccount";
		columnName = "username";
		step = UsernameConstraintUpdater.NewInstance(stepName, tableName, columnName);
		stepList.add(step);
		
		//TODO test and PostGreSQL / SQL Server
		//PermissionGroup unique
		stepName = "Update Group unique indexes";
		tableName = "PermissionGroup";
		columnName = "name";
		step = UsernameConstraintUpdater.NewInstance(stepName, tableName, columnName);
		stepList.add(step);
		
		//TODO test and PostGreSQL / SQL Server
		//GrantedAuthority unique
		stepName = "Update User unique indexes";
		tableName = "GrantedAuthorityImpl";
		columnName = "authority";
		step = UsernameConstraintUpdater.NewInstance(stepName, tableName, columnName);
		stepList.add(step);
		
		//TODO test
		stepName = "Add label column to derived unit";
		tableName = "SpecimenOrObservationBase";
		columnName = "originalLabelInfo";
		step = ColumnAdder.NewClobInstance(stepName, tableName, columnName, INCLUDE_AUDIT);
		stepList.add(step);
		
			
		//TODO test
		//set default value to true where required
		stepName = "Set publish to true if null";
		String query = " UPDATE @@TaxonBase@@ " +
					" SET publish = @TRUE@ " + 
					" WHERE DTYPE IN ('Synonym') AND publish IS NULL ";
		step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, "TaxonBase", 99);
		stepList.add(step);
		
		
		return stepList;

	}

	@Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_33_331.NewInstance();
	}

}
