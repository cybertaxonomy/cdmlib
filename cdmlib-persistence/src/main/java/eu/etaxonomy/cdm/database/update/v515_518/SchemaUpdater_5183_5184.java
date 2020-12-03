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
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 20.10.2020
 */
public class SchemaUpdater_5183_5184 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5183_5184.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_18_03;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_18_04;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5183_5184 NewInstance() {
		return new SchemaUpdater_5183_5184();
	}

	protected SchemaUpdater_5183_5184() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#6589
		ProtologMover.NewInstance(stepList);

		//#9263 use lower case for specimen type designation status
		updateSpecimenTypeDesignationStatus(stepList);

        return stepList;
    }

    //#9263 use lower case for specimen type designation status
    private void updateSpecimenTypeDesignationStatus(List<ISchemaUpdaterStep> stepList) {

        //holotype
        String stepName = "Holotype => holotype";
        UUID uuidTerm = UUID.fromString("a407dbc7-e60c-46ff-be11-eddf4c5a970d");
        UUID uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        String label = "holotype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //lectotype
        stepName = "Lectotype => lectotype";
        uuidTerm = UUID.fromString("05002d46-083e-4b27-8731-2e7c28a8825c");
        label = "lectotype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //isotype
        stepName = "Isotype => isotype";
        uuidTerm = UUID.fromString("93ef8257-0a08-47bb-9b36-542417ae7560");
        label = "isotype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //isolectotype
        stepName = "Isolectotype => isolectotype";
        uuidTerm = UUID.fromString("7a1a8a53-78f4-4fc0-89f7-782e94992d08");
        label = "isolectotype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //syntype
        stepName = "Syntype => syntype";
        uuidTerm = UUID.fromString("f3b60bdb-4638-4ca9-a0c7-36e77d8459bb");
        label = "syntype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //isosyntype
        stepName = "Isosyntype => isosyntype";
        uuidTerm = UUID.fromString("052a5ff0-8e9a-4355-b24f-5e4bb6071f44");
        label = "isosyntype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //neotype
        stepName = "Neotype => neotype";
        uuidTerm = UUID.fromString("26e13359-8f77-4e40-a85a-56c01782fce0");
        label = "neotype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //isoneotype
        stepName = "Isoneotype => isoneotype";
        uuidTerm = UUID.fromString("7afc2f4f-f70a-4aa5-80a5-87764f746bde");
        label = "isoneotype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //epitype
        stepName = "Epitype => epitype";
        uuidTerm = UUID.fromString("989a2715-71d5-4fbe-aa9a-db9168353744");
        label = "epitype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //isoepitype
        stepName = "Isoepitype => isoepitype";
        uuidTerm = UUID.fromString("95b90696-e103-4bc0-b60b-c594983fb566");
        label = "isoepitype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //paratype
        stepName = "Paratype => paratype";
        uuidTerm = UUID.fromString("eb7df2e5-d9a7-479d-970c-c6f2b0a761d7");
        label = "paratype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //isoparatype
        stepName = "Isoparatype => isoparatype";
        uuidTerm = UUID.fromString("497137f3-b614-4183-8a22-97fcd6e2bdd8");
        label = "isoparatype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //paralectotype
        stepName = "Paralectotype => paralectotype";
        uuidTerm = UUID.fromString("7244bc51-14d8-41a6-9524-7dc5303bba29");
        label = "paralectotype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //paraneotype
        stepName = "Paraneotype => paraneotype";
        uuidTerm = UUID.fromString("0c39e2a5-2fe0-4d4f-819a-f609b5340339");
        label = "paraneotype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //second step lectotype
        stepName = "Second Step Lectotype => second step lectotype";
        uuidTerm = UUID.fromString("01d91053-7004-4984-aa0d-9f4de59d6205");
        label = "second step lectotype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //second step neotype
        stepName = "Second Step Neotype => second step neotype";
        uuidTerm = UUID.fromString("8d2fed1f-242e-4bcf-bbd7-e85133e479dc");
        label = "second step neotype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //original material
        stepName = "original material => original material";
        uuidTerm = UUID.fromString("49c96cae-6be6-401e-9b36-1bc12d9dc8f9");
        label = "original material";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //iconotype
        stepName = "Iconotype => iconotype";
        uuidTerm = UUID.fromString("643513d0-32f5-46ba-840b-d9b9caf8160f");
        label = "iconotype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //phototype
        stepName = "Pphototype => phototype";
        uuidTerm = UUID.fromString("b7807acc-f559-474e-ad4a-e7a41e085e34");
        label = "phototype";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //unspecified
        stepName = "Unspecified => unspecified";
        uuidTerm = UUID.fromString("230fd762-b143-49de-ac2e-744bcc48a63b");
        label = "unspecified";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

        //unknown type category
        stepName = "Unknown Type Category => unknown type category";
        uuidTerm = UUID.fromString("7194020b-a326-4b47-9bfe-9f31a30aba7f");
        label = "unknown type category";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm,
                label, label, null, uuidLanguage);

    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5182_5183.NewInstance();
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
        return SchemaUpdater_5184_5185.NewInstance();
	}
}