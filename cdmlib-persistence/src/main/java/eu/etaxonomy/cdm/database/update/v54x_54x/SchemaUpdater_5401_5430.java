/**
 * Copyright (C) 2023 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v54x_54x;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SingleTermRemover;
import eu.etaxonomy.cdm.database.update.v538_540.SchemaUpdater_5400_5401;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 2024-05-31
 */
public class SchemaUpdater_5401_5430 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_40_01;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_43_00;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5400_5401.NewInstance();
    }

	public static SchemaUpdater_5401_5430 NewInstance() {
		return new SchemaUpdater_5401_5430();
	}

	SchemaUpdater_5401_5430() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#10511 set wfo-id pattern
		stepName = "set wfo-id pattern";
		String sql = "UPDATE DefinedTermBase "
		        + " SET urlPattern='https://www.worldfloraonline.org/taxon/{@ID}' "
		        + " WHERE uuid = '048e0cf9-f59c-42dd-bfeb-3a5cba0191c7'";
		tableName = "DefinedTermBase";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

		//#10511 set IF (index fungorum) id pattern
        stepName = "set IF (index fungorum) id pattern";
        sql = "UPDATE DefinedTermBase "
                + " SET urlPattern='https://www.indexfungorum.org/names/NamesRecord.asp?RecordID={@ID}' "
                + " WHERE uuid = 'f405be9f-359a-49ba-b09b-4a7920386190'";
        tableName = "DefinedTermBase";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        //#10489 Deduplicate Etymology
        stepName = "deduplicate etymology";
        sql = "     SELECT count(*) as n "
                + " FROM DescriptionElementBase deb INNER JOIN DefinedTermBase dtb ON deb.feature_id = dtb.id "
                + " WHERE dtb.uuid = 'dd653d48-355c-4aec-a4e7-724f6eb29f8d' ";
        String uuidTerm = "dd653d48-355c-4aec-a4e7-724f6eb29f8d";
        SingleTermRemover.NewInstance(stepList, stepName, uuidTerm, sql);

        return stepList;
    }
}