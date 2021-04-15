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
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * @author a.mueller
 * @since 03.11.2020
 */
public class OriginalSpellingMover  extends SchemaUpdaterStepBase {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(OriginalSpellingMover.class);

    private static final String stepName = "Move original spelling to nomenclatural source";

    public static final OriginalSpellingMover NewInstance(List<ISchemaUpdaterStep> stepList){
        OriginalSpellingMover result = new OriginalSpellingMover(stepList);
        return result;
    }

    protected OriginalSpellingMover(List<ISchemaUpdaterStep> stepList) {
        super(stepList, stepName);
    }

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

        String sql =
              " SELECT n1.id nameId1, nr.id nrId, n2.id nameId2, osb.id sourceId "
            + " FROM @@TaxonName@@ n1 "
            + " INNER JOIN @@NameRelationship@@ nr ON n1.id = nr.relatedfrom_id "
            + " INNER JOIN @@TaxonName@@ n2 ON nr.relatedto_id = n2.id "
            + " INNER JOIN @@DefinedTermBase@@ nrType ON nrType.id = nr.type_id "
            + " LEFT  JOIN @@OriginalSourceBase@@ osb ON n2.id = osb.sourcedName_id "
            + " WHERE nrType.uuid = '264d2be4-e378-4168-9760-a9512ffbddc4' ";

        ResultSet rs = datasource.executeQuery(caseType.replaceTableNames(sql));
        while (rs.next()){
            int nameId1 = rs.getInt("nameId1");
            int nameId2 = rs.getInt("nameId2");
            int nrId = rs.getInt("nrId");
            Integer nomSourceId = nullSafeInt(rs, "sourceId");
            if (nomSourceId == null){
                long rev = getOrCreateAuditEvent(datasource, monitor, caseType, result, nrId);
                nomSourceId = createSource(datasource, caseType, result, nameId2, rev);
            }
            updateNameInSource(datasource, caseType, result, rs, nomSourceId, nameId1);

            deleteNameRelationship(datasource, caseType, result, nrId);

        }
        //deleting name relationship type will be done explicitly in next step;
    }

    private void deleteNameRelationship(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result,
            int nrId) throws SQLException {
        String sql = " DELETE FROM @@NameRelationship@@ "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, nrId));
        datasource.executeUpdate(sql);

        sql = " DELETE FROM @@NameRelationship_AUD@@ "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, nrId));
        datasource.executeUpdate(sql);

    }

    private long getOrCreateAuditEvent(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result, int nrId) throws SQLException {

        String sql = "SELECT REV FROM @@NameRelationship_AUD@@ WHERE id = " + nrId + " AND REVTYPE = 0";
        ResultSet rs2 = datasource.executeQuery(caseType.replaceTableNames(sql));
        long rev;
        if (rs2.next()){
            rev = rs2.getLong("REV");
        }else{
            rev = createAuditEvent(datasource, caseType, monitor, result);
        }
        return rev;
    }

    private void updateNameInSource(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result,
            ResultSet rs, int nomSourceId, int nameId1) throws SQLException {

        String sql = "UPDATE @@OriginalSourceBase@@ SET nameUsedInSource_id = %d "
                + " WHERE id = %d ";
        sql = caseType.replaceTableNames(String.format(sql, nameId1, nomSourceId));
        datasource.executeUpdate(sql);

        sql = "UPDATE @@OriginalSourceBase_AUD@@ SET nameUsedInSource_id = %d "
                + " WHERE id = %d ";  //not fully correct for earlier revisions, but creating an INSERT is a bit more complex and maybe not so important
        sql = caseType.replaceTableNames(String.format(sql, nameId1, nomSourceId));
        datasource.executeUpdate(sql);
    }

    /**
     * @return the new source id
     */
    private int createSource(ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result, int nameId, Long rev) throws SQLException {

        //insert empty OriginalSourceBase record
        int nextSourceId = this.getMaxId1(datasource, "OriginalSourceBase", true, null, caseType, result);
        String sql = "INSERT INTO @@OriginalSourceBase@@ (DTYPE, id, uuid, sourceType, sourcedName_id, created, createdBy_id ) "
                + " VALUES ('NomenclaturalSource', %d, '%s', '%s', %d, '%s', NULL)";
        sql = String.format(sql, nextSourceId, UUID.randomUUID(), "NOR",
                nameId, this.getNowString());
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        if (rev != null){
            sql = "INSERT INTO @@OriginalSourceBase_AUD@@ (REV, revtype, DTYPE, id, uuid, sourceType, sourcedName_id, created, createdBy_id ) "
                    + " VALUES (%d, 0, 'NomenclaturalSource', %d, '%s', '%s', %d, '%s', NULL)";
            sql = String.format(sql, rev, nextSourceId, UUID.randomUUID(), "NOR",
                    nameId, this.getNowString());
            datasource.executeUpdate(caseType.replaceTableNames(sql));
        }

        return nextSourceId;
    }
}