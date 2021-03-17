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
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
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

		//9327
        stepName = "Add sourcedTaxon column to SecundumSource";
        tableName = "OriginalSourceBase";
        newColumnName = "sourcedTaxon_id";
        referencedTable = "TaxonBase";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referencedTable);

	    //9327
        //move nomenclatural reference to nomenclatural source
        stepName = "move secundum reference to secundum source";
        tableName = "TaxonBase";
        String referenceColumnName = "sec_id";
        String microReferenceColumnName = "secMicroReference";
        String sourceColumnName = "sourcedTaxon_id";
        String sourceType = "SEC";
        String dtype = "SecundumSource";
        SecReference2SourceMover.NewInstance(stepList, stepName, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName, dtype, sourceType);

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