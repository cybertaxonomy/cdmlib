// $Id$
/**
* Copyright (C) 2007 EDIT
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

/**
 * @author a.mueller
 * @created Nov 08, 2010
 * @version 1.0
 */
public class SchemaUpdater_26_30 extends SchemaUpdaterBase {


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_26_30.class);
	private static final String startSchemaVersion = "2.6.0.0.201010231255";
	private static final String endSchemaVersion = "3.0.XXXXX";
	
// ********************** FACTORY METHOD *******************************************
	
	public static SchemaUpdater_26_30 NewInstance(){
		return new SchemaUpdater_26_30();
	}
	
	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_26_30() {
		super(startSchemaVersion, endSchemaVersion);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterBase#getUpdaterList()
	 */
	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {
		
		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();
		String stepName;
		
		//add the table hibernate_sequences
		stepName = "Add the table hibernate_sequences to store the table specific sequences in";
//		SequenceTableCreator step = SequenceTableCreator.NewInstance(stepName);
//		stepList.add(step);
		
		
		
		//add PolytomousKey table
		stepName = "Create PolytomousKey table";
		PolytomousKeyTableCreator step = PolytomousKeyTableCreator.NewInstance(stepName);
		stepList.add(step);
		
		//add PolytomousKey MN tables
		
		//add PolytomousKeyNode table
		
		//add PolytomousKeyNode MN tables
		
		//add KeyStatementTable
		
		//add KeyStatement MN tables
		
		//fill tree attribute
		//move PolytomousKey data to new tables
		
		//Remove attributes from feature node
		
		//remove feature node MN tables
		
		//add feature tree attribute to feature node table
		
		
		
		return stepList;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterBase#getNextUpdater()
	 */
	@Override
	public ISchemaUpdater getNextUpdater() {
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterBase#getPreviousUpdater()
	 */
	@Override
	public ISchemaUpdater getPreviousUpdater() {
		return SchemaUpdater_25_26.NewInstance();
	}

}
