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

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableDropper;
import eu.etaxonomy.cdm.database.update.TreeIndexUpdater;

/**
 * @author a.mueller
 * @since Jan 14, 2014
 */
public class SchemaUpdater_33_331 extends SchemaUpdaterBase {

	private static final String startSchemaVersion = "3.3.0.0.201309240000";
	private static final String endSchemaVersion = "3.3.1.0.201401140000";

	// ********************** FACTORY METHOD ***********************************

	public static SchemaUpdater_33_331 NewInstance() {
		return new SchemaUpdater_33_331();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_33_331() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		String stepName;
		String tableName;
		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();

		//add rootnode column for classification
		stepName = "Add root node foreign key column to classification";
		tableName = "Classification";
		columnName = "rootnode_id";
		String referencedTable = "TaxonNode";
		ColumnAdder.NewIntegerInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT, false, referencedTable);

		//update rootnode data for classification
		ClassificationRootNodeUpdater.NewInstance(stepList);

		// update treeindex for taxon nodes
		stepName = "Update TaxonNode treeindex";
		tableName = "TaxonNode";
		String treeIdColumnName = "classification_id";
		columnName = "treeIndex";
		TreeIndexUpdater.NewInstance(stepList, stepName, tableName,
				treeIdColumnName, columnName, ! INCLUDE_AUDIT);   //update does no yet work for ANSI SQL (e.g. PosGres / H2 with multiple entries for same id in AUD table)

		// Drop Classification_TaxonNode table
		stepName = "Drop Classification_TaxonNode table";
		tableName = "Classification_TaxonNode";
		TableDropper.NewInstance(stepList, stepName, tableName, INCLUDE_AUDIT);

		//add rootnode column for classification
		stepName = "Add unknownData column to DescriptionElementBase";
		tableName = "DescriptionElementBase";
		columnName = "unknownData";
		Boolean defaultValue = null;
		ColumnAdder.NewBooleanInstance(stepList, stepName, tableName, columnName, INCLUDE_AUDIT, defaultValue);

		//set default value to false where adaquate
		stepName = "Set unknownData to default value (false)";
		String query = " UPDATE @@DescriptionElementBase@@ " +
					" SET unknownData = @FALSE@ " +
					" WHERE DTYPE IN ('CategoricalData', 'QuantitativeData') ";
		SimpleSchemaUpdaterStep.NewAuditedInstance(stepList, stepName, query, "DescriptionElementBase");

		return stepList;

	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_31_33.NewInstance();
	}

}
