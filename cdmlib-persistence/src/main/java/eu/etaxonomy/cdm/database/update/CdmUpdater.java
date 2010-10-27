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

import eu.etaxonomy.cdm.common.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class CdmUpdater {
	private static final Logger logger = Logger.getLogger(CdmUpdater.class);
	

	/**
	 * @param datasource
	 * @param monitor may be <code>null</code>
	 * @return
	 */
	public boolean updateToCurrentVersion(ICdmDataSource datasource, IProgressMonitor monitor){
		boolean result = true;
		if (monitor == null){
			monitor = DefaultProgressMonitor.NewInstance();
		}
		
		ISchemaUpdater currentSchemaUpdater = getCurrentSchemaUpdater();
		// TODO do we really always update the terms??
		ITermUpdater currentTermUpdater = getCurrentTermUpdater();
		
		int steps = currentSchemaUpdater.countSteps(datasource);
		steps += currentTermUpdater.countSteps(datasource);
		
		String taskName = "Update to schema version " + currentSchemaUpdater.getTargetVersion() + " and to term version " + currentTermUpdater.getTargetVersion(); //+ currentSchemaUpdater.getVersion();
		monitor.beginTask(taskName, steps);
		
		try {
			result &= currentSchemaUpdater.invoke(datasource, monitor);
			// the above apparently did not work while testing. Did not want to set the version in CdmMetaData yet
//			result &= currentSchemaUpdater.invoke(currentSchemaUpdater.getTargetVersion(), datasource, monitor);
			result &= currentTermUpdater.invoke(datasource, monitor);
		} catch (Exception e) {
			result = false;
			monitor.warning("Stopped schema updater");
		} finally {
			String message = "Update finished " + (result ? "successfully" : "with ERRORS");
			monitor.subTask(message);
			monitor.done();
			logger.info(message);
		}
		
		return result;
	}
	
	private ITermUpdater getCurrentTermUpdater() {
		return TermUpdater_24_25.NewInstance();
	}

	/**
	 * Returns the current CDM updater
	 * @return
	 */
	private ISchemaUpdater getCurrentSchemaUpdater() {
		return SchemaUpdater_24_25.NewInstance();
//		return SchemaUpdater_25_26.NewInstance();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		logger.warn("main method not yet fully implemented (only works with mysql!!!)");
		if(args.length < 2){
			logger.error("Arguments missing: server database [username [password]]");
		}
		//TODO better implementation
		CdmUpdater myUpdater = new CdmUpdater();
		String server = args[0];
		String database  = args[1];
		String username = args.length > 2 ? args[2] : null;
		String password  = args.length > 3 ? args[3] : null;
		
		ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance(server, database, username, password);
		boolean success = myUpdater.updateToCurrentVersion(dataSource, null);
		System.out.println("DONE " + (success ? "successfully" : "with ERRORS"));
	}

}
