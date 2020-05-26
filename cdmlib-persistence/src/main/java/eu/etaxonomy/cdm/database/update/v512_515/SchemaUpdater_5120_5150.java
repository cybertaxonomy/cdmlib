/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v512_515;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.Float2BigDecimalTypeChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableNameChanger;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.database.update.v511_512.SchemaUpdater_5112_5120;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 02.12.2019
 */
public class SchemaUpdater_5120_5150 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5120_5150.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_12_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_15_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5120_5150 NewInstance() {
		return new SchemaUpdater_5120_5150();
	}

	protected SchemaUpdater_5120_5150() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;
		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

	    //#8964 update label for later homonym
        stepName = "update label for later homonym";
        UUID uuidTerm = UUID.fromString("80f06f65-58e0-4209-b811-cb40ad7220a6");
        String label = "is later homonym of";
        UUID uuidLanguage = Language.uuidEnglish;
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName,
                uuidTerm, label, label, null, uuidLanguage);

        stepName = "update label for treated as later homonym";
        uuidTerm = UUID.fromString("2990a884-3302-4c8b-90b2-dfd31aaa2778");
        label = "is treated as later homonym of";
        uuidLanguage = Language.uuidEnglish;
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName,
                uuidTerm, label, label, null, uuidLanguage);

        //#8978
        stepName = "make statistical measurment value BigDecimal";
        tableName = "StatisticalMeasurementValue";
        columnName = "value";
        String scaleColumnName = "value_scale";
        int newPrecision = 18;
        int newScale = 9;
//        ColumnTypeChanger.NewFloat2BigDecimalInstance(stepList, stepName, tableName, columnName, scaleColumnName, newPrecision, newScale, INCLUDE_AUDIT);
        Float2BigDecimalTypeChanger.NewInstance(stepList, stepName, tableName, columnName, scaleColumnName, newPrecision, newScale, INCLUDE_AUDIT);

        //#8802
        tableName = "DescriptionElementBase";
        stepName = "add period_start to DescriptionElementBase";
        newColumnName = "period_start";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);
        stepName = "add period_endt to DescriptionElementBase";
        newColumnName = "period_end";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);
        stepName = "add period_extremestart to DescriptionElementBase";
        newColumnName = "period_extremestart";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);
        stepName = "add period_extremeend to DescriptionElementBase";
        newColumnName = "period_extremeend";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);
        stepName = "add period_freetext to DescriptionElementBase";
        newColumnName = "period_freetext";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        //#9005
        stepName = "Rename excludedNote -> statusNote(1)";
        String oldTableName = "TaxonNode_ExcludedNote";
        String newTableName = "TaxonNode_StatusNote";
        TableNameChanger.NewInstance(stepList, stepName, oldTableName, newTableName, INCLUDE_AUDIT);

        stepName = "Rename excludedNote -> statusNote(2)";
        tableName = "TaxonNode_StatusNote";
        String oldColumnName = "excludedNote_id";
        newColumnName = "statusNote_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        stepName = "Rename excludedNote -> statusNote(3)";
        tableName = "TaxonNode_StatusNote";
        oldColumnName = "excludedNote_mapkey_id";
        newColumnName = "statusNote_KEY";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        stepName = "Add TaxonNode.status column";
        tableName = "TaxonNode";
        columnName = "status";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName, 10, INCLUDE_AUDIT);

        stepName = "Set TaxonNode.status";
        tableName = "TaxonNode";
        String sql = "UPDATE @@TaxonNode@@ SET status = 'DOU' WHERE doubtful = @TRUE@";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName, 99);

        stepName = "Set TaxonNode.status";
        tableName = "TaxonNode";
        sql = "UPDATE @@TaxonNode@@ SET status = 'UNP' WHERE unplaced = @TRUE@";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName, 99);

        stepName = "Set TaxonNode.status";
        tableName = "TaxonNode";
        sql = "UPDATE @@TaxonNode@@ SET status = 'EXC' WHERE excluded = @TRUE@";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName, 99);

        //#9027
        stepName = "add availableFor column to DefinedTermBase(Feature)";
        tableName = "DefinedTermBase";
        newColumnName = "availableFor";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        stepName = "Set availableFor default value for features";
        tableName = "DefinedTermBase";
        sql = "UPDATE @@DefinedTermBase@@ "
                + " SET availableFor = '#TAX#' "
                + " WHERE DTYPE = 'Feature'";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName, 99);

        stepName = "Set availableFor default values for character";
        tableName = "DefinedTermBase";
        sql = "UPDATE @@DefinedTermBase@@ "
                + " SET availableFor = '#TAX#OCC#' "
                + " WHERE DTYPE = 'Character'";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName, 99);

        setAvailableFor(stepList, "71b356c5-1e3f-4f5d-9b0f-c2cf8ae7779f", "TNA");  //update protologue
        setAvailableFor(stepList, "2c355c16-cb04-4858-92bf-8da8d56dea95", "TNA");  //additional publication
        setAvailableFor(stepList, "355b2f47-d213-42af-a9e3-58a5db0f5b5c", "TNA");  //typification notes
        setAvailableFor(stepList, "0e5a93fc-3783-4248-be66-4cc346cd8e18", "TNA");  //orthography
        setAvailableFor(stepList, "3b46f5f2-5619-4f1a-884f-d7a805471942", "TNA");  //etymology
        updateAvailableFor(stepList, "910307f1-dc3c-452c-a6dd-af5ac7cd365c", "OCC"); //Unknown Feature Type
        updateAvailableFor(stepList, "9087cdcd-8b08-4082-a1de-34c9ba9fb493", "OCC"); //Description
        updateAvailableFor(stepList, "aa923827-d333-4cf5-9a5f-438ae0a4746b", "OCC"); //Ecology
        updateAvailableFor(stepList, "fb16929f-bc9c-456f-9d40-dec987b36438", "OCC"); //Habitat
        updateAvailableFor(stepList, "9fdc4663-4d56-47d0-90b5-c0bf251bafbb", "OCC"); //Habitat & Ecology
        updateAvailableFor(stepList, "6f677e98-d8d5-4bc5-80bf-affdb7e3945a", "OCC"); //Chromosome numbers
        updateAvailableFor(stepList, "9832e24f-b670-43b4-ac7c-20a7261a1d8c", "OCC"); //Biology And Ecology
        updateAvailableFor(stepList, "99b2842f-9aa7-42fa-bd5f-7285311e0101", "TNA"); //Citation
        updateAvailableFor(stepList, "84193b2c-327f-4cce-90ef-c8da18fd5bb5", "OCC"); //Image
        updateAvailableFor(stepList, "94213b2c-e67a-4d37-25ef-e8d316edfba1", "OCC"); //Anatomy
        updateAvailableFor(stepList, "6e9de1d5-05f0-40d5-8786-2fe30d0d894d", "OCC"); //Host plant
        updateAvailableFor(stepList, "002d05f2-fd72-49f1-ba4d-196cf09240b5", "OCC"); //Pathogen agent
        updateAvailableForUsed(stepList, "TaxonDescription", "taxon_id", "TAX");
        updateAvailableForUsed(stepList, "SpecimenDescription", "specimen_id", "OCC");
        updateAvailableForUsed(stepList, "NameDescription", "taxonName_id", "TNA");

        //#9026
        stepName = "add supportedDataTypes column to DefinedTermBase(Feature)";
        tableName = "DefinedTermBase";
        newColumnName = "supportedDataTypes";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        stepName = "Set supportedDataTypes default values for feature and character";
        tableName = "DefinedTermBase";
        sql = "UPDATE @@DefinedTermBase@@ "
                + " SET supportedDataTypes = '#' "
                + " WHERE DTYPE = 'FEATURE' OR DTYPE = 'CHARACTER'";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName, 99);

        updateSupportDataType(stepList, "supportsTextData", "TDA");
        updateSupportDataType(stepList, "supportsCommonTaxonName", "CTN");
        updateSupportDataType(stepList, "supportsDistribution", "DIS");
        updateSupportDataType(stepList, "supportsIndividualAssociation", "IAS");
        updateSupportDataType(stepList, "supportsTaxonInteraction", "TIN");
        updateSupportDataType(stepList, "supportsCategoricalData", "CDA");
        updateSupportDataType(stepList, "supportsQuantitativeData", "QDA");



        return stepList;
    }

    private void updateAvailableForUsed(List<ISchemaUpdaterStep> stepList, String clazzStr, String fkColumn, String key) {
        String stepName = " Update availableFor for features used by " + clazzStr;
        String tableName = "DefinedTermBase";
        String sql = " UPDATE @@DefinedTermBase@@ "
             + " SET availableFor = CONCAT(availableFor, '"+key+"#') "
             + " WHERE id IN ( "
                + " SELECT deb.feature_id "
                + " FROM @@DescriptionElementBase@@ deb "
                + " INNER JOIN @@DescriptionBase@@ db ON db.id = deb.indescription_id "
                + " WHERE db." + fkColumn + " IS NOT NULL "
             + " ) AND availableFor NOT LIKE '%#" + key + "#%'";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName, 99);
    }

    private void updateAvailableFor(List<ISchemaUpdaterStep> stepList, String uuidStr, String key) {
        String stepName = "Update availableFor for " + uuidStr;
        String tableName = "DefinedTermBase";
        String sql = "UPDATE @@DefinedTermBase@@ "
                + " SET availableFor = CONCAT(availableFor, '"+key+"#') "
                + " WHERE uuid = '" + uuidStr + "'";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName, 99);
    }

    private void setAvailableFor(List<ISchemaUpdaterStep> stepList, String uuidStr, String key) {
        String stepName = "Set availableFor for " + uuidStr;
        String tableName = "DefinedTermBase";
        String sql = "UPDATE @@DefinedTermBase@@ "
                + " SET availableFor = '#" + key + "#' "
                + " WHERE uuid = '" + uuidStr + "'";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName, 99);
    }

    private void updateSupportDataType(List<ISchemaUpdaterStep> stepList, String methodName, String key) {
        String stepName;
        String tableName;
        stepName = "Update " + methodName;
        tableName = "DefinedTermBase";
        String sql = "UPDATE @@DefinedTermBase@@ "
                + " SET supportedDataTypes = CONCAT(supportedDataTypes, '"+key+"#') "
                + " WHERE " + methodName + " = @TRUE@ ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName, 99);
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5112_5120.NewInstance();
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}
}
