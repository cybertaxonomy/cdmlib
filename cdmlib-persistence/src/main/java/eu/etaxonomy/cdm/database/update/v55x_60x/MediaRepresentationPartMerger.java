/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v55x_60x;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * Merges {@code MediaRepresentationPart} data into {@code MediaRepresentation}.
 * <p>
 * Assumes columns {@code uri}, {@code size}, {@code height}, {@code width},
 * {@code duration} and {@code DTYPE} already exist on {@code MediaRepresentation}.
 * The precondition that each representation has at most one part is checked
 * earlier by {@link MediaRepresentationPartSinglePartChecker}.
 * <p>
 * SQL uses correlated subselects so it works on MySQL/MariaDB, H2 and PostgreSQL
 * (MySQL {@code UPDATE … JOIN} is not portable).
 *
 * @author muellera
 * @since 08.10.2026
 */
public class MediaRepresentationPartMerger extends SchemaUpdaterStepBase {

    private static final Logger logger = LogManager.getLogger();

    private static final String stepName = "Merge MediaRepresentationPart into MediaRepresentation";

    private static final String DTYPE_EXPR =
            "CASE "
            + " WHEN mrp.DTYPE = 'MediaRepresentationPart' OR mrp.DTYPE IS NULL "
            + " THEN 'MediaRepresentation' "
            + " ELSE mrp.DTYPE END";

    public static final MediaRepresentationPartMerger NewInstance(List<ISchemaUpdaterStep> stepList) {
        return new MediaRepresentationPartMerger(stepList);
    }

    protected MediaRepresentationPartMerger(List<ISchemaUpdaterStep> stepList) {
        super(stepList, stepName);
    }

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

        copyPartDataToRepresentation(datasource, caseType);
        copyPartDataToRepresentationAud(datasource, caseType);
        setDefaultDtypeWhereMissing(datasource, caseType);
        remapMediaMetaData(datasource, caseType);
        remapMediaMetaDataAud(datasource, caseType);
    }

    private void copyPartDataToRepresentation(ICdmDataSource datasource, CaseType caseType)
            throws SQLException {

        String sql = "UPDATE @@MediaRepresentation@@ mr "
                + " SET "
                + "   uri = (SELECT mrp.uri FROM @@MediaRepresentationPart@@ mrp WHERE mrp.representation_id = mr.id), "
                + "   size = (SELECT mrp.size FROM @@MediaRepresentationPart@@ mrp WHERE mrp.representation_id = mr.id), "
                + "   height = (SELECT mrp.height FROM @@MediaRepresentationPart@@ mrp WHERE mrp.representation_id = mr.id), "
                + "   width = (SELECT mrp.width FROM @@MediaRepresentationPart@@ mrp WHERE mrp.representation_id = mr.id), "
                + "   duration = (SELECT mrp.duration FROM @@MediaRepresentationPart@@ mrp WHERE mrp.representation_id = mr.id), "
                + "   DTYPE = (SELECT " + DTYPE_EXPR
                + "            FROM @@MediaRepresentationPart@@ mrp WHERE mrp.representation_id = mr.id) "
                + " WHERE EXISTS ("
                + "   SELECT 1 FROM @@MediaRepresentationPart@@ mrp WHERE mrp.representation_id = mr.id"
                + " )";
        int updated = datasource.executeUpdate(caseType.replaceTableNames(sql));
        logger.info("Copied MediaRepresentationPart data into MediaRepresentation for " + updated + " rows");
    }

    private void copyPartDataToRepresentationAud(ICdmDataSource datasource, CaseType caseType)
            throws SQLException {

        // Prefer matching audit revisions
        String sql = "UPDATE @@MediaRepresentation_AUD@@ mr "
                + " SET "
                + "   uri = (SELECT mrp.uri FROM @@MediaRepresentationPart_AUD@@ mrp "
                + "          WHERE mrp.representation_id = mr.id AND mrp.REV = mr.REV), "
                + "   size = (SELECT mrp.size FROM @@MediaRepresentationPart_AUD@@ mrp "
                + "           WHERE mrp.representation_id = mr.id AND mrp.REV = mr.REV), "
                + "   height = (SELECT mrp.height FROM @@MediaRepresentationPart_AUD@@ mrp "
                + "             WHERE mrp.representation_id = mr.id AND mrp.REV = mr.REV), "
                + "   width = (SELECT mrp.width FROM @@MediaRepresentationPart_AUD@@ mrp "
                + "            WHERE mrp.representation_id = mr.id AND mrp.REV = mr.REV), "
                + "   duration = (SELECT mrp.duration FROM @@MediaRepresentationPart_AUD@@ mrp "
                + "               WHERE mrp.representation_id = mr.id AND mrp.REV = mr.REV), "
                + "   DTYPE = (SELECT " + DTYPE_EXPR
                + "            FROM @@MediaRepresentationPart_AUD@@ mrp "
                + "            WHERE mrp.representation_id = mr.id AND mrp.REV = mr.REV) "
                + " WHERE EXISTS ("
                + "   SELECT 1 FROM @@MediaRepresentationPart_AUD@@ mrp "
                + "   WHERE mrp.representation_id = mr.id AND mrp.REV = mr.REV"
                + " )";
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        // Fill remaining AUD rows from live part data
        sql = "UPDATE @@MediaRepresentation_AUD@@ mr "
                + " SET "
                + "   uri = COALESCE(uri, (SELECT mrp.uri FROM @@MediaRepresentationPart@@ mrp "
                + "                        WHERE mrp.representation_id = mr.id)), "
                + "   size = COALESCE(size, (SELECT mrp.size FROM @@MediaRepresentationPart@@ mrp "
                + "                          WHERE mrp.representation_id = mr.id)), "
                + "   height = COALESCE(height, (SELECT mrp.height FROM @@MediaRepresentationPart@@ mrp "
                + "                              WHERE mrp.representation_id = mr.id)), "
                + "   width = COALESCE(width, (SELECT mrp.width FROM @@MediaRepresentationPart@@ mrp "
                + "                            WHERE mrp.representation_id = mr.id)), "
                + "   duration = COALESCE(duration, (SELECT mrp.duration FROM @@MediaRepresentationPart@@ mrp "
                + "                                  WHERE mrp.representation_id = mr.id)), "
                + "   DTYPE = COALESCE(DTYPE, (SELECT " + DTYPE_EXPR
                + "                           FROM @@MediaRepresentationPart@@ mrp "
                + "                           WHERE mrp.representation_id = mr.id)) "
                + " WHERE EXISTS ("
                + "   SELECT 1 FROM @@MediaRepresentationPart@@ mrp WHERE mrp.representation_id = mr.id"
                + " )";
        datasource.executeUpdate(caseType.replaceTableNames(sql));
    }

    private void setDefaultDtypeWhereMissing(ICdmDataSource datasource, CaseType caseType)
            throws SQLException {

        String sql = "UPDATE @@MediaRepresentation@@ SET DTYPE = 'MediaRepresentation' WHERE DTYPE IS NULL";
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        sql = "UPDATE @@MediaRepresentation_AUD@@ SET DTYPE = 'MediaRepresentation' WHERE DTYPE IS NULL";
        datasource.executeUpdate(caseType.replaceTableNames(sql));
    }

    private void remapMediaMetaData(ICdmDataSource datasource, CaseType caseType) throws SQLException {

        // mediaRepresentation_id pointed to MediaRepresentationPart.id → MediaRepresentation.id
        String sql = "UPDATE @@MediaMetaData@@ mmd "
                + " SET mediaRepresentation_id = ("
                + "   SELECT mrp.representation_id FROM @@MediaRepresentationPart@@ mrp "
                + "   WHERE mmd.mediaRepresentation_id = mrp.id"
                + " ) "
                + " WHERE EXISTS ("
                + "   SELECT 1 FROM @@MediaRepresentationPart@@ mrp "
                + "   WHERE mmd.mediaRepresentation_id = mrp.id"
                + " )";
        int updated = datasource.executeUpdate(caseType.replaceTableNames(sql));
        logger.info("Remapped MediaMetaData.mediaRepresentation_id for " + updated + " rows");
    }

    private void remapMediaMetaDataAud(ICdmDataSource datasource, CaseType caseType) throws SQLException {

        String sql = "UPDATE @@MediaMetaData_AUD@@ mmd "
                + " SET mediaRepresentation_id = ("
                + "   SELECT mrp.representation_id FROM @@MediaRepresentationPart@@ mrp "
                + "   WHERE mmd.mediaRepresentation_id = mrp.id"
                + " ) "
                + " WHERE EXISTS ("
                + "   SELECT 1 FROM @@MediaRepresentationPart@@ mrp "
                + "   WHERE mmd.mediaRepresentation_id = mrp.id"
                + " )";
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        sql = "UPDATE @@MediaMetaData_AUD@@ mmd "
                + " SET mediaRepresentation_id = ("
                + "   SELECT mrp.representation_id FROM @@MediaRepresentationPart_AUD@@ mrp "
                + "   WHERE mmd.mediaRepresentation_id = mrp.id AND mmd.REV = mrp.REV"
                + " ) "
                + " WHERE EXISTS ("
                + "   SELECT 1 FROM @@MediaRepresentationPart_AUD@@ mrp "
                + "   WHERE mmd.mediaRepresentation_id = mrp.id AND mmd.REV = mrp.REV"
                + " )";
        datasource.executeUpdate(caseType.replaceTableNames(sql));
    }
}
