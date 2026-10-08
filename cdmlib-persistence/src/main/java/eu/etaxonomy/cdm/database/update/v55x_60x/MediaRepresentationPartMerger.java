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
 *
 * @author muellera
 * @since 08.10.2026
 */
public class MediaRepresentationPartMerger extends SchemaUpdaterStepBase {

    private static final Logger logger = LogManager.getLogger();

    private static final String stepName = "Merge MediaRepresentationPart into MediaRepresentation";

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

        // DTYPE: base class MediaRepresentationPart becomes MediaRepresentation
        String sql = "UPDATE @@MediaRepresentation@@ mr "
                + " INNER JOIN @@MediaRepresentationPart@@ mrp ON mrp.representation_id = mr.id "
                + " SET mr.uri = mrp.uri, "
                + "     mr.size = mrp.size, "
                + "     mr.height = mrp.height, "
                + "     mr.width = mrp.width, "
                + "     mr.duration = mrp.duration, "
                + "     mr.DTYPE = CASE "
                + "         WHEN mrp.DTYPE = 'MediaRepresentationPart' OR mrp.DTYPE IS NULL "
                + "         THEN 'MediaRepresentation' "
                + "         ELSE mrp.DTYPE END ";
        int updated = datasource.executeUpdate(caseType.replaceTableNames(sql));
        logger.info("Copied MediaRepresentationPart data into MediaRepresentation for " + updated + " rows");
    }

    private void copyPartDataToRepresentationAud(ICdmDataSource datasource, CaseType caseType)
            throws SQLException {

        // Prefer matching audit revisions; then fill remaining AUD rows from live part data
        String sql = "UPDATE @@MediaRepresentation_AUD@@ mr "
                + " INNER JOIN @@MediaRepresentationPart_AUD@@ mrp "
                + "     ON mrp.representation_id = mr.id AND mrp.REV = mr.REV "
                + " SET mr.uri = mrp.uri, "
                + "     mr.size = mrp.size, "
                + "     mr.height = mrp.height, "
                + "     mr.width = mrp.width, "
                + "     mr.duration = mrp.duration, "
                + "     mr.DTYPE = CASE "
                + "         WHEN mrp.DTYPE = 'MediaRepresentationPart' OR mrp.DTYPE IS NULL "
                + "         THEN 'MediaRepresentation' "
                + "         ELSE mrp.DTYPE END ";
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        sql = "UPDATE @@MediaRepresentation_AUD@@ mr "
                + " INNER JOIN @@MediaRepresentationPart@@ mrp ON mrp.representation_id = mr.id "
                + " SET mr.uri = COALESCE(mr.uri, mrp.uri), "
                + "     mr.size = COALESCE(mr.size, mrp.size), "
                + "     mr.height = COALESCE(mr.height, mrp.height), "
                + "     mr.width = COALESCE(mr.width, mrp.width), "
                + "     mr.duration = COALESCE(mr.duration, mrp.duration), "
                + "     mr.DTYPE = COALESCE(mr.DTYPE, "
                + "         CASE WHEN mrp.DTYPE = 'MediaRepresentationPart' OR mrp.DTYPE IS NULL "
                + "              THEN 'MediaRepresentation' ELSE mrp.DTYPE END) ";
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
                + " INNER JOIN @@MediaRepresentationPart@@ mrp ON mmd.mediaRepresentation_id = mrp.id "
                + " SET mmd.mediaRepresentation_id = mrp.representation_id ";
        int updated = datasource.executeUpdate(caseType.replaceTableNames(sql));
        logger.info("Remapped MediaMetaData.mediaRepresentation_id for " + updated + " rows");
    }

    private void remapMediaMetaDataAud(ICdmDataSource datasource, CaseType caseType) throws SQLException {

        String sql = "UPDATE @@MediaMetaData_AUD@@ mmd "
                + " INNER JOIN @@MediaRepresentationPart@@ mrp ON mmd.mediaRepresentation_id = mrp.id "
                + " SET mmd.mediaRepresentation_id = mrp.representation_id ";
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        sql = "UPDATE @@MediaMetaData_AUD@@ mmd "
                + " INNER JOIN @@MediaRepresentationPart_AUD@@ mrp "
                + "     ON mmd.mediaRepresentation_id = mrp.id AND mmd.REV = mrp.REV "
                + " SET mmd.mediaRepresentation_id = mrp.representation_id ";
        datasource.executeUpdate(caseType.replaceTableNames(sql));
    }
}