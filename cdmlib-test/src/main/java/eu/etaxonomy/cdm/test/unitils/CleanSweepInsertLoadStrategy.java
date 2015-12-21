package eu.etaxonomy.cdm.test.unitils;

import static org.unitils.thirdparty.org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.unitils.dbunit.datasetfactory.impl.MultiSchemaXmlDataSetFactory;
import org.unitils.dbunit.datasetloadstrategy.impl.CleanInsertLoadStrategy;
import org.unitils.dbunit.util.DbUnitDatabaseConnection;
import org.unitils.dbunit.util.MultiSchemaDataSet;
import org.unitils.thirdparty.org.apache.commons.io.IOUtils;
import org.unitils.util.FileUtils;

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
        URL fileUrl = null;
        try {
            MultiSchemaXmlDataSetFactory dataSetFactory = new MultiSchemaXmlDataSetFactory();
            fileUrl = getClass().getClassLoader().getResource(clearDataResource);
            if (fileUrl == null) {
                throw new IOException("the Resource " + clearDataResource + " could not be found");
            }
            File file;
            logger.debug("fileUrl:" + fileUrl.toString() + "; protocol: " +  fileUrl.getProtocol());
            if(fileUrl.toString().startsWith("jar:file:")){
                // extract file from jar into tmp folder
                String millisecTimeStamp = String.valueOf(System.currentTimeMillis());
                file = copyClassPathResource(fileUrl, System.getProperty("java.io.tmpdir") + File.separator + millisecTimeStamp);
            } else {
                file = new File(fileUrl.toURI());
            }
            multiSchemaDataset = dataSetFactory.createDataSet(file);
        } catch (Exception e) {
            logger.error("unable to load the clearing dataset as resource from " + fileUrl.toString(), e);
        }

        if(multiSchemaDataset != null){
               for (String name : multiSchemaDataset.getSchemaNames()) {
                IDataSet clearDataSet = multiSchemaDataset.getDataSetForSchema(name);
                logger.debug("doing CLEAN_INSERT with dataset '" + name + "'");
                DatabaseOperation.CLEAN_INSERT.execute(dbUnitDatabaseConnection, clearDataSet);
            }
        }

        super.doExecute(dbUnitDatabaseConnection, dataSet);

        // DEBUGGING the resulting database
//        try {
//            OutputStream out;
//            out = new FileOutputStream("CleanSweepInsertLoadStrategy-debug.xml");
////			printDataSet(dbUnitDatabaseConnection, out, (String[]) null);
//            printDataSet(dbUnitDatabaseConnection, out, new String[]{"TAXONNODE", "CLASSIFICATION", "CLASSIFICATION_TAXONNODE"});
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }


    /**
     * more or less 1:1 copy from {@link FileUtils#copyClassPathResource(String, String)}
     *
     * @param classPathResourceName
     * @param fileSystemDirectoryName
     * @return
     * @throws IOException
     */
    public File copyClassPathResource(URL resourceURL, String fileSystemDirectoryName) throws IOException {

        InputStream resourceInputStream = null;
        OutputStream fileOutputStream = null;
        File file = null;
        try {
            resourceInputStream = resourceURL.openStream();
            String fileName = StringUtils.substringAfterLast(resourceURL.getPath(), "/");
            File fileSystemDirectory = new File(fileSystemDirectoryName);
            fileSystemDirectory.mkdirs();
            String filePath = fileSystemDirectoryName + "/" + fileName;
            fileOutputStream = new FileOutputStream(filePath);
            IOUtils.copy(resourceInputStream, fileOutputStream);
            file = new File(filePath);
            if(!file.canRead()){
                throw new IOException("tmp file " + file.toString() + " not readable.");
            }
        } catch (IOException e) {
            throw e;
        } finally {
            closeQuietly(resourceInputStream);
            closeQuietly(fileOutputStream);
        }
        return file;
    }

    @SuppressWarnings("unused")
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
