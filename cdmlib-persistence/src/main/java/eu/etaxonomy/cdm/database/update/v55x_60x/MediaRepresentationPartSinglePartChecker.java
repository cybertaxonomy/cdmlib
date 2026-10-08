/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.database.update.v55x_60x;

import java.sql.ResultSet;
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
 * Precondition check for merging {@code MediaRepresentationPart} into
 * {@code MediaRepresentation}: aborts the schema update if any
 * MediaRepresentation has more than one part, before any schema changes
 * are applied.
 *
 * @author muellera
 * @since 08.10.2026
 * @see MediaRepresentationPartMerger
 */
public class MediaRepresentationPartSinglePartChecker extends SchemaUpdaterStepBase {

    private static final Logger logger = LogManager.getLogger();

    private static final String stepName = "Check MediaRepresentation has at most one part";

    public static final MediaRepresentationPartSinglePartChecker NewInstance(List<ISchemaUpdaterStep> stepList) {
        return new MediaRepresentationPartSinglePartChecker(stepList);
    }

    protected MediaRepresentationPartSinglePartChecker(List<ISchemaUpdaterStep> stepList) {
        super(stepList, stepName);
    }

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

        String sql = "SELECT representation_id, COUNT(*) AS cnt "
                + " FROM @@MediaRepresentationPart@@ "
                + " GROUP BY representation_id "
                + " HAVING COUNT(*) > 1 ";
        ResultSet rs = datasource.executeQuery(caseType.replaceTableNames(sql));
        StringBuilder ids = new StringBuilder();
        int n = 0;
        while (rs.next()) {
            if (n > 0) {
                ids.append(", ");
            }
            ids.append(rs.getInt("representation_id")).append("(").append(rs.getInt("cnt")).append(")");
            n++;
            if (n >= 20) {
                ids.append(", ...");
                break;
            }
        }
        if (n > 0) {
            String message = "Cannot merge MediaRepresentationPart into MediaRepresentation: "
                    + "found MediaRepresentation(s) with more than one part. "
                    + "Aborting without schema or data changes. representation_id(count): " + ids;
            logger.error(message);
            result.addError(message, this, "invoke");
        }
    }
}
