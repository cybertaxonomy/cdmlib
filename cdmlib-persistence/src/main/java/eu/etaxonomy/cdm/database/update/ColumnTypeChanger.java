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
public class ColumnTypeChanger
        extends AuditedSchemaUpdaterStepBase {

    private static final Logger logger = Logger.getLogger(ColumnTypeChanger.class);

	private final String columnName;
	private final String newColumnType;
	private final Object defaultValue;
	private final boolean isNotNull;
	private final String referencedTable;


	public static final ColumnTypeChanger NewStringSizeInstance(String stepName, String tableName, String columnName, int newSize, boolean includeAudTable){
		return new ColumnTypeChanger(stepName, tableName, columnName, "nvarchar("+newSize+")", includeAudTable, null, false, null);
	}

	public static final ColumnTypeChanger NewClobInstance(String stepName, String tableName, String columnName, boolean includeAudTable){
		return new ColumnTypeChanger(stepName, tableName, columnName, "clob", includeAudTable, null, false, null);
	}

	public static final ColumnTypeChanger NewInt2DoubleInstance(String stepName, String tableName, String columnName, boolean includeAudTable){
		return new ColumnTypeChanger(stepName, tableName, columnName, "double", includeAudTable, null, false, null);
	}

	public static final ColumnTypeChanger NewInt2StringInstance(String stepName, String tableName, String columnName, int size, boolean includeAudTable, Integer defaultValue, boolean notNull){
		return new ColumnTypeChanger(stepName, tableName, columnName, "nvarchar("+size+")", includeAudTable, defaultValue, notNull, null);
	}

//	public static final ColumnTypeChanger NewChangeAllowNullOnStringChanger(){
//
//	}


	protected ColumnTypeChanger(String stepName, String tableName, String columnName, String newColumnType, boolean includeAudTable, Object defaultValue, boolean notNull, String referencedTable) {
		super(stepName, tableName, includeAudTable);
		this.columnName = columnName;
		this.newColumnType = newColumnType;
		this.defaultValue = defaultValue;
		this.isNotNull = notNull;
		this.referencedTable = referencedTable;
	}

	@Override
	protected boolean invokeOnTable(String tableName, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) {
		boolean result = true;
		try {

			String updateQuery;
			if (this.isNotNull){
				updateQuery = getNotNullUpdateQuery(tableName, datasource, monitor);
				datasource.executeUpdate(updateQuery);
			}

			updateQuery = getUpdateQueryString(tableName, datasource, monitor);
			datasource.executeUpdate(updateQuery);

			if (defaultValue instanceof Boolean){
				updateQuery = "UPDATE @tableName SET @columnName = " + (defaultValue == null ? "null" : getBoolean((Boolean) defaultValue, datasource));
				updateQuery = updateQuery.replace("@tableName", tableName);
				updateQuery = updateQuery.replace("@columnName", columnName);
				datasource.executeUpdate(updateQuery);
			}
			if (referencedTable != null){
				result &= TableCreator.makeForeignKey(tableName, datasource, monitor, columnName, referencedTable, caseType);
			}

			return result;
		} catch ( Exception e) {
			monitor.warning(e.getMessage(), e);
			logger.error(e);
			return false;
		}
	}

	private String getNotNullUpdateQuery(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) {
		String query = " UPDATE %s SET %s = %S WHERE %s IS NULL ";
		String defaultValueStr = String.valueOf(this.defaultValue);
		if (this.defaultValue instanceof Integer){
			//OK
		}else{
			defaultValueStr = "'" + defaultValueStr + "'";
		}
		query = String.format(query, tableName, this.columnName, defaultValueStr, this.columnName);
		return query;
	}

	public String getUpdateQueryString(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) throws DatabaseTypeNotSupportedException {
		String updateQuery;
		DatabaseTypeEnum type = datasource.getDatabaseType();
		String databaseColumnType = getDatabaseColumnType(datasource, this.newColumnType);

		if (type.equals(DatabaseTypeEnum.SqlServer2005)){
			//MySQL allows both syntaxes
			updateQuery = "ALTER TABLE @tableName ALTER COLUMN @columnName @columnType";
		}else if (type.equals(DatabaseTypeEnum.H2)){
			updateQuery = "ALTER TABLE @tableName ALTER COLUMN @columnName @columnType";
		}else if (type.equals(DatabaseTypeEnum.PostgreSQL)){
			updateQuery = "ALTER TABLE @tableName ALTER COLUMN @columnName TYPE @columnType";
		}else if (type.equals(DatabaseTypeEnum.MySQL)){
			updateQuery = "ALTER TABLE @tableName MODIFY COLUMN @columnName @columnType";
		}else{
			updateQuery = null;
			String warning = "Update step '" + this.getStepName() + "' is not supported by " + type.getName();
			monitor.warning(warning);
			throw new DatabaseTypeNotSupportedException(warning);
		}
		if (isNotNull){
			if (datasource.getDatabaseType().equals(DatabaseTypeEnum.PostgreSQL)){
				logger.warn("NOT NULL not implementd for POSTGRES");
			}else{
				updateQuery += " NOT NULL";
			}
		} else{
			if (! datasource.getDatabaseType().equals(DatabaseTypeEnum.PostgreSQL)){
				updateQuery += " NULL ";
			}
		}
		updateQuery = updateQuery.replace("@tableName", tableName);
		updateQuery = updateQuery.replace("@columnName", columnName);
		updateQuery = updateQuery.replace("@columnType", databaseColumnType);
//		updateQuery = updateQuery.replace("@addSeparator", getAddColumnSeperator(datasource));

		return updateQuery;
	}

	private String getDatabaseColumnType(ICdmDataSource datasource, String columnType) {
		return ColumnAdder.getDatabaseColumnType(datasource, columnType);
	}

	public String getReferencedTable() {
		return referencedTable;
	}
//
//	public String getNewColumnName() {
//		return columnName;
//	}

}
