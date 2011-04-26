// $Id$
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

import eu.etaxonomy.cdm.database.update.ISchemaUpdater;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterBase;


/**
 * @author a.mueller
 * @created Nov 08, 2010
 */
public class SchemaUpdater_301_31 extends SchemaUpdaterBase {


	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdater_301_31.class);
	private static final String startSchemaVersion = "3.0.1.0.201104190000";
	private static final String endSchemaVersion = "0";
	
// ********************** FACTORY METHOD *******************************************
	
	public static SchemaUpdater_301_31 NewInstance(){
		return new SchemaUpdater_301_31();
	}
	
	/**
	 * @param startSchemaVersion
	 * @param endSchemaVersion
	 */
	protected SchemaUpdater_301_31() {
		super(startSchemaVersion, endSchemaVersion);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterBase#getUpdaterList()
	 */
	@Override
	protected List<ISchemaUpdaterStep> getUpdaterList() {
		
		List<ISchemaUpdaterStep> stepList = new ArrayList<ISchemaUpdaterStep>();
		String stepName;
		
		
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
		return SchemaUpdater_30_301.NewInstance();
	}

}
