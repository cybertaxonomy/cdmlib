/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v41_47;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.strategy.cache.agent.PersonDefaultCacheStrategy;

/**
 * Updates the field Person.initials from Person.firstname.
 * @author a.mueller
 * @date 21.05.2017
 *
 */
public class InitialsUpdater extends SchemaUpdaterStepBase{
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(InitialsUpdater.class);

    /**
     * @return
     */
    public static InitialsUpdater NewInstance() {
        return new InitialsUpdater();
    }

    private static final String stepName = "Make Person initials from firstname";

    /**
     * @param stepName
     */
    protected InitialsUpdater() {
        super(stepName);
    }



    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

        try {
            PersonDefaultCacheStrategy formatter = PersonDefaultCacheStrategy.NewInstance();

            String sqlCopyFirstname = "UPDATE AgentBase SET initials = firstname WHERE DTYPE='Person'"
                    + " AND firstname IS NOT NULL ";

            String sqlRemoveFirstname = "UPDATE AgentBase SET firstname = %s, initials = '%s' WHERE id = %d";

            String sqlSelectInitials = "SELECT id, firstname FROM AgentBase WHERE DTYPE='Person'"
                    + " AND firstname IS NOT NULL ";

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



    /**
     * @param datasource
     * @param formatter
     * @param sqlRemoveFirstname
     * @param rs
     * @param monitor
     * @throws SQLException
     */
    private void handleSingle(ICdmDataSource datasource, PersonDefaultCacheStrategy formatter,
            String sqlRemoveFirstname, ResultSet rs, IProgressMonitor monitor, SchemaUpdateResult result) throws SQLException {
        try {
            Integer id = rs.getInt("id");
            String firstname = rs.getString("firstname");

            String initialsAllow = formatter.getInitialsFromFirstname(firstname, false);
            String initialsForced = formatter.getInitialsFromFirstname(firstname, true);

            String firstnameSql;
            String initialsSql;
            if (CdmUtils.equalsIgnoreWS(firstname, initialsForced)){
                //firstname was initials
                firstnameSql = " null ";
                initialsSql = initialsForced;
            }else if (CdmUtils.equalsIgnoreWS(firstname, initialsAllow)){
                //firstname has only abbreviations, but not all of them being 1-letter, keep everything
                firstnameSql = " firstname ";
                initialsSql = initialsAllow;
            }else {
                //firstname has non abbreviated parts, keep it as it is and use initials forced as initials
                firstnameSql = " firstname ";
                initialsSql = initialsForced;
            }
            if (initialsSql!= null){
                initialsSql = initialsSql.replace("'", "\\'");
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
