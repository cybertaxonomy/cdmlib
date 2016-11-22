/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v40_41;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ITermUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * Creates new synonym records for all synonym having >1 synonym relationships.
 * Expects unplaced, excluded and taxonomicChildrenCount to not exist anymore
 * @author a.mueller
 * @date 14.11.2016
 *
 */
public class SynonymDeduplicator extends SchemaUpdaterStepBase<SynonymDeduplicator> implements ITermUpdaterStep{
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(SynonymDeduplicator.class);

    /**
     * @return
     */
    public static SynonymDeduplicator NewInstance() {
        return new SynonymDeduplicator();
    }

    private static final String stepName = "Deduplicate synonyms code";

    /**
     * @param stepName
     */
    protected SynonymDeduplicator() {
        super(stepName);
    }

    String idListSelect = " SELECT sr.uuid, sr.relatedfrom_id ";
    String selectAll = " SELECT syn.DTYPE, (SELECT Max(id)+1 FROM TaxonBase), " +
            " sr.created, sr.uuid, sr.updated, " +
            " syn.lsid_authority, syn.lsid_lsid, syn.lsid_namespace, syn.lsid_object, syn.lsid_revision, " +
            " syn.protectedtitlecache, syn.titleCache, syn.appendedphrase, " +
            " syn.doubtful, syn.usenamecache, syn.taxonstatusunknown, " +
            " sr.createdby_id,sr.updatedby_id, " +
            " syn.name_id, syn.sec_id, syn.publish, syn.secMicroReference,"
            + "sr.partial, sr.proParte, sr.type_id, sr.relatedTo_id ";
    String insert = " INSERT INTO @@TaxonBase@@ (DTYPE, id, created,uuid, updated,"
            + "lsid_authority,lsid_lsid,lsid_namespace,lsid_object,lsid_revision, protectedtitlecache,titleCache, appendedphrase,"
            + "doubtful, usenamecache,taxonstatusunknown,"
            + "createdby_id, updatedby_id, name_id, sec_id, publish, secMicroReference,"
            + "partial, proParte, type_id, acceptedTaxon_id)";

    String fromSQL =
       " FROM @@TaxonBase@@ syn INNER JOIN @@SynonymRelationship@@ sr ON sr.relatedfrom_id = syn.id ";
    String whereSQL =  " WHERE EXISTS ( " +
           "  SELECT * FROM @@SynonymRelationship@@ srFirst " +
           "  WHERE srFirst.id < sr.id " +
           "    AND srFirst.relatedfrom_id = sr.relatedfrom_id " +
        " )";

    @Override
    public Integer invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) throws SQLException {

        //id list of all synonym relationships that need the synonym to be duplicated
        String listSql = caseType.replaceTableNames(idListSelect + fromSQL + whereSQL);
        ResultSet rs = datasource.executeQuery(listSql);
        while (rs.next()){
            String uuid = "'" + rs.getString(1) +"'";
            Integer oldSynonymId = rs.getInt(2);

            //create new synonym
            String update = insert + selectAll + fromSQL + " WHERE sr.uuid = " + uuid;
            datasource.executeUpdate(caseType.replaceTableNames(update));

            String sqlGetId = "SELECT id FROM @@TaxonBase@@ WHERE uuid = " + uuid;
            Integer newSynonymId = (Integer)datasource.getSingleValue(caseType.replaceTableNames(sqlGetId));

            //clone annotations
            cloneExtensions(oldSynonymId, newSynonymId, datasource, caseType, "annotations_id", "Annotation", "text, linkbackUri, language_id, annotationtype_id, commentator_id", false);
            //clone marker
            cloneExtensions(oldSynonymId, newSynonymId, datasource, caseType, "markers_id", "Marker", "flag, markertype_id", false);
            //clone credit
            cloneExtensions(oldSynonymId, newSynonymId, datasource, caseType, "credits_id", "Credit", "text, abbreviatedtext, language_id, agent_id", true);
            //clone extension
            cloneExtensions(oldSynonymId, newSynonymId, datasource, caseType, "extensions_id", "Extension", "value, type_id", false);
            //clone identifier
            cloneExtensions(oldSynonymId, newSynonymId, datasource, caseType, "identifiers_id", "Identifier", "identifier, type_id", true);
            //clone sources
            cloneExtensions(oldSynonymId, newSynonymId, datasource, caseType, "sources_id", "OriginalSourceBase", "DTYPE, citationmicroreference, originalnamestring, idinsource, idnamespace, citation_id, nameusedinsource_id, sourceType", false);
            //clone rightsInfo
            cloneExtensions(oldSynonymId, newSynonymId, datasource, caseType, "rights_id", "RightsInfo", "text, abbreviatedtext, uri, language_id, agent_id, type_id ", false);

            //remove old relationship
            String delete = "DELETE FROM @@SynonymRelationship@@ WHERE uuid = " + uuid;
            datasource.executeUpdate(caseType.replaceTableNames(delete));
        }

        return 0;
    }

    private void cloneExtensions(Integer oldSynonymId, Integer newSynonymId, ICdmDataSource datasource, CaseType caseType, String mnCol, String tableName, String specificParams, boolean withSortIndex) throws SQLException {
        String mnTableName = caseType.transformTo("TaxonBase_" + tableName);
        String listSql = " SELECT @mnCol FROM @mnTable WHERE taxonBase_id = @oldSynonymId "
                .replace("@mnCol", mnCol)
                .replace("@mnTable", mnTableName)
                .replace("@oldSynonymId", String.valueOf(oldSynonymId))
                ;
        if (withSortIndex){
            listSql += " ORDER BY sortIndex ";
        }
        ResultSet rs = datasource.executeQuery(listSql);

        Integer sortIndex = 0;
        while (rs.next()){
            sortIndex++;
            Integer oldExtensionId = rs.getInt(1);
            cloneExtension(newSynonymId, oldExtensionId, mnTableName, mnCol, datasource, caseType, tableName, specificParams, withSortIndex ? sortIndex : null);
        }

    }

    /**
     * @param id
     * @param caseType
     * @throws SQLException
     */
    private void cloneExtension(Integer newSynonymId, Integer oldExtensionId, String mnTableName, String mnCol, ICdmDataSource datasource, CaseType caseType,
            String tableName, String specificParams, Integer sortIndex) throws SQLException {

        try {
            //new id
            String maxIdSql = "SELECT max(id) FROM @tableName"
                    .replace("@tableName",  caseType.transformTo(tableName));
            int newExtensionId = (Integer)datasource.getSingleValue(maxIdSql) + 1;

//      insert clone record
            String idParams = "id, uuid,";
            String generalParams = "created, updated, createdBy_id, updatedBy_id,";
            String allParams = idParams + generalParams + specificParams;
            String idSelect = newExtensionId + ", '" + UUID.randomUUID() + "',";
            String selectParams = idSelect + generalParams + specificParams;
            String sql = "INSERT INTO @tableName (@allParams)"
                    + " SELECT @selectParams FROM @tableName WHERE id = " + oldExtensionId;
            sql = sql.replace("@selectParams", selectParams)
                    .replace("@allParams", allParams)
                    .replace("@tableName", caseType.transformTo(tableName))
                    ;
            datasource.executeUpdate(sql);

            //insert MN
            String sortIndexStr = "";
            String sortIndexValueStr = "";
            if (sortIndex != null){
                sortIndexStr = ", sortIndex";
                sortIndexValueStr = ", " + sortIndex;
            }
            String insertMNSql = ("INSERT INTO @mnTable (taxonbase_id, @mn_col @sortIndexInsert)"
                    + "VALUES (@newSynonymId, @extensionId @sortIndexValue)")
                        .replace("@mnTable", mnTableName)
                        .replace("@mn_col", mnCol)
                        .replace("@newSynonymId", String.valueOf(newSynonymId))
                        .replace("@extensionId", String.valueOf(newExtensionId))
                        .replace("@sortIndexInsert", sortIndexStr)
                        .replace("@sortIndexValue", sortIndexValueStr)
                        ;
            datasource.executeUpdate(insertMNSql);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("Synonym extension could not be cloned");
            throw e;
        }

    }
}
