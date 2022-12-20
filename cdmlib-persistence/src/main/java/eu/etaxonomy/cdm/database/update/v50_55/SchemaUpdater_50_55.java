/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v50_55;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ColumnTypeChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.MnTableCreator;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableNameChanger;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.database.update.v40_50.SchemaUpdater_47_50;
import eu.etaxonomy.cdm.model.term.TermType;

/**
/**
 * @author a.mueller
 * @date 09.06.2017
 */
public class SchemaUpdater_50_55 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger(SchemaUpdater_50_55.class);
	private static final String startSchemaVersion = "5.0.0.0.20180514";
	private static final String endSchemaVersion = "5.5.0.0.20190221";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_50_55 NewInstance() {
		return new SchemaUpdater_50_55();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_50_55() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;
		String query;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();


		//#6699 delete term version
		//just in case not fixed before yet
		stepName = "Delete term version";
		query = "DELETE FROM @@CdmMetaData@@ WHERE propertyName = 'TERM_VERSION'";
		SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query);

        //#7414 remove mediaCreatedOld column
        stepName = "remove mediaCreatedOld column";
        tableName = "Media";
        String oldColumnName = "mediaCreatedOld";
        ColumnRemover.NewInstance(stepList, stepName, tableName, oldColumnName, INCLUDE_AUDIT);

        //#7772 rename TermBase_inverseRepresentation to DefinedTermBase_InverseRepresentation
        stepName = "Rename TermBase_inverseRepresentation to DefinedTermBase_InverseRepresentation";
        String oldName = "TermBase_inverseRepresentation";
        String newName = "DefinedTermBase_InverseRepresentation";
        TableNameChanger.NewInstance(stepList, stepName, oldName,
                newName, INCLUDE_AUDIT);

        //#7772 rename DefinedTermBase_InverseRepresentation.term_id to .definedTermBase_id
        stepName = "Rename TermBase_inverseRepresentation to DefinedTermBase_InverseRepresentation";
        tableName = "DefinedTermBase_InverseRepresentation";
        oldColumnName = "term_id";
        newColumnName = "definedTermBase_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //#8004 add sortindex to description element
        stepName = "Add sortindex to description element";
        tableName = "DescriptionElementBase";
        newColumnName = "sortIndex";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, null, !NOT_NULL);

        //#7682 update Point.precision from 0 to null
        stepName = "update Point.precision from 0 to null";
        query = "UPDATE @@GatheringEvent@@ SET exactLocation_errorRadius = null WHERE exactLocation_errorRadius = 0 ";
        tableName = "GatheringEvent";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName);

        //#7859 CdmPreference.value as CLOB
        stepName = "Make CdmPreference.value a CLOB";
        String columnName = "value";
        tableName = "CdmPreference";
        // TODO check non MySQL and with existing data (probably does not exist)
        ColumnTypeChanger.NewClobInstance(stepList, stepName, tableName,
                columnName, !INCLUDE_AUDIT);

        //#7857 update name realtionships
        updateNameRelationships(stepList);

        //#7683 allow null for ExternalLink_AUD.uuid
        stepName = "Allow null for ExternalLink_AUD.uuid ";
        columnName = "uuid";
        tableName = "ExternalLink_AUD";
        ColumnTypeChanger.NewStringSizeInstance(stepList, stepName, tableName, columnName, 36, !INCLUDE_AUDIT);

        //#7514 change symbols for pro parte synonyms and misapplied name relationship types
        updateConceptRelationshipSymbols(stepList);

        //#8006
        updateTaxonRelationshipLabels(stepList);

        //#7372
        stepName = "Add allowDuplicates to feature tree";
        tableName = "FeatureTree";
        newColumnName = "allowDuplicates";
        ColumnAdder.NewBooleanInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, false);

        //#6794 add term type to feature tree
        stepName = "Add termType to feature tree";
        tableName = "FeatureTree";
        newColumnName = "termType";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, 255, TermType.Feature.getKey(), INCLUDE_AUDIT)
                .setNotNull(NOT_NULL);

        //#6794 add term type to feature node
        stepName = "Add termType to feature node";
        tableName = "FeatureNode";
        newColumnName = "termType";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, 255, TermType.Feature.getKey(), INCLUDE_AUDIT)
                .setNotNull(NOT_NULL);

        //#8120 add structure modifier
        stepName = "Add structure modifier to Character";
        tableName = "DefinedTermBase";
        String referedTableName = "DefinedTermBase";
        newColumnName = "structureModifier_id";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referedTableName);

        //#8120 add property modifier
        stepName = "Add property modifier to Character";
        tableName = "DefinedTermBase";
        referedTableName = "DefinedTermBase";
        newColumnName = "propertyModifier_id";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, !NOT_NULL, referedTableName);

        //#8142 add plural to representations
        stepName = "Add plural to representations";
        tableName = "Representation";
        newColumnName = "plural";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        //#8017 add type designation sources
        stepName = "Add plural type designation sources";
        String firstTableName = "TypeDesignationBase";
        String secondTableName = "OriginalSourceBase";
        MnTableCreator.NewMnInstance(stepList, stepName, firstTableName, null, secondTableName, null, "sources", INCLUDE_AUDIT, !IS_LIST, IS_1_TO_M);

        return stepList;

	}

    //8006 update taxon relationships
    private void updateTaxonRelationshipLabels(List<ISchemaUpdaterStep> stepList) {

//        //7857 Update symmetrical for name relationships
//        String stepName = "Update symmetrical for name relationships";
//        String query = "UPDATE @@DefinedTermBase@@ "
//                + " SET symmetrical=0 "
//                + " WHERE uuid IN ('049c6358-1094-4765-9fae-c9972a0e7780', '6e23ad45-3f2a-462b-ad87-d2389cd6e26c', "
//                + " 'c6f9afcb-8287-4a2b-a6f6-4da3a073d5de', 'eeaea868-c4c1-497f-b9fe-52c9fc4aca53') ";
//        String tableName = "DefinedTermBase";
//        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName);

        //Taxonomically Included in
        String stepName = "Taxonomically Included in => is taxonomically included in";
        UUID uuidTerm = UUID.fromString("d13fecdf-eb44-4dd7-9244-26679c05df1c");
        UUID uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        String label = "is taxonomically included in";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        stepName = "taxonomically includes => taxonomically includes";
        label = "taxonomically includes";
        TermRepresentationUpdater.NewInverseInstance(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //Misapplied Name for
        stepName = "Misapplied Name for => is misapplied name for";
        uuidTerm = UUID.fromString("1ed87175-59dd-437e-959e-0d71583d8417");
        label = "is misapplied name for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        stepName = "Has Misapplied Name => has misapplied name";
        label = "has misapplied name";
        TermRepresentationUpdater.NewInverseInstance(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //Pro parte Misapplied Name for
        stepName = "Pro parte Misapplied Name for => is pro parte misapplied name for";
        uuidTerm = UUID.fromString("b59b4bd2-11ff-45d1-bae2-146efdeee206");
        label = "is pro parte misapplied name for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        stepName = "Has Pro parte Misapplied Name => has pro parte misapplied name";
        label = "has pro parte misapplied name";
        TermRepresentationUpdater.NewInverseInstance(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //Partial Misapplied Name for
        stepName = "Partial Misapplied Name for => is partial misapplied name for";
        uuidTerm = UUID.fromString("859fb615-b0e8-440b-866e-8a19f493cd36");
        label = "is partial misapplied name for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        stepName = "Has Partial Misapplied Name => has partial misapplied name";
        label = "has partial misapplied name";
        TermRepresentationUpdater.NewInverseInstance(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //Pro parte Synonym for
        stepName = "Pro parte Synonym for => is pro parte synonym for";
        uuidTerm = UUID.fromString("8a896603-0fa3-44c6-9cd7-df2d8792e577");
        label = "is pro parte synonym for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        stepName = "Has Pro parte Synonym => has pro parte synonym";
        label = "has pro parte synonym";
        TermRepresentationUpdater.NewInverseInstance(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //Partial Synonym for
        stepName = "Partial Synonym for => is partial synonym for";
        uuidTerm = UUID.fromString("9d7a5e56-973c-474c-b6c3-a1cb00833a3c");
        label = "is partial synonym for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        stepName = "Has Partial Synonym => has partial synonym";
        label = "has partial synonym";
        TermRepresentationUpdater.NewInverseInstance(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //Invalid Designation for
        stepName = "Invalid Designation for => is invalid designation for";
        uuidTerm = UUID.fromString("605b1d01-f2b1-4544-b2e0-6f08def3d6ed");
        label = "is invalid designation for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        stepName = "Has Invalid Designation => has invalid designation";
        label = "has invalid designation";
        TermRepresentationUpdater.NewInverseInstance(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //Not yet worked on
        stepName = "Unclear => Not yet worked on";
        label = "Not yet worked on";
        uuidTerm = UUID.fromString("8d47e59a-790d-428f-8060-01d443519166");
        TermRepresentationUpdater.NewInverseInstance(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);
    }


	//7514
    private void updateConceptRelationshipSymbols(List<ISchemaUpdaterStep> stepList) {

        //Update misapplied name symbols
        String stepName = "Update misapplied name symbols";
        String query = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol='"+UTF8.EM_DASH_DOUBLE+"' , inverseSymbol = '"+UTF8.EN_DASH+"' "
                + " WHERE uuid = '1ed87175-59dd-437e-959e-0d71583d8417' ";
        String tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName);

        //Update pro parte misapplied name symbols
        stepName = "Update pro parte misapplied name symbols";
        query = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol='"+UTF8.EM_DASH_DOUBLE+"(p.p.)' , inverseSymbol = '"+UTF8.EN_DASH+"(p.p.)' "
                + " WHERE uuid = 'b59b4bd2-11ff-45d1-bae2-146efdeee206' ";
        tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName);

        //Update partial misapplied name symbols
        stepName = "Update partial misapplied name symbols";
        query = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol='"+UTF8.EM_DASH_DOUBLE+"(part.)' , inverseSymbol = '"+UTF8.EN_DASH+"(part.)' "
                + " WHERE uuid = '859fb615-b0e8-440b-866e-8a19f493cd36' ";
        tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName);

        //Update pro parte synonym symbols
        stepName = "Update pro parte synonym symbols";
        query = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol='⊃p.p.' , inverseSymbol = 'p.p.' "
                + " WHERE uuid = '8a896603-0fa3-44c6-9cd7-df2d8792e577' ";
        tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName);

        //Update partial synonym symbols
        stepName = "Update partial synonym symbols";
        query = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol='⊃part.' , inverseSymbol = 'part.' "
                + " WHERE uuid = '9d7a5e56-973c-474c-b6c3-a1cb00833a3c' ";
        tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName);

    }

    //7857 update name realtionships
    private void updateNameRelationships(List<ISchemaUpdaterStep> stepList) {

        //7857 Update symmetrical for name relationships
        String stepName = "Update symmetrical for name relationships";
        String query = "UPDATE @@DefinedTermBase@@ "
                + " SET symmetrical=0 "
                + " WHERE uuid IN ('049c6358-1094-4765-9fae-c9972a0e7780', '6e23ad45-3f2a-462b-ad87-d2389cd6e26c', "
                + " 'c6f9afcb-8287-4a2b-a6f6-4da3a073d5de', 'eeaea868-c4c1-497f-b9fe-52c9fc4aca53') ";
        String tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, tableName);

        //orthographic variant for
        stepName = "orthographic variant for => is orthographic variant for";
        UUID uuidTerm = UUID.fromString("eeaea868-c4c1-497f-b9fe-52c9fc4aca53");
        UUID uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        String label = "is orthographic variant for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //original spelling for
        stepName = "original spelling for => is original spelling for";
        uuidTerm = UUID.fromString("264d2be4-e378-4168-9760-a9512ffbddc4");
        label = "is original spelling for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //misspelling for
        stepName = "misspelling for => is misspelling for";
        uuidTerm = UUID.fromString("c6f9afcb-8287-4a2b-a6f6-4da3a073d5de");
        label = "is misspelling for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //later homonym for
        stepName = "later homonym for => is later homonym for";
        uuidTerm = UUID.fromString("80f06f65-58e0-4209-b811-cb40ad7220a6");
        label = "is later homonym for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //treated as later homonym for
        stepName = " => is treated as later homonym for";
        uuidTerm = UUID.fromString("2990a884-3302-4c8b-90b2-dfd31aaa2778");
        label = "is treated as later homonym for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //alternative name for
        stepName = "alternative name for => is alternative name for";
        uuidTerm = UUID.fromString("049c6358-1094-4765-9fae-c9972a0e7780");
        label = "is alternative name for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //basionym for
        stepName = "basionym for => is basionym for";
        uuidTerm = UUID.fromString("25792738-98de-4762-bac1-8c156faded4a");
        label = "is basionym for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //replaced synonym for
        stepName = "replaced synonym for => is replaced synonym for";
        uuidTerm = UUID.fromString("71c67c38-d162-445b-b0c2-7aba56106696");
        label = "is replaced synonym for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //conserved against
        stepName = "conserved against => is conserved against";
        uuidTerm = UUID.fromString("e6439f95-bcac-4ebb-a8b5-69fa5ce79e6a");
        label = "is conserved against";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //validated by
        stepName = "validated by => is validated by";
        uuidTerm = UUID.fromString("a176c9ad-b4c2-4c57-addd-90373f8270eb");
        label = "is validated by";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //later validated by
        stepName = "later validated by => is later validated by";
        uuidTerm = UUID.fromString("a25ee4c1-863a-4dab-9499-290bf9b89639");
        label = "is later validated by";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //blocking name for
        stepName = "blocking name for => is blocking name for";
        uuidTerm = UUID.fromString("1dab357f-2e12-4511-97a4-e5153589e6a6");
        label = "blocking name for";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //emendation for
        stepName = "emendation for => is emendation for";
        uuidTerm = UUID.fromString("6e23ad45-3f2a-462b-ad87-d2389cd6e26c");
        label = "is emendation for";
        TermRepresentationUpdater.NewInverseInstance(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);
    }

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_47_50.NewInstance();
	}
}