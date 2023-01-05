/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v529_532;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.database.update.UniqueIndexDropper;
import eu.etaxonomy.cdm.database.update.v527_529.SchemaUpdater_5271_5290;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 08.07.2022
 */
public class SchemaUpdater_5290_5320 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_29_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_32_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5290_5320 NewInstance() {
		return new SchemaUpdater_5290_5320();
	}

	protected SchemaUpdater_5290_5320() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5271_5290.NewInstance();
    }

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#10083 Update inverse representation of 'is blocking name for'
		stepName = "Update inverse representation of 'is blocking name for'";
		UUID uuidTerm = UUID.fromString("1dab357f-2e12-4511-97a4-e5153589e6a6");
		String description = "has blocking name";
		String label = "has blocking name";
		String abbrev = null;
		UUID uuidEnglish = Language.uuidEnglish;
		TermRepresentationUpdater.NewInverseInstance(stepList, stepName, uuidTerm, description, label, abbrev, uuidEnglish);

	    //#10057 add accessed columns to OriginalSourceBase
        stepName = "Add accessed_start to OriginalSourceBase";
        tableName = "OriginalSourceBase";
        String newColumnName = "accessed_start";
        int size = 50;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, size, INCLUDE_AUDIT);

        stepName = "Add accessed_end to OriginalSourceBase";
        newColumnName = "accessed_end";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, size, INCLUDE_AUDIT);

        stepName = "Add accessed_freetext to OriginalSourceBase";
        newColumnName = "accessed_freetext";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        //#10057 add timePeriod columns to Credit
        tableName = "Credit";
        stepName = "Add timePeriod_start to Credit";
        newColumnName = "timePeriod_start";
        size = 50;
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, size, INCLUDE_AUDIT);

        stepName = "Add timePeriod_end to Credit";
        newColumnName = "timePeriod_end";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, size, INCLUDE_AUDIT);

        stepName = "Add timePeriod_freetext to Credit";
        newColumnName = "timePeriod_freetext";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        //#10097
        stepName = "Rename originalNameString to originalInfo";
        tableName = "OriginalSourceBase";
        String oldColumnName = "originalNameString";
        newColumnName = "originalInfo";
        size = 255;
        ColumnNameChanger.NewVarCharInstance(stepList, stepName, tableName, oldColumnName, newColumnName, size, INCLUDE_AUDIT);

        //#9901 Remove unique key Media_RightsInfo.rights_id
        stepName = "Remove unique key Media_RightsInfo.rights_id";
        tableName = "Media_RightsInfo";
        String indexColumn = "rights_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, indexColumn, INCLUDE_AUDIT);

        //#9830 Add accent to México Distrito Federal
        stepName = "";
        uuidTerm = UUID.fromString("565751f1-613e-4ddc-bfbb-4b54f2267971");
        description = "México Distrito Federal";
        label = "México Distrito Federal";
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName, uuidTerm, description, label, abbrev, uuidEnglish);

        //#9785 Add missing unit_ids
        stepName = "Add missing unit_ids";
        String query = "UPDATE DescriptionElementBase deb LEFT OUTER JOIN DefinedTermBase fe ON fe.id = deb.feature_id "
                + " SET deb.unit_id = ("
                + "        SELECT MN2.recommendedMeasurementUnits_id"
                + "        FROM DefinedTermBase fe2 INNER JOIN DefinedTermBase_MeasurementUnit MN2 ON MN2.Feature_id = fe2.id"
                + "        WHERE fe.id = fe2.id AND (fe2.DTYPE = 'Feature' OR fe2.DTYPE = 'Character')"
                + "        GROUP BY fe2.id"
                + "        HAVING COUNT(*) = 1"
                + ") "
                + " WHERE deb.DTYPE = 'QuantitativeData' AND deb.unit_id IS NULL "
                + " AND fe.id IN ("
                + "    SELECT fe.id "
                + "    FROM DefinedTermBase fe INNER JOIN DefinedTermBase_MeasurementUnit MN ON MN.Feature_id = fe.id INNER JOIN DefinedTermBase mu ON MN.recommendedMeasurementUnits_id = mu.id"
                + "    WHERE fe.DTYPE = 'Feature' OR fe.DTYPE = 'Character'"
                + "    GROUP BY fe.id, mu.id"
                + "    HAVING COUNT(*) = 1"
                + ")";
        SimpleSchemaUpdaterStep.NewNonAuditedInstance(stepList, stepName, query)
             .withErrorRecovery("SQL statement for adding missing measurement unit_ids failed");

		return stepList;
    }
}