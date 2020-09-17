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
            " SELECT n.titleCache, mrp.uri uri, n.nomenclaturalSource_id nomSourceId, osb.id sid, "
                + "  n.id nameId, db.id dbid, deb.id debId, m.id AS 'mid', mr.id mrId, mrp.id mrpid, feature.id fid "
            + " FROM @@TaxonName@@ n "
            + " INNER JOIN @@DescriptionBase@@ db ON db.taxonName_id = n.id "
            + " INNER JOIN @@DescriptionElementBase@@ deb ON deb.inDescription_id = db.id "
            + " INNER JOIN @@DescriptionElementBase_Media@@ MN ON deb.id = MN.DescriptionElementBase_id "
            + " INNER JOIN @@Media@@ m ON m.id = MN.media_id "
            + " INNER JOIN @@MediaRepresentation@@ mr ON mr.media_id = m.id "
            + " INNER JOIN @@MediaRepresentationPart@@ mrp ON mrp.representation_id = mr.id "
            + " INNER JOIN @@DefinedTermBase feature@@ ON feature.id = deb.feature_id "
            + "                                       AND feature.uuid = '71b356c5-1e3f-4f5d-9b0f-c2cf8ae7779f' "
            + " LEFT OUTER JOIN @@OriginalSourceBase@@ osb ON n.nomenclaturalSource_id = osb.id "
            + " ORDER BY n.titleCache";

        ResultSet rs = datasource.executeQuery(caseType.replaceTableNames(sql));
        while (rs.next()){
            int nameId = rs.getInt("nameId");
            String uri = rs.getString("uri");
            Integer nomSourceId = nullSafeInt(rs, "nomSourceId");
            if (isNotBlank(uri)){
                createSourceLink(datasource, caseType, result, nameId, uri, nomSourceId);
            }
            deleteMedia(datasource, caseType, result, rs, nameId);

            //TODO Auditing
        }
    }

    private void deleteMedia(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result, ResultSet rs, int nameId) {
        //if mrp has no child, delete and
          //if media has no child, check if linked from elsewhere, if not delete and
          //delete DescriptionElementBase_Media record
          //if TextData has no further media records and no other attached data, delete TextData
          //if DescriptionBase has no other description elements, delete DescriptionBase

          //check possible supplemental data in real data
    }

    private void createSourceLink(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result, int nameId, String uri, Integer nomSourceId) throws SQLException {
        if (nomSourceId == null){
            nomSourceId = createSource(datasource, caseType, result, nameId);
        }
        addLink(datasource, caseType, result, nomSourceId, uri);
    }

    private void addLink(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result,
            Integer nomSourceId, String uri) throws SQLException {

        //insert ExternalLink record
        int nextLinkId = this.getMaxId1(datasource, "ExternalLink", true, null, caseType, result);
        String sql = "INSERT INTO @@ExternalLink@@ (id, uuid, linkType, uri, created, createdBy_id) "
                + " VALUES (%d, '%s', '%s', '%s', '%s', NULL )";
        //TODO ELType
        sql = String.format(sql, nextLinkId, UUID.randomUUID(), ExternalLinkType.WebSite.getKey(),
                uri, this.getNowString());
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        //insert MN value in OriginalSourceBase_ExternalLink
        sql = String.format("INSERT INTO @@OriginalSourceBase_ExternalLink@@ (OriginalSourceBase_id, links_id) "
                + "VALUES (%d, %d) ", nomSourceId, nextLinkId);
        datasource.executeUpdate(caseType.replaceTableNames(sql));
    }

    /**
     * @return the new source id
     * @throws SQLException
     */
    private int createSource(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result, int nameId) throws SQLException {
        //insert empty OriginalSourceBase record
        int nextSourceId = this.getMaxId1(datasource, "OriginalSourceBase", true, null, caseType, result);
        String sql = "INSERT INTO @@OriginalSourceBase@@ (DTYPE, id, uuid, sourceType, created, createdBy_id) "
                + " VALUES (DescriptionElementSource, %d, '%s', '%s', '%s', '%s', NULL )";
        sql = String.format(sql, nextSourceId, UUID.randomUUID(), OriginalSourceType.NomenclaturalReference.getKey(),
                this.getNowString());
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        //update nomSource_id
        sql = "UPDATE @@TaxonName@@ "
            + "SET nomenclaturalSource_id = " + nextSourceId
            + "WHERE id = " + nameId;
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        return nextSourceId;
    }

}
