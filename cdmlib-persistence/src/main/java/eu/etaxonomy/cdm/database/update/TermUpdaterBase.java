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
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.CdmMetaData;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public abstract class TermUpdaterBase implements ITermUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdaterBase.class);
	protected static final UUID uuidFeatureVocabulary = UUID.fromString("b187d555-f06f-4d65-9e53-da7c93f8eaa8");
	
	private List<SingleTermUpdater> list;
	private String startTermVersion;
	private String targetTermVersion;
	
	
	
	protected TermUpdaterBase(String startTermVersion, String targetTermVersion){
		this.startTermVersion = startTermVersion;
		this.targetTermVersion = targetTermVersion;
		list = getUpdaterList();
	}
	

	@Override
	public int countSteps(ICdmDataSource datasource){
		int result = 0;
		//TODO test if previous updater is needed
		if (getPreviousUpdater() != null){
			result += getPreviousUpdater().countSteps(datasource);
		}
		result += list.size();
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#invoke()
	 */
	@Override
	public boolean invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws Exception{
		String currentLibraryTermVersion = CdmMetaData.getCurrentTermsVersion();
		return invoke(currentLibraryTermVersion, datasource, monitor);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#invoke()
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
		boolean isAfterMyTargetVersion = isAfterMyTargetVersion(targetVersion, monitor);
		boolean isBeforeMyTargetVersion = isBeforeMyTargetVersion(targetVersion, monitor);
		boolean isDatasourceBeforeMyTargetVersion = isBeforeMyTargetVersion(datasourceSchemaVersion, monitor);
		

		if (! isDatasourceBeforeMyTargetVersion){
			String warning = "Target version ("+targetVersion+") is not before updater target version ("+this.targetTermVersion+"). Nothing to update.";
			monitor.warning(warning);
			return true;
		}
		
		if (isAfterMyStartVersion){
			String warning = "Database version is higher than updater start version";
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
			result &= getPreviousUpdater().invoke(startTermVersion, datasource, monitor);
		}

		
		if (isBeforeMyTargetVersion){
			String warning = "Target version ("+targetVersion+") is lower than updater target version ("+this.targetTermVersion+")";
			RuntimeException exeption = new RuntimeException(warning);
			monitor.warning(warning, exeption);
			throw exeption;
		}

		
		
		for (SingleTermUpdater step : list){
			try {
				monitor.subTask(step.getStepName());
				Integer stepResult = step.invoke(datasource, monitor);
				result &= (stepResult != null);
				monitor.worked(1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				monitor.warning("Exception occurred while updating schema", e);
				result = false;
			}
		}
		updateTermVersion(datasource, monitor);

		return result;
	}
	
	private void updateTermVersion(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		int intSchemaVersion = 1;
		String sqlUpdateSchemaVersion = "UPDATE CdmMetaData SET value = '" + this.targetTermVersion + "' WHERE propertyname = " +  intSchemaVersion;
		try {
			datasource.executeUpdate(sqlUpdateSchemaVersion);
		} catch (Exception e) {
			monitor.warning("Error when trying to set new schemaversion: ", e);
			throw new SQLException(e);
		}
	
}
	
	protected abstract List<SingleTermUpdater> getUpdaterList();

	protected boolean isAfterMyStartVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, startTermVersion, depth, monitor);
		return compareResult > 0;
	}

	protected boolean isBeforeMyStartVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, startTermVersion, depth, monitor);
		return compareResult < 0;
	}

	protected boolean isAfterMyTargetVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, targetTermVersion, depth, monitor);
		return compareResult > 0;
	}

	protected boolean isBeforeMyTargetVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, targetTermVersion, depth, monitor);
		return compareResult < 0;
	}
	

	protected String getCurrentVersion(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		int intSchemaVersion = 1;
		try {
			String sqlCount = "SELECT count(*) FROM CdmMetaData WHERE propertyname = " +  intSchemaVersion;
			Integer count = (Integer)datasource.getSingleValue(sqlCount);
			if (count == 0){
				String defaultVersion = "2.4.2.2.201006011715";
				String sqlMaxId = "SELECT max(id) FROM CdmMetaData";
				Integer maxId = (Integer)datasource.getSingleValue(sqlMaxId) + 1;
				String sqlUpdate = "INSERT INTO CdmMetaData (id, created, propertyname, value) VALUES (" + maxId + "'2010-09-21 13:52:54', 1, '" + defaultVersion + "')";
				datasource.executeUpdate(sqlUpdate);
				return defaultVersion;
			}else{
				String sqlSchemaVersion = "SELECT value FROM CdmMetaData WHERE propertyname = " +  intSchemaVersion;
				String value = (String)datasource.getSingleValue(sqlSchemaVersion);
				return value;
			}
		} catch (SQLException e) {
			monitor.warning("Error when trying to receive schemaversion: ", e);
			throw e;
		}
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getNextUpdater()
	 */
	@Override
	public abstract ITermUpdater getNextUpdater();

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getPreviousUpdater()
	 */
	@Override
	public abstract ITermUpdater getPreviousUpdater();

	
	@Override
	public String getTargetVersion() {
		return this.targetTermVersion;
	}
}
