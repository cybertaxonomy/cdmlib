/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v512_515;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.Float2BigDecimalTypeChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TermRepresentationUpdater;
import eu.etaxonomy.cdm.database.update.v511_512.SchemaUpdater_5112_5120;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData.CdmVersion;

/**
 * @author a.mueller
 * @date 02.12.2019
 */
public class SchemaUpdater_5120_5150 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5120_5150.class);

	private static final CdmVersion startSchemaVersion = CdmVersion.V_05_12_00;
	private static final CdmVersion endSchemaVersion = CdmVersion.V_05_15_00;

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5120_5150 NewInstance() {
		return new SchemaUpdater_5120_5150();
	}

	protected SchemaUpdater_5120_5150() {
		super(startSchemaVersion.versionString(), endSchemaVersion.versionString());
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;
		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

	    //#8964 update label for later homonym
        stepName = "update label for later homonym";
        UUID uuidTerm = UUID.fromString("80f06f65-58e0-4209-b811-cb40ad7220a6");
        String label = "is later homonym of";
        UUID uuidLanguage = Language.uuidEnglish;
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName,
                uuidTerm, label, label, null, uuidLanguage);

        stepName = "update label for treated as later homonym";
        uuidTerm = UUID.fromString("2990a884-3302-4c8b-90b2-dfd31aaa2778");
        label = "is treated as later homonym of";
        uuidLanguage = Language.uuidEnglish;
        TermRepresentationUpdater.NewInstanceWithTitleCache(stepList, stepName,
                uuidTerm, label, label, null, uuidLanguage);

        //#8978
        stepName = "make statistical measurment value BigDecimal";
        tableName = "StatisticalMeasurementValue";
        columnName = "value";
        String scaleColumnName = "value_scale";
        int newPrecision = 18;
        int newScale = 9;
//        ColumnTypeChanger.NewFloat2BigDecimalInstance(stepList, stepName, tableName, columnName, scaleColumnName, newPrecision, newScale, INCLUDE_AUDIT);
        Float2BigDecimalTypeChanger.NewInstance(stepList, stepName, tableName, columnName, scaleColumnName, newPrecision, newScale, INCLUDE_AUDIT);

        //#8802
        tableName = "DescriptionElementBase";
        stepName = "add period_start to DescriptionElementBase";
        newColumnName = "period_start";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);
        stepName = "add period_endt to DescriptionElementBase";
        newColumnName = "period_end";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);
        stepName = "add period_extremestart to DescriptionElementBase";
        newColumnName = "period_extremestart";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);
        stepName = "add period_extremeend to DescriptionElementBase";
        newColumnName = "period_extremeend";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);
        stepName = "add period_freetext to DescriptionElementBase";
        newColumnName = "period_freetext";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT);

        return stepList;
    }

    @Override
    public ISchemaUpdater getPreviousUpdater() {
        return SchemaUpdater_5112_5120.NewInstance();
    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}
}
