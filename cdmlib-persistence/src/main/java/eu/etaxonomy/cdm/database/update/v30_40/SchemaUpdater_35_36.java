/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v30_40;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ColumnNameChanger;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.MnTableRemover;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableCreator;
import eu.etaxonomy.cdm.database.update.UniqueIndexDropper;

/**
 * @author a.mueller
 * @since Mar 01, 2015
 */
public class SchemaUpdater_35_36 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_35_36.class);
	private static final String startSchemaVersion = "3.5.0.0.201531030000";
	private static final String endSchemaVersion = "3.6.0.0.201527040000";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_35_36 NewInstance() {
		return new SchemaUpdater_35_36();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_35_36() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
//		String columnName;
		String newColumnName;
		String oldColumnName;
		String columnNames[];
		String referencedTables[];
		String columnTypes[];
//		boolean includeCdmBaseAttributes = false;

		List<ISchemaUpdaterStep> stepList = new ArrayList<>();

        //#4843
        //Allow NULL for DefinedTermBase_SupportedCategoricalEnumeration
		//.supportedcategoricalenumerations_id
        stepName = "Remove NOT NULL from supportedcategoricalenumerations_id";
        tableName = "DefinedTermBase_SupportedCategoricalEnumeration";
        oldColumnName = "supportedcategoricalenumerations_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, oldColumnName, !INCLUDE_AUDIT);

        //#4843
        //Allow NULL for DefinedTermBase_RecommendedModifierEnumeration
        //.recommendedmodifierenumeration_id
        stepName = "Remove NOT NULL from recommendedmodifierenumeration_id";
        tableName = "DefinedTermBase_RecommendedModifierEnumeration";
        oldColumnName = "recommendedmodifierenumeration_id";
        UniqueIndexDropper.NewInstance(stepList, tableName, oldColumnName, ! INCLUDE_AUDIT);

        //add hasMoreMembers
        stepName = "Add hasMoreMembers to Team";
        tableName = "AgentBase";
        newColumnName = "hasMoreMembers";
        ColumnAdder.NewBooleanInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, false);

        //SingleReadAlignment firstSeqPosition
        stepName = "Add firstSeqPosition";
        tableName = "SingleReadAlignment";
        newColumnName = "firstSeqPosition";
        Integer defaultValue = null;
        boolean notNull = false;
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, defaultValue, notNull);

        //SingleReadAlignment leftCutPosition
        stepName = "Add leftCutPosition";
        tableName = "SingleReadAlignment";
        newColumnName = "leftCutPosition";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, defaultValue, notNull);

        //SingleReadAlignment rightCutPosition
        stepName = "Add rightCutPosition";
        tableName = "SingleReadAlignment";
        newColumnName = "rightCutPosition";
        ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, newColumnName, INCLUDE_AUDIT, defaultValue, notNull);

        //DescriptionElementBase_StateData
        stepName = "Simplify DescriptionElementBase_StateData";
        tableName = "DescriptionElementBase_StateData";
        newColumnName = "categoricaldata_id";
        MnTableRemover.NewInstance(stepList, stepName,
                tableName,
                newColumnName,
                "DescriptionElementBase_id",
                "statedata_id",
                "DescriptionElementBase",
                "StateData",
                INCLUDE_AUDIT);

        //DescriptionElementBase_StatisticalMeasurementValue
        stepName = "Simplify DescriptionElementBase_StatisticalMeasurementValue";
        tableName = "DescriptionElementBase_StatisticalMeasurementValue";
        newColumnName = "quantitativedata_id";
        MnTableRemover.NewInstance(stepList, stepName,
                tableName,
                newColumnName,
                "DescriptionElementBase_id",
                "statisticalvalues_id",
                "DescriptionElementBase",
                "StatisticalMeasurementValue",
                INCLUDE_AUDIT);

        //TaxonNodeAgentRelation
        //#3583
        stepName = "Add TaxonNodeAgentRelation table";
        tableName = "TaxonNodeAgentRelation";
        columnNames = new String[]{"taxonnode_id","agent_id","type_id"};
        referencedTables = new String[]{"TaxonNode","AgentBase","DefinedTermBase"};
        columnTypes = new String[]{"int","int","int"};
        TableCreator.NewAnnotatableInstance(stepList, stepName, tableName, columnNames, columnTypes, referencedTables, INCLUDE_AUDIT);

        //authorTeam -> authorship for TaxonName #4332
        stepName = "Rename TaxonNameBase.combinationAuthorTeam_id column";
        tableName = "TaxonNameBase";
        oldColumnName = "combinationAuthorTeam_id";
        newColumnName = "combinationAuthorship_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //authorTeam -> authorship for TaxonName #4332
        stepName = "Rename TaxonNameBase.exCombinationAuthorTeam_id column";
        tableName = "TaxonNameBase";
        oldColumnName = "exCombinationAuthorTeam_id";
        newColumnName = "exCombinationAuthorship_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //authorTeam -> authorship for TaxonName #4332
        stepName = "Rename TaxonNameBase.basionymAuthorTeam_id column";
        tableName = "TaxonNameBase";
        oldColumnName = "basionymAuthorTeam_id";
        newColumnName = "basionymAuthorship_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

        //authorTeam -> authorship for TaxonName #4332
        stepName = "Rename TaxonNameBase.exBasionymAuthorTeam_id column";
        tableName = "TaxonNameBase";
        oldColumnName = "exBasionymAuthorTeam_id";
        newColumnName = "exBasionymAuthorship_id";
        ColumnNameChanger.NewIntegerInstance(stepList, stepName, tableName, oldColumnName, newColumnName, INCLUDE_AUDIT);

		return stepList;
	}


	@Override
	public ISchemaUpdater getNextUpdater() {
		return SchemaUpdater_36_40.NewInstance();
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_341_35.NewInstance();
	}

}
