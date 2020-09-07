/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v515_518;

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

/**
 * @author a.mueller
 * @since 12.06.2020
 */
public class RecommendedMeasurementUnitAdder  extends SchemaUpdaterStepBase {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RecommendedMeasurementUnitAdder.class);

    private final UUID uuidFeature;
    private final UUID uuidUnit;

    public static final RecommendedMeasurementUnitAdder NewInstance(List<ISchemaUpdaterStep> stepList,
            String stepName, UUID uuidFeature, UUID uuidUnit){
        RecommendedMeasurementUnitAdder result = new RecommendedMeasurementUnitAdder(
                stepList, stepName, uuidFeature, uuidUnit);
        return result;
    }

    protected RecommendedMeasurementUnitAdder(List<ISchemaUpdaterStep> stepList, String stepName, UUID uuidFeature, UUID uuidUnit) {
        super(stepList, stepName);
        this.uuidFeature = uuidFeature;
        this.uuidUnit = uuidUnit;
    }

    @Override
    public List<ISchemaUpdaterStep> getInnerSteps() {
        List<ISchemaUpdaterStep> result = new ArrayList<>();
        return result;
    }

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

        boolean includeAudit = true;

        //find IDs
        String sql = "SELECT id "
                + " FROM "+caseType.transformTo("DefinedTermBase")+" t "
                + " WHERE t.uuid = '"+uuidFeature+"'";
        Integer idFeature = (Integer)datasource.getSingleValue(sql);
        if (idFeature == null){
            return;
        }

        sql = "SELECT id "
                + " FROM "+caseType.transformTo("DefinedTermBase")+" smv "
                + " WHERE smv.uuid = '"+uuidUnit+"'";
        Integer idUnit = (Integer)datasource.getSingleValue(sql);

        sql = "SELECT count(*) "
                + "FROM @@DefinedTermBase_StatisticalMeasure@@ "
                + " WHERE Feature_id = "+idFeature+" AND recommendedStatisticalMeasures_id = " + idUnit;
        Long count = (Long)datasource.getSingleValue(caseType.replaceTableNames(sql));
        if (count > 0){
            return;
        }

        //insert records
        sql = "INSERT INTO @@DefinedTermBase_StatisticalMeasure@@ (Feature_id, recommendedStatisticalMeasures_id)"
           + " VALUES (" + idFeature + "," + idUnit + ")";
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        if (includeAudit){
            Integer rev;
            try {
                sql = "SELECT MAX(REV) "
                        + " FROM "+caseType.transformTo("DefinedTermBase_AUD")+" t "
                        + " WHERE t.uuid = '"+uuidFeature+"'";
                rev = (Integer)datasource.getSingleValue(sql);
            } catch (Exception e) {
                //TODO we could also create a new AUDIT event
                result.addWarning("Revision number for adding measurement unit to feature could not be defined. Adding is not audited.", this, "");
                includeAudit = false;
                return;
            }
            sql = "INSERT INTO @@DefinedTermBase_StatisticalMeasure_AUD@@ (REV, Feature_id, recommendedStatisticalMeasures_id, REVTYPE)"
                    + " VALUES ("+rev+"," + idFeature + "," + idUnit + ","+0+")";
            datasource.executeUpdate(caseType.replaceTableNames(sql));
        }
    }
}
