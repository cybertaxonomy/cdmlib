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
	private String mySchemaVersion;
	
	private List<SimpleSchemaUpdaterStep> list;
	
	
	
	protected SchemaUpdaterBase(String mySchemaVersion){
		this.mySchemaVersion = mySchemaVersion;
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
		boolean result = true;
		
		String datasourceSchemaVersion;
		try {
			datasourceSchemaVersion = getCurrentVersion(datasource, monitor);
		} catch (SQLException e1) {
			monitor.warning("SQLException", e1);
			return false;
		}
		
		boolean isAfterMyVersion = isAfterMyVersion(datasourceSchemaVersion, monitor);
		if (isAfterMyVersion){
			String warning = "Database version is higher than updater version";
			RuntimeException exeption = new RuntimeException(warning);
			monitor.warning(warning, exeption);
			throw exeption;
		}
		
		boolean isBeforeMyVersion = isBeforeMyVersion(datasourceSchemaVersion, monitor);
		if (isBeforeMyVersion){
			if (getPreviousUpdater() == null){
				String warning = "Database version is before updater version but no previous version updater exists";
				RuntimeException exeption = new RuntimeException(warning);
				monitor.warning(warning, exeption);
				throw exeption;
			}
			result &= getPreviousUpdater().invoke(datasource, monitor);
		}
		
		
		for (SimpleSchemaUpdaterStep step : list){
			try {
				monitor.subTask(step.getStepName());
				result &= step.invoke(datasource, monitor);
				monitor.worked(1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				monitor.warning("Exception occurred while updating schema", e);
				throw e;
			}
		}
		return result;
		
		
	}

	
	protected abstract List<SimpleSchemaUpdaterStep> getUpdaterList();

	protected boolean isAfterMyVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, mySchemaVersion, depth, monitor);
		return compareResult > 0;
	}

	protected boolean isBeforeMyVersion(String dataSourceSchemaVersion, IProgressMonitor monitor) {
		int depth = 4;
		int compareResult = CdmMetaData.compareVersion(dataSourceSchemaVersion, mySchemaVersion, depth, monitor);
		return compareResult > 0;
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
}
