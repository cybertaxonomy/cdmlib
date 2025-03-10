/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update;

import java.sql.ResultSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.v54x_54x.SchemaUpdater_5480_5490;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;

/**
 * This class launches CDM model updates.
 * <BR>
 * For each new schema version number there usually exists 1 {@link ISchemaUpdater} which
 * represents a list of schema update steps. {@link ISchemaUpdater schema updaters} are linked
 * to previous updaters which are called, if relevant, previous to the latest updater.
 * So it is possible to upgrade multiple schema version steps in one call.
 * <BR><BR>
 * As said before each {@link ISchemaUpdater schema updater} creates a list of
 * {@link ISchemaUpdaterStep update steps}.
 * <BR><BR>
 * {@link ISchemaUpdater} support progression monitoring with each update step being one task.
 * <BR><BR>
 * ATTENTION: Some steps in the schema update are not transactional by nature. E.g. adding or removing a column
 * to a table in a SQL database can not be handled in a transaction. Therefore failures in
 * certain steps may not lead to a complete rollback of all steps covered by a {@link ISchemaUpdater}.
 * This may lead to a situation where the database becomes inconsistent.
 * <BR><BR>
 * <u>HOW TO ADD A NEW UPDATER?</u><BR>
 * Adding a new updater currently still needs adjustment at multiple places.
 * <BR>
 * <BR>1.) Increment {@link CdmMetaData} schema version number.
 * <BR>2.) Create a new class instance of {@link SchemaUpdaterBase} (e.g. by copying an old one).
 * <BR>3.) Update startSchemaVersion and endSchemaVersion in this new class, where startSchemaVersion
 * is the old schema version and endSchemaVersion is the new schema version.
 * <BR>4.) Implement {@link ISchemaUpdater#getPreviousUpdater()} and {@link ISchemaUpdater#getNextUpdater()}
 * in a way that the former returns an instance of the previous schema updater and the later returns null (for now).
 * <BR>5.) Go to the previous schema updater class and adjust {@link ISchemaUpdater#getNextUpdater()}
 * in a way that it returns an instance of the newly created updater.
 * <BR>6.) Adjust {@link CdmUpdater#getCurrentSchemaUpdater()} to return
 * instances of the newly created updater.
 *
 * NOTE: Prior to cdmlib version 4.8/schema version 4.7 the CdmUpdater was split into a schema updater
 * and a term updater. This architecture caused problems and was therefore removed in 4.8.
 *
 * @see ISchemaUpdater
 * @see ISchemaUpdaterStep
 *
 * @author a.mueller
 * @since 10.09.2010
 */
public class CdmUpdater {

    private static final Logger logger = LogManager.getLogger();

    private static final ISchemaUpdater getCurrentSchemaUpdater() {
        return SchemaUpdater_5480_5490.NewInstance();
    }

    public static CdmUpdater NewInstance(){
        return new CdmUpdater();
    }

    public SchemaUpdateResult updateToCurrentVersion(ICdmDataSource datasource, IProgressMonitor monitor){
        SchemaUpdateResult result = new SchemaUpdateResult();
        if (monitor == null){
            monitor = DefaultProgressMonitor.NewInstance();
        }
        CaseType caseType = CaseType.caseTypeOfDatasource(datasource);

        ISchemaUpdater currentSchemaUpdater = getCurrentSchemaUpdater();

        int steps = currentSchemaUpdater.countSteps(datasource, monitor, caseType);
        steps++;  //for hibernate_sequences update

        String taskName = "Update to schema version " + currentSchemaUpdater.getTargetVersion();
        monitor.beginTask(taskName, steps);

        try {
            datasource.startTransaction();
            currentSchemaUpdater.invoke(datasource, monitor, caseType, result);
            if (result.isSuccess()){
                //TODO should not run if no update was necesssary
                updateHibernateSequence(datasource, monitor, caseType, result);
            }
            if (!result.isSuccess()){
                datasource.rollback();  //does not work for ddl statements, therefore not really necessary
            }else{
                datasource.commitTransaction();
            }
        } catch (Exception e) {
            String message = "Stopped schema updater";
            result.addException(e, message, "CdmUpdater");
            monitor.warning(message);
        } finally {
            String message = "Update finished " + (result.isSuccess() ? "successfully" : "with ERRORS");
            monitor.subTask(message);
            if (!result.isSuccess()){
                monitor.warning(message);
                monitor.setCanceled(true);
            }else{
                monitor.done();
            }
            logger.info(message);
        }

        return result;
    }

    /**
     * Updating terms often inserts new terms, vocabularies and representations.
     * Therefore the counter in hibernate_sequences must be increased.
     * We do this once at the end of term updating.
     * @param caseType
     * @param result2
     * @return true if update was successful, false otherwise
     */
    private void updateHibernateSequence(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) {
        monitor.subTask("Update hibernate sequences");
        try {
            String sql = "SELECT * FROM hibernate_sequences ";
            ResultSet rs = datasource.executeQuery(sql);
            while (rs.next()){
                String table = rs.getString("sequence_name");
                Integer val = rs.getInt("next_val");
                updateSingleValue(datasource, monitor, table, val, caseType, result);
            }
        } catch (Exception e) {
            String message = "Exception occurred when trying to update hibernate_sequences table: " + e.getMessage();
            monitor.warning(message, e);
            logger.error(message);
            result.addException(e, message, "CdmUpdater.updateHibernateSequence");
        }finally{
            monitor.worked(1);
        }
        return;
    }

    private void updateSingleValue(ICdmDataSource datasource, IProgressMonitor monitor, String table,
                Integer oldVal, CaseType caseType, SchemaUpdateResult result){
        if (table.equals("default")){  //found in flora central africa test database
            return;
        }
        try {
            Integer newVal;
            try {
                String id = table.equalsIgnoreCase("AuditEvent")? "revisionNumber" : "id";
                String sql = " SELECT max(%s) FROM %s ";
                newVal = (Integer)datasource.getSingleValue(String.format(sql, id, caseType.transformTo(table)));
            } catch (Exception e) {
                String message = "Could not retrieve max value for table '%s'. Will not update hibernate_sequence for this table. " +
                        "Usually this will not cause problems, however, if new data has been added to " +
                        "this table by the update script one may encounter 'unique identifier' " +
                        "exceptions when trying to add further data.";
                monitor.warning(String.format(message,table), e);
                result.addWarning(message, (String)null, "table = " + table);
                return;
            }

            if (newVal != null){
                //This is how {@link PooledOptimizer#generate(org.hibernate.id.enhanced.AccessCallback)} works
                //it substracts the increment size from the value in hibernate_sequences to get the initial value.
                //Haven't checked why.
                //For the correct increment size see eu.etaxonomy.cdm.model.common.package-info.java
                int incrementSize = 10;
                newVal = newVal + incrementSize;
                if (newVal >= oldVal){
                    String sql = " UPDATE hibernate_sequences " +
                            " SET next_val = %d " +
                            " WHERE sequence_name = '%s' ";
                    datasource.executeUpdate(String.format(sql, newVal + 1 , table) );
                }
            }
            return;
        } catch (Exception e) {
            String message = "Exception occurred when trying to read or update hibernate_sequences table for value " + table + ": " + e.getMessage();
            monitor.warning(message, e);
            logger.error(message);
            result.addException(e, message, "CdmUpdater.updateSingleValue(table = " + table + ")");
        }
    }

    /**
     * @param args SERVER DB_NAME1[,DB_NAME2,...] [USER] [PASSWORD] [PORT]
     */
    public static void main(String[] args) {
//        logger.warn("main method not yet fully implemented (only works with mysql!!!)");
//        if(args.length < 2){
//            logger.error("Arguments missing: server database [username [password]]");
//        }
        //TODO better implementation
        CdmUpdater myUpdater = new CdmUpdater();
        System.out.println("CdmUpdater\nArguments: SERVER DB_NAME1[,DB_NAME2,...] [USER] [PASSWORD] [PORT]");
        String server = args[0];
        String database  = args[1];
        String[] databaseNames = StringUtils.split(database, ',');
        String username = args.length > 2 ? args[2] : null;
        String password  = args.length > 3 ? args[3] : null;
        int port  = 3306;
        if( args.length > 4){
            try {
                port = Integer.parseInt(args[4]);
            } catch (Exception e) {
                // ignore
            }
        }
        System.out.println("Number of databases to update: " + databaseNames.length);
        for(String dnName : databaseNames){
            System.out.println(dnName + " UPDATE ...");
            ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance(server, dnName, port, username, password);
            SchemaUpdateResult result = myUpdater.updateToCurrentVersion(dataSource, null);
            System.out.println(dnName + " DONE " + (result.isSuccess() ? "successfully" : "with ERRORS"));
            System.out.println(result.createReport().toString());
            System.out.println("====================================================================");
        }
    }
}