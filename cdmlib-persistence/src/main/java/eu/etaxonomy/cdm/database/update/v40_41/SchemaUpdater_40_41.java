// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v40_41;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SortIndexUpdater;
import eu.etaxonomy.cdm.database.update.v36_40.SchemaUpdater_36_40;

/**
 * @author a.mueller
 * @created 16.04.2016
 */
public class SchemaUpdater_40_41 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_40_41.class);
	private static final String endSchemaVersion = "4.1.0.0.201607300000";
	private static final String startSchemaVersion = "4.0.0.0.201604200000";

	// ********************** FACTORY METHOD *************************************

	public static SchemaUpdater_40_41 NewInstance() {
		return new SchemaUpdater_40_41();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_40_41() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		ISchemaUpdaterStep step;
//		String columnName;
		String query;
		String newColumnName;
		String oldColumnName;
		String columnNames[];
		String referencedTables[];
		String columnTypes[];
//		boolean includeCdmBaseAttributes = false;

		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();


        //#5970
        //Implement allowOverride in CdmPreference
        stepName = "Add allowOverride in CdmPreference";
        tableName = "CdmPreference";
        newColumnName = "allowOverride";
        step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, ! INCLUDE_AUDIT, false);
        stepList.add(step);

        //#5875
        //Implement isDefault to DescriptionBase
        stepName = "Add isDefault in DescriptionBase";
        tableName = "DescriptionBase";
        newColumnName = "isDefault";
        step = ColumnAdder.NewBooleanInstance(stepName, tableName, newColumnName, INCLUDE_AUDIT, false);
        stepList.add(step);

        //#5826
        //Cleanup empty name descriptions
        stepName = "Cleanup empty name descriptions";
        query = " DELETE FROM @@DescriptionBase@@ db " +
                 " WHERE db.DTYPE = 'TaxonNameDescription' " +
                 " AND NOT EXISTS (SELECT * FROM @@DescriptionElementBase@@ deb WHERE deb.indescription_id = db.id )";
        SimpleSchemaUpdaterStep simpleStep = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, "DescriptionBase", -99);
        stepList.add(simpleStep);

        //#5921
        //UPDATE congruent symbol in DefinedTermBase
        stepName = "UPDATE congruent symbol in DefinedTermBase";
        query = " UPDATE @@DefinedTermBase@@ "
                + " SET idInVocabulary = Replace(idInVocabulary, '\u2245', '\u225c'), symbol = Replace(symbol, '\u2245', '\u225c'), inverseSymbol = Replace(inverseSymbol, '\u2245', '\u225c')"
                + " WHERE DTYPE like 'TaxonRel%' "
                + "     AND (idInVocabulary like '%\u2245%' OR symbol like '%\u2245%' OR inverseSymbol like '%\u2245%' )";
        simpleStep = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, "DefinedTermBase", -99);
        stepList.add(simpleStep);

        //#5921
        //UPDATE congruent symbol in Representations
        stepName = "UPDATE congruent symbol in Representations";
        query = " UPDATE @@Representation@@ "
                + " SET abbreviatedLabel = Replace(abbreviatedLabel, '\u2245', '\u225c') "
                + " WHERE (abbreviatedLabel like '%\u2245%' )";
        simpleStep = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, "Representation", -99);
        stepList.add(simpleStep);



        //#5976
        //update sortindex on FeatureNode children
        stepName = "Update sort index on FeatureNode children";
        tableName = "FeatureNode";
        String parentIdColumn = "parent_id";
        String sortIndexColumn = "sortIndex";
        SortIndexUpdater updateSortIndex = SortIndexUpdater.NewInstance(stepName, tableName, "parent_fk", sortIndexColumn, INCLUDE_AUDIT);
        stepList.add(updateSortIndex);

        //#5976
        // update sortindex for TaxonNodes
        stepName = "Update sort index on TaxonNode children";
        tableName = "TaxonNode";
        parentIdColumn = "parent_id";
        sortIndexColumn = "sortIndex";
        updateSortIndex = SortIndexUpdater.NewInstance(
                stepName, tableName, parentIdColumn, sortIndexColumn,
                INCLUDE_AUDIT);
        stepList.add(updateSortIndex);

        //#5976
        // TODO?: update sortindex for PolytomousKeyNodes


        return stepList;

    }

    @Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_36_40.NewInstance();
	}

}
