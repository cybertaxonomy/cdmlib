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
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;

/**
 * #9124
 *
 * This schema update step class is to correct an error introduced by the
 * first implementation of class {@link RecommendedMeasurementUnitAdder}
 * which added the recommendedMeasurmentUnit for altitudes to table
 * DefinedTermBase_StatisticalMeasure and not to table DefinedTermBase_MeasurementUnit.
 *
 * @author a.mueller
 * @since 24.09.2020
 */
public class AltitudeMeterCorrector  extends SchemaUpdaterStepBase {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AltitudeMeterCorrector.class);

    private final UUID uuidFeature = Feature.uuidAltitude;
    private final UUID uuidUnit = MeasurementUnit.uuidMeter;
    private static final String stepName = "Correct altitude recommended measurement unit";

    public static final AltitudeMeterCorrector NewInstance(List<ISchemaUpdaterStep> stepList){
        AltitudeMeterCorrector result = new AltitudeMeterCorrector(stepList);
        return result;
    }

    protected AltitudeMeterCorrector(List<ISchemaUpdaterStep> stepList) {
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

        boolean includeAudit = true;

        //remove incorrect entry
        String sql = "SELECT MN.feature_id, MN.recommendedstatisticalmeasures_id "
                + " FROM @@DefinedTermBase_StatisticalMeasure@@ MN "
                + " INNER JOIN @@DefinedTermBase@@ alt ON alt.id = MN.feature_id "
                + " INNER JOIN @@DefinedTermBase@@ sm ON sm.id = MN.recommendedstatisticalmeasures_id "
                + " WHERE alt.uuid = '"+uuidFeature+"' AND sm.uuid = '"+uuidUnit+"'";
        sql = caseType.replaceTableNames(sql);
        ResultSet rs = datasource.executeQuery(caseType.replaceTableNames(sql));
        while (rs.next()){
            int featureId = rs.getInt("feature_id");
            int unitId = rs.getInt("recommendedstatisticalmeasures_id");
            sql = "DELETE FROM @@DefinedTermBase_StatisticalMeasure@@ "
                    + " WHERE feature_id = "+featureId+" AND recommendedstatisticalmeasures_id = " + unitId;
            datasource.executeUpdate(caseType.replaceTableNames(sql));

            sql = "SELECT count(*) "
                    + "FROM @@DefinedTermBase_MeasurementUnit@@ "
                    + " WHERE Feature_id = "+featureId+" AND recommendedMeasurementUnits_id = " + unitId;
            Long count = (Long)datasource.getSingleValue(caseType.replaceTableNames(sql));
            if (count > 0){
                return;
            }

            //insert records
            sql = "INSERT INTO @@DefinedTermBase_MeasurementUnit@@ (Feature_id, recommendedMeasurementUnits_id)"
                    + " VALUES (" + featureId + "," + unitId + ")";
            datasource.executeUpdate(caseType.replaceTableNames(sql));

            //audit
            if (includeAudit){
                sql = "DELETE FROM @@DefinedTermBase_StatisticalMeasure_AUD@@ "
                        + " WHERE feature_id = "+featureId+" AND recommendedstatisticalmeasures_id = " + unitId;
                datasource.executeUpdate(caseType.replaceTableNames(sql));
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
                sql = "INSERT INTO @@DefinedTermBase_MeasurementUnit_AUD@@ (REV, Feature_id, recommendedMeasurementUnits_id, REVTYPE)"
                        + " VALUES ("+rev+"," + featureId + "," + unitId + ","+0+")";
                datasource.executeUpdate(caseType.replaceTableNames(sql));
            }
        }
    }
}
