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
 * Common interface for {@link ISchemaUpdater} and {@link ITermUpdater}
 *
 * @author a.mueller
 * @date 16.11.2010
 */
public interface IUpdater<U extends IUpdater<U>> {


	/**
	 * Returns the previous CDM term/schema updater
	 * @return
	 */
	public U getPreviousUpdater();

	/**
	 * Returns the next CDM term/schema updater
	 * @return
	 */
	public U getNextUpdater();


	/**
	 * Returns the number of steps to run to update the datasource
	 * to the schema this schema updater is updating to.
	 * This includes needed steps in previous updaters.
	 * @param caseType
	 * @see #getPreviousUpdater()
	 * @return number of steps
	 */
	int countSteps(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType);


	public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
	        CaseType caseType, SchemaUpdateResult result) throws Exception;

	public void invoke(String targetVersion, ICdmDataSource datasource,
	        IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) throws Exception;

}
