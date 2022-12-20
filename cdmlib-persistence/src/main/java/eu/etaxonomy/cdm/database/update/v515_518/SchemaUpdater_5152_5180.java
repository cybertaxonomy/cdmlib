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
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.database.update.v512_515.Reference2SourceMover;
import eu.etaxonomy.cdm.database.update.v512_515.SchemaUpdater_5151_5152;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 02.09.2020
 */
public class SchemaUpdater_5152_5180 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(SchemaUpdater_5152_5180.class);

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
        Reference2SourceMover.NewInstance(stepList, stepName, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName);

        //6581
        //move name relationship reference to source
        stepName = "move name relationship reference to source";
        tableName = "NameRelationship";
        referenceColumnName = "citation_id";
        microReferenceColumnName = "citationMicroReference";
        sourceColumnName = "source_id";
        Reference2SourceMover.NewInstance(stepList, stepName, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName);

        //6581
        //move taxon relationship reference to source
        stepName = "move taxon relationship reference to source";
        tableName = "TaxonRelationship";
        referenceColumnName = "citation_id";
        microReferenceColumnName = "citationMicroReference";
        sourceColumnName = "source_id";
        Reference2SourceMover.NewInstance(stepList, stepName, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName);

        //6581
        //move hybrid relationship reference to source
        stepName = "move hybrid relationship reference to source";
        tableName = "HybridRelationship";
        referenceColumnName = "citation_id";
        microReferenceColumnName = "citationMicroReference";
        sourceColumnName = "source_id";
        Reference2SourceMover.NewInstance(stepList, stepName, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName);

        //6581
        //move type designation reference to source
        stepName = "move type designation reference to source";
        tableName = "TypeDesignationBase";
        referenceColumnName = "citation_id";
        microReferenceColumnName = "citationMicroReference";
        sourceColumnName = "source_id";
        Reference2SourceMover.NewInstance(stepList, stepName, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName);

        //6581
        //move nomenclatural reference to nomenclatural source
        stepName = "move nomenclatural reference to nomenclatural source";
        tableName = "TaxonName";
        referenceColumnName = "nomenclaturalReference_id";
        microReferenceColumnName = "nomenclaturalMicroReference";
        sourceColumnName = "nomenclaturalSource_id";
        String sourceType = "NOR";
        String dtype = "NomenclaturalSource";
        Reference2SourceMover.NewInstance(stepList, stepName, tableName, referenceColumnName, microReferenceColumnName, sourceColumnName, dtype, sourceType);

        //9094
        // update TaxonNode.source from IdentifiableSource to DescriptionElementSource
        stepName = "update TaxonNode.source from IdentifiableSource to DescriptionElementSource";
        String sql = " UPDATE @@OriginalSourceBase@@ "
                   + " SET DTYPE = 'DescriptionElementSource' "
                   + " WHERE id IN (SELECT source_id FROM TaxonNode tn)";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

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

        //#9062
        //remove StatisticalMeasurementValue.value_old
        stepName = "remove StatisticalMeasurementValue.value_old";
        tableName = "StatisticalMeasurementValue";
        String oldColumnName = "value_old";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //#9121
        //add abbrevLabel to feature flowering
        stepName = "add abbreviated label to feature flowering";
        sql = "UPDATE @@Representation@@ "
                   + " SET abbreviatedlabel = 'Fl.' "
                   + " WHERE abbreviatedlabel IS NULL AND label = 'Flowering Period' ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        //add abbrevLabel to feature fruiting
        stepName = "add abbreviated label to feature fruiting";
        sql = "UPDATE @@Representation@@ "
                   + " SET abbreviatedlabel = 'Fr.' "
                   + " WHERE abbreviatedlabel IS NULL AND label = 'Fruiting Period' ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        //add symbol to feature flowering
        stepName = "add abbreviated label to feature flowering";
        sql = "UPDATE @@DefinedTermBase@@ "
                   + " SET symbol = 'Fl.' "
                   + " WHERE uuid='03710cb5-606e-444a-a3e6-594268e3cc47' ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        //add symbol to feature fruiting
        stepName = "add abbreviated label to feature fruiting";
        sql = "UPDATE @@DefinedTermBase@@ "
                   + " SET symbol = 'Fr.' "
                   + " WHERE uuid='04aa8993-24b4-43e3-888c-5afaa733376e' ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        //#9120
        //update flowering period as supports temporal data
        stepName = "update flowering period as supports temporal data";
        tableName = "DefinedTermBase";
        sql = "UPDATE @@DefinedTermBase@@ "
                + " SET supportedDataTypes = CONCAT(supportedDataTypes, 'TED#') "
                + " WHERE uuid = '03710cb5-606e-444a-a3e6-594268e3cc47' AND supportedDataTypes NOT LIKE '%#TED#%' ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        //#9124
        //add meter to altitude
        stepName = "Add measurement unit [m] to feature altitude";
        UUID uuidAltitude = Feature.uuidAltitude;
        UUID uuidMeter = MeasurementUnit.uuidMeter;
        RecommendedMeasurementUnitAdder.NewInstance(stepList, stepName, uuidAltitude, uuidMeter);

        //8326
        //update label for blocking name and isonym relationships
        stepName = "Update label for 'is blocking name for' relationship";
        UUID uuidTerm = UUID.fromString("1dab357f-2e12-4511-97a4-e5153589e6a6");
        String label = "is blocking name for";
        String abbrev = null;
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, abbrev, Language.uuidEnglish);

        stepName = "Update label for 'is isonym for' relationship";
        uuidTerm = UUID.fromString("29ab238d-598d-45b9-addd-003cf39ccc3e");
        label = "is later isonym of";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, abbrev, Language.uuidEnglish);

        //#8964 update reverse label for later homonym
        stepName = "update label for later homonym";
        uuidTerm = UUID.fromString("80f06f65-58e0-4209-b811-cb40ad7220a6");
        label = "is earlier homonym of";
        UUID uuidLanguage = Language.uuidEnglish;
        TermRepresentationUpdater.NewInverseInstance(stepList, stepName,
                uuidTerm, label, label, null, uuidLanguage);

        stepName = "update label for treated as later homonym";
        uuidTerm = UUID.fromString("2990a884-3302-4c8b-90b2-dfd31aaa2778");
        label = "is treated as earlier homonym of";
        uuidLanguage = Language.uuidEnglish;
        TermRepresentationUpdater.NewInverseInstance(stepList, stepName,
                uuidTerm, label, label, null, uuidLanguage);

        return stepList;
    }

    //9082
    private void fixEmptyPartialsHandling(List<ISchemaUpdaterStep> stepList,
            String tableName, String columnName) {

//        String stepName = "fix empty partials_start handling for " + tableName;
//        String sql = "UPDATE @@"+tableName+"@@ "
//                   + " SET "+columnName+"_start = NULL "
//                   + " WHERE "+columnName+"_start = '00000000' ";
//        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);
//
//        stepName = "fix empty partials_end handling for " + tableName;
//        sql = "UPDATE @@"+tableName+"@@ "
//                   + " SET "+columnName+"_end = NULL "
//                   + " WHERE "+columnName+"_end = '00000000'";
//        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5151_5152.NewInstance();
    }
}