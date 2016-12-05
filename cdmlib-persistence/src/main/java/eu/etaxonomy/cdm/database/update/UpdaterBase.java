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

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;


/**
 * Common updater base class for updating schema or terms.
 *
 * @see CdmUpdater
 * @see ISchemaUpdater
 * @see ITermUpdater
 *
 * @author a.mueller
 * @date 16.11.2010
 *
 */
public abstract class UpdaterBase<T extends ISchemaUpdaterStep, U extends IUpdater<U>> implements IUpdater<U> {
	private static final Logger logger = Logger.getLogger(TermUpdaterBase.class);

	protected List<T> list;
	protected String startVersion;
	protected String targetVersion;


	protected abstract boolean updateVersion(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException;

	protected abstract String getCurrentVersion(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException;

	@Override
	public int countSteps(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType){
		int result = 0;
		//TODO test if previous updater is needed
		if (isToBeInvoked(datasource, monitor, caseType)){
			for (T step: list){
				result++; //+= list.size();
				result += step.getInnerSteps().size();
			}
			if (getPreviousUpdater() != null){
				result += getPreviousUpdater().countSteps(datasource, monitor, caseType);
			}
		}
		return result;
	}


	private boolean isToBeInvoked(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) {
		boolean result = true;
		String datasourceVersion;
		try {
			datasourceVersion = getCurrentVersion(datasource, monitor, caseType);
		} catch (SQLException e1) {
			monitor.warning("SQLException", e1);
			return false;
		}

		boolean isAfterMyStartVersion = isAfterMyStartVersion(datasourceVersion, monitor);
		boolean isBeforeMyStartVersion = isBeforeMyStartVersion(datasourceVersion, monitor);
//		boolean isBeforeMyTargetVersion = isBeforeMyTargetVersion(targetVersion, monitor);
		boolean isBeforeMyTargetVersion = isBeforeMyTargetVersion(targetVersion, monitor);
		boolean isDatasourceBeforeMyTargetVersion = isBeforeMyTargetVersion(datasourceVersion, monitor);

		result &= isDatasourceBeforeMyTargetVersion;
		result &= !(isAfterMyStartVersion && isBeforeMyTargetVersion);
		result &= ! (isBeforeMyStartVersion && getPreviousUpdater() == null);
		result &= !isBeforeMyTargetVersion;
		return result;
	}


	@Override
	public boolean invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws Exception{
		String currentLibrarySchemaVersion = CdmMetaData.getDbSchemaVersion();
		return invoke(currentLibrarySchemaVersion, datasource, monitor, caseType);
	}

	@Override
	public boolean invoke(String targetVersion, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws Exception{
		boolean result = true;
		String datasourceVersion;

		try {
			datasourceVersion = getCurrentVersion(datasource, monitor, caseType);
		} catch (SQLException e1) {
			monitor.warning("SQLException", e1);
			return false;
		}


		boolean isAfterMyStartVersion = isAfterMyStartVersion(datasourceVersion, monitor);
		boolean isBeforeMyStartVersion = isBeforeMyStartVersion(datasourceVersion, monitor);
//		boolean isAfterMyTargetVersion = isAfterMyTargetVersion(targetVersion, monitor);
		boolean isBeforeMyTargetVersion = isBeforeMyTargetVersion(targetVersion, monitor);
		boolean isDatasourceBeforeMyTargetVersion = isBeforeMyTargetVersion(datasourceVersion, monitor);



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
			result &= getPreviousUpdater().invoke(startVersion, datasource, monitor, caseType);
		}



		if (isBeforeMyTargetVersion){
			String warning = "Target version ("+targetVersion+") is lower than updater target version ("+this.targetVersion+")";
			RuntimeException exeption = new RuntimeException(warning);
			monitor.warning(warning, exeption);
			throw exeption;
		}

		if (result == false){
			return result;
		}
//		datasource.startTransaction();  transaction already started by CdmUpdater
		try {
			for (T step : list){
				result &= handleSingleStep(datasource, monitor, result, step, false, caseType);
				if (result == false){
					break;
				}
			}
			if (result == true){
				result &= updateVersion(datasource, monitor, caseType);
			}else{
				datasource.rollback();
			}

		} catch (Exception e) {
			datasource.rollback();
			logger.error("Error occurred while trying to run updater: " + this.getClass().getName());
			result = false;
		}
		return result;

	}

//	protected abstract boolean handleSingleStep(ICdmDataSource datasource,	IProgressMonitor monitor, boolean result, ISchemaUpdaterStep step, boolean isInnerStep) throws Exception;

	protected boolean handleSingleStep(ICdmDataSource datasource, IProgressMonitor monitor, boolean result, ISchemaUpdaterStep step, boolean isInnerStep, CaseType caseType)
			throws Exception {
		try {
			monitor.subTask(step.getStepName());
			Integer invokeResult = step.invoke(datasource, monitor, caseType);
			result &= (invokeResult != null);
			for (ISchemaUpdaterStep innerStep : step.getInnerSteps()){
				result &= handleSingleStep(datasource, monitor, result, innerStep, true, caseType);
				if (!result){
				    break;
				}
			}
//			if (! isInnerStep){
				monitor.worked(1);
//			}
		} catch (Exception e) {
			monitor.warning("Monitor: Exception occurred while handling single schema updating step", e);
			datasource.rollback();
			result = false;
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

	@Override
	public abstract U getNextUpdater();

	@Override
	public abstract U getPreviousUpdater();


	/**
	 * @return
	 */
	public String getTargetVersion() {
		return this.targetVersion;
	}
}
