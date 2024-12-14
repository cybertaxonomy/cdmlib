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
public class SchemaUpdater_5461_5480 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_46_01;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_48_00;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5460_5461.NewInstance();
    }

	public static SchemaUpdater_5461_5480 NewInstance() {
		return new SchemaUpdater_5461_5480();
	}

	SchemaUpdater_5461_5480() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#10612
		//Set nomenclatural standing to NO for nom. rej.
        stepName = "Set nomenclatural standing to NO for nom. rej.";
        String nonAuditedTableName = "DefinedTermBase";
        String defaultQuery = "UPDATE @@DefinedTermBase@@ "
                + " SET nomenclaturalStanding = 'NO' "
                + " WHERE uuid IN ( "
                + "     '48107cc8-7a5b-482e-b438-efbba050b851'"   //status nom. rej.
                + "    ) ";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, defaultQuery, nonAuditedTableName);

        //#10614 Update label for language Spanish
        stepName = "Update label for language Spanish";
        UUID uuidSpanish = UUID.fromString("511d8125-f5e6-445d-aee2-6327375238be");
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidSpanish,
                "Spanish", "Spanish", null, UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523"));

        return stepList;
    }
}