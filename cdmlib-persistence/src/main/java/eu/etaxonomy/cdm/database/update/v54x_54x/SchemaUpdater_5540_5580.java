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

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ColumnValueUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SingleTermRemover;
import eu.etaxonomy.cdm.database.update.TableDropper;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 2024-10-17
 */
public class SchemaUpdater_5540_5580 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_54_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_58_00;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5490_5540.NewInstance();
    }

	public static SchemaUpdater_5540_5580 NewInstance() {
		return new SchemaUpdater_5540_5580();
	}

	SchemaUpdater_5540_5580() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#10956
        stepName = "Add wikidataID to DefinedTerms";
        String tableName = "DefinedTermBase";
        String columnName = "wikiDataItemId";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName, 16, INCLUDE_AUDIT);

        //#10522 remove AgentBase.contact
        stepName = "Remove AgentBase_contact_emailAddresses";
        tableName = "AgentBase_contact_emailAddresses";
        TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

        stepName = "Remove AgentBase_contact_faxNumbers";
        tableName = "AgentBase_contact_faxNumbers";
        TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

        stepName = "Remove AgentBase_contact_phoneNumbers";
        tableName = "AgentBase_contact_phoneNumbers";
        TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

        stepName = "Remove AgentBase_contact_urls";
        tableName = "AgentBase_contact_urls";
        TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

        //#10522 Annotations versionable only
        stepName = "Remove Annotation_Annotation";
        tableName = "Annotation_Annotation";
        TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

        stepName = "Remove Annotation_Marker";
        tableName = "Annotation_Marker";
        TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

        //#10522 remove storedUnder
        stepName = "Remove DerivedUnit.storedUnder";
        tableName = "SpecimenOrObservationBase";
        columnName = "storedUnder_id";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        //#10217, #10216 mark old passwords
        stepName = "Mark old passwords";
        tableName = "UserAccount";
        columnName = "password";
        ColumnValueUpdater.NewPrefixAdderInstance(stepList, stepName, tableName, columnName, "{md5}", "password NOT LIKE '{%}%'", !INCLUDE_AUDIT);

        //#10217, revert #7210
        stepName = "Remove User.salt";
        tableName = "UserAccount";
        columnName = "salt";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, !INCLUDE_AUDIT);

        //#10924 add is autonym tri-state flag
        stepName = "Add autonym to TaxonName";
        tableName = "TaxonName";
        String newColumnName = "autonymFlag";
        int length = 10;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, length, INCLUDE_AUDIT);

        //#10924 set default for is autonym
        stepName = "Set isAutonym to indetermined";
        tableName = "TaxonName";
        columnName = "autonymFlag";
        String where = null;
        ColumnValueUpdater.NewStringInstance(stepList, stepName, tableName, newColumnName, "I", where, INCLUDE_AUDIT);

        //#10877 remove 'Protologue' name feature
        stepName = "remove 'Protologue' name feature";
        UUID uuidProtologue = UUID.fromString("71b356c5-1e3f-4f5d-9b0f-c2cf8ae7779f");
        where =   " SELECT count(*) "
                + " FROM DescriptionElementBase deb INNER JOIN DefinedTermBase f ON deb.feature_id = f.id "
                + " WHERE f.uuid = '"+uuidProtologue+"'";
        SingleTermRemover.NewInstance(stepList, stepName, uuidProtologue, where);

        //#10877 remove 'protologue' from CdmPreference values
        stepName = "remove 'protologue' from CdmPreference values";
        String sql = "UPDATE CdmPreference "
                + " SET value = REPLACE(value, ';71b356c5-1e3f-4f5d-9b0f-c2cf8ae7779f', '')"
                + " WHERE value like '%71b356c5-1e3f-4f5d-9b0f-c2cf8ae7779f%' ";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, sql);

        sql = "UPDATE CdmPreference "
                + " SET value = REPLACE(value, '71b356c5-1e3f-4f5d-9b0f-c2cf8ae7779f;', '')"
                + " WHERE value like '%71b356c5-1e3f-4f5d-9b0f-c2cf8ae7779f%' ";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, sql);

        sql = "DELETE FROM CdmPreference "
                + " WHERE value = '71b356c5-1e3f-4f5d-9b0f-c2cf8ae7779f' ";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, sql);

        return stepList;
    }
}