/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v50_51;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.database.update.v47_50.SchemaUpdater_47_50;

/**
/**
 * @author a.mueller
 * @date 09.06.2017
 *
 */
public class SchemaUpdater_50_55 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_50_55.class);
	private static final String startSchemaVersion = "5.0.0.0.20180514";
	private static final String endSchemaVersion = "5.5.0.0.20190122";

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
		ISchemaUpdaterStep step;
		String newColumnName;
		String query;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();


		//#6699 delete term version
		//just in case not fixed before yet
		stepName = "Delete term version";
		query = "DELETE FROM @@CdmMetaData@@ WHERE propertyName = 'TERM_VERSION'";
		step = SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepName, query, -99);
        stepList.add(step);

        //#7414 remove mediaCreatedOld column
        stepName = "remove mediaCreatedOld column";
        tableName = "Media";
        String oldColumnName = "mediaCreatedOld";
        step = ColumnRemover.NewInstance(stepName, tableName, oldColumnName, INCLUDE_AUDIT);
        stepList.add(step);

        //TODO remove proparte and partial columns

        //#8004 add sortindex to description element
        stepName = "Add sortindex to description element";
        tableName = "DescriptionElementBase";
        newColumnName = "sortIndex";
        step = ColumnAdder.NewIntegerInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, null, !NOT_NULL);
        stepList.add(step);

        //7682 update Point.precision from 0 to null
        stepName = "update Point.precision from 0 to null";
        query = "UPDATE @@GatheringEvent@@ SET exactLocation_errorRadius = null WHERE exactLocation_errorRadius = 0 ";
        tableName = "GatheringEvent";
        step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, -99);
        stepList.add(step);

        //#7859 CdmPreference.value as CLOB
        stepName = "Make CdmPreference.value a CLOB";
        String columnName = "value";
        tableName = "CdmPreference";
        // TODO check non MySQL and with existing data (probably does not exist)
        step = ColumnNameChanger.NewClobInstance(stepName, tableName,
                columnName, columnName, INCLUDE_AUDIT);
        stepList.add(step);

        //7857 update name realtionships
        updateNameRelationships(stepList);

        return stepList;

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
        ISchemaUpdaterStep step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, tableName, -99);
        stepList.add(step);

        //orthographic variant for
        stepName = "orthographic variant for => is orthographic variant for";
        UUID uuidTerm = UUID.fromString("eeaea868-c4c1-497f-b9fe-52c9fc4aca53");
        UUID uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        String label = "is orthographic variant for";
        step = TermRepresentationUpdater.NewInstanceWithTitleCache(stepName, uuidTerm,
                label, label, null, uuidLanguage);
        stepList.add(step);

        //original spelling for
        stepName = "original spelling for => is original spelling for";
        uuidTerm = UUID.fromString("264d2be4-e378-4168-9760-a9512ffbddc4");
        label = "is original spelling for";
        step = TermRepresentationUpdater.NewInstanceWithTitleCache(stepName, uuidTerm,
                label, label, null, uuidLanguage);
        stepList.add(step);

        //misspelling for
        stepName = "misspelling for => is misspelling for";
        uuidTerm = UUID.fromString("c6f9afcb-8287-4a2b-a6f6-4da3a073d5de");
        label = "is misspelling for";
        step = TermRepresentationUpdater.NewInstanceWithTitleCache(stepName, uuidTerm,
                label, label, null, uuidLanguage);
        stepList.add(step);

        //later homonym for
        stepName = "later homonym for => is later homonym for";
        uuidTerm = UUID.fromString("80f06f65-58e0-4209-b811-cb40ad7220a6");
        label = "is later homonym for";
        step = TermRepresentationUpdater.NewInstanceWithTitleCache(stepName, uuidTerm,
                label, label, null, uuidLanguage);
        stepList.add(step);

        //treated as later homonym for
        stepName = " => is treated as later homonym for";
        uuidTerm = UUID.fromString("2990a884-3302-4c8b-90b2-dfd31aaa2778");
        label = "is treated as later homonym for";
        step = TermRepresentationUpdater.NewInstanceWithTitleCache(stepName, uuidTerm,
                label, label, null, uuidLanguage);
        stepList.add(step);

        //alternative name for
        stepName = "alternative name for => is alternative name for";
        uuidTerm = UUID.fromString("049c6358-1094-4765-9fae-c9972a0e7780");
        label = "is alternative name for";
        step = TermRepresentationUpdater.NewInstanceWithTitleCache(stepName, uuidTerm,
                label, label, null, uuidLanguage);
        stepList.add(step);

        //basionym for
        stepName = "basionym for => is basionym for";
        uuidTerm = UUID.fromString("25792738-98de-4762-bac1-8c156faded4a");
        label = "is basionym for";
        step = TermRepresentationUpdater.NewInstanceWithTitleCache(stepName, uuidTerm,
                label, label, null, uuidLanguage);
        stepList.add(step);

        //replaced synonym for
        stepName = "replaced synonym for => is replaced synonym for";
        uuidTerm = UUID.fromString("71c67c38-d162-445b-b0c2-7aba56106696");
        label = "is replaced synonym for";
        step = TermRepresentationUpdater.NewInstanceWithTitleCache(stepName, uuidTerm,
                label, label, null, uuidLanguage);
        stepList.add(step);

        //conserved against
        stepName = "conserved against => is conserved against";
        uuidTerm = UUID.fromString("e6439f95-bcac-4ebb-a8b5-69fa5ce79e6a");
        label = "is conserved against";
        step = TermRepresentationUpdater.NewInstanceWithTitleCache(stepName, uuidTerm,
                label, label, null, uuidLanguage);
        stepList.add(step);

        //validated by
        stepName = "validated by => is validated by";
        uuidTerm = UUID.fromString("a176c9ad-b4c2-4c57-addd-90373f8270eb");
        label = "is validated by";
        step = TermRepresentationUpdater.NewInstanceWithTitleCache(stepName, uuidTerm,
                label, label, null, uuidLanguage);
        stepList.add(step);

        //later validated by
        stepName = "later validated by => is later validated by";
        uuidTerm = UUID.fromString("a25ee4c1-863a-4dab-9499-290bf9b89639");
        label = "is later validated by";
        step = TermRepresentationUpdater.NewInstanceWithTitleCache(stepName, uuidTerm,
                label, label, null, uuidLanguage);
        stepList.add(step);

        //blocking name for
        stepName = "blocking name for => is blocking name for";
        uuidTerm = UUID.fromString("1dab357f-2e12-4511-97a4-e5153589e6a6");
        label = "blocking name for";
        step = TermRepresentationUpdater.NewInstanceWithTitleCache(stepName, uuidTerm,
                label, label, null, uuidLanguage);
        stepList.add(step);

        //emendation for
        stepName = "emendation for => is emendation for";
        uuidTerm = UUID.fromString("6e23ad45-3f2a-462b-ad87-d2389cd6e26c");
        label = "is emendation for";
        step = TermRepresentationUpdater.NewReverseInstance(stepName, uuidTerm,
                label, label, null, uuidLanguage);
        stepList.add(step);
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_47_50.NewInstance();
	}

}
