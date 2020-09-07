/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v515_518;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.v512_515.Reference2SourceMover;
import eu.etaxonomy.cdm.database.update.v512_515.SchemaUpdater_5151_5152;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 02.09.2020
 */
public class SchemaUpdater_5152_5180 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5152_5180.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_15_02;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_18_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5152_5180 NewInstance() {
		return new SchemaUpdater_5152_5180();
	}

	protected SchemaUpdater_5152_5180() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //6581
        //move nomenclatural status reference to source
        stepName = "move nomenclatural status reference to source";
        tableName = "NomenclaturalStatus";
        String referenceColumnName = "citation_id";
        String microReferenceColumnName = "citationMicroReference";
        String sourceColumnName = "source_id";
        Reference2SourceMover.NewInstance(stepList, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName);

        //6581
        //move name relationship reference to source
        stepName = "move name relationship reference to source";
        tableName = "NameRelationship";
        referenceColumnName = "citation_id";
        microReferenceColumnName = "citationMicroReference";
        sourceColumnName = "source_id";
        Reference2SourceMover.NewInstance(stepList, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName);

        //6581
        //move taxon relationship reference to source
        stepName = "move taxon relationship reference to source";
        tableName = "TaxonRelationship";
        referenceColumnName = "citation_id";
        microReferenceColumnName = "citationMicroReference";
        sourceColumnName = "source_id";
        Reference2SourceMover.NewInstance(stepList, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName);

        //6581
        //move hybrid relationship reference to source
        stepName = "move hybrid relationship reference to source";
        tableName = "HybridRelationship";
        referenceColumnName = "citation_id";
        microReferenceColumnName = "citationMicroReference";
        sourceColumnName = "source_id";
        Reference2SourceMover.NewInstance(stepList, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName);

        //6581
        //move type designation reference to source
        stepName = "move type designation reference to source";
        tableName = "TypeDesignationBase";
        referenceColumnName = "citation_id";
        microReferenceColumnName = "citationMicroReference";
        sourceColumnName = "source_id";
        Reference2SourceMover.NewInstance(stepList, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName);

        //6581
        //move nomenclatural reference to nomenclatural source
        stepName = "move nomenclatural reference to nomenclatural source";
        tableName = "TaxonName";
        referenceColumnName = "nomenclaturalReference_id";
        microReferenceColumnName = "nomenclaturalMicroReference";
        sourceColumnName = "nomenclaturalSource_id";
        Reference2SourceMover.NewInstance(stepList, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName);

        //9094
        // update TaxonNode.source from IdentifiableSource to DescriptionElementSource
        stepName = "update TaxonNode.source from IdentifiableSource to DescriptionElementSource";
        String sql = "UPDATE @@OriginalSourceBase@@ "
                   + " SET DTYPE = 'DescriptionElementSource' "
                   + " WHERE id IN (SELECT source_id FROM TaxonNode tn)";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName, 99);

        //9082
        //fix empty partials_start and partials_end handling
        tableName = "AgentBase";
        String columnName ="lifespan";
        fixEmptyPartialsHandling(stepList, tableName, columnName);
        tableName = "Amplification";
        columnName ="timeperiod";
        fixEmptyPartialsHandling(stepList, tableName, columnName);
        tableName = "DerivationEvent";
        fixEmptyPartialsHandling(stepList, tableName, columnName);
        tableName = "DeterminationEvent";
        fixEmptyPartialsHandling(stepList, tableName, columnName);
        tableName = "GatheringEvent";
        fixEmptyPartialsHandling(stepList, tableName, columnName);
        tableName = "SingleRead";
        fixEmptyPartialsHandling(stepList, tableName, columnName);
        tableName = "MaterialOrMethodEvent";
        fixEmptyPartialsHandling(stepList, tableName, columnName);
        tableName = "DescriptionElementBase";
        fixEmptyPartialsHandling(stepList, tableName, columnName);
        tableName = "Classification";
        fixEmptyPartialsHandling(stepList, tableName, columnName);
        tableName = "DescriptionElementBase";
        columnName ="period";
        fixEmptyPartialsHandling(stepList, tableName, columnName);
        tableName = "DefinedTermBase";
        columnName ="validPeriod";
        fixEmptyPartialsHandling(stepList, tableName, columnName);
        tableName = "Media";
        columnName ="mediaCreated";
        fixEmptyPartialsHandling(stepList, tableName, columnName);
        tableName = "Reference";
        columnName ="datePublished";
        fixEmptyPartialsHandling(stepList, tableName, columnName);

        return stepList;
    }

    //9082
    private void fixEmptyPartialsHandling(List<ISchemaUpdaterStep> stepList,
            String tableName, String columnName) {

        String stepName = "fix empty partials_start handling for " + tableName;
        String sql = "UPDATE @@"+tableName+"@@ "
                   + " SET "+columnName+"_start = NULL "
                   + " WHERE "+columnName+"_start = '00000000' ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName, 99);

        stepName = "fix empty partials_end handling for " + tableName;
        sql = "UPDATE @@"+tableName+"@@ "
                   + " SET "+columnName+"_end = NULL "
                   + " WHERE "+columnName+"_end = '00000000'";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName, 99);
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5151_5152.NewInstance();
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}
}
