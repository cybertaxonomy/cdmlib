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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * @author a.mueller
 * @since 16.09.2010
 */
public class ColumnNameChanger
        extends AuditedSchemaUpdaterStepBase{

    private static final Logger logger = LogManager.getLogger();

	private String newColumnName;
	private String oldColumnName;
	private Datatype datatype;
	private Integer size;  //only required for MySQL

	public static ColumnNameChanger NewIntegerInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String oldColumnName, String newColumnName, boolean includeAudTable){
		return new ColumnNameChanger(stepList, stepName, tableName, oldColumnName, newColumnName, includeAudTable, null, Datatype.INTEGER, null);
	}

    public static ColumnNameChanger NewFloatInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String oldColumnName, String newColumnName, boolean includeAudTable){
        return new ColumnNameChanger(stepList, stepName, tableName, oldColumnName, newColumnName, includeAudTable, null, Datatype.FLOAT, null);
    }

	public static ColumnNameChanger NewClobInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String oldColumnName,
	        String newColumnName, boolean includeAudTable){
		return new ColumnNameChanger(stepList, stepName, tableName, oldColumnName, newColumnName, includeAudTable, null, Datatype.CLOB, null);
	}

    public static ColumnNameChanger NewVarCharInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String oldColumnName,
            String newColumnName, int size, boolean includeAudTable){
        return new ColumnNameChanger(stepList, stepName, tableName, oldColumnName, newColumnName, includeAudTable, null, Datatype.VARCHAR, size);
    }


    public static ColumnNameChanger NewDateTimeInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String oldColumnName,
            String newColumnName, boolean includeAudTable){
        return new ColumnNameChanger(stepList, stepName, tableName, oldColumnName, newColumnName, includeAudTable, null, Datatype.DATETIME, null);
    }

// **************************************** Constructor ***************************************/

	protected ColumnNameChanger(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String oldColumnName,
	        String newColumnName, boolean includeAudTable, Object defaultValue, Datatype datatype, Integer size) {
		super(stepList, stepName, tableName, includeAudTable);
		this.newColumnName = newColumnName;
		this.oldColumnName = oldColumnName;
		this.datatype = datatype;
		this.size = size;
	}

    @Override
    protected void invokeOnTable(String tableName, ICdmDataSource datasource,
            IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) {
        try {
			DatabaseTypeEnum type = datasource.getDatabaseType();
			String updateQuery;

			if (type.equals(DatabaseTypeEnum.SqlServer2005)){
			    result.addWarning("SQLServer column name changer syntax not yet tested. Table name: " + this.tableName + "; old column name: " + oldColumnName + "; new column name: " + newColumnName);
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
				String message = "Update step '" + this.getStepName() + "' is not supported by " + type.getName();
				monitor.warning(message);
				result.addError(message, getStepName() + ", ColumnNameChanger.invokeOnTable");
				return;
			}
			updateQuery = updateQuery.replace("@tableName", tableName);
			updateQuery = updateQuery.replace("@oldColumnName", oldColumnName);
			updateQuery = updateQuery.replace("@newColumnName", newColumnName);
			updateQuery = updateQuery.replace("@definition", getDefinition(datasource));
			datasource.executeUpdate(updateQuery);

			return;
		} catch (Exception e) {
		    String message = e.getMessage();
			monitor.warning(message, e);
			logger.error(e);
			result.addException(e, message, getStepName() + ", ColumnNameChanger.invokeOnTable");
			return;
		}
	}

	private CharSequence getDefinition(ICdmDataSource datasource) {
		return datatype.format(datasource, size);
	}

}
