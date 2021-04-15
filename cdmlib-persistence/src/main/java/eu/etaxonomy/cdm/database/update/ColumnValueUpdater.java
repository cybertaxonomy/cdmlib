/**
* Copyright (C) 2021 EDIT
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
import eu.etaxonomy.cdm.database.ICdmDataSource;

/**
 * Updates a value for a single table column.
 *
 * @author a.mueller
 * @since 18.03.2021
 */
public class ColumnValueUpdater
        extends AuditedSchemaUpdaterStepBase {

    private static final Logger logger = Logger.getLogger(ColumnValueUpdater.class);

    private final String columnName;
    private final String newValueStr;
    private final Integer newValueInt;
    private final String where;

    public static ColumnValueUpdater NewIntegerInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName,
            String columnName, Integer newValue, String where, boolean includeAudTable){
        return new ColumnValueUpdater(stepList, stepName, tableName, columnName, null, newValue, where, includeAudTable);
    }

    public static ColumnValueUpdater NewStringInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName,
            String columnName, String newValue, String where, boolean includeAudTable){
        return new ColumnValueUpdater(stepList, stepName, tableName, columnName, newValue, null, where, includeAudTable);
    }


// **************************************** Constructor ***************************************/

    protected ColumnValueUpdater(List<ISchemaUpdaterStep> stepList, String stepName, String tableName,
            String columnName, String newValueStr, Integer newValueInt, String where, boolean includeAudTable) {
        super(stepList, stepName, tableName, includeAudTable);
        this.columnName = columnName;
        this.newValueStr = newValueStr;
        this.newValueInt = newValueInt;
        this.where = isBlank(where)? " (1=1) " : where;
    }

    @Override
    protected void invokeOnTable(String tableName, ICdmDataSource datasource,
            IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) {
        try {
            String value = (newValueStr == null && newValueInt == null) ?
                    " = NULL " : newValueInt != null ? String.valueOf(newValueInt) :
                        "'"+newValueStr+"'";

            String updateQuery = "UPDATE %s "
                    + " SET %s = %s "
                    + " WHERE %s ";
            updateQuery = String.format(updateQuery, caseType.transformTo(tableName),
                    columnName, value, where);

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
}