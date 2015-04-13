// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.database.update.v35_36;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.v34_35.SchemaUpdater_341_35;

/**
 * @author a.mueller
 * @created Mar 01, 2015
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
		ISchemaUpdaterStep step;
//		String columnName;
		String newColumnName;
		String oldColumnName;
		String query;
		String columnNames[];
		String referencedTables[];
		String columnTypes[];
//		boolean includeCdmBaseAttributes = false;

		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();
		
		
//		//IntextReference
//		//#4706
//		stepName = "Add IntextReference table";
//		tableName = "IntextReference";
//		columnNames = new String[]{"startpos","endpos","agent_id","annotation_id",
//				"languagestring_id","media_id","occurrence_id","reference_id","taxon_id","taxonname_id"};
//		referencedTables = new String[]{null, null, "AgentBase","Annotation","LanguageString","Media",
//				"SpecimenOrObservationBase","Reference","TaxonBase","TaxonNameBase"};
//		columnTypes = new String[]{"int","int","int","int","int","int","int","int","int","int"};
//		step = TableCreator.NewVersionableInstance(stepName, tableName, columnNames, 
//				columnTypes, referencedTables, INCLUDE_AUDIT);
//		stepList.add(step);
		
		return stepList;
	}
	

	@Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_341_35.NewInstance();
	}

}
