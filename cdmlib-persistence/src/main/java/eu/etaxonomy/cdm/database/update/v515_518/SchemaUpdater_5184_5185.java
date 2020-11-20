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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SingleTermRemover;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 03.11.2020
 */
public class SchemaUpdater_5184_5185 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5184_5185.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_18_04;
	//FIXME
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_18_04;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5184_5185 NewInstance() {
		return new SchemaUpdater_5184_5185();
	}

	protected SchemaUpdater_5184_5185() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#6591
		OriginalSpellingMover.NewInstance(stepList);

		String stepName = "remove original spelling name relationship type";
		String uuidTerm = "264d2be4-e378-4168-9760-a9512ffbddc4";
		String checkUsedQueries = "SELECT count(*) FROM @@NameRelationship@@ nr "
                + " INNER JOIN @@DefinedTermBase@@ nrType ON nrType.id = nr.type_id "
                + " WHERE nrType.uuid = '264d2be4-e378-4168-9760-a9512ffbddc4'";
		SingleTermRemover.NewInstance(stepList, stepName, uuidTerm, checkUsedQueries, -99);

		//#9311
        changeSomeTermLabels(stepList);

        return stepList;
    }

	//#9311
    private void changeSomeTermLabels(List<ISchemaUpdaterStep> stepList) {

        String stepName = "Change abbrev label op. utique oppr.";
        String uuidTerm = "a5055d80-dbba-4660-b091-a1835d59fe7c";
        String description = null;
        String label = null;
        String abbrev = "op. utique oppr.";
        UUID uuidLanguage = Language.uuidLatin;
        boolean withIdInVoc = true;
        TermRepresentationUpdater.NewInstance(stepList, stepName, UUID.fromString(uuidTerm), description, label, abbrev, uuidLanguage, withIdInVoc);

        stepName = "Change abbrev label orth. cons.";
        uuidTerm = "34a7d383-988b-4117-b8c0-52b947f8c711";
        description = null;
        label = null;
        abbrev = "orth. cons.";
        uuidLanguage = Language.uuidLatin;
        withIdInVoc = true;
        TermRepresentationUpdater.NewInstance(stepList, stepName, UUID.fromString(uuidTerm), description, label, abbrev, uuidLanguage, withIdInVoc);

        stepName = "Change abbrev label orth. cons. prop.";
        uuidTerm = "02f82bc5-1066-454b-a023-11967cba9092";
        description = null;
        label = null;
        abbrev = "orth. cons. prop.";
        withIdInVoc = true;
        uuidLanguage = Language.uuidLatin;
        TermRepresentationUpdater.NewInstance(stepList, stepName, UUID.fromString(uuidTerm), description, label, abbrev, uuidLanguage, withIdInVoc);

        stepName = "Change label + desc. has misspelling => is correct spelling for";
        uuidTerm = "c6f9afcb-8287-4a2b-a6f6-4da3a073d5de";
        description = "is correct spelling for";
        label = "is correct spelling for";
        abbrev = null;
        uuidLanguage = Language.uuidEnglish;
        TermRepresentationUpdater.NewInverseInstance(stepList, stepName, UUID.fromString(uuidTerm), description, label, abbrev, uuidLanguage);

        stepName = "Change label + desc. 'has earlier isonym' => 'is earlier isonym'";
        uuidTerm = "29ab238d-598d-45b9-addd-003cf39ccc3e";
        description = "is earlier isonym of";
        label = "is earlier isonym of";
        abbrev = null;
        uuidLanguage = Language.uuidEnglish;
        TermRepresentationUpdater.NewInverseInstance(stepList, stepName, UUID.fromString(uuidTerm), description, label, abbrev, uuidLanguage);

        stepName = "Change label + desc. 'Conserved Desig' => 'Conservation Designated'";
        uuidTerm = "34a7d383-988b-4117-b8c0-52b947f8c711";
        description = "Conservation Designated";
        label = "Conservation designated";
        abbrev = null;
        uuidLanguage = Language.uuidLatin;  //not really true but all nom. status currently use Latin representations
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, UUID.fromString(uuidTerm), description, label, abbrev, uuidLanguage);
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5183_5184.NewInstance();
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}
}