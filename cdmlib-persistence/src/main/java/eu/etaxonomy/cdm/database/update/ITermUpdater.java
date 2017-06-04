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
 * Class which represents a CDM term version step. See {@link CdmUpdater}
 * for further documentation or implementing classes for examples.
 *
 * @see CdmUpdater
 * @see ITermUpdaterStep
 * @see ISchemaUpdaterStep
 * @see ISchemaUpdater
 *
 * @author a.mueller
 * @date 09.2010
 *
 */
public interface ITermUpdater extends IUpdater<ITermUpdater>{

	/**
	 * Invokes this CDM term updater and updates the schema up to the current CDM
	 * term version. Throws an exception if this updaters target version does
	 * not equal the current CDM schema version.
	 * @param datasource
	 * @param monitor
	 * @return
	 * @throws Exception
	 */
	@Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
	        CaseType caseType, SchemaUpdateResult result) throws Exception;

	/**
	 * Invokes this CDM term updater and updates the terms up to the given
	 * target version. Throws an exception if this updaters target version does
	 * not equal the given target version.
	 * @param targetVersion
	 * @param datasource
	 * @param monitor
	 * @return
	 * @throws Exception
	 */
	@Override
    public void invoke(String targetVersion, ICdmDataSource datasource, IProgressMonitor monitor,
	        CaseType caseType, SchemaUpdateResult result) throws Exception;


	public String getTargetVersion();

}
