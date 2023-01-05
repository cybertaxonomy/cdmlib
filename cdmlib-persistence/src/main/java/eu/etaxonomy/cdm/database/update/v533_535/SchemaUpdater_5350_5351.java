/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v533_535;

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
 * @date 30.09.2022
 */
public class SchemaUpdater_5350_5351 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_35_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_35_01;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5350_5351 NewInstance() {
		return new SchemaUpdater_5350_5351();
	}

	protected SchemaUpdater_5350_5351() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5330_5350.NewInstance();
    }

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#10194 Add specimen to DescriptionElementSource
		stepName = "Add usePlural to StateData";
		String tableName = "StateData";
		String newColumnName = "usePlural";
		ColumnAdder.NewBooleanInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL);

 		return stepList;
    }
}