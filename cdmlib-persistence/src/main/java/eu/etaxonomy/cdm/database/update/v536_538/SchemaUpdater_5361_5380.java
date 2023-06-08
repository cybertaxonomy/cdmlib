/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v536_538;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.MnTableCreator;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.v535_536.SchemaUpdater_5360_5361;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 10.05.2023
 */
public class SchemaUpdater_5361_5380 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_36_01;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_38_00;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5360_5361.NewInstance();
    }

	public static SchemaUpdater_5361_5380 NewInstance() {
		return new SchemaUpdater_5361_5380();
	}

	protected SchemaUpdater_5361_5380() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}


    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#10320 make term relation annotatable
		stepName = "Make term relation annotatable (Annotation)";
		tableName = "TermRelation";
		columnName = "TermRelationBase";
		String secondTableName = "Annotation";
//		TableCreator.makeMnTables(stepList, tableName, true, false);
		MnTableCreator.NewMnInstance(stepList, stepName, tableName, tableName, columnName,
		        secondTableName, secondTableName, secondTableName, SchemaUpdaterBase.INCLUDE_AUDIT, !IS_LIST, IS_1_TO_M);

		//#10320 ... for markers
        stepName = "Make term relation annotatable (Marker)";
        tableName = "TermRelation";
        columnName = "TermRelationBase";
        secondTableName = "Marker";
        MnTableCreator.NewMnInstance(stepList, stepName, tableName, tableName, columnName,
                secondTableName, secondTableName, secondTableName, SchemaUpdaterBase.INCLUDE_AUDIT, !IS_LIST, IS_1_TO_M);


		//#10328 add maxPerDataset
		stepName = "Add maxPerDataset column to Feature";
		tableName = "DefinedTermBase";
		columnName = "maxPerDataset";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT, null, !NOT_NULL);

	    //#10328 add maxStates
        stepName = "Add maxStates column to Feature";
        tableName = "DefinedTermBase";
        columnName = "maxStates";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT, null, !NOT_NULL);

        //#10206 add publish flag to description base
        stepName = "Add publish flag to description base";
        tableName = "DescriptionBase";
        columnName = "publish";
        ColumnAdder.NewBooleanInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT, true);

        //#10311 make misspelling and emendation assymetric
        stepName = "Make misspelling and emendation assymetric";
        String sql = "UPDATE DefinedTermBase "
                + " SET symmetrical = 0 "
                + " WHERE UUID IN ('6e23ad45-3f2a-462b-ad87-d2389cd6e26c', 'c6f9afcb-8287-4a2b-a6f6-4da3a073d5de')";
        tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);


		return stepList;
    }
}