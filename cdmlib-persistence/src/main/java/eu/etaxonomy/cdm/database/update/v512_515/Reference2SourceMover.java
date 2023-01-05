/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v512_515;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * @author a.mueller
 * @since 12.06.2020
 */
public class Reference2SourceMover extends SchemaUpdaterStepBase {

    private final String tableName;
    private final String citationsIdAttr;
    private final String detailAttr;
    private final String sourceAttr;
    private final String sourceType;
    private final String dtype;

    public static final Reference2SourceMover NewInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName,
            String citationsIdAttr, String detailAttr, String sourceAttr){
        Reference2SourceMover result = new Reference2SourceMover(stepList, stepName, tableName, citationsIdAttr, detailAttr, sourceAttr, null, null);

        return result;
    }

    public static final Reference2SourceMover NewInstance(List<ISchemaUpdaterStep> stepList, String stepName, String tableName,
            String citationsIdAttr, String detailAttr, String sourceAttr, String dtype, String sourceType){
        Reference2SourceMover result = new Reference2SourceMover(stepList, stepName, tableName, citationsIdAttr, detailAttr, sourceAttr, sourceType, dtype);

        return result;
    }

    protected Reference2SourceMover(List<ISchemaUpdaterStep> stepList, String stepName, String tableName,
            String citationsIdAttr, String detailAttr, String sourceAttr, String sourceType, String dtype) {
        super(stepList, stepName);
        this.tableName = tableName;
        this.citationsIdAttr = citationsIdAttr;
        this.detailAttr = detailAttr;
        this.sourceAttr = sourceAttr;
        this.sourceType = sourceType == null? "PTS" : sourceType;
        this.dtype = dtype == null? "DescriptionElementSource" : dtype;
        this.stepName = stepName;
    }

    @Override
    public List<ISchemaUpdaterStep> getInnerSteps() {
        List<ISchemaUpdaterStep> result = new ArrayList<>();

        return result;
    }

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

        boolean includeAudit = true;
        int id = getMaxId1(datasource, "OriginalSourceBase", includeAudit, monitor, caseType, result);

        String sql = "SELECT * "
                + " FROM "+caseType.transformTo(tableName)+" t "
                + " WHERE t."+this.citationsIdAttr+" IS NOT NULL OR t."+this.detailAttr+" IS NOT NULL ";

        ResultSet rs = datasource.executeQuery(sql);
        while(rs.next()){
            int tnId = rs.getInt("id");
            Integer citationId = nullSafeInt(rs, citationsIdAttr);
            Integer createdById = nullSafeInt(rs, "createdBy_id");
            String detail = rs.getString(detailAttr);

            sql = "INSERT INTO @@OriginalSourceBase@@ (DTYPE, sourceType, uuid, id, citation_id, citationMicroReference, createdBy_id, created)"
               + " VALUES ('"+dtype+"', '"+sourceType+"','"+UUID.randomUUID()+"'," + id + ", " + citationId + "," + nullSafeParam(detail) + "," + createdById + ",'" + this.getNowString() + "')";
            datasource.executeUpdate(caseType.replaceTableNames(sql));

            sql = "UPDATE " + caseType.transformTo(tableName)
                    + " SET " + sourceAttr + " = " + id
                    + " WHERE id = " + tnId;
            id++;

            datasource.executeUpdate(caseType.replaceTableNames(sql));
        }
    }
}