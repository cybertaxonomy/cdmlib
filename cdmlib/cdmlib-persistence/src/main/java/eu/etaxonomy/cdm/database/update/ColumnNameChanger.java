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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @date 16.09.2010
 *
 */
public class ColumnNameChanger extends AuditedSchemaUpdaterStepBase<ColumnNameChanger> implements ISchemaUpdaterStep {
	private static final Logger logger = Logger.getLogger(ColumnNameChanger.class);
	
	private String newColumnName;
	private String oldColumnName;
	private Datatype datatype; //TODO make enum
	
	private enum Datatype{
		integer,
		clob
	}
	
	public static ColumnNameChanger NewIntegerInstance(String stepName, String tableName, String oldColumnName, String newColumnName, boolean includeAudTable){
		return new ColumnNameChanger(stepName, tableName, oldColumnName, newColumnName, includeAudTable, null, Datatype.integer);
	}
	
	public static ColumnNameChanger NewClobInstance(String stepName, String tableName, String oldColumnName, String newColumnName, boolean includeAudTable){
		return new ColumnNameChanger(stepName, tableName, oldColumnName, newColumnName, includeAudTable, null, Datatype.clob);
	}

	protected ColumnNameChanger(String stepName, String tableName, String oldColumnName, String newColumnName, boolean includeAudTable, Object defaultValue, Datatype datatype) {
		super(stepName);
		this.tableName = tableName;
		this.newColumnName = newColumnName;
		this.oldColumnName = oldColumnName;
		this.includeAudTable = includeAudTable;
		this.datatype = datatype;
	}

	@Override
	protected boolean invokeOnTable(String tableName, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) {
		try {
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
			datasource.executeUpdate(updateQuery);
			
			return result;
		} catch (Exception e) {
			monitor.warning(e.getMessage(), e);
			logger.error(e);
			return false;
		}
	}


	//TODO use same code as ColumnTypeChanger or ColumnAdder
	private CharSequence getDefinition() {
		if (this.datatype == Datatype.integer){
			return "integer";
		}else if (this.datatype == Datatype.clob){
			return "longtext";
		}else{
			throw new RuntimeException("Definition type not supported");
		}
	}

}
