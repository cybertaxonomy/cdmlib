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

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.strategy.cache.agent.PersonDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.agent.TeamDefaultCacheStrategy;

/**
 * #4311
 * @author a.mueller
 * @since 22.04.2021
 */
public class CollectorTitleUpdater extends SchemaUpdaterStepBase {

    private static final String step = "Update collector title";

    public static CollectorTitleUpdater NewInstance (List<ISchemaUpdaterStep> stepList){
        return new CollectorTitleUpdater(stepList);
    }

    protected CollectorTitleUpdater(List<ISchemaUpdaterStep> stepList) {
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
        String sql = "UPDATE @@AgentBase@@ SET collectorTitleCache = titleCache WHERE DTYPE = 'Person' OR DTYPE = 'Team'";
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        //to remove changes previously made in SchemaUpdater_5230_5250 before handlePerson() was adapted
        sql = "UPDATE @@AgentBase@@ SET collectorTitle = NULL WHERE DTYPE = 'Person'";
        datasource.executeUpdate(caseType.replaceTableNames(sql));

        //for teams being part of gathering event
        sql = " SELECT ab.* FROM @@AgentBase@@ ab WHERE id IN (SELECT actor_id FROM @@GatheringEvent@@) ";
        ResultSet rs = datasource.executeQuery(caseType.replaceTableNames(sql));
        while (rs.next()){
            String dtype = rs.getString("DTYPE");
            int id = rs.getInt("id");
            if ("Team".equalsIgnoreCase(dtype)){
                boolean protectedTitleCache = rs.getBoolean("protectedTitleCache");
                if (protectedTitleCache){
                    //set protectedCollector = true if protectedTitleCache = true
                    sql = "UPDATE @@AgentBase@@ SET protectedCollectorTitleCache = "+getBoolean(true, datasource)+" WHERE id = " + id;
                    datasource.executeUpdate(caseType.replaceTableNames(sql));
                }else{
                    //for each team member handle like persons below
                    sql = "SELECT p.* FROM @@AgentBase_AgentBase@@ MN INNER JOIN @@AgentBase@@ p ON p.id = MN.teamMembers_id WHERE MN.team_ID = " + id + " ORDER BY sortIndex ";
                    ResultSet rs2 = datasource.executeQuery(caseType.replaceTableNames(sql));
                    Team team = Team.NewInstance();
                    while (rs2.next()){
                        Person member = handlePerson(rs2, datasource, caseType);
                        team.addTeamMember(member);
                    }
                    rs2.close();
                    String collectorTitleCache = TeamDefaultCacheStrategy.INSTANCE().getCollectorTitleCache(team);
                    sql = " UPDATE @@AgentBase@@ SET collectorTitleCache = '" + escapeSingleQuote(collectorTitleCache) + "' WHERE id = " + id;
                    datasource.executeUpdate(caseType.replaceTableNames(sql));
                }
            }else if ("Person".equalsIgnoreCase(dtype)){
                //for each person in gathering event
                handlePerson(rs, datasource, caseType);
            }
        }
    }

    private Person handlePerson(ResultSet rs, ICdmDataSource datasource, CaseType caseType) throws SQLException {
        //set collectorTitle
        int id = rs.getInt("id");
        String familyName = rs.getString("familyName");
        String initials = rs.getString("initials");
        String givenName = rs.getString("givenName");
        String titleCache = rs.getString("titleCache");
        String nomenclaturalTitle = rs.getString("nomenclaturalTitle");
        boolean protectedTitleCache = rs.getBoolean("protectedTitleCache");

        Person person = Person.NewInstance(nomenclaturalTitle, familyName, initials, givenName);
        person.setTitleCache(titleCache, protectedTitleCache);
        String collectorTitle =  PersonDefaultCacheStrategy.INSTANCE().getCollectorTitleCache(person);

        String sql = "UPDATE @@AgentBase@@ SET collectorTitleCache = '"+escapeSingleQuote(collectorTitle)+"', collectorTitle = '"+escapeSingleQuote(collectorTitle)+"' WHERE id = " + id;
        datasource.executeUpdate(caseType.replaceTableNames(sql));
        return person;
    }
}