/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v518_522;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ColumnValueUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.v512_515.Reference2SourceMover;
import eu.etaxonomy.cdm.database.update.v515_518.SchemaUpdater_5185_5186;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 02.09.2020
 */
public class SchemaUpdater_5186_5220 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5186_5220.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_18_06;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_22_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5186_5220 NewInstance() {
		return new SchemaUpdater_5186_5220();
	}

	protected SchemaUpdater_5186_5220() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;
		String referencedTable;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//9507
        stepName = "Add ratioToStructure column to DefinedTermBase (Character)";
        tableName = "DefinedTermBase";
        newColumnName = "ratioToStructure_id";
        referencedTable = "DefinedTermBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //9331
        //set DTYPE for NamedSource-s
        stepName = "Set DTYPE for NamedSources";
        String query = " UPDATE @@OriginalSourceBase@@ "
                + " SET DTYPE = 'NamedSource' "
                + " WHERE id IN (SELECT source_id FROM @@HybridRelationship@@) "
                + "    OR id IN (SELECT source_id FROM @@NameRelationship@@) "
                + "    OR id IN (SELECT source_id FROM @@TaxonRelationship@@) "
                + "    OR id IN (SELECT source_id FROM @@TaxonNode@@) "
                + "    OR id IN (SELECT source_id FROM @@NomenclaturalStatus@@) "
                + "    OR id IN (SELECT source_id FROM @@TypeDesignationBase@@) ";
        String auditQuery = " UPDATE @@OriginalSourceBase_AUD@@ "
                + " SET DTYPE = 'NamedSource' "
                + " WHERE id IN (SELECT source_id FROM @@HybridRelationship_AUD@@) "
                + "    OR id IN (SELECT source_id FROM @@NameRelationship_AUD@@) "
                + "    OR id IN (SELECT source_id FROM @@TaxonRelationship_AUD@@) "
                + "    OR id IN (SELECT source_id FROM @@TaxonNode@@) "
                + "    OR id IN (SELECT source_id FROM @@NomenclaturalStatus_AUD@@) "
                + "    OR id IN (SELECT source_id FROM @@TypeDesignationBase_AUD@@) ";
        SimpleSchemaUpdaterStep.NewExplicitAuditedInstance(stepList, stepName, query, auditQuery, -99);

        //#9327
        //add sourcedElement column
        stepName = "Add sourcedElement_id column to DescriptionElementSource";
        tableName = "OriginalSourceBase";
        newColumnName = "sourcedElement_id";
        referencedTable = "DescriptionElementBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //#9327
        //update sourcedElement column
        stepName = "UPdate sourcedElement column";
        String sql = " UPDATE @@OriginalSourceBase@@ "
                + " SET sourcedElement_id = (SELECT MN.descriptionElementBase_id "
                + "         FROM @@DescriptionElementBase_OriginalSourceBase@@ MN "
                + "         WHERE MN.sources_id = @@OriginalSourceBase@@.id) "
                + " WHERE EXISTS ( "
                + "       SELECT * "
                + "       FROM @@DescriptionElementBase_OriginalSourceBase@@ MN "
                + "       WHERE MN.sources_id = @@OriginalSourceBase@@.id)";
         String sql_aud = " UPDATE @@OriginalSourceBase_AUD@@ "
                    + " SET sourcedElement_id = (SELECT MN.id "
                    + "         FROM @@DescriptionElementBase_OriginalSourceBase_AUD@@ tn "
                    + "         WHERE MN.sources_id = @@OriginalSourceBase_AUD@@.id) "
                    + " WHERE EXISTS ( "
                    + "       SELECT * "
                    + "       FROM @@DescriptionElementBase_OriginalSourceBase_AUD@@ MN "
                    + "       WHERE MN.sources_id = @@OriginalSourceBase_AUD@@.id)";
         SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, sql_aud, -99);

		//#9327
        //add sourcedTaxon column
        stepName = "Add sourcedTaxon column to SecundumSource";
        tableName = "OriginalSourceBase";
        newColumnName = "sourcedTaxon_id";
        referencedTable = "TaxonBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

	    //#9327
        //move secundum reference to nomenclatural source
        stepName = "move secundum reference to secundum source";
        tableName = "TaxonBase";
        String referenceColumnName = "sec_id";
        String microReferenceColumnName = "secMicroReference";
        String sourceColumnName = "sourcedTaxon_id";
        String sourceType = "PTS";
        String dtype = "SecundumSource";
        SecReference2SourceMover.NewInstance(stepList, stepName, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName, dtype, sourceType);

        //#9330
        //remove source type 'NOR'
        stepName = "Remove source type 'nomenclatural reference'";
        tableName = "OriginalSourceBase";
        String columnName = "sourceType";
        String newValue = "PTS";
        String where = columnName + "='NOR'";
        ColumnValueUpdater.NewStringInstance(stepList, stepName, tableName,
                columnName, newValue, where, INCLUDE_AUDIT);

        //#9332
        stepName = "Rename TypeDesignationBase.soure to designationSource";
        tableName = "TypeDesignationBase";
        String oldColumnName = "source_id";
        newColumnName = "designationSource_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //#8761
        //add name column to nomenclatural status
        stepName = "add name_id column to nomenclatural status";
        tableName = "NomenclaturalStatus";
        newColumnName = "name_id";
        referencedTable = "TaxonName";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

        //#8761
        //update nomenclatural status' name column
        stepName = "Update nomenclatural status' name column";
        sql = " UPDATE @@NomenclaturalStatus@@ "
                + " SET name_id = (SELECT MN.TaxonName_id "
                + "         FROM @@TaxonName_NomenclaturalStatus@@ MN "
                + "         WHERE MN.status_id = @@NomenclaturalStatus@@.id) "
                + " WHERE EXISTS ( "
                + "       SELECT * "
                + "       FROM @@TaxonName_NomenclaturalStatus@@ MN "
                + "       WHERE MN.status_id = @@NomenclaturalStatus@@.id)";
         sql_aud = " UPDATE @@NomenclaturalStatus_AUD@@ "
                + " SET name_id = (SELECT MN.id "
                + "         FROM @@TaxonName_NomenclaturalStatuse_AUD@@ tn "
                + "         WHERE MN.status_id = @@NomenclaturalStatus_AUD@@.id) "
                + " WHERE EXISTS ( "
                + "       SELECT * "
                + "       FROM @@TaxonName_NomenclaturalStatus_AUD@@ MN "
                + "       WHERE MN.status_id = @@NomenclaturalStatus_AUD@@.id)";
         SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, sql_aud, -99);

         //9211
         //move classification reference to classification source
         stepName = "move classification reference to classification source";
         tableName = "Classification";
         referenceColumnName = "reference_id";
         microReferenceColumnName = "microReference";
         sourceColumnName = "source_id";
         sourceType = "PTS";
         dtype = "NamedSource";
         Reference2SourceMover.NewInstance(stepList, stepName, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName, dtype, sourceType);

        //#9315
        removeOldSingleSourceCitations(stepList);

        return stepList;
    }

    //#9315 remove
    private void removeOldSingleSourceCitations(List<ISchemaUpdaterStep> stepList) {
        //TaxonName.nomenclaturalSource_id
        String stepName = "Remove TaxonName.nomenclaturalSource_id";
        String tableName = "TaxonName";
        String oldColumnName = "nomenclaturalSource_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //TaxonName.nomenclaturalMicroReference
        stepName = "Remove TaxonName.nomenclaturalMicroReference";
        tableName = "TaxonName";
        oldColumnName = "nomenclaturalMicroReference";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //TaxonName.nomenclaturalReference_id
        stepName = "Remove TaxonName.nomenclaturalReference_id";
        tableName = "TaxonName";
        oldColumnName = "nomenclaturalReference_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //TaxonNode.microReferenceForParentChildRelation
        stepName = "Remove TaxonNode.microReferenceForParentChildRelation";
        tableName = "TaxonNode";
        oldColumnName = "microReferenceForParentChildRelation";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //TaxonNode.referenceForParentChildRelation_id
        stepName = "Remove TaxonNode.referenceForParentChildRelation_id";
        tableName = "TaxonNode";
        oldColumnName = "referenceForParentChildRelation_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //NomenclaturalStatus.citationMicroReference
        stepName = "Remove NomenclaturalStatus.citationMicroReference";
        tableName = "NomenclaturalStatus";
        oldColumnName = "citationMicroReference";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //NomenclaturalStatus.citation_id
        stepName = "Remove NomenclaturalStatus.citation_id";
        tableName = "NomenclaturalStatus";
        oldColumnName = "citation_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //TypeDesignationBase.citationMicroReference
        stepName = "Remove TypeDesignationBase.citationMicroReference";
        tableName = "TypeDesignationBase";
        oldColumnName = "citationMicroReference";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //TypeDesignationBase.citation_id
        stepName = "Remove TypeDesignationBase.citation_id";
        tableName = "TypeDesignationBase";
        oldColumnName = "citation_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //TaxonRelationship.citationMicroReference
        stepName = "Remove TaxonRelationship.citationMicroReference";
        tableName = "TaxonRelationship";
        oldColumnName = "citationMicroReference";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //TaxonRelationship.citation_id
        stepName = "Remove TaxonRelationship.citation_id";
        tableName = "TaxonRelationship";
        oldColumnName = "citation_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //NameRelationship.citationMicroReference
        stepName = "Remove NameRelationship.citationMicroReference";
        tableName = "NameRelationship";
        oldColumnName = "citationMicroReference";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //NameRelationship.citation_id
        stepName = "Remove NameRelationship.citation_id";
        tableName = "NameRelationship";
        oldColumnName = "citation_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //HybridRelationship.citationMicroReference
        stepName = "Remove HybridRelationship.citationMicroReference";
        tableName = "HybridRelationship";
        oldColumnName = "citationMicroReference";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //HybridRelationship.citation_id
        stepName = "Remove HybridRelationship.citation_id";
        tableName = "HybridRelationship";
        oldColumnName = "citation_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5185_5186.NewInstance();
    }
}