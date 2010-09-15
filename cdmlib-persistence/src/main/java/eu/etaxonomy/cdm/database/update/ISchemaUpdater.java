package eu.etaxonomy.cdm.database.update;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

public interface ISchemaUpdater {

	/**
	 * Invokes this CDM schema updater
	 * @param datasource the datasource
	 * @param monitor the progress monitor and event listener
	 * @return
	 * @throws Exception 
	 */
	public boolean invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws Exception;
	
	/**
	 * Returns the previous CDM schema updater
	 * @return
	 */
	public ISchemaUpdater getPreviousUpdater();

	/**
	 * Returns the next CDM schema updater
	 * @return
	 */
	public ISchemaUpdater getNextUpdater();

	/**
	 * Returns the number of steps to run to update the datasource
	 * to the schema this schema updater is updating to.
	 * This includes needed steps in previous updaters.
	 * @see #getPreviousUpdater()
	 * @return number of steps
	 */
	int countSteps(ICdmDataSource datasource);
	
}