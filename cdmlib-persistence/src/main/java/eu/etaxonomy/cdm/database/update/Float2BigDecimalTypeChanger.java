/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.hibernate.BigDecimalUserType;

/**
 * Changes a float column into a BigDecimal (2 columns) for usage in {@link BigDecimalUserType}.
 * First renames the old column and creates the 2 new columns via {@link #getInnerSteps() inner steps}
 * and then fills the new columns by using the mapping algorithm defined in
 *  {@link #invokeOnTable(String, ICdmDataSource, IProgressMonitor, CaseType, SchemaUpdateResult)}.
 *
 * Does not yet delete the old column. This should be done in a follow up model update.
 *
 * @author a.mueller
 * @since 27.04.2020
 */
public class Float2BigDecimalTypeChanger extends AuditedSchemaUpdaterStepBase {

    private static final String OLD_POSTFIX = "_old";

    private final String valueColumn;
    private final String scaleColumn;
    private final Integer newScale;
    private final Integer newPrecision;
    private final boolean isNotNull = false;
    private boolean isRounder = false;

    public static Float2BigDecimalTypeChanger NewInstance (List<? extends ISchemaUpdaterStep> stepList,
            String stepName, String tableName, String valueColumn, String scaleColumn, Integer newPrecision, Integer newScale,
            boolean includedAudTable){
        return new Float2BigDecimalTypeChanger(stepList, stepName, tableName, valueColumn,
                scaleColumn, newPrecision, newScale, includedAudTable);
    }

    protected Float2BigDecimalTypeChanger(List<? extends ISchemaUpdaterStep> stepList,
            String stepName, String tableName, String valueColumn, String scaleColumn,
            Integer newPrecision, Integer newScale, boolean includedAudTable) {
        super(stepList, stepName, tableName, includedAudTable);
        this.valueColumn = valueColumn;
        this.scaleColumn = scaleColumn;
        this.newPrecision = newPrecision;
        this.newScale = newScale;
    }

    @Override
    protected void invokeOnTable(String tableName, ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) {
        if(!isRounder){
            return;
        }
        String sql = "SELECT id, %s as value_float FROM %s as t ";
        sql = String.format(sql, valueColumn+OLD_POSTFIX, tableName);
        try {
            ResultSet rs = datasource.executeQuery(sql);
            while(rs.next()){
                Float floatValue = rs.getFloat("value_float");
                BigDecimal bd = new BigDecimal(floatValue.toString());
                int id = rs.getInt("id");
                sql = "UPDATE %s SET %s = %s, %s = %s WHERE id = %s ";
                sql = String.format(sql, tableName, valueColumn, bd.toString(), scaleColumn, bd.scale(), String.valueOf(id));
                datasource.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<ISchemaUpdaterStep> getInnerSteps() {
        if(!isRounder){
            List<ISchemaUpdaterStep> result = new ArrayList<>();
            ColumnNameChanger.NewFloatInstance(result, stepName +  " - rename old column", tableName, valueColumn, valueColumn + OLD_POSTFIX, includeAudTable);
            ColumnAdder.NewDecimalInstance(result, stepName + " - add value column", tableName, valueColumn, newPrecision, newScale, includeAudTable, 0, isNotNull);
            ColumnAdder.NewIntegerInstance(result, stepName + " - add scale column", tableName, scaleColumn, includeAudTable, newScale, isNotNull);
            Float2BigDecimalTypeChanger rounder = Float2BigDecimalTypeChanger.NewInstance(result, stepName + " - round", tableName, valueColumn, scaleColumn, newPrecision, newScale, this.includeAudTable);
            rounder.isRounder = true;
            return result;
        }else{
            return super.getInnerSteps();
        }
    }

}
