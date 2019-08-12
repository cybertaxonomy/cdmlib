/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v55_58;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableCreator;

/**
 * @author a.mueller
 * @date 02.08.2019
 */
public class SchemaUpdater_58_581 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_58_581.class);

	private static final String endSchemaVersion = "5.8.1.0.20190813";
	private static final String startSchemaVersion = "5.8.0.0.201906020";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_58_581 NewInstance() {
		return new SchemaUpdater_58_581();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_58_581() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;
		String oldColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#8429
		stepName = "Rename ";
		tableName = "DeterminationEvent_Reference";
		oldColumnName = "setOfReferences_id";
		newColumnName = "references_id";
		ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName,
		        oldColumnName, newColumnName, INCLUDE_AUDIT);

		//7099 Make CdmAuthority a persistable class - create 'Authority' table
		stepName = "Create Authority table ";
        tableName = "Authority";
        String[] columnNames = new String[]{"DTYPE", "operations", "permissionClass",
                "property", "targetUuid", "role"};
        String[] columnTypes = new String[]{"string_255","string_255","string_255","string_255",
                "string_36","string_255"};
        String[] referencedTables = new String[]{null, null, null, null, null, null};
        TableCreator.NewNonVersionableInstance(stepList, stepName, tableName,
                columnNames, columnTypes, referencedTables);

        //7099 Make CdmAuthority a persistable class - create PermissionGroup_Authority table
        stepName = "Create PermissionGroup_Authority table ";
        tableName = "PermissionGroup_Authority";
        columnNames = new String[]{"Group_id", "authorities_id"};
        columnTypes = new String[]{"int","int"};
        referencedTables = new String[]{"PermissionGroup", "Authority"};
        TableCreator.NewNonVersionableInstance(stepList, stepName, tableName,
                columnNames, columnTypes, referencedTables);

        //7099 Make CdmAuthority a persistable class - create UserAccount_Authority table
        stepName = "Create UserAccount_Authority table ";
        tableName = "UserAccount_Authority";
        columnNames = new String[]{"User_id", "authorities_id"};
        columnTypes = new String[]{"int","int"};
        referencedTables = new String[]{"UserAccount", "Authority"};
        TableCreator.NewNonVersionableInstance(stepList, stepName, tableName,
                columnNames, columnTypes, referencedTables);

    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_55_58.NewInstance();
	}
}
