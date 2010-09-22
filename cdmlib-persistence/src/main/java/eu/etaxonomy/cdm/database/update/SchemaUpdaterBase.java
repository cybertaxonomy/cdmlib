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
 * @date 10.09.2010
 *
 */
public abstract class SchemaUpdaterBase implements ISchemaUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdaterBase.class);
	private String startSchemaVersion;
	private String targetSchemaVersion;


	protected static boolean INCLUDE_AUDIT = true;
	
	private List<ISchemaUpdaterStep> list;
	
	
	
	protected SchemaUpdaterBase(String startSchemaVersion, String endSchemaVersion){
		this.startSchemaVersion = startSchemaVersion;
		this.targetSchemaVersion = endSchemaVersion;
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
		String currentLibrarySchemaVersion = CdmMetaData.getDbSchemaVersion();
		return invoke(currentLibrarySchemaVersion, datasource, monitor);
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
			String warning = "Target version ("+targetVersion+") is not before updater target version ("+this.targetSchemaVersion+"). Nothing to update.";
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
			result &= getPreviousUpdater().invoke(startSchemaVersion, datasource, monitor);
		}
		

		
		if (isBeforeMyTargetVersion){
			String warning = "Target version ("+targetVersion+") is lower than updater target version ("+this.targetSchemaVersion+")";
			RuntimeException exeption = new RuntimeException(warning);
			monitor.warning(warning, exeption);
			throw exeption;
		}
		
		
		for (ISchemaUpdaterStep step : list){
			try {
				monitor.subTask(step.getStepName());
				Integer termId = step.invoke(datasource, monitor);
				result &= (termId != null);
				monitor.worked(1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				monitor.warning("Exception occurred while updating schema", e);
				throw e;
			}
		}
		updateSchemaVersion(datasource, monitor);
		
		return result;
		
		
	}

	
	private void updateSchemaVersion(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
			int intSchemaVersion = 0;
			String sqlUpdateSchemaVersion = "UPDATE CdmMetaData SET value = '" + this.targetSchemaVersion + "' WHERE propertyname = " +  intSchemaVersion;
			try {
				datasource.executeUpdate(sqlUpdateSchemaVersion);
			} catch (Exception e) {
				monitor.warning("Error when trying to set new schemaversion: ", e);
				throw new SQLException(e);
			}
		
	}

	protected abstract List<ISchemaUpdaterStep> getUpdaterList();

	protected boolean isAfterMyStartVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, startSchemaVersion, depth, monitor);
		return compareResult > 0;
	}

	protected boolean isBeforeMyStartVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, startSchemaVersion, depth, monitor);
		return compareResult < 0;
	}
	protected boolean isAfterMyTargetVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, targetSchemaVersion, depth, monitor);
		return compareResult > 0;
	}

	protected boolean isBeforeMyTargetVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, targetSchemaVersion, depth, monitor);
		return compareResult < 0;
	}


	protected String getCurrentVersion(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		int intSchemaVersion = 0;
		String sqlSchemaVersion = "SELECT value FROM CdmMetaData WHERE propertyname = " +  intSchemaVersion;
		try {
			String value = (String)datasource.getSingleValue(sqlSchemaVersion);
			return value;
		} catch (SQLException e) {
			monitor.warning("Error when trying to receive schemaversion: ", e);
			throw e;
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getNextUpdater()
	 */
	@Override
	public abstract ISchemaUpdater getNextUpdater();

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.ICdmUpdater#getPreviousUpdater()
	 */
	@Override
	public abstract ISchemaUpdater getPreviousUpdater();
	
	@Override
	public String getTargetVersion() {
		return this.targetSchemaVersion;
	}
	
}
