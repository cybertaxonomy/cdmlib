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

    String idListSelect = " SELECT sr.uuid ";
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

        String listSql = caseType.replaceTableNames(idListSelect + fromSQL + whereSQL);
        ResultSet rs = datasource.executeQuery(listSql);
        while (rs.next()){
            String uuid = "'" + rs.getString(1) +"'";
            //create new synonym
            String update = insert + selectAll + fromSQL + " WHERE sr.uuid = " + uuid;
            datasource.executeUpdate(caseType.replaceTableNames(update));
            //remove old relationship
            String delete = "DELETE FROM @@SynonymRelationship@@ WHERE uuid = " + uuid;
            datasource.executeUpdate(caseType.replaceTableNames(delete));
        }

        return 0;
    }
}
