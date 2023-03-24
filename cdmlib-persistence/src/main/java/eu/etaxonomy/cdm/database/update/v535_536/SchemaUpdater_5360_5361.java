/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v535_536;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 06.01.2023
 */
public class SchemaUpdater_5360_5361 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_36_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_36_01;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5360_5361 NewInstance() {
		return new SchemaUpdater_5360_5361();
	}

	protected SchemaUpdater_5360_5361() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5351_5360.NewInstance();
    }

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String columnName;
		String sql;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//10286
		stepName = "Add publisher2 to Reference";
		tableName = "Reference";
		columnName = "publisher2";
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

		stepName = "Add placePublished2 to Reference";
        tableName = "Reference";
        columnName = "placePublished2";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

		return stepList;
    }
}