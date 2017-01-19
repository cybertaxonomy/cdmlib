/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 16.09.2010
 *
 */
public class TableDroper extends AuditedSchemaUpdaterStepBase<TableDroper> implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(TableDroper.class);

	private boolean ifExists = true;

	public static final TableDroper NewInstance(String stepName, String tableName, boolean includeAudTable){
		return new TableDroper(stepName, tableName, includeAudTable, true);
	}

	/**
	 * @param stepName
	 * @param tableName
	 * @param includeAudTable
	 * @param ifExists if false, and error will be thrown if the table does not exist and can therefore not be dropped.
	 * @see #NewInstance(String, String, boolean)
	 * @return
	 */
	public static final TableDroper NewInstance(String stepName, String tableName, boolean includeAudTable, boolean ifExists){
		return new TableDroper(stepName, tableName, includeAudTable, ifExists);
	}


	protected TableDroper(String stepName, String tableName, boolean includeAudTable, boolean ifExists) {
		super(stepName, tableName, includeAudTable);
		this.ifExists = ifExists;
	}

	@Override
	protected boolean invokeOnTable(String tableName, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) {
		boolean result = true;
		try {
			String updateQuery = getUpdateQueryString(tableName, datasource, monitor);
			datasource.executeUpdate(updateQuery);
			if (! this.isAuditing){
				removeFromHibernateSequences(datasource, monitor, tableName);
			}
			return result;
		} catch ( Exception e) {
			monitor.warning(e.getMessage(), e);
			logger.error(e);
			return false;
		}
	}

	private boolean removeFromHibernateSequences(ICdmDataSource datasource, IProgressMonitor monitor, String tableName) {
		try {
			//TODO do we need to "case" this table name?
			String sql = " DELETE FROM hibernate_sequences WHERE sequence_name = '%s'";
			sql = String.format(sql, tableName);
			datasource.executeUpdate(sql);
			return true;
		} catch (Exception e) {
			String message = "Exception occurred when trying to read or update hibernate_sequences table for value " + this.tableName + ": " + e.getMessage();
			monitor.warning(message, e);
			logger.error(message);
			return false;
		}

	}

	/**
	 * @param tableName cased tableName
	 * @param datasource
	 * @param monitor
	 * @param caseType
	 * @return
	 * @throws DatabaseTypeNotSupportedException
	 */
	public String getUpdateQueryString(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) throws DatabaseTypeNotSupportedException {
		String updateQuery;
		DatabaseTypeEnum type = datasource.getDatabaseType();

		updateQuery = "DROP TABLE @ifExists @tableName ";
		if (type.equals(DatabaseTypeEnum.SqlServer2005)){
			//MySQL allows both syntaxes
			updateQuery = " if exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME='@tableName') BEGIN drop table @tableName end ";
		}else if (type.equals(DatabaseTypeEnum.H2) || type.equals(DatabaseTypeEnum.PostgreSQL) || type.equals(DatabaseTypeEnum.MySQL)){
//			updateQuery = "ALTER TABLE @tableName @addSeparator @columnName @columnType";
		}else{
			updateQuery = null;
			String warning = "Update step '" + this.getStepName() + "' is not supported by " + type.getName();
			monitor.warning(warning);
			throw new DatabaseTypeNotSupportedException(warning);
		}
		updateQuery = updateQuery.replace("@tableName", tableName);
		if (ifExists == true){
			updateQuery = updateQuery.replace("@ifExists", "IF EXISTS");
		}else{
			updateQuery = updateQuery.replace("@ifExists", "");
		}

		return updateQuery;
	}


}
