/**
* Copyright (C) 2019 EDIT
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
 * @since 24.07.2019
 *
 */
public class AllowNullUpdater
        extends AuditedSchemaUpdaterStepBase{

    private static final Logger logger = Logger.getLogger(AllowNullUpdater.class);

    private String columnName;
    private Datatype datatype;
    private Integer size;  //only required for MySQL


    public static AllowNullUpdater NewStringInstance(List<ISchemaUpdaterStep> stepList, String stepName,
            String tableName, String columnName, boolean includeAudTable){
        return new AllowNullUpdater(stepList, stepName, tableName, columnName, includeAudTable, Datatype.VARCHAR, 255);
    }

    public static AllowNullUpdater NewStringInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String columnName,
            int size, boolean includeAudTable){
        return new AllowNullUpdater(stepList, stepName, tableName, columnName, includeAudTable, Datatype.VARCHAR, size);
    }

 // **************************************** Constructor ***************************************/

    protected AllowNullUpdater(List<ISchemaUpdaterStep> stepList, String stepName, String tableName, String columnName,
            boolean includeAudTable, Datatype datatype, Integer size) {
        super(stepList, stepName, tableName, includeAudTable);
        this.columnName = columnName;
        this.datatype = datatype;
        this.size = size;
    }

    @Override
    protected void invokeOnTable(String tableName, ICdmDataSource datasource,
            IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) {
        try {
            DatabaseTypeEnum type = datasource.getDatabaseType();
            String updateQuery1;

            if (type.equals(DatabaseTypeEnum.SqlServer2005)){
                String message = "SQLServer column name changer syntax not yet tested. Table name: " + this.tableName + "; column name: " + columnName;
                monitor.warning(message);
                result.addWarning(message);
                updateQuery1 = "ALTER TABLE @tableName ALTER COLUMN @columnName @definition";
            }else if ( type.equals(DatabaseTypeEnum.MySQL)){
                //FIXME MySQL column name changer
                //logger.warn("Changing column name not yet supported for MySQL");
                updateQuery1 = "ALTER TABLE @tableName MODIFY @columnName @definition";
            }else if (type.equals(DatabaseTypeEnum.H2)){
                updateQuery1 = "ALTER TABLE @tableName ALTER COLUMN @columnName DROP NOT NULL";
            }else if ( type.equals(DatabaseTypeEnum.PostgreSQL) ){
                updateQuery1 = "ALTER TABLE @tableName ALTER COLUMN @columnName DROP NOT NULL";
            }else{
                String message = "Update step '" + this.getStepName() + "' is not supported by " + type.getName();
                monitor.warning(message);
                result.addError(message, getStepName() + ", ColumnNameChanger.invokeOnTable");
                return;
            }
            updateQuery1 = updateQuery1.replace("@tableName", tableName);
            updateQuery1 = updateQuery1.replace("@columnName", columnName);
            updateQuery1 = updateQuery1.replace("@definition", getDefinition(datasource));
            datasource.executeUpdate(updateQuery1);


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
        return datatype.format(datasource,size);
    }


}
