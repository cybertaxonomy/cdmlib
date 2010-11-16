// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database.update.v25_26;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;
import eu.etaxonomy.cdm.database.update.v24_25.SchemaUpdater_24_25;
import eu.etaxonomy.cdm.database.update.v26_30.SequenceTableCreator;

/**
 * @author n.hoffmann
 * @created Oct 25, 2010
 * @version 1.0
 */
public class SchemaUpdater_25_26 extends SchemaUpdaterBase {


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_25_26.class);
	private static final String startSchemaVersion = "2.5.0.0.201009211255";
	private static final String endSchemaVersion = "2.6.0.0.201010231255";
	
// ********************** FACTORY METHOD *******************************************
	
	public static SchemaUpdater_25_26 NewInstance(){
		return new SchemaUpdater_25_26();
	}
	
	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_25_26() {
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
		SequenceTableCreator step = SequenceTableCreator.NewInstance(stepName);
		stepList.add(step);
		
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
		return SchemaUpdater_24_25.NewInstance();
	}

}
