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
public class ColumnTypeChanger extends SchemaUpdaterStepBase<ColumnTypeChanger> implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(ColumnTypeChanger.class);
	
	private String tableName;
	private String newColumnName;
	private String oldColumnName;
	private String newColumnType;
	private boolean includeAudTable;
	private Object defaultValue;
	private boolean isNotNull;
	private String referencedTable;

	
	public static final ColumnTypeChanger NewClobInstance(String stepName, String tableName, String oldColumnName, String newColumnName, boolean includeAudTable){
		return new ColumnTypeChanger(stepName, tableName, oldColumnName, newColumnName, "clob", includeAudTable, null, false, null);
	}
	
	public static final ColumnTypeChanger NewInt2DoubleInstance(String stepName, String tableName, String oldColumnName, String newColumnName, boolean includeAudTable){
		return new ColumnTypeChanger(stepName, tableName, oldColumnName, newColumnName, "double", includeAudTable, null, false, null);
	}
	
	protected ColumnTypeChanger(String stepName, String tableName, String oldColumnName, String newColumnName, String newColumnType, boolean includeAudTable, Object defaultValue, boolean notNull, String referencedTable) {
		super(stepName);
		this.tableName = tableName;
		this.newColumnName = newColumnName;
		this.newColumnType = newColumnType;
		this.oldColumnName =oldColumnName;
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
		result &= changeColumn(tableName, datasource, monitor);
		if (includeAudTable){
			String aud = "_AUD";
			result &= changeColumn(tableName + aud, datasource, monitor);
		}
		return (result == true )? 0 : null;
	}

	private boolean changeColumn(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) {
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
			updateQuery += " NOT NULL";
		}
		updateQuery = updateQuery.replace("@tableName", tableName);
		updateQuery = updateQuery.replace("@columnName", newColumnName);
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

	public String getNewColumnName() {
		return newColumnName;
	}

}
