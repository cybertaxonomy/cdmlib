/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v24_25;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.SortIndexUpdater;
import eu.etaxonomy.cdm.database.update.v25_30.SchemaUpdater_25_30;

/**
 * @author a.mueller
 * @since 10.09.2010
 *
 */
public class SchemaUpdater_24_25 extends SchemaUpdaterBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_24_25.class);
	private static final String startSchemaVersion = "2.4.1.2.201004231015";
	private static final String endSchemaVersion = "2.5.0.0.201009211255";

// ********************** FACTORY METHOD *******************************************

	public static SchemaUpdater_24_25 NewInstance(){
		return new SchemaUpdater_24_25();
	}


// ********************** CONSTRUCTOR *******************************************/

	private SchemaUpdater_24_25(){
		super(startSchemaVersion, endSchemaVersion);
	}

// ************************ NEXT / PREVIOUS **************************************/

	@Override
	public ISchemaUpdater getNextUpdater() {
		return SchemaUpdater_25_30.NewInstance();
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return null;
	}

// ************************** UPDATE STEPS ************************************************

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {
		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();
		String stepName;

		//sortIndex on children in FeatureNode
		stepName = "Add sort index on FeatureNode children";
		ColumnAdder step = ColumnAdder.NewIntegerInstance(stepName, "FeatureNode", "sortindex", INCLUDE_AUDIT, false, null);
		stepList.add(step);

		//update sortindex on FeatureNode children
		stepName = "Update sort index on FeatureNode children";
//		updateQuery = "UPDATE FeatureNode SET sortindex = id WHERE sortindex is null";
//		SimpleSchemaUpdaterStep updateSortindex = SimpleSchemaUpdaterStep.NewInstance(stepName, updateQuery);
//		stepList.add(updateSortindex);
		SortIndexUpdater updateSortIndex = SortIndexUpdater.NewInstance(stepName, "FeatureNode", "parent_fk", "sortindex", INCLUDE_AUDIT);
		stepList.add(updateSortIndex);

		//add country to gathering event
		stepName = "Add country column to gathering event";
		step = ColumnAdder.NewIntegerInstance(stepName, "GatheringEvent", "country_id", INCLUDE_AUDIT, false, "DefinedTermBase");
		stepList.add(step);

		//add unplaced and excluded to taxon
		stepName = "Add unplaced to taxon";
		Boolean defaultValue = false;
		step = ColumnAdder.NewBooleanInstance(stepName, "TaxonBase", "unplaced", INCLUDE_AUDIT, defaultValue);
		stepList.add(step);

		//add excluded to taxon
		stepName = "Add excluded to taxon";
		defaultValue = false;
		step = ColumnAdder.NewBooleanInstance(stepName, "TaxonBase", "excluded", INCLUDE_AUDIT, defaultValue);
		stepList.add(step);

		//add barcode to derived unit base
		stepName = "Add barcode to specimen";
		step = ColumnAdder.NewStringInstance(stepName, "SpecimenOrObservationBase", "barcode", INCLUDE_AUDIT);
		stepList.add(step);

		return stepList;
	}
}
