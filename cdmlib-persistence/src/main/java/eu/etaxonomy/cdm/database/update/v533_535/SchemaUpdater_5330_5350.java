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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SingleTermRemover;
import eu.etaxonomy.cdm.database.update.v532_533.SchemaUpdater_5320_5330;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 30.09.2022
 */
public class SchemaUpdater_5330_5350 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_33_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_35_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5330_5350 NewInstance() {
		return new SchemaUpdater_5330_5350();
	}

	protected SchemaUpdater_5330_5350() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5320_5330.NewInstance();
    }

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#10194 Add specimen to DescriptionElementSource
		stepName = "Add specimen to DescriptionElementSource";
		String tableName = "OriginalSourceBase";
		String newColumnName = "specimen_id";
		String referencedTable = "SpecimenOrObservationBase";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //#10201 change synonym type to enum
		stepName = "Add TaxonBase.type";
		tableName = "TaxonBase";
		newColumnName = "type";
		ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

		SynonymTypeChanger.NewInstance(stepList);

		stepName = "Remove TaxonBase.type_id";
        tableName = "TaxonBase";
        String columnName = "type_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        //Remove terms
        stepName = "Remove synonym type terms";
        Map<UUID,String> map = new HashMap<>();
        map.put(UUID.fromString("1afa5429-095a-48da-8877-836fa4fe709e"), "SYN");
        map.put(UUID.fromString("294313a9-5617-4ed5-ae2d-c57599907cb2"), "HOM");
        map.put(UUID.fromString("4c1e2c59-ca55-41ac-9a82-676894976084"), "HET");
        map.put(UUID.fromString("cb5bad12-9dbc-4b38-9977-162e45089c11"), "INS");
        map.put(UUID.fromString("f55a574b-c1de-45cc-9ade-1aa2e098c3b5"), "ING");
        map.put(UUID.fromString("089c1926-eb36-47e7-a2d1-fd5f3918713d"), "INE");
        map.put(UUID.fromString("7c45871f-6dc5-40e7-9f26-228318d0f63a"), "POT");
        for (UUID uuid : map.keySet()) {
            SingleTermRemover.NewInstance(stepList, stepName, uuid.toString(), (String)null);
            SingleTermRemover.NewAudInstance(stepList, stepName, uuid.toString(), null);
        }

		return stepList;
    }
}