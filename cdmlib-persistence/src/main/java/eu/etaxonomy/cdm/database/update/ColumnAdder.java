/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @since 16.09.2010
 *
 */
public class ColumnAdder extends AuditedSchemaUpdaterStepBase {
	private static final Logger logger = Logger.getLogger(ColumnAdder.class);

	private final String newColumnName;
	private final Datatype columnType;
	private Integer size;
	private final Object defaultValue;
	private boolean isNotNull;
	private final String referencedTable;


	/**
	 * Add ForeignKey.
	 * @param referencedTable
	 * @return
	 */
	public static final ColumnAdder NewIntegerInstance(List<? extends ISchemaUpdaterStep> stepList, String stepName, String tableName, String newColumnName, boolean includeAudTable, boolean notNull, String referencedTable){
		return new ColumnAdder(stepList, stepName, tableName, newColumnName, Datatype.INTEGER, null, includeAudTable, null, notNull, referencedTable);
	}

	public static final ColumnAdder NewIntegerInstance(List<? extends ISchemaUpdaterStep> stepList, String stepName, String tableName, String newColumnName, boolean includeAudTable, Integer defaultValue, boolean notNull){
		return new ColumnAdder(stepList, stepName, tableName, newColumnName, Datatype.INTEGER, null, includeAudTable, defaultValue, notNull, null);
	}

	public static final ColumnAdder NewTinyIntegerInstance(List<? extends ISchemaUpdaterStep> stepList, String stepName, String tableName, String newColumnName, boolean includeAudTable, boolean notNull){
		return new ColumnAdder(stepList, stepName, tableName, newColumnName, Datatype.TINYINTEGER, null, includeAudTable, null, notNull, null);
	}

	public static final ColumnAdder NewDoubleInstance(List<? extends ISchemaUpdaterStep> stepList, String stepName, String tableName, String newColumnName, boolean includeAudTable, boolean notNull){
		return new ColumnAdder(stepList, stepName, tableName, newColumnName, Datatype.DOUBLE, null, includeAudTable, null, notNull, null);
	}

	public static final ColumnAdder NewBooleanInstance(List<? extends ISchemaUpdaterStep> stepList, String stepName, String tableName, String newColumnName, boolean includeAudTable, Boolean defaultValue){
		return new ColumnAdder(stepList, stepName, tableName, newColumnName, Datatype.BIT, null, includeAudTable, defaultValue, false, null);
	}

	/**
	 * Adds a string column with length 255 and default value <code>null</code>
	 */
	public static final ColumnAdder NewStringInstance(List<? extends ISchemaUpdaterStep> stepList, String stepName, String tableName, String newColumnName, boolean includeAudTable){
		return new ColumnAdder(stepList, stepName, tableName, newColumnName, Datatype.VARCHAR, 255, includeAudTable, null, false, null);
	}

    public static final ColumnAdder NewDTYPEInstance(List<? extends ISchemaUpdaterStep> stepList, String stepName, String tableName, String defaultValue, boolean includeAudTable){
        return new ColumnAdder(stepList, stepName, tableName, "DTYPE", Datatype.VARCHAR, 31, includeAudTable, defaultValue, true, null);
    }

	public static final ColumnAdder NewStringInstance(List<? extends ISchemaUpdaterStep> stepList, String stepName, String tableName, String newColumnName, int length, boolean includeAudTable){
		return new ColumnAdder(stepList, stepName, tableName, newColumnName, Datatype.VARCHAR, length, includeAudTable, null, false, null);
	}
    public static final ColumnAdder NewStringInstance(List<? extends ISchemaUpdaterStep> stepList, String stepName, String tableName, String newColumnName, int length, String defaultValue, boolean includeAudTable){
        return new ColumnAdder(stepList, stepName, tableName, newColumnName, Datatype.VARCHAR, length, includeAudTable, defaultValue, false, null);
    }

	public static final ColumnAdder NewClobInstance(List<? extends ISchemaUpdaterStep> stepList, String stepName, String tableName, String newColumnName, boolean includeAudTable){
		return new ColumnAdder(stepList, stepName, tableName, newColumnName, Datatype.CLOB, null, includeAudTable, null, false, null);
	}

	public static final ColumnAdder NewDateTimeInstance(List<? extends ISchemaUpdaterStep> stepList, String stepName, String tableName, String newColumnName, boolean includeAudTable, boolean notNull){
		return new ColumnAdder(stepList, stepName, tableName, newColumnName, Datatype.DATETIME, null, includeAudTable, null, notNull, null);
	}

	protected ColumnAdder(List<? extends ISchemaUpdaterStep> stepList, String stepName, String tableName, String newColumnName, Datatype columnType, Integer size, boolean includeAudTable, Object defaultValue, boolean notNull, String referencedTable) {
		super(stepList, stepName, tableName, includeAudTable);
		this.newColumnName = newColumnName;
		this.columnType = columnType;
		this.size = size;
		this.defaultValue = defaultValue;
		this.isNotNull = notNull;
		this.referencedTable = referencedTable;
	}

	public ColumnAdder setNotNull(boolean isNotNull) {
		this.isNotNull = isNotNull;
		return this;
	}

    @Override
    protected void invokeOnTable(String tableName, ICdmDataSource datasource,
            IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) {

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
            }else if (defaultValue instanceof String){
                String defaultValueQuery = "UPDATE @tableName SET @columnName = " + (defaultValue == null ? "NULL" : "'" + defaultValue + "'");
                defaultValueQuery = defaultValueQuery.replace("@tableName", tableName);
                defaultValueQuery = defaultValueQuery.replace("@columnName", newColumnName);
                datasource.executeUpdate(defaultValueQuery);
			}else if (defaultValue != null){
				logger.warn("Default Value not implemented for type " + defaultValue.getClass().getName());
			}
			if (referencedTable != null){
				TableCreator.makeForeignKey(tableName, datasource, monitor, newColumnName, referencedTable, caseType, result);
			}
			return;
		} catch ( Exception e) {
		    String message = "Unhandled exception when trying to add column " +
		            newColumnName + " for table " +  tableName;
			monitor.warning(message, e);
			logger.error(e);
			result.addException(e, message, getStepName());
			return;
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
		String databaseColumnType = this.columnType.format(datasource, size);

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
		if (isNotNull && !isAuditing){
			updateQuery += " NOT NULL";
		}
		updateQuery = updateQuery.replace("@tableName", tableName);
		updateQuery = updateQuery.replace("@columnName", newColumnName);
		updateQuery = updateQuery.replace("@columnType", databaseColumnType);
		updateQuery = updateQuery.replace("@addSeparator", getAddColumnSeperator(datasource));

		return updateQuery;
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
