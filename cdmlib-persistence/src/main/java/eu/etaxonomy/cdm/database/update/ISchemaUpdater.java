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
 * Class which represents a CDM schema version step. See {@link CdmUpdater}
 * for further documentation or implementing classes for examples.
 *
 * @see CdmUpdater
 * @see ISchemaUpdaterStep
 * @see ISchemaUpdater
 * @see ITermUpdaterStep
 *
 * @author a.mueller
 \* @since 09.2010
 */
public interface ISchemaUpdater extends IUpdater<ISchemaUpdater>{

	/**
	 * Invokes this CDM schema updater and updates the schema up to the current CDM
	 * schema version. Throws an exception if this updater's target version does
	 * not equal the current CDM schema version.
	 * @param datasource the datasource
	 * @param monitor the progress monitor and event listener
	 * @param caseType the
	 * @param result
	 * @return
	 * @throws Exception
	 */
	@Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
	        CaseType caseType, SchemaUpdateResult result) throws Exception;



	/**
	 * Invokes this CDM schema updater and updates the schema up to the given
	 * target version. Throws an exception if this updaters target version does
	 * not equal the given target version.
	 * @param targetVersion
	 * @param datasource the datasource
	 * @param monitor the progress monitor and event listener
	 * @result the accumlated result of the schema update
	 * @return
	 * @throws Exception
	 */
	@Override
    public void invoke(String targetVersion, ICdmDataSource datasource,
            IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) throws Exception;

	public String getTargetVersion();

}
