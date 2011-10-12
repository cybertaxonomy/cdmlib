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
public class ColumnNameChanger extends SchemaUpdaterStepBase<ColumnNameChanger> implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(ColumnNameChanger.class);
	
	private String tableName;
	private String newColumnName;
	private String oldColumnName;
	private boolean includeAudTable;
	private boolean isInteger; //TODO make enum
	
	public static ColumnNameChanger NewIntegerInstance(String stepName, String tableName, String oldColumnName, String newColumnName, boolean includeAudTable){
		return new ColumnNameChanger(stepName, tableName, oldColumnName, newColumnName, includeAudTable, null, true);
	}

	protected ColumnNameChanger(String stepName, String tableName, String oldColumnName, String newColumnName, boolean includeAudTable, Object defaultValue, boolean isInteger) {
		super(stepName);
		this.tableName = tableName;
		this.newColumnName = newColumnName;
		this.oldColumnName = oldColumnName;
		this.includeAudTable = includeAudTable;
		this.isInteger = isInteger;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase#invoke(eu.etaxonomy.cdm.database.ICdmDataSource, eu.etaxonomy.cdm.common.IProgressMonitor)
	 */
	@Override
	public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor) throws SQLException {
		boolean result = true;
		result &= changeColumnName(tableName, datasource, monitor);
		if (includeAudTable){
			String aud = "_AUD";
			result &= changeColumnName(tableName + aud, datasource, monitor);
		}
		return (result == true )? 0 : null;
	}

	private boolean changeColumnName(String tableName, ICdmDataSource datasource, IProgressMonitor monitor) {
		boolean result = true;
		DatabaseTypeEnum type = datasource.getDatabaseType();
		String updateQuery;
		
		
		if (type.equals(DatabaseTypeEnum.SqlServer2005)){
			logger.warn("SQLServer column name changer syntax not yet tested");
			updateQuery = "EXEC sp_rename '@oldName', '@newName'";
		}else if (type.equals(DatabaseTypeEnum.H2)){
			updateQuery = "ALTER TABLE @tableName ALTER COLUMN @oldColumnName RENAME TO @newColumnName";
		}else if ( type.equals(DatabaseTypeEnum.MySQL)){
			//FIXME MySQL column name changer
//			logger.warn("Changing column name not yet supported for MySQL");
			updateQuery = "ALTER TABLE @tableName CHANGE COLUMN @oldColumnName @newColumnName @definition";
		}else if ( type.equals(DatabaseTypeEnum.PostgreSQL) ){
			updateQuery = "ALTER TABLE @tableName RENAME COLUMN @oldColumnName TO @newColumnName;";
		}else{
			updateQuery = null;
			monitor.warning("Update step '" + this.getStepName() + "' is not supported by " + type.getName());
			return false;
		}
		updateQuery = updateQuery.replace("@tableName", tableName);
		updateQuery = updateQuery.replace("@oldColumnName", oldColumnName);
		updateQuery = updateQuery.replace("@newColumnName", newColumnName);
		updateQuery = updateQuery.replace("@definition", getDefinition());
		try {
			datasource.executeUpdate(updateQuery);
		} catch (SQLException e) {
			logger.error(e);
			result = false;
		}

		return result;
	}

	private CharSequence getDefinition() {
		if (isInteger){
			return "integer";
		}else{
			throw new RuntimeException("Definition type not supported");
		}
	}

}
