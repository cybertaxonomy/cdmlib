/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.update.v30_31;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ColumnAdder;
import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.TableDroper;
import eu.etaxonomy.cdm.database.update.UniqueIndexDropper;
import eu.etaxonomy.cdm.database.update.v25_30.SchemaUpdater_25_30;
import eu.etaxonomy.cdm.database.update.v31_33.SchemaUpdater_31_33;


/**
 * This updater adds the parent_id column to PolytomousKeyNode_AUD
 * @author a.mueller
 * @created 19.04.2011
 */
public class SchemaUpdater_30_301 extends SchemaUpdaterBase {


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_30_301.class);
	private static final String startSchemaVersion = "3.0.0.0.201011090000";
	private static final String endSchemaVersion = "3.0.1.0.201104190000";

// ********************** FACTORY METHOD *******************************************

	public static SchemaUpdater_30_301 NewInstance(){
		return new SchemaUpdater_30_301();
	}

	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_30_301() {
		super(startSchemaVersion, endSchemaVersion);
	}

	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {

		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();

		//drop unique index for DefinedTermBase_media.media_id
		ISchemaUpdaterStep step = UniqueIndexDropper.NewInstance("DefinedTermBase_media", "media_id", ! INCLUDE_AUDIT);
		stepList.add(step);

		//drop unique index for StateData_DefinedTermBase.modifier_id
		//this was part of schema version 2.5 but an updater was never written
		step = UniqueIndexDropper.NewInstance("StateData_definedtermbase", "modifiers_id", ! INCLUDE_AUDIT);
		stepList.add(step);

		//drop unique index for StateData_DefinedTermBase.modifier_id
		//this was part of schema version 2.5 but an updater was never written
		step = UniqueIndexDropper.NewInstance("StatisticalMeasurementValue_definedtermbase", "modifiers_id", ! INCLUDE_AUDIT);
		stepList.add(step);

		//Makes PolytomousKeyNode parent-child bidirectional
		step = ColumnAdder.NewIntegerInstance("Add parent_id to PolytomousKeyNode_AUD", "PolytomousKeyNode_AUD", "parent_id", ! INCLUDE_AUDIT, false, "PolytomousKeyNode");
		stepList.add(step);
		step = TableDroper.NewInstance("Drop PolytomousKeyNode_PolytomousKeyNode_AUD table", "PolytomousKeyNode_PolytomousKeyNode_AUD", ! INCLUDE_AUDIT);
		stepList.add(step);


		return stepList;
	}


	@Override
	public ISchemaUpdater getNextUpdater() {
		return SchemaUpdater_31_33.NewInstance();
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_25_30.NewInstance();
	}

}
