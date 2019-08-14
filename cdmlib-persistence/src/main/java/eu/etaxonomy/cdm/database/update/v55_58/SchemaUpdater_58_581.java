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

import eu.etaxonomy.cdm.database.update.ColumnAdder;
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
		stepName = "Rename DeterminationEvent.setOfReferences";
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

        //#8442
        addExternalDataHandler(stepList, "DefinedTermBase");

        //#8442
        addExternalDataHandler(stepList, "TermCollection");

        //#8442 / #6663
        stepName = "Add importMethod to Reference";
        tableName = "Reference";
        newColumnName = "importMethod";
        int length = 30;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        //
        UsernameRegexAdapter.NewInstance(stepList);

        return stepList;
    }

    /**
     * @param stepList
     * @param string
     */
    private void addExternalDataHandler(List<ISchemaUpdaterStep> stepList, String tableName) {
        //Add "lastRetrieved"
        String stepName = "Add 'lastRetrieved' to " + tableName;
        String newColumnName = "lastRetrieved";
        ColumnAdder.NewDateTimeInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL);

        stepName = "Add externalId to DefinedTermBase";
        newColumnName = "externalId";
        int length = 255;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        stepName = "Add externalLink to DefinedTermBase";
        newColumnName = "externalLink";
        ColumnAdder.NewClobInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        stepName = "Add authorityType to DefinedTermBase";
        newColumnName = "authorityType";
        length = 10;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        stepName = "Add importMethod to DefinedTermBase";
        newColumnName = "importMethod";
        length = 30;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return SchemaUpdater_581_582.NewInstance();
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_55_58.NewInstance();
	}
}
