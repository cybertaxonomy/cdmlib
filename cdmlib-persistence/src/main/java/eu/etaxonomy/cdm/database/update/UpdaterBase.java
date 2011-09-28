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

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.CdmMetaData;


/**
 * @author a.mueller
 * @date 16.11.2010
 *
 */
public abstract class UpdaterBase<T extends ISchemaUpdaterStep, U extends IUpdater<U>> implements IUpdater<U> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdaterBase.class);
	
	protected List<T> list;
	protected String startVersion;
	protected String targetVersion;
	
	protected abstract void updateVersion(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException;
	
	protected abstract String getCurrentVersion(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException;
	
	@Override
	public int countSteps(ICdmDataSource datasource, IProgressMonitor monitor){
		int result = 0;
		//TODO test if previous updater is needed
		if (isToBeInvoked(/*targetVerison, */datasource, monitor)){
			for (T step: list){
				result++; //+= list.size();
				result += step.getInnerSteps().size();
			}	
			if (getPreviousUpdater() != null){
				result += getPreviousUpdater().countSteps(/*targetVerison, */datasource, monitor);
			}
		}
		return result;
	}
	
	
	private boolean isToBeInvoked(ICdmDataSource datasource, IProgressMonitor monitor) {
		boolean result = true;
		String datasourceSchemaVersion;
		try {
			datasourceSchemaVersion = getCurrentVersion(datasource, monitor);
		} catch (SQLException e1) {
			monitor.warning("SQLException", e1);
			return false;
		}
		
		boolean isAfterMyStartVersion = isAfterMyStartVersion(datasourceSchemaVersion, monitor);
		boolean isBeforeMyStartVersion = isBeforeMyStartVersion(datasourceSchemaVersion, monitor);
//		boolean isBeforeMyTargetVersion = isBeforeMyTargetVersion(targetVersion, monitor);
		boolean isDatasourceBeforeMyTargetVersion = isBeforeMyTargetVersion(datasourceSchemaVersion, monitor);
		
		result &= isDatasourceBeforeMyTargetVersion;
		result &= !(isAfterMyStartVersion /*&& isBeforeMyTargetVersion*/);
		result &= ! (isBeforeMyStartVersion && getPreviousUpdater() == null);
//		result &= !isBeforeMyTargetVersion;
		return result;
	}
	
	
	@Override
	public boolean invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws Exception{
		String currentLibrarySchemaVersion = CdmMetaData.getDbSchemaVersion();
		return invoke(currentLibrarySchemaVersion, datasource, monitor);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.IUpdater#invoke(java.lang.String, eu.etaxonomy.cdm.database.ICdmDataSource, eu.etaxonomy.cdm.common.IProgressMonitor)
	 */
	@Override
	public boolean invoke(String targetVersion, ICdmDataSource datasource, IProgressMonitor monitor) throws Exception{
		boolean result = true;
		String datasourceSchemaVersion;
		try {
			datasourceSchemaVersion = getCurrentVersion(datasource, monitor);
		} catch (SQLException e1) {
			monitor.warning("SQLException", e1);
			return false;
		}		

		
		boolean isAfterMyStartVersion = isAfterMyStartVersion(datasourceSchemaVersion, monitor);
		boolean isBeforeMyStartVersion = isBeforeMyStartVersion(datasourceSchemaVersion, monitor);
//		boolean isAfterMyTargetVersion = isAfterMyTargetVersion(targetVersion, monitor);
		boolean isBeforeMyTargetVersion = isBeforeMyTargetVersion(targetVersion, monitor);
		boolean isDatasourceBeforeMyTargetVersion = isBeforeMyTargetVersion(datasourceSchemaVersion, monitor);
		
		
		
		if (! isDatasourceBeforeMyTargetVersion){
			String warning = "Target version ("+targetVersion+") is not before updater target version ("+this.targetVersion+"). Nothing to update.";
			monitor.warning(warning);
			return true;
		}
		
		if (isAfterMyStartVersion && isBeforeMyTargetVersion){
			String warning = "Database version is higher than updater start version but lower than updater target version";
			RuntimeException exeption = new RuntimeException(warning);
			monitor.warning(warning, exeption);
			throw exeption;
		}
		
		if (isBeforeMyStartVersion){
			if (getPreviousUpdater() == null){
				String warning = "Database version is before updater version but no previous version updater exists";
				RuntimeException exeption = new RuntimeException(warning);
				monitor.warning(warning, exeption);
				throw exeption;
			}
			result &= getPreviousUpdater().invoke(startVersion, datasource, monitor);
		}
		

		
		if (isBeforeMyTargetVersion){
			String warning = "Target version ("+targetVersion+") is lower than updater target version ("+this.targetVersion+")";
			RuntimeException exeption = new RuntimeException(warning);
			monitor.warning(warning, exeption);
			throw exeption;
		}
		
		
		for (T step : list){
			result = handleSingleStep(datasource, monitor, result, step, false);
		}
		// TODO schema version gets updated even if something went utterly wrong while executing the steps
		// I don't think we want this to happen
		updateVersion(datasource, monitor);
		
		return result;
	
	}
	
//	protected abstract boolean handleSingleStep(ICdmDataSource datasource,	IProgressMonitor monitor, boolean result, ISchemaUpdaterStep step, boolean isInnerStep) throws Exception;
	
	protected boolean handleSingleStep(ICdmDataSource datasource, IProgressMonitor monitor, boolean result, ISchemaUpdaterStep step, boolean isInnerStep)
			throws Exception {
		try {
			monitor.subTask(step.getStepName());
			Integer invokeResult = step.invoke(datasource, monitor);
			result &= (invokeResult != null);
			for (ISchemaUpdaterStep innerStep : step.getInnerSteps()){
				result &= handleSingleStep(datasource, monitor, result, innerStep, true);
			}
//			if (! isInnerStep){
				monitor.worked(1);
//			}
		} catch (Exception e) {
			monitor.warning("Exception occurred while updating schema", e);
			throw e;
		}
		return result;
	}
	
	protected boolean isAfterMyStartVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, startVersion, depth, monitor);
		return compareResult > 0;
	}

	protected boolean isBeforeMyStartVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, startVersion, depth, monitor);
		return compareResult < 0;
	}

	protected boolean isAfterMyTargetVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, targetVersion, depth, monitor);
		return compareResult > 0;
	}

	protected boolean isBeforeMyTargetVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, targetVersion, depth, monitor);
		return compareResult < 0;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.IUpdater#getNextUpdater()
	 */
	@Override
	public abstract U getNextUpdater();

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.IUpdater#getPreviousUpdater()
	 */
	@Override
	public abstract U getPreviousUpdater();

	
	/**
	 * @return
	 */
	public String getTargetVersion() {
		return this.targetVersion;
	}
}
