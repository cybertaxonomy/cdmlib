/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v505_508;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.ISchemaUpdaterStep;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;
import eu.etaxonomy.cdm.model.permission.User;

/**
 * @author a.mueller
 * @since 12.08.2019
 *
 */
public class UsernameRegexAdapter  extends SchemaUpdaterStepBase {


    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(UsernameRegexAdapter.class);

    private static final String stepName = "Adapt username to username regex";


    public static final UsernameRegexAdapter NewInstance(List<ISchemaUpdaterStep> stepList){
        UsernameRegexAdapter result = new UsernameRegexAdapter(stepList);
        return result;
    }

    protected UsernameRegexAdapter(List<ISchemaUpdaterStep> stepList) {
        super(stepList, stepName);
    }

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result) throws SQLException {
        doForTable(datasource, monitor, caseType, result, "UserAccount", "username");
        doForTable(datasource, monitor, caseType, result, "PermissionGroup", "name");
    }

    /**
     * @param datasource
     * @param monitor
     * @param caseType
     * @param result
     * @param tableName
     * @param attribute
     * @throws SQLException
     */
    private void doForTable(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType,
            SchemaUpdateResult result, String tableName, String attribute) throws SQLException {
        tableName = caseType.transformTo(tableName);
        String sql = "SELECT id, "+attribute+" FROM " + tableName;
        ResultSet rs = datasource.executeQuery(sql);
        while (rs.next()){
            boolean updated = false;
            String value = rs.getString(attribute);
            if (StringUtils.isEmpty(value)){
                value = "___empty___";
            }
            StringBuilder builder = new StringBuilder(value);
            for (int i=0; i<value.length() ;i++){
                char c = value.charAt(i);
                if (!String.valueOf(c).matches(User.USERNAME_REGEX)){
                    builder.setCharAt(i, '_');
                    updated = true;
                }
            }
            if (updated){
                int id = rs.getInt("id");
                sql = " UPDATE "+tableName+ " "
                    + " SET " + attribute + " = '" + builder.toString() + "'"
                    + " WHERE id = " + id;
                datasource.executeUpdate(sql);
            }
        }
    }

}
