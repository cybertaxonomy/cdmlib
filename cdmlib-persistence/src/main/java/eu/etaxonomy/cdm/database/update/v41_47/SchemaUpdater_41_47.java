/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v41_47;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.MnTableCreator;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.v40_41.SchemaUpdater_40_41;

/**
 * @author a.mueller
 * @created 16.04.2016
 */
public class SchemaUpdater_41_47 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_41_47.class);
	private static final String startSchemaVersion = "4.1.0.0.201607300000";
	private static final String endSchemaVersion = "4.7.0.0.201710040000";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_41_47 NewInstance() {
		return new SchemaUpdater_41_47();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_41_47() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		ISchemaUpdaterStep step;
		String newColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#6529
		//Extend WorkingSet to allow a more fine grained definiton of taxon set
		//min rank
        stepName = "Add minRank column";
        tableName = "WorkingSet";
        newColumnName = "minRank_id";
        String referencedTable = "DefinedTermBase";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
        stepList.add(step);

        //max rank
        stepName = "Add maxRank column";
        tableName = "WorkingSet";
        newColumnName = "maxRank_id";
        referencedTable = "DefinedTermBase";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);
        stepList.add(step);

        //subtree filter
        stepName= "Add geo filter MN table to WorkingSet";
        String firstTableName = "WorkingSet";
        String secondTableName = "NamedArea";
        String secondTableAlias = "geoFilter";
        boolean hasSortIndex = false;
        boolean secondTableInKey = true;
        step = MnTableCreator.NewMnInstance(stepName, firstTableName, null, secondTableName, secondTableAlias, INCLUDE_AUDIT, hasSortIndex, secondTableInKey);
        stepList.add(step);

        //subtree filter
        stepName= "Add subtree filter MN table to WorkingSet";
        firstTableName = "WorkingSet";
        secondTableName = "TaxonNode";
        secondTableAlias = "taxonSubtreeFilter";
        hasSortIndex = false;
        secondTableInKey = true;
        step = MnTableCreator.NewMnInstance(stepName, firstTableName, null, secondTableName, secondTableAlias, INCLUDE_AUDIT, hasSortIndex, secondTableInKey);
        stepList.add(step);

        //#6258
        stepName = "Add Registration table";
        tableName = "Registration";
        String[] columnNames = new String[]{"identifier","specificIdentifier","registrationDate","status",
                "institution_id","name_id","submitter_id"};
        String[] referencedTables = new String[]{null, null, null, null,
                "AgentBase","TaxonNameBase","User"};
        String[] columnTypes = new String[]{"string_255","string_255","datetime","string_255","int","int","int"};
        step = TableCreator.NewAnnotatableInstance(stepName, tableName,
                columnNames, columnTypes, referencedTables, INCLUDE_AUDIT);
        stepList.add(step);

        //add blockedBy_id
        stepName= "Add blockedBy_id to Registration";
        firstTableName = "Registration";
        secondTableName = "Registration";
        secondTableAlias = "blockedBy";
        hasSortIndex = false;
        secondTableInKey = true;
        step = MnTableCreator.NewMnInstance(stepName, firstTableName, null, secondTableName, secondTableAlias, INCLUDE_AUDIT, hasSortIndex, secondTableInKey);
        stepList.add(step);

        //add type designations
        stepName= "Add type designations to Registration";
        firstTableName = "Registration";
        secondTableName = "TypeDesignationBase";
        secondTableAlias = "typeDesignations";
        hasSortIndex = false;
        secondTableInKey = true;
        step = MnTableCreator.NewMnInstance(stepName, firstTableName, null, secondTableName, secondTableAlias, INCLUDE_AUDIT, hasSortIndex, secondTableInKey);
        stepList.add(step);

        //#5258
        //Add "accessed" to Reference
        stepName = "Add 'accessed' to Reference";
        tableName = "Reference";
        newColumnName = "accessed";
        step = ColumnAdder.NewDateTimeInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL);
        stepList.add(step);

        return stepList;
    }


    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_40_41.NewInstance();
	}

}
