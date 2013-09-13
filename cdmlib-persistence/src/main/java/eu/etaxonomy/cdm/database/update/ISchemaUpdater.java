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

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 09.2010
 *
 */
public interface ISchemaUpdater extends IUpdater<ISchemaUpdater>{

	/**
	 * Invokes this CDM schema updater and updates the schema up to the current CDM
	 * schema version. Throws an exception if this updater's target version does
	 * not equal the current CDM schema version.
	 * @param datasource the datasource
	 * @param monitor the progress monitor and event listener
	 * @return
	 * @throws Exception 
	 */
	public boolean invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws Exception;
	

	
	/**
	 * Invokes this CDM schema updater and updates the schema up to the given
	 * target version. Throws an exception if this updaters target version does
	 * not equal the given target version.
	 * @param targetVersion
	 * @param datasource the datasource
	 * @param monitor the progress monitor and event listener
	 * @return
	 * @throws Exception 
	 */
	public boolean invoke(String targetVersion, ICdmDataSource datasource, IProgressMonitor monitor) throws Exception;

	public String getTargetVersion();

}