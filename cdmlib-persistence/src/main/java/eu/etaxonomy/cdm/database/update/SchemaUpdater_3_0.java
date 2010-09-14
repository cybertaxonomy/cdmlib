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

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class SchemaUpdater_3_0 extends SchemaUpdaterBase implements ISchemaUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_3_0.class);
	private static final String thisUpdatersStartSchemaVersion = "2.4.1.2.201004231015";
	
// ********************** FACTORY METHOD *******************************************
	
	public static SchemaUpdater_3_0 NewInstance(){
		return new SchemaUpdater_3_0();
	}

	
// ********************** CONSTRUCTOR *******************************************/
	
	private SchemaUpdater_3_0(){
		super(thisUpdatersStartSchemaVersion);
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
	protected List<SimpleSchemaUpdaterStep> getUpdaterList() {
		List<SimpleSchemaUpdaterStep> stepList = new ArrayList<SimpleSchemaUpdaterStep>();
		
		String updateQuery = "UPDATE TaxonNameBase SET titleCache = 'XXX' WHERE titleCache = 'SDFSFSDF'";
		stepList.add(SimpleSchemaUpdaterStep.NewInstance("Update title cache", updateQuery));
		
		return stepList;
	}
}
