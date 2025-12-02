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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.NotNullUpdater;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 2024-10-17
 */
public class SchemaUpdater_5490_5540 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_49_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_54_00;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5480_5490.NewInstance();
    }

	public static SchemaUpdater_5490_5540 NewInstance() {
		return new SchemaUpdater_5490_5540();
	}

	SchemaUpdater_5490_5540() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#10834
        stepName = "Rename Registration.institution -> registrationCenter";
        String tableName = "Registration";
        String oldName = "institution_id";
        String newName = "registrationCenter_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldName, newName, INCLUDE_AUDIT);

        //#10103
        stepName = "Set 'included' as default status for taxon node status";
        String sql = "UPDATE TaxonNode SET status = 'INC' WHERE status IS NULL";
        tableName = "TaxonNode";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        stepName = "Set TaxonNode.status to NOT NULL";
        String columnName = "status";
        NotNullUpdater.NewStringInstance(stepList, stepName, tableName, columnName, !INCLUDE_AUDIT);

        //#9673
        stepName = "Set '0' as default value for CdmPreference.allowOverride";
        sql = "UPDATE CdmPreference SET allowOverride = 0 WHERE allowOverride IS NULL";
        tableName = "CdmPreference";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        stepName = "Set CdmPreference.allowOverride to NOT NULL";
        columnName = "allowOverride";
        NotNullUpdater.NewBooleanInstance(stepList, stepName, tableName, columnName, !INCLUDE_AUDIT);


        return stepList;
    }
}