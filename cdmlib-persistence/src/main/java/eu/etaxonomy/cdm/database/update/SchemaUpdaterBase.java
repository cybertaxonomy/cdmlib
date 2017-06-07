/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;
import eu.etaxonomy.cdm.model.metadata.CdmMetaDataPropertyName;

/**
 * Base class for updating a schema.
 *
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public abstract class SchemaUpdaterBase
            extends UpdaterBase<ISchemaUpdaterStep, ISchemaUpdater>
            implements ISchemaUpdater {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdaterBase.class);

	public static final boolean INCLUDE_AUDIT = true;
	protected static final boolean INCLUDE_CDM_BASE = true;
	protected static final boolean NOT_NULL = true;
	protected static final boolean IS_LIST = true;
	protected static final boolean IS_1_TO_M = true;
	protected static final boolean IS_M_TO_M = false;


	protected abstract List<ISchemaUpdaterStep> getUpdaterList();


	protected SchemaUpdaterBase(String startSchemaVersion, String endSchemaVersion){
		this.startVersion = startSchemaVersion;
		this.targetVersion = endSchemaVersion;
		list = getUpdaterList();
	}

	@Override
	protected void updateVersion(ICdmDataSource datasource, IProgressMonitor monitor,
	            CaseType caseType, SchemaUpdateResult result) throws SQLException {
			int intSchemaVersion = 0;
			String sqlUpdateSchemaVersionOld = "UPDATE %s SET value = '" + this.targetVersion + "' WHERE propertyname = " +  intSchemaVersion;
			sqlUpdateSchemaVersionOld = String.format(sqlUpdateSchemaVersionOld, caseType.transformTo("CdmMetaData"));
			String sqlUpdateSchemaVersion = "UPDATE %s SET value = '" + this.targetVersion + "' WHERE propertyname = '%s'";
			sqlUpdateSchemaVersion = String.format(sqlUpdateSchemaVersion, caseType.transformTo("CdmMetaData"), CdmMetaDataPropertyName.DB_SCHEMA_VERSION.getKey());

			boolean isPriorTo4_7 = CdmMetaData.compareVersion("4.6.0.0", this.targetVersion, 2, monitor) > 0;

			String sql = isPriorTo4_7 ? sqlUpdateSchemaVersionOld : sqlUpdateSchemaVersion;
			try {
				int n = datasource.executeUpdate(sql);
				if (n == 0){
				    result.addError("Schema version was not updated", "SchemaUpdaterBase.updateVersion()");
				}
				return;

			} catch (Exception e) {
				monitor.warning("Error when trying to set new schemaversion: ", e);
				throw new SQLException(e);
			}
	}

	@Override
	protected String getCurrentVersion(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		String sqlSchemaVersion = caseType.replaceTableNames( "SELECT value FROM @@CdmMetaData@@ WHERE propertyname = 'SCHEMA_VERSION'");
		try {
            String value = (String)datasource.getSingleValue(sqlSchemaVersion);
            return value;
		} catch (Exception e) {
		    //looks like propertyname is still integer;
		    //ATTENTION: the below SQL returns all records if run against CdmMetaData with propertyname being a string
		    sqlSchemaVersion = caseType.replaceTableNames( "SELECT value FROM @@CdmMetaData@@ WHERE propertyname = 0 ORDER BY propertyname");
		    try {
		        ResultSet rs = datasource.executeQuery(sqlSchemaVersion);
		        boolean hasMoreThanOneRecord = false;
		        String result = null;
		        while(rs.next()){
		            if (hasMoreThanOneRecord){
		                String message = "Reading schema version from database returns more than 1 record";
		                monitor.warning(message);
		                throw new RuntimeException(message);
		            }
		            result = rs.getString("value");
		        }
		        if (result == null){
		            String message = "Reading schema version from database returned no result";
                    monitor.warning(message);
                    throw new RuntimeException(message);
		        }
		        return result;
		    } catch (SQLException e1) {
		        monitor.warning("Error when trying to receive schemaversion: ", e1);
		        throw e;
		    }
        }

	}
}
