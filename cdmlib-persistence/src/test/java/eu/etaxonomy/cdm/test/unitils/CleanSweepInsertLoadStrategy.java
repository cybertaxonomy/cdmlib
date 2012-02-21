package eu.etaxonomy.cdm.test.unitils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.unitils.dbunit.datasetfactory.impl.MultiSchemaXmlDataSetFactory;
import org.unitils.dbunit.datasetloadstrategy.impl.CleanInsertLoadStrategy;
import org.unitils.dbunit.util.DbUnitDatabaseConnection;
import org.unitils.dbunit.util.MultiSchemaDataSet;

/**
 * will clear all data in the DataSet except all term related tables
   before doing a CLEAN_INSERT:
 * @author Andreas Kohlbecker
 *
 */
public class CleanSweepInsertLoadStrategy extends CleanInsertLoadStrategy {

	protected static final Logger logger = Logger.getLogger(CleanSweepInsertLoadStrategy.class);

	private static String clearDataResource = "eu/etaxonomy/cdm/database/ClearDBDataSet.xml";


	/**
     * Executes this DataSetLoadStrategy.
     * This means the given dataset is inserted in the database using the given dbUnit
     * database connection object.
     *
     * @param dbUnitDatabaseConnection DbUnit class providing access to the database
     * @param dataSet                  The dbunit dataset
     */
    @Override
    public void doExecute(DbUnitDatabaseConnection dbUnitDatabaseConnection, IDataSet dataSet) throws DatabaseUnitException, SQLException {

    	// will clear all data in the DataSet except all term related tables
    	// before doing a CLEAN_INSERT:
    	MultiSchemaDataSet multiSchemaDataset = null;
    	try {
			MultiSchemaXmlDataSetFactory dataSetFactory = new MultiSchemaXmlDataSetFactory();
			URL fileUrl = getClass().getClassLoader().getResource(clearDataResource);
			if (fileUrl == null) {
				throw new IOException("the Resource " + clearDataResource + " could not be found");
			}
			multiSchemaDataset = dataSetFactory.createDataSet(new File(fileUrl.toURI()));
		} catch (Exception e) {
			logger.error("unable to load the clearing dataset as resource", e);
		}

    	if(multiSchemaDataset != null){
	       	for (String name : multiSchemaDataset.getSchemaNames()) {
	    		IDataSet clearDataSet = multiSchemaDataset.getDataSetForSchema(name);
	    		DatabaseOperation.CLEAN_INSERT.execute(dbUnitDatabaseConnection, clearDataSet);
	    	}
    	}

    	super.doExecute(dbUnitDatabaseConnection, dataSet);

    	// DEBUGGING the resulting database
		try {
			OutputStream out;
			out = new FileOutputStream("CleanSweepInsertLoadStrategy-debug.xml");
			printDataSet(dbUnitDatabaseConnection, out, (String[]) null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }

    private void printDataSet(DbUnitDatabaseConnection dbUnitDatabaseConnection, OutputStream out, String ... tableNames) {

		try {
			IDataSet actualDataSet;
			if(tableNames == null){
				actualDataSet = dbUnitDatabaseConnection.createDataSet();
			} else {
				actualDataSet = dbUnitDatabaseConnection.createDataSet(tableNames);
			}
			FlatXmlDataSet.write(actualDataSet, out);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				dbUnitDatabaseConnection.close();
			} catch (SQLException sqle) {
				logger.error(sqle);
			}
		}
	}


}
