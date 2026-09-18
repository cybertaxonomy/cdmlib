/**
 * Copyright (C) 2024 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v55x_60x;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.v540_558.SchemaUpdater_5540_5580;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 2024-10-17
 */
public class SchemaUpdater_5580_6010 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_58_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_06_01_00;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5540_5580.NewInstance();
    }

	public static SchemaUpdater_5580_6010 NewInstance() {
		return new SchemaUpdater_5580_6010();
	}

	SchemaUpdater_5580_6010() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#10922
        stepName = "Add gender to taxon name";
        String tableName = "TaxonName";
        String columnName = "gender";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName, 1, INCLUDE_AUDIT);


        return stepList;
    }
}