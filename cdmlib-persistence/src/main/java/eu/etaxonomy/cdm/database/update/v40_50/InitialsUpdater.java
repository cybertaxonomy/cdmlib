/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v40_50;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.strategy.cache.agent.PersonDefaultCacheStrategy;

/**
 * Updates the field Person.initials from Person.firstname.
 * @author a.mueller
 * @since 21.05.2017
 */
public class InitialsUpdater extends SchemaUpdaterStepBase{

    public static InitialsUpdater NewInstance(List<ISchemaUpdaterStep> stepList) {
        return new InitialsUpdater(stepList);
    }

    private static final String stepName = "Make Person initials from firstname";

    protected InitialsUpdater(List<ISchemaUpdaterStep> stepList) {
        super(stepList, stepName);
    }

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

        try {
            PersonDefaultCacheStrategy formatter = PersonDefaultCacheStrategy.NewInstance();

            String sqlCopyFirstname = "UPDATE AgentBase SET initials = firstname WHERE DTYPE='Person'"
                    + " AND firstname IS NOT NULL AND initials IS NULL ";

            String sqlRemoveFirstname = "UPDATE AgentBase SET firstname = %s, initials = %s WHERE id = %d";

            String sqlSelectInitials = "SELECT id, firstname FROM AgentBase WHERE DTYPE='Person'"
                    + " AND firstname IS NOT NULL AND initials = firstname ";

            datasource.executeUpdate(sqlCopyFirstname);

            ResultSet rs = datasource.executeQuery(sqlSelectInitials);
            while (rs.next()){
                handleSingle(datasource, formatter, sqlRemoveFirstname, rs, monitor, result);
            }
        } catch (Exception e) {
            String message = e.getMessage();
            monitor.warning(message, e);
            result.addException(e, message, this, "invoke");
        }

        return;
    }

    private void handleSingle(ICdmDataSource datasource, PersonDefaultCacheStrategy formatter,
            String sqlRemoveFirstname, ResultSet rs, IProgressMonitor monitor, SchemaUpdateResult result) throws SQLException {
        try {
            Integer id = rs.getInt("id");
            String firstname = rs.getString("firstname");

            String initialsAllow = formatter.getInitialsFromGivenName(firstname, false);
            String initialsForced = formatter.getInitialsFromGivenName(firstname, true);

            String firstnameSql;
            String initialsSql;
            if (CdmUtils.equalsIgnoreWS(firstname, initialsForced)){
                //firstname was initials
                firstnameSql = " null ";
                initialsSql = initialsForced;
            }else if (CdmUtils.equalsIgnoreWS(firstname, initialsAllow)){
                //first name has only abbreviations, but not all of them being 1-letter, keep everything
                firstnameSql = " firstname ";
                initialsSql = initialsAllow;
            }else {
                //first name has non abbreviated parts, keep it as it is and use initials forced as initials
                firstnameSql = " firstname ";
                initialsSql = initialsForced;
            }
            if (initialsSql!= null){
                initialsSql = initialsSql.replace("'", "\\'");
            }
            //handle blank
            if (StringUtils.isBlank(firstname)){
                firstnameSql = " null ";
                initialsSql = " null ";
            }else {
                initialsSql = "'" + initialsSql + "'";
            }

            String sql = String.format(sqlRemoveFirstname, firstnameSql, initialsSql, id);
            //remove old relationship
            datasource.executeUpdate(sql);
        } catch (Exception e) {
            String message = e.getMessage();
            monitor.warning(message, e);
            result.addException(e, message, this, "handleSingle");
        }
    }
}