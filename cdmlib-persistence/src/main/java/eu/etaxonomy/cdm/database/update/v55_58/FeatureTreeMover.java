/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v55_58;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.database.update.TreeIndexUpdater;

/**
 * @author a.mueller
 * @since 20.06.2019
 */
public class FeatureTreeMover extends SchemaUpdaterStepBase {


    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FeatureTreeMover.class);

    private static final String stepName = "Move FeatureTree to TermCollection";


    public static final FeatureTreeMover NewInstance(List<ISchemaUpdaterStep> stepList){
        FeatureTreeMover result = new FeatureTreeMover(stepList);

        return result;
    }

    protected FeatureTreeMover(List<ISchemaUpdaterStep> stepList) {
        super(stepList, stepName);
    }


    @Override
    public List<ISchemaUpdaterStep> getInnerSteps() {
        List<ISchemaUpdaterStep> result = new ArrayList<>();

        // update tree index for feature node
        //note: it could also be enough to only replace the first index entry by graph_id as only the graph_id changed
        String stepName = "Update TermNode treeindex";
        String tableName = "TermRelation";
        String treeIdColumnName = "graph_id";
        TreeIndexUpdater.NewInstance(result, stepName, tableName,
                treeIdColumnName, "treeIndex", false);  // see comment for TaxonTree

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

        int maxIdTermVoc = getMaxIdTermVoc(datasource, monitor, caseType, result);
        List<Integer> featureTreeIds = getFeatureTreeIds(datasource, monitor, caseType, result);
        for (Integer featureTreeId : featureTreeIds){
            maxIdTermVoc = moveFeatureTree(featureTreeId, maxIdTermVoc, datasource, monitor, caseType, result);
        }
    }

    /**
     * @param featureTreeId
     * @param maxIdTermVoc
     * @param datasource
     * @param monitor
     * @param caseType
     * @param result
     * @return
     * @throws SQLException
     */
    private int moveFeatureTree(Integer featureTreeId, int maxIdTermVoc, ICdmDataSource datasource,
            IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) throws SQLException {

        maxIdTermVoc++;
        String attributes = "termType, created, updated, uuid, lsid_authority, lsid_lsid, lsid_namespace, lsid_object, lsid_revision, "
                + "protectedTitleCache, titleCache, createdBy_id, updatedBy_id, root_id, allowDuplicates";
        String sql = "INSERT INTO @@TermCollection@@ (DTYPE,"+attributes+",id, isFlat, orderRelevant) "
                + " SELECT 'TermTree', "+attributes+","+ maxIdTermVoc +",0,1 FROM @@FeatureTree@@ WHERE id="+featureTreeId;
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        //AUD
        attributes = attributes + ",REV,REVTYPE";
        sql = "INSERT INTO @@TermCollection_AUD@@ (DTYPE,"+attributes+",id, isFlat, orderRelevant) "
                + " SELECT 'TermTree', "+attributes+","+ maxIdTermVoc +",0,1 FROM @@FeatureTree_AUD@@ WHERE id="+featureTreeId;
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        updateSupplement("Annotation", "annotations_id", featureTreeId, maxIdTermVoc, datasource, monitor, caseType, result, false);
        updateSupplement("Credit", "credits_id", featureTreeId, maxIdTermVoc, datasource, monitor, caseType, result, true);
        updateSupplement("Extension", "extensions_id", featureTreeId, maxIdTermVoc, datasource, monitor, caseType, result, false);
        updateSupplement("Identifier", "identifiers_id", featureTreeId, maxIdTermVoc, datasource, monitor, caseType, result, true);
        updateSupplement("Marker", "markers_id", featureTreeId, maxIdTermVoc, datasource, monitor, caseType, result, false);
        updateSupplement("OriginalSourceBase", "sources_id", featureTreeId, maxIdTermVoc, datasource, monitor, caseType, result, false);
        updateSupplement("Representation", "representations_id", featureTreeId, maxIdTermVoc, datasource, monitor, caseType, result, false);
        updateSupplement("RightsInfo", "rights_id", featureTreeId, maxIdTermVoc, datasource, monitor, caseType, result, false);

        updateDescriptiveSystem(featureTreeId, maxIdTermVoc, datasource, monitor, caseType, result);
        updateTermNode(featureTreeId, maxIdTermVoc, datasource, monitor, caseType, result);

//        xx  treeIndex update;



        return maxIdTermVoc;
    }

    /**
     * @param featureTreeId
     * @param maxIdTermVoc
     * @param datasource
     * @param monitor
     * @param caseType
     * @param result
     * @throws SQLException
     */
    private void updateTermNode(Integer featureTreeId, int maxIdTermVoc, ICdmDataSource datasource,
            IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) throws SQLException {
        String update = "UPDATE @@TermRelation@@ "
                + " SET graph_id = " + maxIdTermVoc
                + " WHERE featureTree_id =" + featureTreeId;

        datasource.executeUpdate(caseType.replaceTableNames(update));

        update = "UPDATE @@TermRelation_AUD@@ "
                + " SET graph_id = " + maxIdTermVoc
                + " WHERE featureTree_id =" + featureTreeId;

        datasource.executeUpdate(caseType.replaceTableNames(update));

    }

    /**
     * @param featureTreeId
     * @param maxIdTermVoc
     * @param datasource
     * @param monitor
     * @param caseType
     * @param result
     * @throws SQLException
     */
    private void updateDescriptiveSystem(Integer featureTreeId, int maxIdTermVoc, ICdmDataSource datasource,
            IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result) throws SQLException {
        String update = "UPDATE @@DescriptiveDataSet@@ "
                + " SET descriptiveSystem_id = " + maxIdTermVoc
                + " WHERE descriptiveSystemOld_id =" + featureTreeId;

        datasource.executeUpdate(caseType.replaceTableNames(update));

        update = "UPDATE @@DescriptiveDataSet_AUD@@ "
                + " SET descriptiveSystem_id = " + maxIdTermVoc
                + " WHERE descriptiveSystemOld_id =" + featureTreeId;

        datasource.executeUpdate(caseType.replaceTableNames(update));

    }

    /**
     * @param string
     * @param featureTreeId
     * @param maxIdTermVoc
     * @param datasource
     * @param monitor
     * @param caseType
     * @param result
     * @param withSortIndex
     * @throws SQLException
     */
    private void updateSupplement(String supplement, String attr, Integer featureTreeId, int maxIdTermVoc, ICdmDataSource datasource,
            IProgressMonitor monitor, CaseType caseType, SchemaUpdateResult result, boolean withSortIndex) throws SQLException {

        if(withSortIndex){
            attr = attr + ",sortIndex";
        }
        String sql = "INSERT INTO @@TermCollection_"+supplement+"@@ (TermCollection_id, "+attr+") "
                + " SELECT FeatureTree_id, "+attr+" FROM @@FeatureTree_"+supplement+"@@ WHERE FeatureTree_id="+featureTreeId;
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        sql = "INSERT INTO @@TermCollection_"+supplement+"_AUD@@ (TermCollection_id, "+attr+",REV,REVTYPE) "
                + " SELECT FeatureTree_id, "+attr+",REV,REVTYPE FROM @@FeatureTree_"+supplement+"_AUD@@ WHERE FeatureTree_id="+featureTreeId;
        datasource.executeUpdate(caseType.replaceTableNames(sql));


    }

    /**
     * @param datasource
     * @param monitor
     * @param caseType
     * @param result
     * @return
     * @throws SQLException
     */
    private List<Integer> getFeatureTreeIds(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult updateResult) throws SQLException {

        List<Integer> result = new ArrayList<>();
        String sql = "SELECT DISTINCT id FROM " +caseType.transformTo("FeatureTree") + " ORDER BY id";
        ResultSet rs = datasource.executeQuery(sql);
        while (rs.next()){
            Integer id = rs.getInt("id");
            result.add(id);
        }

        sql = "SELECT DISTINCT id FROM " +caseType.transformTo("FeatureTree_AUD") + " ORDER BY id";
        rs = datasource.executeQuery(sql);
        while (rs.next()){
            Integer id = rs.getInt("id");
            if (!result.contains(id)){
                result.add(id);
            }
        }

        return result;
    }

    /**
     * @param datasource
     * @param monitor
     * @param caseType
     * @param result
     * @return
     * @throws SQLException
     * @throws NumberFormatException
     */
    private int getMaxIdTermVoc(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws NumberFormatException, SQLException {
        String sql = "SELECT max(id) FROM " +caseType.transformTo("TermCollection");
        int maxId = Integer.valueOf(datasource.getSingleValue(sql).toString());
        sql = "SELECT max(id) FROM " +caseType.transformTo("TermCollection_AUD");
        int maxIdAud = Integer.valueOf(datasource.getSingleValue(sql).toString());
        return Math.max(maxId, maxIdAud);
    }

}
