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

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
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
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_18_05;

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

        //#9282
        stepName = "Add nomenclatural standing enumeration";
        String tableName = "DefinedTermBase";
        String newColumnName = "nomenclaturalStanding";
        int length = 10;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        stepName = "Add inverse nomenclatural standing for name relationship";
        tableName = "DefinedTermBase";
        newColumnName = "nomenclaturalStandingInverse";
        length = 10;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        setNomenclaturalStanding(stepList);

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


    private void setNomenclaturalStanding(List<ISchemaUpdaterStep> stepList) {

        //default NONE
        String stepName = "Set nom status and name relationship types nomenclatural standing to NONE";
        String defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET nomenclaturalStanding = 'NO' "
                + " WHERE termType IN ('NST','NRT') ";
        String nonAuditedTableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName, -99);
//        + "     '82bab006-5aed-4301-93ec-980deb30cbb1',"   //status cons. prop.
//        + "     '02f82bc5-1066-454b-a023-11967cba9092',"   //status orth. cons. prop.
        //nom. nov., comb. nov.
        //basionym, replaced synonym
        //subnudum, nom. dub.

        //unclear:
        //is validated by, is later validated by,
        //is blocking name for, is emendation for
        //is alternative name for
        //status(+relation) orthography conserved (this is more a name property than a status,
        //    unclear if result is illegitimate or not, but it is expected to be a name not a designation, so in future it may become the status of a common super class)

        //VALID
        stepName = "Set nom status and name relationship types to VALID where appropriate";
        defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET nomenclaturalStanding = 'VA' "
                + " WHERE uuid IN ('bd036217-5499-4ccd-8f4c-72e06158db93',"   //status valid
                + "     '51a3613c-b53b-4561-b0cd-9163d91c15aa',"   //status legitimate
                + "     '6330f719-e2bc-485f-892b-9f882058a966',"   //status(+relation) conserved
                + "     'e6439f95-bcac-4ebb-a8b5-69fa5ce79e6a',"   //relation conserved against
                + "     '1afe55c4-76aa-46c0-afce-4dc07f512733',"   //status sanctioned
                + "     'd071187a-512d-4955-b75c-d1706702f098',"   //status(+relation) protected
                + "     '3b8a8519-420f-4dfa-b050-b410cc257961',"   //status(+relation) nom. alt.
                + "     '643ee07f-026c-426c-b838-c778c8613383',"   //status nom. utique rej. prop.
                + "     '248e44c2-5436-4526-a352-f7467ecebd56',"   //status nom. rej. prop.
                + "     '4e9c9702-a74d-4033-9d47-792ad123712c',"   //status nom. cons. des.
                + "     '049c6358-1094-4765-9fae-c9972a0e7780'"   //relation is alternative name for
                + "    ) ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName, -99);

        //ILLEGITIMATE
        stepName = "Set nom status and name relationship types to ILLEGITIMATE where appropriate";
        defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET nomenclaturalStanding = 'IL' "
                + " WHERE uuid IN ('b7c544cf-a375-4145-9d3e-4b97f3f18108',"   //status nom. illeg.
                + "     'd901d455-4e01-45cb-b653-01a840b97eed',"   //status comb. illeg.
                + "     '6890483a-c6ba-4ae1-9ab1-9fbaa5736ce9',"   //status nom. superfl.
                + "     '80f06f65-58e0-4209-b811-cb40ad7220a6',"   //relation later homonym
                + "     '2990a884-3302-4c8b-90b2-dfd31aaa2778',"   //relation treated as later homonym
                + "     '2bef7039-c129-410b-815e-2a1f7249127b',"   //status zoo invalid
                + "     'a61602c7-fbd4-4eb4-98a2-44919db8920b'"    //status zoo suppressed
                + "    ) ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName, -99);

        //INVALID
        stepName = "Set nom status and name relationship types to INVALID where appropriate";
        defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET nomenclaturalStanding = 'IN' "
                + " WHERE uuid IN ('b09d4f51-8a77-442a-bbce-e7832aaf46b7',"   //status nom. inval.
                + "     'f858e619-7b7f-4225-913b-880a2143ec83',"   //status comb. inval.
                + "     'e0d733a8-7777-4b27-99a3-05ab50e9f312',"   //status nom. nud.
                + "     'a277507e-ad93-4978-9419-077eb889c951',"   //status nom. prov.
                + "     'a5055d80-dbba-4660-b091-a1835d59fe7c',"   //status op. utique oppr.
                + "     'f080cee4-6e0a-466f-986e-bad59e9f4ea7',"   //status in sched.
                + "     '54900d07-a18f-4e11-b4be-3929bb78416a',"   //status pro syn.
                + "     '6d9ed462-b761-4da3-9304-4749e883d4eb'"    //status not available (zool.)
                + "    ) ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName, -99);

        //OTHER DESIGNATIONS
        stepName = "Set nom status and name relationship types to OTHER DESIGNATIONS where appropriate";
        defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET nomenclaturalStanding = 'OD' "
                + " WHERE uuid IN ('29ab238d-598d-45b9-addd-003cf39ccc3e',"   //relation later isonym
                + "     'edff8771-4983-4814-a9c3-2255d8dc963e',"   //relation later citation
                + "     '48107cc8-7a5b-482e-b438-efbba050b851',"   //status(+relation) rejected
                + "     '04338fdd-c12a-402f-a1ca-68b4bf0be042',"   //status(+relation) utique rejected
                + "     '39a25673-f716-4ec7-ae27-2498fce43166',"   //relation orth. rej.
                + "     '24955174-aa5c-4e71-a2fd-3efc79e885db',"   //status nom. confus.
                + "     '51429574-c6f9-4aa1-bab9-0bbc5b160ba1',"   //status nom. ined.
                + "     '90f5012b-705b-4488-b4c6-002d2bc5198e',"   //status nom. ambig.
                + "     'eeaea868-c4c1-497f-b9fe-52c9fc4aca53',"   //relation orth. var.
                + "     'c6f9afcb-8287-4a2b-a6f6-4da3a073d5de',"   //relation misspelling for
                + "     '6a6f7a88-991f-4f76-8ce9-4110839fae8b' "   //status(+relation) nom. oblitum (zool.)
                + "    ) ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName, -99);

        //SET inverse nomenclatural standing
        //default NONE
        stepName = "Set name relationship types inverse nomenclatural standing to NONE";
        defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET nomenclaturalStandingInverse = 'NO' "
                + " WHERE termType IN ('NRT') ";
        nonAuditedTableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName, -99);

        //VALID
        stepName = "Set inverse name relationship types to VALID where appropriate";
        defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET nomenclaturalStandingInverse = 'VA' "
                + " WHERE uuid IN ('049c6358-1094-4765-9fae-c9972a0e7780',"   //relation is alternative name for
                + "        'e6439f95-bcac-4ebb-a8b5-69fa5ce79e6a'"   //relation rejected in favor of
                + "    ) ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName, -99);

        //OTHER DESIGNATION
        stepName = "Set inverse name relationship types to OTHER DESIGNATION where appropriate";
        defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET nomenclaturalStandingInverse = 'OD' "
                + " WHERE uuid IN ("
                + "        'e6439f95-bcac-4ebb-a8b5-69fa5ce79e6a'"   //relation conserved against
                + "    ) ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName, -99);

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