/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v523_525;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * #9664 #4311
 * @author a.mueller
 * @since 22.04.2021
 */
public class NomenclaturalTitleUpdater extends SchemaUpdaterStepBase {

    private static final String step = "Update collector title";

    public static NomenclaturalTitleUpdater NewInstance (List<ISchemaUpdaterStep> stepList){
        return new NomenclaturalTitleUpdater(stepList);
    }

    protected NomenclaturalTitleUpdater(List<ISchemaUpdaterStep> stepList) {
        super(stepList, step);
    }
    @Override
    public List<ISchemaUpdaterStep> getInnerSteps() {
        List<ISchemaUpdaterStep> result = new ArrayList<>();

        return result;
    }

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {

        //set collectorCache = titleCache for all persons and teams
        String sql = "UPDATE @@AgentBase@@ "
                  + " SET nomenclaturalTitleCache = nomenclaturalTitle, nomenclaturalTitle = NULL "
                  + " WHERE DTYPE = 'Person' OR DTYPE = 'Team'";
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        //for teams being used as nomenclatural authors
        sql = " SELECT ab.* FROM @@AgentBase@@ ab "
                + " WHERE id IN (SELECT combinationAuthorship_id FROM @@TaxonName@@) "
                + "    OR id IN (SELECT exCombinationAuthorship_id FROM @@TaxonName@@) "
                + "    OR id IN (SELECT basionymAuthorship_id FROM @@TaxonName@@) "
                + "    OR id IN (SELECT exBasionymAuthorship_id FROM @@TaxonName@@) ";
        ResultSet rs = datasource.executeQuery(caseType.replaceTableNames(sql));
        while (rs.next()){
            String dtype = rs.getString("DTYPE");
            int id = rs.getInt("id");
            if ("Team".equalsIgnoreCase(dtype)){

                //for each team member handle like persons below (NOTE: we even handle team members of teams with protectedNomTitleCache in this way as the members could potentially be used in future and potentially be correct for some reason)
                sql = "SELECT p.* FROM @@AgentBase_AgentBase@@ MN INNER JOIN @@AgentBase@@ p ON p.id = MN.teamMembers_id WHERE MN.team_ID = " + id;
                ResultSet rs2 = datasource.executeQuery(caseType.replaceTableNames(sql));
                while (rs2.next()){
                    handlePerson(rs2, datasource, caseType, result);
                }
                rs2.close();
            }else if ("Person".equalsIgnoreCase(dtype)){
                //for each person in gathering event
                handlePerson(rs, datasource, caseType, result);
            }
        }

        sql = "SELECT * FROM @@AgentBase@@ ab "
           + " WHERE DTYPE = 'Person' AND nomenclaturalTitle IS NULL AND nomenclaturalTitleCache <> titleCache ";
        rs = datasource.executeQuery(caseType.replaceTableNames(sql));
        while (rs.next()){
            if (isNomTitleMaybeRelevant(rs)){
                handlePerson(rs, datasource, caseType, result);
            }
        }
        //TODO check if nomtitle = titleCache und Punkt
    }

    private boolean isNomTitleMaybeRelevant(ResultSet rs) throws SQLException {
        String titleCache = rs.getString("titleCache");
        String nomenclaturalTitleCache = rs.getString("nomenclaturalTitleCache");
        String familyName = rs.getString("familyName");
        String givenName = rs.getString("givenName");

        boolean protectedTitleCache = rs.getBoolean("protectedTitleCache");
        if (isBlank(nomenclaturalTitleCache) || nomenclaturalTitleCache.contains(",")){
            return false;
        }
        //if titleCache is protected and they are more or less equal it is expected that the nomCache was created from titleCache not the other way round
        if (protectedTitleCache && normalizeCache(titleCache).equals(normalizeCache(nomenclaturalTitleCache))){
            return false;
        }
        nomenclaturalTitleCache = nomenclaturalTitleCache.trim();
        if (nomenclaturalTitleCache.matches(".*(\\s+)(\\s*[A-Z]\\.)+")){ //if it ends with an upper case initial consider it not to be a valid nom. title
            return false;
        }
        return true;
    }


    private String normalizeCache(String str) {
        return (CdmUtils.Nz(str).replace(",", "").replaceAll("\\s", "").replace(".", ""));
    }

    private void handlePerson(ResultSet rs, ICdmDataSource datasource, CaseType caseType, SchemaUpdateResult result) throws SQLException {
        //preliminary set protected cache for all Persons being in use as nom. author
        int id = rs.getInt("id");
        String sql = "UPDATE @@AgentBase@@ SET nomenclaturalTitle = nomenclaturalTitleCache WHERE id = " + id;
        datasource.executeUpdate(caseType.replaceTableNames(sql));
    }
}
