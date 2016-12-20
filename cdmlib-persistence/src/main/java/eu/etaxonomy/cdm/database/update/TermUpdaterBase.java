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

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public abstract class TermUpdaterBase extends UpdaterBase<ITermUpdaterStep, ITermUpdater> implements ITermUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermUpdaterBase.class);
	protected static final UUID uuidFeatureVocabulary = UUID.fromString("b187d555-f06f-4d65-9e53-da7c93f8eaa8");
	
	protected TermUpdaterBase(String startTermVersion, String targetTermVersion){
		this.startVersion = startTermVersion;
		this.targetVersion = targetTermVersion;
		list = getUpdaterList();
	}
	
	@Override
	protected boolean updateVersion(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		int intSchemaVersion = 1;
		String sqlUpdateSchemaVersion = "UPDATE %s SET value = '%s' WHERE propertyname = " +  intSchemaVersion;
		sqlUpdateSchemaVersion = String.format(sqlUpdateSchemaVersion, caseType.transformTo("CdmMetaData"), this.targetVersion);
		try {
			int n = datasource.executeUpdate(sqlUpdateSchemaVersion);
			return n > 0;
		} catch (Exception e) {
			monitor.warning("Error when trying to set new schemaversion: ", e);
			throw new SQLException(e);
		}
	}
	
	protected abstract List<ITermUpdaterStep> getUpdaterList();

	
	@Override
	public boolean invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws Exception{
		String currentLibrarySchemaVersion = CdmMetaData.getTermsVersion();
		return invoke(currentLibrarySchemaVersion, datasource, monitor, caseType);
	}

	@Override
	protected String getCurrentVersion(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		int intSchemaVersion = 1;
		try {
			String sqlCount = "SELECT count(*) FROM @@CdmMetaData@@ WHERE propertyname = " +  intSchemaVersion;
			Long count = (Long)datasource.getSingleValue(caseType.replaceTableNames(sqlCount));
			if (count == 0){
				String defaultVersion = "2.4.2.2.201006011715";
				String sqlMaxId = caseType.replaceTableNames("SELECT max(id) FROM @@CdmMetaData@@");
				Integer maxId = (Integer)datasource.getSingleValue(sqlMaxId) + 1;
				String sqlUpdate = "INSERT INTO @@CdmMetaData@@ (id, created, uuid, propertyname, value) VALUES (" + maxId + ", '2010-09-21 13:52:54', '"+UUID.randomUUID()+"', 1, '" + defaultVersion + "')";
				datasource.executeUpdate(caseType.replaceTableNames(sqlUpdate));
				return defaultVersion;
			}else{
				String sqlSchemaVersion = "SELECT value FROM @@CdmMetaData@@ WHERE propertyname = " +  intSchemaVersion;
				String value = (String)datasource.getSingleValue(caseType.replaceTableNames(sqlSchemaVersion));
				return value;
			}
		} catch (SQLException e) {
			monitor.warning("Error when trying to receive schemaversion: ", e);
			throw e;
		}
	}
	


}
