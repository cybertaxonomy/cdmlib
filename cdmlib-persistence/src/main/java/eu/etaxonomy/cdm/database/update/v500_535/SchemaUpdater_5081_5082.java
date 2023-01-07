/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v500_535;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableCreator;

/**
 * @author a.mueller
 * @date 14.08.2019
 */
public class SchemaUpdater_5081_5082 extends SchemaUpdaterBase {

	private static final String endSchemaVersion = "5.8.2.0.20190815";
	private static final String startSchemaVersion = "5.8.1.0.20190813";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_5081_5082 NewInstance() {
		return new SchemaUpdater_5081_5082();
	}

	protected SchemaUpdater_5081_5082() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String newColumnName;
		String oldColumnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

		//#8441
		stepName = "Add FeatureState";
		tableName = "FeatureState";
        String[] columnNames = new String[]{"feature_id","state_id"};
        String[] columnTypes = new String[]{"int","int"};
        String[] referencedTables = new String[]{"DefinedTermBase","DefinedTermBase"};
		TableCreator.NewVersionableInstance(stepList, stepName, tableName,
		        columnNames, columnTypes, referencedTables, INCLUDE_AUDIT);

		stepName = "Add TermNode_InapplicableIf";
		tableName = "TermNode_InapplicableIf";
		columnNames = new String[]{"TermNode_id","inapplicableIf_id"};
        columnTypes = new String[]{"int","int"};
        referencedTables = new String[]{"TermRelation","FeatureState"};
        boolean includeCdmBaseAttributes = false;
		TableCreator.NewInstance(stepList, stepName, tableName, columnNames,
		        columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes)
		   .setPrimaryKeyParams("TermNode_id,inapplicableIf_id", "REV, TermNode_id, inapplicableIf_id");

        stepName = "Add TermNode_OnlyapplicableIf";
        tableName = "TermNode_OnlyApplicableIf";
        columnNames = new String[]{"TermNode_id","onlyApplicableIf_id"};
        columnTypes = new String[]{"int","int"};
        referencedTables = new String[]{"TermRelation","FeatureState"};
        includeCdmBaseAttributes = false;
        TableCreator.NewInstance(stepList, stepName, tableName, columnNames,
                columnTypes, referencedTables, INCLUDE_AUDIT, includeCdmBaseAttributes)
            .setPrimaryKeyParams("TermNode_id,onlyApplicableIf_id", "REV, TermNode_id, onlyApplicableIf_id");

        //
        stepName = "Rename column TermNode_DefinedTermBase_InapplicableIf.inapplicableIf_id";
        tableName = "TermNode_DefinedTermBase_InapplicableIf";
        oldColumnName = "inapplicableIf_id";
        newColumnName = "inapplicableIf_old_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //
        stepName = "Rename column TermNode_DefinedTermBase_OnlyApplicable.onlyApplicableIf_id";
        tableName = "TermNode_DefinedTermBase_OnlyApplicable";
        oldColumnName = "onlyApplicableIf_id";
        newColumnName = "onlyApplicableIf_old_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //#7957 Add type(s) to DescriptionBase
        stepName = "Add types to DescriptionBase";
        tableName = "DescriptionBase";
        newColumnName ="types";
        ColumnAdder.NewStringInstance(stepList, stepName, tableName, newColumnName, 255, "#", INCLUDE_AUDIT).setNotNull(true);

        return stepList;
    }

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_508_5081.NewInstance();
	}
}