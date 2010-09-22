package eu.etaxonomy.cdm.database.update;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;

public interface ITermUpdater {

	/**
	 * Invokes this CDM term updater and updates the schema up to the current CDM
	 * term version. Throws an exception if this updaters target version does
	 * not equal the current CDM schema version.
	 * @param datasource
	 * @param monitor
	 * @return
	 * @throws Exception 
	 */
	boolean invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws Exception;	

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
	boolean invoke(String targetVersion, ICdmDataSource datasource, IProgressMonitor monitor) throws Exception;	
	
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

	String getTargetVersion();
	
}