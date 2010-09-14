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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class CdmUpdater {
	private static final Logger logger = Logger.getLogger(CdmUpdater.class);
	
	/**
	 * 
	 * @return
	 */
	public boolean updateToCurrentVersion(ICdmDataSource datasource, IProgressMonitor monitor){
		boolean result = true;
		ISchemaUpdater currentSchemaUpdater = getCurrentSchemaUpdater();
		ITermUpdater currentTermUpdater = getCurrentTermUpdater();
		
		int steps = currentSchemaUpdater.countSteps(datasource);
		steps += currentTermUpdater.countSteps(datasource);
		
		String taskName = "Update to schema version ... and to term version ... "; //+ currentSchemaUpdater.getVersion();
		monitor.beginTask(taskName, steps);
		
		result &= currentSchemaUpdater.invoke(datasource, monitor);
		
		result &= currentTermUpdater.invoke(datasource, monitor);
		
		
		return result;
	}
	
	private ITermUpdater getCurrentTermUpdater() {
		return TermUpdater_3_0.NewInstance();
	}

	/**
	 * Returns the current CDM updater
	 * @return
	 */
	private ISchemaUpdater getCurrentSchemaUpdater() {
		return SchemaUpdater_3_0.NewInstance();
	}


//	5432
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger.warn("main method not yet implemented");
		CdmUpdater myUpdater = new CdmUpdater();
//		myUpdater.updateToCurrentVersion(datasource, monitor);
	}

}
