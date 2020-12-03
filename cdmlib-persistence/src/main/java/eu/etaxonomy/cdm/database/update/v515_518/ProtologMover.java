/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v515_518;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.model.media.ExternalLinkType;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;

/**
 * @author a.mueller
 * @since 12.06.2020
 */
public class ProtologMover  extends SchemaUpdaterStepBase {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ProtologMover.class);

    private static final String stepName = "Move protologues to nomenclatural source";

    public static final ProtologMover NewInstance(List<ISchemaUpdaterStep> stepList){
        ProtologMover result = new ProtologMover(stepList);
        return result;
    }

    protected ProtologMover(List<ISchemaUpdaterStep> stepList) {
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

        String sql =
            " SELECT n.titleCache, mrp.uri uri, osb.id sid, "
                + "  n.id nameId, db.id dbid, deb.id debId, m.id AS mid, mr.id mrId, mrp.id mrpid, feature.id fid "
            + " FROM @@TaxonName@@ n "
            + " INNER JOIN @@DescriptionBase@@ db ON db.taxonName_id = n.id "
            + " INNER JOIN @@DescriptionElementBase@@ deb ON deb.inDescription_id = db.id "
            + " INNER JOIN @@DescriptionElementBase_Media@@ MN ON deb.id = MN.DescriptionElementBase_id "
            + " INNER JOIN @@Media@@ m ON m.id = MN.media_id "
            + " INNER JOIN @@MediaRepresentation@@ mr ON mr.media_id = m.id "
            + " INNER JOIN @@MediaRepresentationPart@@ mrp ON mrp.representation_id = mr.id "
            + " INNER JOIN @@DefinedTermBase@@ feature ON feature.id = deb.feature_id "
            + "                                       AND feature.uuid = '71b356c5-1e3f-4f5d-9b0f-c2cf8ae7779f' "
            + " LEFT OUTER JOIN @@OriginalSourceBase@@ osb ON n.id = osb.sourcedName_id "
            + " ORDER BY n.titleCache";

        ResultSet rs = datasource.executeQuery(caseType.replaceTableNames(sql));
        while (rs.next()){
            int nameId = rs.getInt("nameId");
            String uri = rs.getString("uri");
            Integer nomSourceId = nullSafeInt(rs, "sid");
            if (isNotBlank(uri)){
                createSourceLink(datasource, caseType, rs, monitor, result, nameId, uri, nomSourceId);
            }
            deleteMedia(datasource, caseType, result, rs);
        }
    }

    private void deleteMedia(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result, ResultSet rs)
            throws SQLException {

        int mediaId = rs.getInt("mid");  //null should not happen with SQL above
        int debId = rs.getInt("debId");
        if (!mediaIsLinked(datasource, caseType, result, mediaId, debId)){
            Integer mediaRepId = nullSafeInt(rs, "mrId");
            Integer mediaRepPartId = nullSafeInt(rs, "mrpid");
            if (!anotherMediaRepresentationPartExists(datasource, caseType, result, mediaRepId, mediaRepPartId)){
                removeMediaRepresentationPart(datasource, caseType, result, mediaRepPartId);
                if (!anotherMediaRepresentationExists(datasource, caseType, result, mediaId, mediaRepId)){
                    removeMediaRepresentation(datasource, caseType, result, mediaRepPartId);
                    removeMedia(datasource, caseType, result, mediaId);
                }
            }
        }
        removeDescriptionElementBaseMediaMN(datasource, caseType, result, debId, mediaId);
        if (textDataIsEmpty(datasource, caseType, result, debId)){
            removeTextData(datasource, caseType, result, debId);
            int dbId = rs.getInt("dbid");
            if (descriptionBaseIsEmpty(datasource, caseType, result, dbId)){
                removeImageGallery(datasource, caseType, result, dbId);
            }
        }
    }

    private void removeImageGallery(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result, int dbId) throws SQLException {
        String sql = " DELETE FROM @@DescriptionBase@@ "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, dbId));
        datasource.executeUpdate(sql);

        sql = " DELETE FROM @@DescriptionBase_AUD@@ "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, dbId));
        datasource.executeUpdate(sql);
    }

    private boolean descriptionBaseIsEmpty(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result,
            int dbId) throws SQLException {
        //handle supplementalData and other linking data => done, data only existed in caryo_spp (only original sources)
        String sql = "SELECT count(*) as n From @@DescriptionElementBase@@ deb "
                + " WHERE deb.inDescription_id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, dbId));
        Long n = (Long)datasource.getSingleValue(sql);
        return n == 0;
    }

    private void removeTextData(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result,
            int debId) throws SQLException {

        String sql = " DELETE FROM @@DescriptionElementBase@@ "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, debId));
        datasource.executeUpdate(sql);

        sql = " DELETE FROM @@DescriptionElementBase_AUD@@ "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, debId));
        datasource.executeUpdate(sql);
    }

    private boolean textDataIsEmpty(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result,
            int debId) {
        // checkTextData (other media, text, ...) => checked, does not exist on production or integration
        //remove supplemental data => done manually in relevant DBs on production
        return true;
    }

    private void removeDescriptionElementBaseMediaMN(ICdmDataSource datasource, CaseType caseType,
            SchemaUpdateResult result, int debId, Integer mediaId) throws SQLException {

        String sql = " DELETE FROM @@DescriptionElementBase_Media@@ "
                + " WHERE DescriptionElementBase_id = %d AND media_id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, debId, mediaId));
        datasource.executeUpdate(sql);

        sql = " DELETE FROM @@DescriptionElementBase_Media_AUD@@ "
                + " WHERE DescriptionElementBase_id = %d AND media_id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, debId, mediaId));
        datasource.executeUpdate(sql);
    }

    private void removeMedia(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result,
            Integer mediaId) throws SQLException {
        //check supplementalData => checked, do not exist on production or integration
        String sql = " DELETE FROM @@Media@@ "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, mediaId));
        datasource.executeUpdate(sql);

        sql = " DELETE FROM @@Media_AUD@@ "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, mediaId));
        datasource.executeUpdate(sql);
    }

    private void removeMediaRepresentation(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result,
            Integer mediaRepId) throws SQLException {

        String sql = " DELETE FROM @@MediaRepresentation@@ "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, mediaRepId));
        datasource.executeUpdate(sql);

        sql = " DELETE FROM @@MediaRepresentation_AUD@@ "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, mediaRepId));
        datasource.executeUpdate(sql);
    }

    private void removeMediaRepresentationPart(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result,
            Integer mediaRepPartId) throws SQLException {
        String sql = " DELETE FROM @@MediaRepresentationPart@@ "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, mediaRepPartId));
        datasource.executeUpdate(sql);

        sql = " DELETE FROM @@MediaRepresentation_MediaRepresentationPart_AUD@@ "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, mediaRepPartId));
        datasource.executeUpdate(sql);

        sql = " DELETE FROM @@MediaRepresentationPart_AUD@@ "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, mediaRepPartId));
        datasource.executeUpdate(sql);
    }

    private boolean anotherMediaRepresentationExists(ICdmDataSource datasource, CaseType caseType,
            SchemaUpdateResult result, Integer mediaId, Integer mediaRepId) throws SQLException {

        String sql = "SELECT count(*) as n From @@MediaRepresentation@@ mr "
                + " WHERE mr.media_id = %d AND mr.id <> %d ";
        sql = caseType.replaceTableNames(String.format(sql, mediaId, mediaRepId));
        Long n = (Long)datasource.getSingleValue(sql);
        return n > 0;
    }

    private boolean anotherMediaRepresentationPartExists(ICdmDataSource datasource, CaseType caseType,
            SchemaUpdateResult result, Integer mediaRepId, Integer mediaRepPartId) throws SQLException {

        String sql = "SELECT count(*) as n From @@MediaRepresentationPart@@ mrp "
                + " WHERE mrp.representation_id = %d AND mrp.id <> %d ";
        sql = caseType.replaceTableNames(String.format(sql, mediaRepId, mediaRepPartId));
        Long n = (Long)datasource.getSingleValue(sql);
        return n > 0;
    }

    private boolean mediaIsLinked(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result,
            Integer mediaId, int debId) throws SQLException {

        String sql = "SELECT count(*) as n "
          + " FROM @@Media@@ m "
          + " WHERE m.id = %d "
           + " AND ( m.id IN (SELECT l.media_id FROM @@CdmLink@@ l ) "
               + " OR m.id IN (SELECT ab.media_id FROM @@AgentBase_Media@@ ab) "
               + " OR m.id IN (SELECT MN.media_id FROM @@Collection_Media@@ MN) "
               + " OR m.id IN (SELECT MN.media_id FROM @@DefinedTermBase_Media@@ MN) "
               + " OR m.id IN (SELECT MN.media_id FROM @@DescriptionElementBase_Media@@ MN WHERE MN.DescriptionElementBase_id <> %d) "
               + " OR m.id IN (SELECT MN.media_id FROM @@Reference_Media@@ MN) "
               + " OR m.id IN (SELECT l.mediaSpecimen_id FROM @@SpecimenOrObservationBase@@ l) "
               + " OR m.id IN (SELECT l.contigFile_id FROM @@Sequence@@ l) "
               + " OR m.id IN (SELECT l.shape_id FROM @@DefinedTermBase@@ l) "
               + " OR m.id IN (SELECT l.pherogram_id FROM @@SingleRead@@ l)) ";
        sql = caseType.replaceTableNames(String.format(sql, mediaId, debId));
        Long n = (Long)datasource.getSingleValue(sql);
        return n > 0;
    }

    private void createSourceLink(ICdmDataSource datasource, CaseType caseType, ResultSet rs, IProgressMonitor monitor, SchemaUpdateResult result, int nameId, String uri, Integer nomSourceId) throws SQLException {
        int mrpid = rs.getInt("mrpid");
        String sql = "SELECT REV FROM @@MediaRepresentationPart_AUD@@ WHERE id = " + mrpid + " AND REVTYPE = 0";
        ResultSet rs2 = datasource.executeQuery(caseType.replaceTableNames(sql));
        long rev;
        if (rs2.next()){
            rev = rs2.getLong("REV");
        }else{
            rev = createAuditEvent(datasource, caseType, monitor, result);
        }

        if (nomSourceId == null){
            nomSourceId = createSource(datasource, caseType, result, nameId, rev);
        }
        addLink(datasource, caseType, result, nomSourceId, uri, rev);
    }


    /**
     * @return the new source id
     */
    private int createSource(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result, int nameId, Long rev) throws SQLException {

        //insert empty OriginalSourceBase record
        int nextSourceId = this.getMaxId1(datasource, "OriginalSourceBase", true, null, caseType, result);
        String sql = "INSERT INTO @@OriginalSourceBase@@ (DTYPE, id, uuid, sourceType, sourcedName_id, created, createdBy_id ) "
                + " VALUES ('NomenclaturalSource', %d, '%s', '%s', %d, '%s', NULL)";
        sql = String.format(sql, nextSourceId, UUID.randomUUID(), OriginalSourceType.NomenclaturalReference.getKey(),
                nameId, this.getNowString());
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        if (rev != null){
            sql = "INSERT INTO @@OriginalSourceBase_AUD@@ (REV, revtype, DTYPE, id, uuid, sourceType, sourcedName_id, created, createdBy_id ) "
                    + " VALUES (%d, 0, 'NomenclaturalSource', %d, '%s', '%s', %d, '%s', NULL)";
            sql = String.format(sql, rev, nextSourceId, UUID.randomUUID(), OriginalSourceType.NomenclaturalReference.getKey(),
                    nameId, this.getNowString());
            datasource.executeUpdate(caseType.replaceTableNames(sql));
        }

        return nextSourceId;
    }

    private void addLink(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result,
            Integer nomSourceId, String uri, Long rev) throws SQLException {

        //insert ExternalLink record
        int nextLinkId = this.getMaxId1(datasource, "ExternalLink", true, null, caseType, result);
        String sql = "INSERT INTO @@ExternalLink@@ (id, uuid, linkType, uri, created, createdBy_id) "
                + " VALUES (%d, '%s', '%s', '%s', '%s', NULL )";
        ExternalLinkType elt = isFile(uri)? ExternalLinkType.File : ExternalLinkType.WebSite;
        sql = String.format(sql, nextLinkId, UUID.randomUUID(), elt.getKey(),
                uri, this.getNowString());
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        //insert MN value in OriginalSourceBase_ExternalLink
        sql = String.format("INSERT INTO @@OriginalSourceBase_ExternalLink@@ (OriginalSourceBase_id, links_id) "
                + "VALUES (%d, %d) ", nomSourceId, nextLinkId);
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        if (rev != null){
            sql = "INSERT INTO @@ExternalLink_AUD@@ (REV, revtype, id, uuid, linkType, uri, created, createdBy_id) "
                    + " VALUES (%d, 0, %d, '%s', '%s', '%s', '%s', NULL )";
            sql = String.format(sql, rev, nextLinkId, UUID.randomUUID(), elt.getKey(),
                    uri, this.getNowString());
            datasource.executeUpdate(caseType.replaceTableNames(sql));

            sql = String.format("INSERT INTO @@OriginalSourceBase_ExternalLink_AUD@@ (REV, revtype, OriginalSourceBase_id, links_id) "
                    + "VALUES (%d, 0, %d, %d) ", rev, nomSourceId, nextLinkId);
            datasource.executeUpdate(caseType.replaceTableNames(sql));
        }
    }

    private boolean isFile(String uri) {
        return uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".tiff") || uri.endsWith(".pdf")
                || uri.endsWith(".jpeg");
    }

}
