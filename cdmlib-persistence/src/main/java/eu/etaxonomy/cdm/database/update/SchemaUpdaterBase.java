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

/**
 * Base class for updating a schema.
 *
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public abstract class SchemaUpdaterBase extends UpdaterBase<ISchemaUpdaterStep, ISchemaUpdater> implements ISchemaUpdater {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SchemaUpdaterBase.class);

	public static boolean INCLUDE_AUDIT = true;
	protected static boolean INCLUDE_CDM_BASE = true;
	protected static boolean NOT_NULL = true;

//	private List<ISchemaUpdaterStep> list;


	protected abstract List<ISchemaUpdaterStep> getUpdaterList();


	protected SchemaUpdaterBase(String startSchemaVersion, String endSchemaVersion){
		this.startVersion = startSchemaVersion;
		this.targetVersion = endSchemaVersion;
		list = getUpdaterList();
	}

	@Override
	protected boolean updateVersion(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
			int intSchemaVersion = 0;
			String sqlUpdateSchemaVersion = "UPDATE %s SET value = '" + this.targetVersion + "' WHERE propertyname = " +  intSchemaVersion;
			sqlUpdateSchemaVersion = String.format(sqlUpdateSchemaVersion, caseType.transformTo("CdmMetaData"), this.targetVersion);
			try {
				int n = datasource.executeUpdate(sqlUpdateSchemaVersion);
				return n > 0;

			} catch (Exception e) {
				monitor.warning("Error when trying to set new schemaversion: ", e);
				throw new SQLException(e);
			}
	}

	@Override
	protected String getCurrentVersion(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {
		int intSchemaVersion = 0;
		String sqlSchemaVersion = caseType.replaceTableNames( "SELECT value FROM @@CdmMetaData@@ WHERE propertyname = " +  intSchemaVersion);

		try {
			String value = (String)datasource.getSingleValue(sqlSchemaVersion);
			return value;
		} catch (SQLException e) {
			monitor.warning("Error when trying to receive schemaversion: ", e);
			throw e;
		}
	}


}
