/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v538_540;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.v536_538.SchemaUpdater_5361_5380;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 27.06.2023
 */
public class SchemaUpdater_5380_5400 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_38_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_40_00;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5361_5380.NewInstance();
    }

	public static SchemaUpdater_5380_5400 NewInstance() {
		return new SchemaUpdater_5380_5400();
	}

	protected SchemaUpdater_5380_5400() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}


    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#2975 add noDataStatus column for descriptive data
		stepName = "Add noDataStatus column for descriptive data";
		tableName = "DescriptionElementBase";
		columnName = "noDataStatus";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName, 10, INCLUDE_AUDIT);

		return stepList;
    }
}