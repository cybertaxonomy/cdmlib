/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v31_33;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SimpleSchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.TableDroper;
import eu.etaxonomy.cdm.database.update.TreeIndexUpdater;
import eu.etaxonomy.cdm.database.update.v33_34.SchemaUpdater_331_34;

/**
 * @author a.mueller
 * @created Jan 14, 2014
 */
public class SchemaUpdater_33_331 extends SchemaUpdaterBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_33_331.class);
	private static final String startSchemaVersion = "3.3.0.0.201309240000";
	private static final String endSchemaVersion = "3.3.1.0.201401140000";

	// ********************** FACTORY METHOD
	// *******************************************

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
		ISchemaUpdaterStep step;
		String columnName;

		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();

		//add rootnode column for classification
		stepName = "Add root node foreign key column to classification";
		tableName = "Classification";
		columnName = "rootnode_id";
		String referencedTable = "TaxonNode";
		step = ColumnAdder.NewIntegerInstance(stepName, tableName, columnName, INCLUDE_AUDIT, false, referencedTable);
		stepList.add(step);
		
		//update rootnode data for classification
		step = ClassificationRootNodeUpdater.NewInstance();
		stepList.add(step);
		
		// update treeindex for taxon nodes
		stepName = "Update TaxonNode treeindex";
		tableName = "TaxonNode";
		String treeIdColumnName = "classification_id";
		columnName = "treeIndex";
		step = TreeIndexUpdater.NewInstance(stepName, tableName,
				treeIdColumnName, columnName, ! INCLUDE_AUDIT);   //update does no yet wok for ANSI SQL (e.g. PosGres / H2 with multiple entries for same id in AUD table)
		stepList.add(step);
		
		// Drop Classification_TaxonNode table
		stepName = "Drop Classification_TaxonNode table";
		tableName = "Classification_TaxonNode";
		step = TableDroper.NewInstance(stepName, tableName, INCLUDE_AUDIT);
		stepList.add(step);
		
		//add rootnode column for classification
		stepName = "Add unknownData column to DescriptionElementBase";
		tableName = "DescriptionElementBase";
		columnName = "unknownData";
		Boolean defaultValue = null;
		step = ColumnAdder.NewBooleanInstance(stepName, tableName, columnName, INCLUDE_AUDIT, defaultValue);
		stepList.add(step);
			
		//set default value to false where adaquate
		stepName = "Set unknownData to default value (false)";
		String query = " UPDATE @@DescriptionElementBase@@ " +
					" SET unknownData = @FALSE@ " + 
					" WHERE DTYPE IN ('CategoricalData', 'QuantitativeData') ";
		step = SimpleSchemaUpdaterStep.NewAuditedInstance(stepName, query, "DescriptionElementBase", 99);
		stepList.add(step);
		
		
		return stepList;

	}

	@Override
	public ISchemaUpdater getNextUpdater() {
		return SchemaUpdater_331_34.NewInstance();
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_31_33.NewInstance();
	}

}
