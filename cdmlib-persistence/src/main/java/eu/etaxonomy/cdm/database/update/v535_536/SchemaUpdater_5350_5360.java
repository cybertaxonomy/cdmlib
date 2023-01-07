/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v535_536;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.VocabularyRemover;
import eu.etaxonomy.cdm.database.update.v500_535.SchemaUpdater_5330_5350;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 06.01.2023
 */
public class SchemaUpdater_5350_5360 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_35_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_36_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5350_5360 NewInstance() {
		return new SchemaUpdater_5350_5360();
	}

	protected SchemaUpdater_5350_5360() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5330_5350.NewInstance();
    }

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#10201 fix syntype for accepted taxa (see 5.33-5.35 updater)
        stepName = "remove synonym type from accepted taxa";
		String sql = " UPDATE @@TaxonBase@@ "
                + " SET type = NULL "
                + " WHERE DTYPE <> 'Synonym' ";
        String tableName = "TaxonBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        //NOTE: only needs to run on test and integration as it was missing there originally
        //      !!! Still need to check what happens if the vocabulary is already removed  !!!
        //#10201 remove syntype vocabulary
        stepName = "remove synonym type vocabulary";
        UUID vocUuid = UUID.fromString("48917fde-d083-4659-b07d-413db843bd50");
        VocabularyRemover.NewInstance(stepList, stepName, vocUuid.toString(), null);

		return stepList;
    }
}