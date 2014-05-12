// $Id$
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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.DefaultProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.v31_33.SchemaUpdater_31_33;
import eu.etaxonomy.cdm.database.update.v31_33.SchemaUpdater_33_331;
import eu.etaxonomy.cdm.database.update.v31_33.TermUpdater_31_33;

/**
 * @author a.mueller
 * @date 10.09.2010
 *
 */
public class CdmUpdater {
    private static final Logger logger = Logger.getLogger(CdmUpdater.class);

    public static CdmUpdater NewInstance(){
        return new CdmUpdater();
    }

    /**
     * @param datasource
     * @param monitor may be <code>null</code>
     * @return
     */
    public boolean updateToCurrentVersion(ICdmDataSource datasource, IProgressMonitor monitor){
        boolean result = true;
        if (monitor == null){
            monitor = DefaultProgressMonitor.NewInstance();
        }
        CaseType caseType = CaseType.caseTypeOfDatasource(datasource);

        ISchemaUpdater currentSchemaUpdater = getCurrentSchemaUpdater();
        // TODO do we really always update the terms??
        ITermUpdater currentTermUpdater = getCurrentTermUpdater();

        int steps = currentSchemaUpdater.countSteps(datasource, monitor, caseType);
        steps += currentTermUpdater.countSteps(datasource, monitor, caseType);
        steps++;  //for hibernate_sequences update

        String taskName = "Update to schema version " + currentSchemaUpdater.getTargetVersion() + " and to term version " + currentTermUpdater.getTargetVersion(); //+ currentSchemaUpdater.getVersion();
        monitor.beginTask(taskName, steps);

        try {
            datasource.startTransaction();
            result &= currentSchemaUpdater.invoke(datasource, monitor, caseType);
            if (result == true){
                result &= currentTermUpdater.invoke(datasource, monitor, caseType);
                updateHibernateSequence(datasource, monitor, caseType);
            }
            if (result == false){
                datasource.rollback();
            }else{
                datasource.commitTransaction();
            }

        } catch (Exception e) {
            result = false;
            monitor.warning("Stopped schema updater");
        } finally {
            String message = "Update finished " + (result ? "successfully" : "with ERRORS");
            monitor.subTask(message);
            if (!result){
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
     * @return true if update was successful, false otherwise
     */
    private boolean updateHibernateSequence(ICdmDataSource datasource, IProgressMonitor monitor, CaseType caseType) {
        boolean result = true;
        monitor.subTask("Update hibernate sequences");
        try {
            String sql = "SELECT * FROM hibernate_sequences ";
            ResultSet rs = datasource.executeQuery(sql);
            while (rs.next()){
                String table = rs.getString("sequence_name");
                Integer val = rs.getInt("next_val");
                result &= updateSingleValue(datasource,monitor, table, val, caseType);
            }
        } catch (Exception e) {
            String message = "Exception occurred when trying to update hibernate_sequences table: " + e.getMessage();
            monitor.warning(message, e);
            logger.error(message);
            result = false;
        }finally{
            monitor.worked(1);
        }
        return result;
    }

    /**
     *
     * @param datasource
     * @param monitor
     * @param table
     * @param oldVal
     * @param caseType
     * @return
     */
    private boolean updateSingleValue(ICdmDataSource datasource, IProgressMonitor monitor, String table, Integer oldVal, CaseType caseType){
        if (table.equals("default")){  //found in flora central africa test database
            return true;
        }
        try {
            Integer newVal;
            try {
                String sql = " SELECT max(id) FROM %s ";
                newVal = (Integer)datasource.getSingleValue(String.format(sql, caseType.transformTo(table)));
            } catch (Exception e) {
                String message = "Could not retrieve max value for table '%s'. Will not update hibernate_sequence for this table. " +
                        "Usually this will not cause problems, however, if new data has been added to " +
                        "this table by the update script one may encounter 'unique identifier' " +
                        "exceptions when trying to add further data.";
                monitor.warning(String.format(message,table), e);
                //TODO
                return true;
            }

            if (newVal != null){
                //This is how {@link PooledOptimizer#generate(org.hibernate.id.enhanced.AccessCallback)} works
                //it substracts the increment size from the value in hibernate_sequences to get the initial value.
                //Haven't checked why.
                //For the correct increment size see eu.etaxonomy.cdm.model.common.package-info.java
                int incrementSize = 10;
                newVal = newVal + incrementSize;
                if (newVal != null && newVal >= oldVal){
                    String sql = " UPDATE hibernate_sequences " +
                            " SET next_val = %d " +
                            " WHERE sequence_name = '%s' ";
                    datasource.executeUpdate(String.format(sql, newVal + 1 , table) );
                }
            }
            return true;
        } catch (Exception e) {
            String message = "Exception occurred when trying to read or update hibernate_sequences table for value " + table + ": " + e.getMessage();
            monitor.warning(message, e);
            logger.error(message);
            return false;
        }

    }


    private ITermUpdater getCurrentTermUpdater() {
        return TermUpdater_31_33.NewInstance();
    }

    /**
     * Returns the current CDM updater
     * @return
     */
    private ISchemaUpdater getCurrentSchemaUpdater() {
        return SchemaUpdater_33_331.NewInstance();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
//        logger.warn("main method not yet fully implemented (only works with mysql!!!)");
//        if(args.length < 2){
//            logger.error("Arguments missing: server database [username [password]]");
//        }
        //TODO better implementation
        CdmUpdater myUpdater = new CdmUpdater();
        String server = args[0];
        String database  = args[1];
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

        ICdmDataSource dataSource = CdmDataSource.NewMySqlInstance(server, database, 3306, username, password, null);
        boolean success = myUpdater.updateToCurrentVersion(dataSource, null);
        System.out.println("DONE " + (success ? "successfully" : "with ERRORS"));
    }

}
