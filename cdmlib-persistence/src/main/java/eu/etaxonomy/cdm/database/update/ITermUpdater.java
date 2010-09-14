package eu.etaxonomy.cdm.database.update;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

public interface ITermUpdater {

	/**
	 * Invokes this CDM term updater
	 * @return
	 */
	boolean invoke(ICdmDataSource datasource, IProgressMonitor monitor);	
	/**
	 * Returns the previous CDM term updater
	 * @return
	 */
	public ITermUpdater getPreviousUpdater();

	/**
	 * Returns the next CDM term updater
	 * @return
	 */
	public ITermUpdater getNextUpdater();

	/**
	 * Returns the number of steps to run to update the datasource
	 * to the term version this term updater is updating to.
	 * This includes needed steps in previous updaters.
	 * @see #getPreviousUpdater()
	 * @return number of steps
	 */
	int countSteps(ICdmDataSource datasource);


	
}