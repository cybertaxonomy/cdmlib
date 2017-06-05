/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v41_47;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ITermUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * Updates all zoological "invalid" attached to botanical names and vice versa.
 *
 * #6340
 *
 * @author a.mueller
 * @date 05.06.2017
 *
 */
public class NomStatusInvalidUpdater extends SchemaUpdaterStepBase implements ITermUpdaterStep{
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(NomStatusInvalidUpdater.class);

    /**
     * @return
     */
    public static NomStatusInvalidUpdater NewInstance() {
        return new NomStatusInvalidUpdater();
    }

    private static final String stepName = "Update 'invalid' status with wrong nomenclatural code";

    /**
     * @param stepName
     */
    protected NomStatusInvalidUpdater() {
        super(stepName);
    }



    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

        try {
            String zooUuid = "2bef7039-c129-410b-815e-2a1f7249127b";
            String botUuid = "b09d4f51-8a77-442a-bbce-e7832aaf46b7";
            String defTerm = caseType.transformTo("DefinedTermBase");
            String statusSql = " SELECT id FROM %s dtb "
                    + " WHERE dtb.uuid = '%s'";
            String zooIdSql = String.format(statusSql, defTerm, zooUuid);
            Number zooId = (Number)datasource.getSingleValue(zooIdSql);

            String botIdSql = String.format(statusSql, defTerm, botUuid);
            Number botId = (Number)datasource.getSingleValue(botIdSql);


            String sqlUpdate = "UPDATE @@NomenclaturalStatus@@ " +
                    " SET type_id = %d " +
                    " WHERE id IN ( " +
                    " SELECT id FROM (SELECT st.id " +
                    "   FROM @@NomenclaturalStatus@@ st  " +
                    "   INNER JOIN @@DefinedTermBase@@ stType ON stType.id = st.type_id  " +
                    "   INNER JOIN @@TaxonNameBase_NomenclaturalStatus@@ MN ON MN.status_id = st.id " +
                    "   INNER JOIN @@TaxonNameBase@@ tn ON tn.id = MN.TaxonNameBase_id " +
                    "   WHERE stType.uuid = '%s' AND tn.DTYPE %s "
                    + ") as drvTbl )"
                    ;
            sqlUpdate = caseType.replaceTableNames(sqlUpdate);

            int n = 0;
            if (botId == null){
                result.addWarning("Botanical invalid status id not found.", this, "invoke");
            }else{
                String botUpdate = String.format(sqlUpdate, botId, zooUuid, "<> 'ZoologicalName'");
                n = datasource.executeUpdate(botUpdate);
            }

            if (zooId == null){
                result.addWarning("Zoological invalid status id not found.", this, "invoke");
            }else{
                String zooUpdate = String.format(sqlUpdate, zooId, botUuid, "= 'ZoologicalName'");
                n = n + datasource.executeUpdate(zooUpdate);
            }


        } catch (Exception e) {
            String message = e.getMessage();
            monitor.warning(message, e);
            result.addException(e, message, this, "invoke");
        }

        return;
    }

}
