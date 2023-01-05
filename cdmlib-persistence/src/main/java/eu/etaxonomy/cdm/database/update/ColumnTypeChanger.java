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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @since 16.09.2010
 */
public class ColumnTypeChanger
        extends AuditedSchemaUpdaterStepBase {

    private static final String _OLDXXX = "_oldxxx";

    private static final Logger logger = LogManager.getLogger();

	private final String columnName;
	private final Datatype newColumnType;
	private final Integer newSize;
	private final Object defaultValue;
	private final boolean isNotNull;
	private final String referencedTable;


	public static final ColumnTypeChanger NewStringSizeInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String columnName, int newSize, boolean includeAudTable){
		return new ColumnTypeChanger(stepList, stepName, tableName, columnName, Datatype.VARCHAR, newSize, includeAudTable, null, false, null);
	}

	public static final ColumnTypeChanger NewClobInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String columnName, boolean includeAudTable){
		return new ColumnTypeChanger(stepList, stepName, tableName, columnName, Datatype.CLOB, null, includeAudTable, null, false, null);
	}

	public static final ColumnTypeChanger NewInt2DoubleInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String columnName, boolean includeAudTable){
		return new ColumnTypeChanger(stepList, stepName, tableName, columnName, Datatype.DOUBLE, null, includeAudTable, null, false, null);
	}

	public static final ColumnTypeChanger NewInt2StringInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String columnName, int size, boolean includeAudTable, Integer defaultValue, boolean notNull){
		return new ColumnTypeChanger(stepList, stepName, tableName, columnName, Datatype.VARCHAR, size, includeAudTable, defaultValue, notNull, null);
	}

	public static final ColumnTypeChanger NewChangeAllowNullOnIntChanger(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String columnName, boolean includeAudTable){
	    return new ColumnTypeChanger(stepList, stepName, tableName, columnName, Datatype.INTEGER, null, includeAudTable, null, false, null);
	}

    public static final ColumnTypeChanger NewChangeAllowNullOnStringChanger(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String columnName, Integer size, boolean includeAudTable){
        return new ColumnTypeChanger(stepList, stepName, tableName, columnName, Datatype.VARCHAR, size, includeAudTable, null, false, null);
    }

	protected ColumnTypeChanger(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String columnName, Datatype newColumnType, Integer size,
	        boolean includeAudTable, Object defaultValue, boolean notNull, String referencedTable) {

	    super(stepList, stepName, tableName, includeAudTable);
		this.columnName = columnName;
		this.newColumnType = newColumnType;
		this.newSize = size;
		this.defaultValue = defaultValue;
		this.isNotNull = notNull;
		this.referencedTable = referencedTable;
	}

    @Override
    protected void invokeOnTable(String tableName, ICdmDataSource datasource,
            IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) {
        try {

			String updateQuery;

			//set null values to default value if NOT NULL
			if (this.isNotNull){
				updateQuery = getNotNullUpdateQuery(tableName);
				datasource.executeUpdate(updateQuery);
			}

			//update
			changeType(tableName, datasource, monitor, caseType, result);

			if (defaultValue instanceof Boolean){
				updateQuery = "UPDATE @tableName SET @columnName = " + (defaultValue == null ? "null" : getBoolean((Boolean) defaultValue, datasource)) +
				        " WHERE @columnName IS NULL ";
				updateQuery = updateQuery.replace("@tableName", tableName);
				updateQuery = updateQuery.replace("@columnName", columnName);
				datasource.executeUpdate(updateQuery);
			}

			//foreign keys
			if (referencedTable != null){
				TableCreator.makeForeignKey(tableName, datasource, monitor, columnName, referencedTable, caseType, result);
			}

			return;
		} catch ( Exception e) {
		    String message = "Unhandled exception when trying to change column type for " +
                    columnName + " for table " +  tableName;
            monitor.warning(message, e);
            logger.error(e);
            result.addException(e, message, getStepName());
            return;
		}
	}


    protected void changeType(String tableName, ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result)
            throws DatabaseTypeNotSupportedException, SQLException {
        DatabaseTypeEnum type = datasource.getDatabaseType();
        if (type.equals(DatabaseTypeEnum.PostgreSQL)){
            handlePostgres(tableName, datasource, monitor, caseType, result);
        }else{
            String updateQuery = getUpdateQueryString(tableName, datasource, monitor);
            datasource.executeUpdate(updateQuery);
        }
    }

    private String getNotNullUpdateQuery(String tableName) {
		String query = " UPDATE %s SET %s = %s WHERE %s IS NULL ";
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
		String databaseColumnType = getDatabaseColumnType(datasource, this.newColumnType, newSize, null);

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
		if (isNotNull && !isAuditing){
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


    /**
     * Postgres has problems with datatype changes as casting does often not really work even if using " ... USING ...."
     * resulting in errors like "operator does not exist: character varying >= integer".
     * Therefore we better create a new column here and transfer the data from the old column to the new column.
     * @param tableName
     * @param datasource
     * @param monitor
     * @param result
     * @param caseType
     * @throws SQLException
     */
    private void handlePostgres(String tableName, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) throws SQLException {
        //
        boolean includeAuditing = false;
        String colNameChanged = this.columnName + _OLDXXX;
        String databaseColumnType = getDatabaseColumnType(datasource, this.newColumnType, newSize, null);

        //change old column name
        //note data type is not relevant for ColumnNameChanger with Postgres
        ISchemaUpdaterStep step = ColumnNameChanger.NewIntegerInstance(null, this.stepName + " - Change column name",
                tableName, this.columnName, colNameChanged, includeAuditing);
        step.invoke(datasource, monitor, caseType, result);

        //create new column
//        step = ColumnAdder.NewStringInstance(this.stepName + " - Add new column", tableName, this.columnName, includeAuditing);
        Object defaultValue = null;
        step = new ColumnAdder(null, this.stepName + " - Add new column", tableName, this.columnName, newColumnType, newSize, null, includeAuditing, defaultValue, false, null);
        step.invoke(datasource, monitor, caseType, result);

        //move data
        String updateQuery = " UPDATE @tableName SET @columnName = @columnOld::@type ";
        String casedTableName = caseType.transformTo(tableName);
        updateQuery = updateQuery.replace("@tableName", casedTableName);
        updateQuery = updateQuery.replace("@columnName", columnName);
        updateQuery = updateQuery.replace("@columnOld", colNameChanged);
        updateQuery = updateQuery.replace("@type", databaseColumnType);
//        if (this.isAuditing){
//            step = SimpleSchemaUpdaterStep.NewAuditedInstance(this.stepName + " - Move data", updateQuery, casedTableName, -99);
//        }else{
            step = SimpleSchemaUpdaterStep.NewNonAuditedInstance(null, this.stepName + " - Move data", updateQuery);
//        }
        step.invoke(datasource, monitor, caseType, result);

        //delete old column
        step = ColumnRemover.NewInstance(null, this.stepName + " - Remove old column", tableName, colNameChanged, includeAuditing);
        step.invoke(datasource, monitor, caseType, result);
    }

	private String getDatabaseColumnType(ICdmDataSource datasource, Datatype columnType, Integer size, Integer scale) {
		return columnType.format(datasource, size, scale);
	}

	public String getReferencedTable() {
		return referencedTable;
	}

}
