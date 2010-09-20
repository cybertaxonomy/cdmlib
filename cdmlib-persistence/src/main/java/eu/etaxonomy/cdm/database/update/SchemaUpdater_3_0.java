// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.DatabaseTypeEnum;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class SchemaUpdater_3_0 extends SchemaUpdaterBase implements ISchemaUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_3_0.class);
	private static final String startSchemaVersion = "2.4.1.2.201004231015";
	private static final String endSchemaVersion = "3.0.0.0.201009201255";
	
// ********************** FACTORY METHOD *******************************************
	
	public static SchemaUpdater_3_0 NewInstance(){
		return new SchemaUpdater_3_0();
	}

	
// ********************** CONSTRUCTOR *******************************************/
	
	private SchemaUpdater_3_0(){
		super(startSchemaVersion, endSchemaVersion);
	}
	
// ************************ NEXT / PREVIOUS **************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getNextUpdater()
	 */
	@Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getPreviousUpdater()
	 */
	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return null;
	}

// ************************** UPDATE STEPS ************************************************
	
	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {
		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();
		String updateQuery;
		String stepName;
		
		//sortIndex on children in FeatureNode
		stepName = "Add sort index on FeatureNode children";
		ColumnAdder step = ColumnAdder.NewIntegerInstance(stepName, "FeatureNode", "sortindex", INCLUDE_AUDIT);
//		updateQuery = "ALTER TABLE FeatureNode ADD COLUMN sortindex int";
//		SimpleSchemaUpdaterStep step = SimpleSchemaUpdaterStep.NewInstance(stepName, updateQuery);
//		step.put(DatabaseTypeEnum.SqlServer2005, "ALTER TABLE FeatureNode ADD sortindex int");
		stepList.add(step);

//		//sortIndex on children in FeatureNode
//		stepName = "Add sort index on FeatureNode children in auditing table";
//		updateQuery = "ALTER TABLE FeatureNode_AUD ADD COLUMN sortindex int";
//		step = SimpleSchemaUpdaterStep.NewInstance(stepName, updateQuery);
//		step.put(DatabaseTypeEnum.SqlServer2005, "ALTER TABLE featurenode_aud ADD sortindex int");
//		stepList.add(step);
		
		//update sortindex on FeatureNode children
		stepName = "Update sort index on FeatureNode children";
		updateQuery = "UPDATE FeatureNode SET sortindex = id WHERE sortindex is null";
		SimpleSchemaUpdaterStep updateSortindex = SimpleSchemaUpdaterStep.NewInstance(stepName, updateQuery);
		stepList.add(updateSortindex);
		
//		//rename taxonomicTree -> classification
//		stepName = "Rename taxonomic tree into classification";
//		TableNameChanger tableChanger = TableNameChanger.NewInstance(stepName, "TaxonomicTree", "Classification", INCLUDE_AUDIT);
//		stepList.add(tableChanger);
		
		//update sortindex on FeatureNode children
		stepName = "Add country column to gathering event";
		step = ColumnAdder.NewIntegerInstance(stepName, "GatheringEvent", "country_id", INCLUDE_AUDIT);
		stepList.add(step);
		
		return stepList;
	}
}
