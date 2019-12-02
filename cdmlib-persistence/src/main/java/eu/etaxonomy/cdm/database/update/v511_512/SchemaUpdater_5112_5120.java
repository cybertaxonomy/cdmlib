/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v511_512;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnTypeChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;

/**
 * @author a.mueller
 * @date 02.12.2019
 */
public class SchemaUpdater_5112_5120 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_5112_5120.class);

	private static final String endSchemaVersion = "5.12.0.0.20191202";
	private static final String startSchemaVersion = "5.11.2.0.20191109";

// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5112_5120 NewInstance() {
		return new SchemaUpdater_5112_5120();
	}

	protected SchemaUpdater_5112_5120() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;
		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#8738  Allow NULL on CdmLink.startPos
		stepName = "Allow NULL on CdmLink.startPos";
		tableName = "CdmLink";
		columnName = "startPos";
		ColumnTypeChanger.NewChangeAllowNullOnIntChanger(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

		//#8738  Allow NULL on CdmLink.endPos
        stepName = "Allow NULL on CdmLink.endPos";
        tableName = "CdmLink";
        columnName = "endPos";
        ColumnTypeChanger.NewChangeAllowNullOnIntChanger(stepList, stepName, tableName, columnName, INCLUDE_AUDIT);

        //#8702  Allow NULL on uuid on AUD tables
        stepName = "Allow NULL on uuid on AmplificationResult_AUD tables";
        tableName = "AmplificationResult_AUD";
        columnName = "uuid";
        ColumnTypeChanger.NewChangeAllowNullOnStringChanger(stepList, stepName, tableName, columnName, 36, !INCLUDE_AUDIT);

        stepName = "Allow NULL on uuid on CdmLink_AUD table";
        tableName = "CdmLink_AUD";
        columnName = "uuid";
        ColumnTypeChanger.NewChangeAllowNullOnStringChanger(stepList, stepName, tableName, columnName, 36, !INCLUDE_AUDIT);

        stepName = "Allow NULL on uuid on DnaQuality_AUD table";
        tableName = "DnaQuality_AUD";
        columnName = "uuid";
        ColumnTypeChanger.NewChangeAllowNullOnStringChanger(stepList, stepName, tableName, columnName, 36, !INCLUDE_AUDIT);

        stepName = "Allow NULL on uuid on Identifier_AUD table";
        tableName = "Identifier_AUD";
        columnName = "uuid";
        ColumnTypeChanger.NewChangeAllowNullOnStringChanger(stepList, stepName, tableName, columnName, 36, !INCLUDE_AUDIT);

        stepName = "Allow NULL on uuid on Registration_AUD table";
        tableName = "Registration_AUD";
        columnName = "uuid";
        ColumnTypeChanger.NewChangeAllowNullOnStringChanger(stepList, stepName, tableName, columnName, 36, !INCLUDE_AUDIT);

        stepName = "Allow NULL on uuid on SingleReadAlignment_AUD table";
        tableName = "SingleReadAlignment_AUD";
        columnName = "uuid";
        ColumnTypeChanger.NewChangeAllowNullOnStringChanger(stepList, stepName, tableName, columnName, 36, !INCLUDE_AUDIT);

        stepName = "Allow NULL on uuid on TaxonNodeAgentRelation_AUD table";
        tableName = "TaxonNodeAgentRelation_AUD";
        columnName = "uuid";
        ColumnTypeChanger.NewChangeAllowNullOnStringChanger(stepList, stepName, tableName, columnName, 36, !INCLUDE_AUDIT);

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
