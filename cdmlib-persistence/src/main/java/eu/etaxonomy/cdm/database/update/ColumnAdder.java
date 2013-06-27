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
public class ColumnAdder extends SchemaUpdaterStepBase<ColumnAdder> implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(ColumnAdder.class);
	
	private String tableName;
	private String newColumnName;
	private String columnType;
	private boolean includeAudTable;
	private Object defaultValue;
	private boolean isNotNull;
	private String referencedTable;

	public static final ColumnAdder NewIntegerInstance(String stepName, String tableName, String newColumnName, boolean includeAudTable, boolean notNull, String referencedTable){
		return new ColumnAdder(stepName, tableName, newColumnName, "int", includeAudTable, null, notNull, referencedTable);
	}
	
	public static final ColumnAdder NewIntegerInstance(String stepName, String tableName, String newColumnName, boolean includeAudTable, Integer defaultValue, boolean notNull){
		return new ColumnAdder(stepName, tableName, newColumnName, "int", includeAudTable, defaultValue, notNull, null);
	}
	
	public static final ColumnAdder NewTinyIntegerInstance(String stepName, String tableName, String newColumnName, boolean includeAudTable, boolean notNull){
		return new ColumnAdder(stepName, tableName, newColumnName, "tinyint", includeAudTable, null, notNull, null);
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
	
	public static final ColumnAdder NewDateTimeInstance(String stepName, String tableName, String newColumnName, boolean includeAudTable){
		return new ColumnAdder(stepName, tableName, newColumnName, "datetime", includeAudTable, null, false, null);
	}
	
	protected ColumnAdder(String stepName, String tableName, String newColumnName, String columnType, boolean includeAudTable, Object defaultValue, boolean notNull, String referencedTable) {
		super(stepName);
		this.tableName = tableName;
		this.newColumnName = newColumnName;
		this.columnType = columnType;
		this.includeAudTable = includeAudTable;
		this.defaultValue = defaultValue;
		this.isNotNull = notNull;
		this.referencedTable = referencedTable;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase#invoke(eu.etaxonomy.cdm.database.ICdmDataSource, eu.etaxonomy.cdm.common.IProgressMonitor)
	 */
	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		boolean result = true;
		result &= addColumn(tableName, datasource, monitor);
		if (includeAudTable){
			String aud = "_AUD";
			result &= addColumn(tableName + aud, datasource, monitor);
		}
		return (result == true )? 0 : null;
	}

	private boolean addColumn(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) {
		boolean result = true;
		try {
			String updateQuery = getUpdateQueryString(tableName, datasource, monitor);
			try {
				datasource.executeUpdate(updateQuery);
			} catch (SQLException e) {
				logger.error(e);
				result = false;
			}
			
			if (defaultValue instanceof Boolean){
				updateQuery = "UPDATE @tableName SET @columnName = " + (defaultValue == null ? "null" : getBoolean((Boolean) defaultValue, datasource));
				updateQuery = updateQuery.replace("@tableName", tableName);
				updateQuery = updateQuery.replace("@columnName", newColumnName);
				try {
					datasource.executeUpdate(updateQuery);
				} catch (SQLException e) {
					logger.error(e);
					result = false;
				}
			}
			if (referencedTable != null){
				result &= TableCreator.makeForeignKey(tableName, datasource, newColumnName, referencedTable);
			}
			
			return result;
		} catch ( DatabaseTypeNotSupportedException e) {
			return false;
		}
	}

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

	private String getDatabaseColumnType(ICdmDataSource datasource, String columnType) {
		String result = columnType;
		if (datasource.getDatabaseType().equals(DatabaseTypeEnum.PostgreSQL)){
			result = result.replace("nvarchar", "varchar");
		}
		return result;
	}
	

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
