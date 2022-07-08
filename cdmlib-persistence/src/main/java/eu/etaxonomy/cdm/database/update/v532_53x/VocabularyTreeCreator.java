/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v529_53x;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * #6794
 * @author a.mueller
 * @since 22.04.2021
 */
public class VocabularyTreeCreator extends SchemaUpdaterStepBase {

    protected VocabularyTreeCreator(List<ISchemaUpdaterStep> stepList, String stepName) {
        super(stepList, stepName);
    }
    @Override
    public List<ISchemaUpdaterStep> getInnerSteps() {
        List<ISchemaUpdaterStep> result = new ArrayList<>();

        return result;
    }

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

        //before this the tree (or graph) attribute needs to be added to TermVocabulary

        //Create tree
        //Set ordered flag
        //set flat flag depending on existing terms
        //getTermsOrdered
        //for each Term
             //addToTree
             //TODO decide on kindOf vs. partOf
        //do the same for hierarchical terms
        //add tree to vocabulary

        //TODO how to handle AUD data

//        boolean includeAudit = true;
//        int osbId = getMaxId1(datasource, "OriginalSourceBase", includeAudit, monitor, caseType, result);
//
//        String sql = "SELECT * "
//                + " FROM "+caseType.transformTo(tableName)+" t "
//                + " WHERE t."+this.citationsIdAttr+" IS NOT NULL OR t."+this.detailAttr+" IS NOT NULL ";
//
//        ResultSet rs = datasource.executeQuery(sql);
//        while(rs.next()){
//            int tnId = rs.getInt("id");
//            Integer citationId = nullSafeInt(rs, citationsIdAttr);
//            Integer createdById = nullSafeInt(rs, "createdBy_id");
//            String detail = rs.getString(detailAttr);
//
//            sql = "INSERT INTO @@OriginalSourceBase@@ (DTYPE, sourceType, uuid, id, citation_id, citationMicroReference, createdBy_id, created, "+sourcedAttr+")"
//               + " VALUES ('"+dtype+"', '"+sourceType+"','"+UUID.randomUUID()+"'," + osbId + ", " + citationId + "," + nullSafeParam(detail) + "," + createdById + ",'" + this.getNowString() + "',"+tnId+")";
//            datasource.executeUpdate(caseType.replaceTableNames(sql));
//
//            osbId++;
//        }
    }
}
