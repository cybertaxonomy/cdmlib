/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v40_50;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * Removes orphaned KeyStatements.
 *
 * #6226
 *
 * @author a.mueller
 * @since 05.06.2017
 *
 */
public class OrphanedKeyStatementRemover extends SchemaUpdaterStepBase{

    private static final String stepName = "Remove orphaned key statements";

    public static OrphanedKeyStatementRemover NewInstance(List<ISchemaUpdaterStep> stepList) {
        return new OrphanedKeyStatementRemover(stepList);
    }

    protected OrphanedKeyStatementRemover(List<ISchemaUpdaterStep> stepList) {
        super(stepList, stepName);
    }

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

        try {
            String sql = " SELECT id FROM @@KeyStatement@@ ks " +
                    " WHERE  "
                    + " id NOT IN (SELECT statement_id FROM @@PolytomousKeyNode@@ WHERE statement_id is NOT NULL) AND "
                    + " id NOT IN (SELECT question_id FROM @@PolytomousKeyNode@@ WHERE question_id is NOT NULL) ";
            sql = caseType.replaceTableNames(sql);

            ResultSet rs = datasource.executeQuery(sql);
            while (rs.next()){
                int id = rs.getInt("id");
                handleSingleStatement(id, datasource, monitor, caseType, result);
            }
        } catch (Exception e) {
            String message = e.getMessage();
            monitor.warning(message, e);
            result.addException(e, message, this, "invoke");
        }
        return;
    }

    private void handleSingleStatement(int keyStatementId, ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {
        String selectSQL = " SELECT MN.label_id id" +
                " FROM @@KeyStatement_LanguageString@@ MN " +
                " WHERE MN.KeyStatement_id = " + keyStatementId ;
        selectSQL = caseType.replaceTableNames(selectSQL);
        ResultSet rs = datasource.executeQuery(selectSQL);
        while (rs.next()){
            int labelId = rs.getInt("id");
            String deleteMN = "DELETE FROM @@KeyStatement_LanguageString@@ "
                    + " WHERE label_id = " + labelId;
            deleteMN = caseType.replaceTableNames(deleteMN);
            datasource.executeUpdate(deleteMN);

            String deleteLanguageString = "DELETE FROM @@LanguageString@@ WHERE id = " + labelId;
            deleteLanguageString = caseType.replaceTableNames(deleteLanguageString);
            datasource.executeUpdate(deleteLanguageString);
        }
        String deleteKeyStatement = "DELETE FROM @@KeyStatement@@ WHERE id = " + keyStatementId;
        deleteKeyStatement = caseType.replaceTableNames(deleteKeyStatement);
        datasource.executeUpdate(deleteKeyStatement);
    }
}