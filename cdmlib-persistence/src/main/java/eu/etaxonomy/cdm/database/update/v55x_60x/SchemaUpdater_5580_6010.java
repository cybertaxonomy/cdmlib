/**
 * Copyright (C) 2024 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v55x_60x;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnRemover;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.v540_558.SchemaUpdater_5540_5580;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 2026-09-18
 */
public class SchemaUpdater_5580_6010 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_58_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_06_01_00;

// ********************** FACTORY METHOD *************************************

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5540_5580.NewInstance();
    }

	public static SchemaUpdater_5580_6010 NewInstance() {
		return new SchemaUpdater_5580_6010();
	}

	SchemaUpdater_5580_6010() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#3556
        // Pre-check before any schema changes: each MediaRepresentation must have ≤1 part
        MediaRepresentationPartSinglePartChecker.NewInstance(stepList);


        //#3556
        // Merge MediaRepresentationPart into MediaRepresentation
        stepName = "Add DTYPE to MediaRepresentation";
        String tableName = "MediaRepresentation";
        ColumnAdder.NewDTYPEInstance(stepList, stepName, tableName, "MediaRepresentation", INCLUDE_AUDIT);

        stepName = "Add uri to MediaRepresentation";
        ColumnAdder.NewClobInstance(stepList, stepName, tableName, "uri", INCLUDE_AUDIT);

        stepName = "Add size to MediaRepresentation";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, "size", INCLUDE_AUDIT, null, !NOT_NULL);

        stepName = "Add height to MediaRepresentation";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, "height", INCLUDE_AUDIT, null, !NOT_NULL);

        stepName = "Add width to MediaRepresentation";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, "width", INCLUDE_AUDIT, null, !NOT_NULL);

        stepName = "Add duration to MediaRepresentation";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, "duration", INCLUDE_AUDIT, null, !NOT_NULL);

        MediaRepresentationPartMerger.NewInstance(stepList);
        //drop tables will be executed in next updater

        //#10922
        stepName = "Add gender to taxon name";
        tableName = "TaxonName";
        String columnName = "gender";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName, 1, INCLUDE_AUDIT);

        //#11040
        stepName = "Add revisionStatus.changed to TaxonBase";
        tableName = "TaxonBase";
        columnName = "revisionStatus_changed";
        ColumnAdder.NewDateTimeInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT, !NOT_NULL);

        stepName = "Add revisionStatus.status to TaxonBase";
        columnName = "revisionStatus_status_id";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT, !NOT_NULL, "DefinedTermBase");

        //#11039 replace TaxonBase.doubtful boolean by TaxonStatus enum
        stepName = "Add TaxonBase.taxonStatus column";
        tableName = "TaxonBase";
        columnName = "taxonStatus";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, columnName, 10, INCLUDE_AUDIT);

        stepName = "Set TaxonBase.taxonStatus=DOU for doubtful=true";
        String sql = "UPDATE @@TaxonBase@@ SET taxonStatus = 'DOU' WHERE doubtful = @TRUE@";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        stepName = "Set TaxonBase.taxonStatus=OK for doubtful=false";
        sql = "UPDATE @@TaxonBase@@ SET taxonStatus = 'OK' WHERE doubtful = @FALSE@ OR doubtful IS NULL OR taxonStatus IS NULL";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        stepName = "Remove TaxonBase.doubtful";
        columnName = "doubtful";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        //#11035 update inverseSymbol misapplied names
        // (mathematical minus U+2212, not hyphen or en-dash)
        stepName = "Update inverseSymbol for partial misapplied name";
        tableName = "DefinedTermBase";
        sql = "UPDATE @@DefinedTermBase@@ "
                + " SET inverseSymbol = '" + UTF8.MINUS + "' "
                + " WHERE uuid = '1ed87175-59dd-437e-959e-0d71583d8417'";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        stepName = "Update inverseSymbol for partial misapplied name";
        tableName = "DefinedTermBase";
        sql = "UPDATE @@DefinedTermBase@@ "
                + " SET inverseSymbol = '" + UTF8.MINUS + "(part.)' "
                + " WHERE uuid = '859fb615-b0e8-440b-866e-8a19f493cd36'";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        stepName = "Update inverseSymbol for pro parte misapplied name";
        sql = "UPDATE @@DefinedTermBase@@ "
                + " SET inverseSymbol = '" + UTF8.MINUS + "(p.p.)' "
                + " WHERE uuid = 'b59b4bd2-11ff-45d1-bae2-146efdeee206'";
        SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, sql, tableName);

        //#10974
        stepName = "Remove accessed from Reference";
        tableName = "Reference";
        columnName = "accessed";
        ColumnRemover.NewInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        return stepList;
    }
}