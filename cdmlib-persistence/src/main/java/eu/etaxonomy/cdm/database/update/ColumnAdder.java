/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.Types;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 16.09.2010
 *
 */
public class ColumnAdder extends AuditedSchemaUpdaterStepBase<ColumnAdder> {
	private static final Logger logger = Logger.getLogger(ColumnAdder.class);

	private final String newColumnName;
	private final String columnType;
	private final Object defaultValue;
	private boolean isNotNull;
	private final String referencedTable;

	/**
	 * Add ForeignKey.
	 * @param referencedTable
	 * @return
	 */
	public static final ColumnAdder NewIntegerInstance(String stepName, String tableName, String newColumnName, boolean includeAudTable, boolean notNull, String referencedTable){
		return new ColumnAdder(stepName, tableName, newColumnName, "int", includeAudTable, null, notNull, referencedTable);
	}

	public static final ColumnAdder NewIntegerInstance(String stepName, String tableName, String newColumnName, boolean includeAudTable, Integer defaultValue, boolean notNull){
		return new ColumnAdder(stepName, tableName, newColumnName, "int", includeAudTable, defaultValue, notNull, null);
	}

	public static final ColumnAdder NewTinyIntegerInstance(String stepName, String tableName, String newColumnName, boolean includeAudTable, boolean notNull){
		return new ColumnAdder(stepName, tableName, newColumnName, "tinyint", includeAudTable, null, notNull, null);
	}

	public static final ColumnAdder NewDoubleInstance(String stepName, String tableName, String newColumnName, boolean includeAudTable, boolean notNull){
		return new ColumnAdder(stepName, tableName, newColumnName, "double", includeAudTable, null, notNull, null);
	}

	public static final ColumnAdder NewBooleanInstance(String stepName, String tableName, String newColumnName, boolean includeAudTable, Boolean defaultValue){
		return new ColumnAdder(stepName, tableName, newColumnName, "bit", includeAudTable, defaultValue, false, null);
	}

	public static final ColumnAdder NewStringInstance(String stepName, String tableName, String newColumnName, boolean includeAudTable){
		return new ColumnAdder(stepName, tableName, newColumnName, "nvarchar(255)", includeAudTable, null, false, null);
	}

	public static final ColumnAdder NewStringInstance(String stepName, String tableName, String newColumnName, int length, boolean includeAudTable){
		return new ColumnAdder(stepName, tableName, newColumnName, "nvarchar("+length+")", includeAudTable, null, false, null);
	}

	public static final ColumnAdder NewClobInstance(String stepName, String tableName, String newColumnName, boolean includeAudTable){
		return new ColumnAdder(stepName, tableName, newColumnName, "clob", includeAudTable, null, false, null);
	}

	public static final ColumnAdder NewDateTimeInstance(String stepName, String tableName, String newColumnName, boolean includeAudTable, boolean notNull){
		return new ColumnAdder(stepName, tableName, newColumnName, "datetime", includeAudTable, null, notNull, null);
	}

	protected ColumnAdder(String stepName, String tableName, String newColumnName, String columnType, boolean includeAudTable, Object defaultValue, boolean notNull, String referencedTable) {
		super(stepName, tableName, includeAudTable);
		this.newColumnName = newColumnName;
		this.columnType = columnType;
		this.defaultValue = defaultValue;
		this.isNotNull = notNull;
		this.referencedTable = referencedTable;
	}

	public ColumnAdder setNotNull(boolean isNotNull) {
		this.isNotNull = isNotNull;
		return this;
	}

	@Override
	protected boolean invokeOnTable(String tableName, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) {
		boolean result = true;
		try {
			String updateQuery = getUpdateQueryString(tableName, datasource, monitor);
			datasource.executeUpdate(updateQuery);

			if (defaultValue instanceof Boolean){
				String defaultValueQuery = "UPDATE @tableName SET @columnName = " + (defaultValue == null ? "NULL" : getBoolean((Boolean) defaultValue, datasource));
				defaultValueQuery = defaultValueQuery.replace("@tableName", tableName);
				defaultValueQuery = defaultValueQuery.replace("@columnName", newColumnName);
				datasource.executeUpdate(defaultValueQuery);
			}else if (defaultValue instanceof Integer){
				String defaultValueQuery = "UPDATE @tableName SET @columnName = " + (defaultValue == null ? "NULL" : defaultValue);
				defaultValueQuery = defaultValueQuery.replace("@tableName", tableName);
				defaultValueQuery = defaultValueQuery.replace("@columnName", newColumnName);
				datasource.executeUpdate(defaultValueQuery);
			}else if (defaultValue != null){
				logger.warn("Default Value not implemented for type " + defaultValue.getClass().getName());
			}
			if (referencedTable != null){
				result &= TableCreator.makeForeignKey(tableName, datasource, monitor, newColumnName, referencedTable, caseType);
			}

			return result;
		} catch ( Exception e) {
			monitor.warning(e.getMessage(), e);
			logger.error(e);
			return false;
		}
	}

	/**
	 * Returns the update query string. tableName must already be cased correctly. See {@link CaseType}.
	 * @param tableName correctly cased table name
	 * @param datasource data source
	 * @param monitor monitor
	 * @return the query string
	 * @throws DatabaseTypeNotSupportedException
	 */
	public String getUpdateQueryString(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) throws DatabaseTypeNotSupportedException {
		String updateQuery;
		DatabaseTypeEnum type = datasource.getDatabaseType();
		String databaseColumnType = getDatabaseColumnType(datasource, this.columnType);

		if (type.equals(DatabaseTypeEnum.SqlServer2005)){
			//MySQL allows both syntaxes
			updateQuery = "ALTER TABLE @tableName ADD @columnName @columnType";
		}else if (type.equals(DatabaseTypeEnum.H2) || type.equals(DatabaseTypeEnum.PostgreSQL) || type.equals(DatabaseTypeEnum.MySQL)){
			updateQuery = "ALTER TABLE @tableName @addSeparator @columnName @columnType";
		}else{
			updateQuery = null;
			String warning = "Update step '" + this.getStepName() + "' is not supported by " + type.getName();
			monitor.warning(warning);
			throw new DatabaseTypeNotSupportedException(warning);
		}
		if (isNotNull){
			updateQuery += " NOT NULL";
		}
		updateQuery = updateQuery.replace("@tableName", tableName);
		updateQuery = updateQuery.replace("@columnName", newColumnName);
		updateQuery = updateQuery.replace("@columnType", databaseColumnType);
		updateQuery = updateQuery.replace("@addSeparator", getAddColumnSeperator(datasource));

		return updateQuery;
	}

	protected static String getDatabaseColumnType(ICdmDataSource datasource, String columnType) {
		String result = columnType;
		DatabaseTypeEnum dbType = datasource.getDatabaseType();
		//nvarchar
		if (dbType.equals(DatabaseTypeEnum.PostgreSQL)){  //TODO use PostgeSQL82 Dialect infos
			result = result.replace("nvarchar", "varchar");
			result = result.replace("double", "float8");
			result = result.replace("bit", DatabaseTypeEnum.PostgreSQL.getHibernateDialect().getTypeName(Types.BIT));
			result = result.replace("datetime", DatabaseTypeEnum.PostgreSQL.getHibernateDialect().getTypeName(Types.TIMESTAMP));
			result = result.replace("tinyint", DatabaseTypeEnum.PostgreSQL.getHibernateDialect().getTypeName(Types.TINYINT));
		}
		//CLOB
		if (columnType.equalsIgnoreCase("clob")){
			//TODO use hibernate dialects
			if (dbType.equals(DatabaseTypeEnum.MySQL)){
				result = "longtext";
			}else if (dbType.equals(DatabaseTypeEnum.H2)){
				result = "CLOB";  //or NVARCHAR
			}else if (dbType.equals(DatabaseTypeEnum.PostgreSQL)){
				result = "text";
			}else if (dbType.equals(DatabaseTypeEnum.SqlServer2005)){
				result = "NVARCHAR(MAX)";
			}
		}
		return result;
	}


	/**
	 * Returns the sql keywords for adding a column. This is usually 'ADD' or 'ADD COLUMN'
	 * @param datasource
	 * @return
	 * @throws DatabaseTypeNotSupportedException
	 */
	public static String getAddColumnSeperator(ICdmDataSource datasource) throws DatabaseTypeNotSupportedException {
		DatabaseTypeEnum type = datasource.getDatabaseType();
		if (type.equals(DatabaseTypeEnum.SqlServer2005)){
			return "ADD ";
		}else if (type.equals(DatabaseTypeEnum.H2) || type.equals(DatabaseTypeEnum.PostgreSQL) || type.equals(DatabaseTypeEnum.MySQL)){
			return "ADD COLUMN ";
		}else{
			throw new DatabaseTypeNotSupportedException(datasource.getName());
		}
	}

	public String getReferencedTable() {
		return referencedTable;
	}

	public String getNewColumnName() {
		return newColumnName;
	}
}
