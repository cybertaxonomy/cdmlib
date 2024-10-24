/**
 * Copyright (C) 2024 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v54x_54x;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnValueUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 2024-10-09
 */
public class SchemaUpdater_5440_5460 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_44_00;
	//TODO
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_46_00;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5431_5440.NewInstance();
    }

	public static SchemaUpdater_5440_5460 NewInstance() {
		return new SchemaUpdater_5440_5460();
	}

	SchemaUpdater_5440_5460() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#9539 update availableFor for identifier types
		stepName = "update availableFor for identifier types";
		tableName = "DefinedTermBase";
		columnName = "availableFor";
		ColumnValueUpdater.NewStringInstance(stepList, stepName, tableName,
		        columnName, "#", "DTYPE='IdentifierType'", INCLUDE_AUDIT);
        ColumnValueUpdater.NewStringInstance(stepList, stepName, tableName,
                columnName, "#TNA#",
                //wfo-name, tropicos, ipni, IF, plant list, sileneae name id
                "uuid='048e0cf9-f59c-42dd-bfeb-3a5cba0191c7' OR uuid='6205e531-75b0-4f2a-9a9c-b1247fb080ab'"
                + " OR uuid = '009a602f-0ff6-4231-93db-f458e8229aca' OR uuid = 'f405be9f-359a-49ba-b09b-4a7920386190'"
                + " OR uuid = '06e4c3bd-7bf6-447a-b96e-2844b279f276' OR uuid = '95ecbf6d-521d-447f-bae5-d82585ff3617' ", INCLUDE_AUDIT);
        ColumnValueUpdater.NewStringInstance(stepList, stepName, tableName,
                //sample designation, alternative filed number, catalogue number, accession number, barcode
                columnName, "#OCC#", "uuid='fadeba12-1be3-4bc7-9ff5-361b088d86fc' OR "
                        + " uuid = '054fd3d1-1961-42f8-b024-b91184ac9e0c' OR "
                        + " uuid = 'ab402e4b-93a0-4311-a85a-696c1498b67d' OR "
                        + " uuid = 'ea7ef3c4-0ff5-4b1c-9264-b0f86dc00b61' OR "
                        + " uuid = 'e0c3b15f-d779-4d94-91f0-7d83888576b2' ", INCLUDE_AUDIT);
        ColumnValueUpdater.NewStringInstance(stepList, stepName, tableName,
                //LSID
                columnName, "#TAX#TNA#OCC#REF#PER#", "uuid='26729412-9df6-4cc3-9e5d-501531ca21f0'", INCLUDE_AUDIT);


        ColumnValueUpdater.NewStringInstance(stepList, stepName, tableName,
                //MCL Identifier, taxNr + reihenfolge (RL), GermanSL letter code, Koperski taxon id, Florein ID (Standardliste)
                columnName, "#TAX#", "uuid='c6873fc6-9bf7-4e78-b4f2-5ab0c0dc1f50' OR "
                        + " uuid = '7d12de50-0db7-47b3-bb8e-703ad1d54fbc' OR "
                        + " uuid = '97961851-b1c1-41fb-adfd-2961b48f7efe' OR "
                        + " uuid = '99b907df-c932-4007-96e9-b6a0d5e1f3bf' OR "
                        + " uuid = '31f3154c-c4a4-4519-a8c9-1057e9c08015' OR "
                        + " uuid = '8b67291e-96e0-4556-8d6a-c94e8750b301' ", INCLUDE_AUDIT);
        ColumnValueUpdater.NewStringInstance(stepList, stepName, tableName,
                //E+M Reference Source Number, DOI
                columnName, "#REF#", "uuid='06b02bbd-bf22-485c-9fd1-fad9175f0d53' OR uuid = '664eb28f-d9b6-42f1-b8fd-b3748784e0a4'", INCLUDE_AUDIT);
        ColumnValueUpdater.NewStringInstance(stepList, stepName, tableName,
                //ORCID, IPNI Author, IPNI, WIKIdata ID (Phycobank)
                columnName, "#PER#", "uuid='fb1764f5-843b-414c-b9e7-d3802e408823' OR"
                        + " uuid = '13888fc1-8eb3-4c6f-a4dc-9f36c4eaf4c3' OR"
                        + " uuid= '3891cad4-751d-464d-9367-73ba8b4e42bd' OR "
                        + " uuid = '2a1099ab-607b-40b4-8210-f336bc0e074c' ", INCLUDE_AUDIT);

        return stepList;
    }
}