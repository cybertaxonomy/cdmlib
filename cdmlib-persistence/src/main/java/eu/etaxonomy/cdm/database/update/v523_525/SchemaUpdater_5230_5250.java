/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v523_525;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnTypeChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.v522_523.SchemaUpdater_5220_5230;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 22.04.2021
 */
public class SchemaUpdater_5230_5250 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5230_5250.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_23_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_25_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5230_5250 NewInstance() {
		return new SchemaUpdater_5230_5250();
	}

	protected SchemaUpdater_5230_5250() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		stepName = "Add collectorTitleCache to AgentBase";
		tableName = "AgentBase";
		newColumnName = "collectorTitleCache";
		int length = 800;
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

//		CollectorTitleUpdater.NewInstance(stepList); => moved to SchemaUpdater_5250_5251

		//#9664 #4311
		stepName = "Add nomenclaturalTitleCache to AgentBase";
		tableName = "AgentBase";
		newColumnName = "nomenclaturalTitleCache";
		length = 800;
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

		NomenclaturalTitleUpdater.NewInstance(stepList);

        //#9634
		stepName = "Increase size for AgentBase.titleCache to 800";
		tableName = "AgentBase";
		String columnName = "titleCache";
		length = 800;
		ColumnTypeChanger.NewInt2StringInstance(stepList, stepName, tableName, columnName, length, INCLUDE_AUDIT, null, !NOT_NULL);

		stepName = "Increase size for DescriptiveDataSet.titleCache to 800";
		tableName = "DescriptiveDataSet";
		columnName = "titleCache";
		length = 800;
		ColumnTypeChanger.NewInt2StringInstance(stepList, stepName, tableName, columnName, length, INCLUDE_AUDIT, null, !NOT_NULL);

        return stepList;
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5220_5230.NewInstance();
    }
}