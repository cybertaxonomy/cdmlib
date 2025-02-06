/**
 * Copyright (C) 2024 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v54x_54x;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 2024-10-17
 */
public class SchemaUpdater_5480_5490 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_48_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_49_00;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5461_5480.NewInstance();
    }

	public static SchemaUpdater_5480_5490 NewInstance() {
		return new SchemaUpdater_5480_5490();
	}

	SchemaUpdater_5480_5490() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#10657
        //Update symbol for MAN
        stepName = "Update symbol for MAN";
        String nonAuditedTableName = "DefinedTermBase";
        String defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol = 'misapplied for' "
                + " WHERE uuid = '1ed87175-59dd-437e-959e-0d71583d8417' ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName);

        stepName = "Update abbrev label for MAN";
        UUID uuidTerm = UUID.fromString("1ed87175-59dd-437e-959e-0d71583d8417");
        UUID uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        String abbrevLabel = "misapplied for";
        TermRepresentationUpdater.NewInstance(stepList, stepName, uuidTerm, null, null, abbrevLabel, uuidLanguage);

        //Update symbol p.p. MAN
        stepName = "Update symbol for p.p. MAN";
        nonAuditedTableName = "DefinedTermBase";
        defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol = 'p.p. misapplied for' "
                + " WHERE uuid = 'b59b4bd2-11ff-45d1-bae2-146efdeee206' ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName);

        stepName = "Update abbrev label for p.p. MAN";
        uuidTerm = UUID.fromString("b59b4bd2-11ff-45d1-bae2-146efdeee206");
        uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        abbrevLabel = "p.p. misapplied for";
        TermRepresentationUpdater.NewInstance(stepList, stepName, uuidTerm, null, null, abbrevLabel, uuidLanguage);


        //Update symbol part. MAN
        stepName = "Update symbol for part. MAN";
        nonAuditedTableName = "DefinedTermBase";
        defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol = 'part. misapplied for' "
                + " WHERE uuid = '9d7a5e56-973c-474c-b6c3-a1cb00833a3c' ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName);

        stepName = "Update abbrev label for part. MAN";
        uuidTerm = UUID.fromString("9d7a5e56-973c-474c-b6c3-a1cb00833a3c");
        uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        abbrevLabel = "part. misapplied for";
        TermRepresentationUpdater.NewInstance(stepList, stepName, uuidTerm, null, null, abbrevLabel, uuidLanguage);

        //Update symbol part. synonym for
        stepName = "Update symbol for part. synoynm for";
        nonAuditedTableName = "DefinedTermBase";
        defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol = 'part. for' "
                + " WHERE uuid = '859fb615-b0e8-440b-866e-8a19f493cd36' ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName);

        stepName = "Update abbrev label for part. synonym for";
        uuidTerm = UUID.fromString("859fb615-b0e8-440b-866e-8a19f493cd36");
        uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        abbrevLabel = "part. for";
        TermRepresentationUpdater.NewInstance(stepList, stepName, uuidTerm, null, null, abbrevLabel, uuidLanguage);

        //Update symbol p.p. synonym for
        stepName = "Update symbol for p.p. synoynm for";
        nonAuditedTableName = "DefinedTermBase";
        defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET symbol = 'p.p. for' "
                + " WHERE uuid = '8a896603-0fa3-44c6-9cd7-df2d8792e577' ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName);

        stepName = "Update abbrev label for p.p. synonym for";
        uuidTerm = UUID.fromString("8a896603-0fa3-44c6-9cd7-df2d8792e577");
        uuidLanguage = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
        abbrevLabel = "p.p. for";
        TermRepresentationUpdater.NewInstance(stepList, stepName, uuidTerm, null, null, abbrevLabel, uuidLanguage);


        return stepList;
    }
}