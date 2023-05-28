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
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableCreator;
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
		stepName = "Make term relation annotatable";
		tableName = "TermRelationBase";
		TableCreator.makeMnTables(stepList, tableName, true, false);

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

		return stepList;
    }
}