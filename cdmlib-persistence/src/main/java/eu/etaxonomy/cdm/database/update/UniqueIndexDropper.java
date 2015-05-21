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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 16.09.2010
 *
 */
public class UniqueIndexDropper extends AuditedSchemaUpdaterStepBase<UniqueIndexDropper> implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(UniqueIndexDropper.class);

	private final String indexColumn;

	public static final UniqueIndexDropper NewInstance(String tableName, String indexColumn, boolean includeAudTable){
		String stepName = "Drop index " + tableName + "-" + indexColumn;
		return new UniqueIndexDropper(stepName, tableName, indexColumn, includeAudTable);
	}


	protected UniqueIndexDropper(String stepName, String tableName, String indexColumn, boolean includeAudTable) {
		super(stepName, tableName, includeAudTable);
		this.indexColumn = indexColumn;
	}

	@Override
	protected boolean invokeOnTable(String tableName, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) {
		try {
			if (checkExists(datasource)){
				String updateQuery = getUpdateQueryString(tableName, datasource, monitor);
				datasource.executeUpdate(updateQuery);
			}
			return true;
		} catch ( Exception e) {
			monitor.warning(e.getMessage(), e);
			return false;
		}
	}

	private boolean checkExists(ICdmDataSource datasource) throws SQLException, DatabaseTypeNotSupportedException {
		DatabaseTypeEnum type = datasource.getDatabaseType();
		if (type.equals(DatabaseTypeEnum.MySQL)){
			String sql = "SELECT count(*)	FROM information_schema.TABLE_CONSTRAINTS " +
					" WHERE table_name ='@tableName' AND CONSTRAINT_SCHEMA = '@dbName' AND CONSTRAINT_TYPE = 'UNIQUE' ";
			sql = sql.replace("@tableName", tableName);
			sql = sql.replace("@columnName", indexColumn);
			sql = sql.replace("@dbName", datasource.getDatabase());
			long count = (Long)datasource.getSingleValue(sql);
			return count > 0;
		}else if (type.equals(DatabaseTypeEnum.PostgreSQL)){
			logger.warn("checkExists not yet implemented for PostGreSQL" );
			return true;
		}else if (type.equals(DatabaseTypeEnum.H2)){
			String indexName = getIndexName(datasource);
			return indexName != null;
		}else{
			// not needed
			return true;
		}
	}


	public String getUpdateQueryString(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) throws DatabaseTypeNotSupportedException, SQLException {
		//NOTE: no caseType required here
		String updateQuery;
		DatabaseTypeEnum type = datasource.getDatabaseType();
		String indexName = getIndexName(datasource);

//		if (type.equals(DatabaseTypeEnum.SqlServer2005)){
			//MySQL allows both syntaxes
//			updateQuery = "ALTER TABLE @tableName ADD @columnName @columnType";
//		}else
			if (type.equals(DatabaseTypeEnum.H2)){
			updateQuery = "ALTER TABLE @tableName DROP CONSTRAINT IF EXISTS @indexName";
		}else if (type.equals(DatabaseTypeEnum.PostgreSQL)){
//			updateQuery = "DROP INDEX IF EXISTS @indexName";  // does not work because index is used in the constraint
//			updateQuery = "ALTER TABLE @tableName DROP CONSTRAINT IF EXISTS @indexName"; //"if exists" does not work (version 8.4)
			updateQuery = "ALTER TABLE @tableName DROP CONSTRAINT @indexName";
		}else if (type.equals(DatabaseTypeEnum.MySQL)){
			updateQuery = "ALTER TABLE @tableName DROP INDEX @indexName";
		}else{
			updateQuery = null;
			String warning = "Update step '" + this.getStepName() + "' is not supported by " + type.getName();
			monitor.warning(warning);
			throw new DatabaseTypeNotSupportedException(warning);
		}
		updateQuery = updateQuery.replace("@tableName", tableName);
		updateQuery = updateQuery.replace("@indexName", indexName);

		return updateQuery;
	}


	private String getIndexName(ICdmDataSource datasource) throws DatabaseTypeNotSupportedException, SQLException {
		String result = this.indexColumn;
		DatabaseTypeEnum type = datasource.getDatabaseType();
		if (type.equals(DatabaseTypeEnum.SqlServer2005)){
			throw new DatabaseTypeNotSupportedException(type.toString());
		}else if (type.equals(DatabaseTypeEnum.MySQL)){
			result = this.indexColumn;
		}else if (type.equals(DatabaseTypeEnum.H2) ){
//			String sql = "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME = @tableName AND INDEX_TYPE_NAME = 'UNIQUE INDEX'";
			String sql = "SELECT CONSTRAINT_NAME " +
					" FROM INFORMATION_SCHEMA.CONSTRAINTS "+
					" WHERE CONSTRAINT_CATALOG = '@dbName' AND "+
					" TABLE_NAME = '@tableName' AND CONSTRAINT_TYPE = 'UNIQUE' AND "+
					" COLUMN_LIST = '@columnName'";
			sql = sql.replace("@tableName", tableName.toUpperCase());
			sql = sql.replace("@columnName", indexColumn.toUpperCase());
			sql = sql.replace("@dbName", datasource.getDatabase().toUpperCase());
			String constraintName = (String)datasource.getSingleValue(sql);
			result = constraintName;
		}else if (type.equals(DatabaseTypeEnum.PostgreSQL)){
			//TODO do we need this cased?
			result = this.tableName + "_" + this.indexColumn + "_key";
		}else{
			throw new DatabaseTypeNotSupportedException(type.toString());
		}
		return result;

	}


}
