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
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableNameChanger;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 2024-10-17
 */
public class SchemaUpdater_5461_5480 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_46_01;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_48_00;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5460_5461.NewInstance();
    }

	public static SchemaUpdater_5461_5480 NewInstance() {
		return new SchemaUpdater_5461_5480();
	}

	SchemaUpdater_5461_5480() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#10612
		//Set nomenclatural standing to NO for nom. rej.
        stepName = "Set nomenclatural standing to NO for nom. rej.";
        String nonAuditedTableName = "DefinedTermBase";
        String defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET nomenclaturalStanding = 'NO' "
                + " WHERE uuid IN ( "
                + "     '48107cc8-7a5b-482e-b438-efbba050b851'"   //status nom. rej.
                + "    ) ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName);

        //#10614 Update label for language Spanish
        stepName = "Update label for language Spanish";
        UUID uuidSpanish = UUID.fromString("511d8125-f5e6-445d-aee2-6327375238be");
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidSpanish,
                "Spanish", "Spanish", null, UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523"));

        //#10558 Update label for country Turkey -> Türkiye
        stepName = "Update label for country Turkey -> Türkiye";
        UUID uuidTurkey = UUID.fromString("f7c15c55-d0b3-4eda-8961-582d5071df78");
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTurkey,
                "Republic of Türkiye", "Türkiye", null, UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523"));

        //#10558 Update label for TDWG 3 area Turkey -> Türkiye
        stepName = "Update label for TDWG 3 area Turkey -> Türkiye";
        UUID uuidTurkeyTDWG = UUID.fromString("48219cbc-82ab-447f-8a67-e97408736c23");
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTurkeyTDWG,
                "Türkiye", "Türkiye", null, UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523"));

        //#10558 Update label for TDWG 4 area Turkey -> Türkiye
        stepName = "Update label for TDWG 3 area Turkey -> Türkiye";
        UUID uuidTurkeyTDWG4 = UUID.fromString("60a9219e-136e-4ac1-92a3-1b889e473c53");
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTurkeyTDWG4,
                "Türkiye", "Türkiye", null, UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523"));

        //#10558 Update label for TDWG 3 area Turkey -> Türkiye
        stepName = "Update label for TDWG 3 area Turkey -> Türkiye";
        UUID uuidTurkeyEuropeTDWG = UUID.fromString("0cc0f22f-df09-48d2-a2e0-27911df17c8b");
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTurkeyEuropeTDWG,
                "Türkiye-in-Europe", "Türkiye-in-Europe", null, UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523"));

        //#10558 Update label for TDWG 4 area Turkey -> Türkiye
        stepName = "Update label for TDWG 4 area Turkey -> Türkiye";
        UUID uuidTurkeyEuropeTDWG4 = UUID.fromString("b7ea03d2-a7f0-44bf-995f-c5a0e352480a");
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTurkeyEuropeTDWG4,
                "Türkiye-in-Europe", "Türkiye-in-Europe", null, UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523"));

        //#10401 Update label for Sample Size
        stepName = "Update label for Sample Size";
        UUID uuidTerm = UUID.fromString("571f86ca-a44c-4484-9981-11fd82138a7a");
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                "Sample Size", "Sample Size", null, UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523"));

        //#10401 Update label for Typical Lower Boundary
        stepName = "Update label for Typical Lower Boundary";
        uuidTerm = UUID.fromString("8372a89a-35ad-4755-a881-7edae6c37c8f");
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                "Typical Lower Boundary", "Typical Lower Boundary", null, UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523"));

        //#10401 Update label for Typical Upper Boundary
        stepName = "Update label for Typical Upper Boundary";
        uuidTerm = UUID.fromString("9eff88ba-b8e7-4631-9e55-a50bd16ba79d");
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                "Typical Upper Boundary", "Typical Upper Boundary", null, UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523"));

        //#10401 Update label for Standard Deviation
        stepName = "Update label for Standard Deviation";
        uuidTerm = UUID.fromString("9ee4397e-3496-4fe1-9114-afc7d7bdc652");
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                "Standard Deviation", "Standard Deviation", null, UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523"));

        //#10593
        //Set URL pattern for ORCID
        stepName = "Set URL pattern for ORCID";
        String sql = "UPDATE @@DefinedTermBase@@ "
                + " SET urlPattern = 'https://orcid.org/{@ID}'"
                + " WHERE uuid = 'fb1764f5-843b-414c-b9e7-d3802e408823'";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, sql);

        //#10100
        //Rename statusNote into placementNote in TaxonNode
        stepName = "Rename statusNote into placementNote in TaxonNode";
        String oldName = "TaxonNode_StatusNote";
        String newName = "TaxonNode_PlacementNote";
        TableNameChanger.NewInstance(stepList, stepName, oldName, newName, INCLUDE_AUDIT);

        //#10100
        //Rename statusNote_id column in renamed table TaxonNode_StatusNote
        stepName = "Rename statusNote_id column in renamed table TaxonNode_StatusNote";
        tableName = "TaxonNode_PlacementNote";
        String oldColumnName = "statusNote_id";
        String newColumnName = "placementNote_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //#10100
        //Rename statusNote_KEY column in renamed table TaxonNode_StatusNote
        stepName = "Rename statusNote_KEY column in renamed table TaxonNode_StatusNote";
        tableName = "TaxonNode_PlacementNote";
        oldColumnName = "statusNote_KEY";
        newColumnName = "placementNote_KEY";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        return stepList;
    }
}