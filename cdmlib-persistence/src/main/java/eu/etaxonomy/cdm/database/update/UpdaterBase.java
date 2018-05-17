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
 * Note: prior to v4.8 this was a common base class for {@link SchemaUpdaterBase}
 * and term updater. Since v4.8 we do not have a term updater anymore.
 * Therefore in future this class could be merged with {@link SchemaUpdaterBase}
 *
 * @see CdmUpdater
 * @see ISchemaUpdater
 *
 * @author a.mueller
 * @since 16.11.2010
 *
 */
public abstract class UpdaterBase<T extends ISchemaUpdaterStep, U extends IUpdater<U>>
            implements IUpdater<U> {

	private static final Logger logger = Logger.getLogger(UpdaterBase.class);

	protected List<T> list;
	protected String startVersion;
	protected String targetVersion;


	protected abstract void updateVersion(ICdmDataSource datasource, IProgressMonitor monitor,
	        CaseType caseType, SchemaUpdateResult result) throws SQLException;

	protected abstract String getCurrentVersion(ICdmDataSource datasource,
	        IProgressMonitor monitor, CaseType caseType) throws SQLException;

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
	public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
	        CaseType caseType, SchemaUpdateResult result) throws Exception{
		String currentLibrarySchemaVersion = CdmMetaData.getDbSchemaVersion();
		invoke(currentLibrarySchemaVersion, datasource, monitor, caseType, result);
	}

	@Override
	public void invoke(String targetVersion, ICdmDataSource datasource,
	        IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) throws Exception{

	    String datasourceVersion;

		try {
			datasourceVersion = getCurrentVersion(datasource, monitor, caseType);
		} catch (SQLException e1) {
		    String message = "SQLException";
			monitor.warning(message, e1);
			result.addException(e1, message, "UpdaterBase.invoke");
			return;
		}

		if (isBefore4_0_0(datasourceVersion, monitor, result)){
		    return;
		}

		boolean isAfterMyStartVersion = isAfterMyStartVersion(datasourceVersion, monitor);
		boolean isBeforeMyStartVersion = isBeforeMyStartVersion(datasourceVersion, monitor);
//		boolean isAfterMyTargetVersion = isAfterMyTargetVersion(targetVersion, monitor);
		boolean isBeforeMyTargetVersion = isBeforeMyTargetVersion(targetVersion, monitor);
		boolean isDatasourceBeforeMyTargetVersion = isBeforeMyTargetVersion(datasourceVersion, monitor);



		if (! isDatasourceBeforeMyTargetVersion){
			String warning = "Target version ("+targetVersion+") is not before updater target version ("+this.targetVersion+"). Nothing to update.";
			monitor.warning(warning);
			result.addWarning(warning);
			return;
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
			getPreviousUpdater().invoke(startVersion, datasource, monitor, caseType, result);
		}

		if (isBeforeMyTargetVersion){
			String warning = "Target version ("+targetVersion+") is lower than updater target version ("+this.targetVersion+")";
			RuntimeException exeption = new RuntimeException(warning);
			monitor.warning(warning, exeption);
			throw exeption;
		}

		if (!result.isSuccess()){
			return;
		}
//		datasource.startTransaction();  transaction already started by CdmUpdater
		try {
			for (T step : list){
				handleSingleStep(datasource, monitor, result, step, false, caseType);
				if (!result.isSuccess()){
					break;
				}
			}
			if (result.isSuccess()){
				updateVersion(datasource, monitor, caseType, result);
			}else{
				datasource.rollback();
			}

		} catch (Exception e) {
			datasource.rollback();
			String message = "Error occurred while trying to run updater: " + this.getClass().getName();
			logger.error(message);
			result.addException(e, message, "UpdaterBase.invoke");
		}
		return;
	}

	protected void handleSingleStep(ICdmDataSource datasource, IProgressMonitor monitor, SchemaUpdateResult result, ISchemaUpdaterStep step, boolean isInnerStep, CaseType caseType)
			throws Exception {
		try {
			monitor.subTask(step.getStepName());
			step.invoke(datasource, monitor, caseType, result);
			for (ISchemaUpdaterStep innerStep : step.getInnerSteps()){
				handleSingleStep(datasource, monitor, result, innerStep, true, caseType);
				if (!result.isSuccess()){
				    break;
				}
			}
//			if (! isInnerStep){
			monitor.worked(1);
//			}
		} catch (Exception e) {
		    String message = "Monitor: Exception occurred while handling single schema updating step";
			monitor.warning(message, e);
			datasource.rollback();
			result.addException(e, message, "handleSingleStep:" + step.getStepName());
		}
		return;
	}

	   /**
     * @param datasourceVersion
     * @param monitor
     * @param result
     * @return
     */
    private boolean isBefore4_0_0(String datasourceVersion, IProgressMonitor monitor, SchemaUpdateResult result) {
        if (CdmMetaData.compareVersion(datasourceVersion, "4.0.0.0", 3, monitor) < 0){
            String message = "Schema version of the database is prior to version 4.0.0.\n"
                    + "Versions prior to 4.0.0 need to be updated by an EDIT Platform between 4.0 and 4.7 (including both).\n"
                    + "Please update first to version 4.0.0 (or higher) before updating to the current version.";
            result.addError(message, "CdmUpdater.updateToCurrentVersion");
            return true;
        }
        return false;
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
