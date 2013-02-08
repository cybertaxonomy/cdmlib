/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.integration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSet;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.filter.ExcludeTableFilter;
import org.dbunit.dataset.filter.ITableFilterSimple;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlWriter;
import org.dbunit.dataset.xml.XmlDataSetWriter;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.operation.DeleteAllOperation;
import org.dbunit.operation.DeleteOperation;
import org.h2.tools.Server;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.unitils.UnitilsJUnit4;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.dbunit.datasetfactory.impl.MultiSchemaXmlDataSetFactory;
import org.unitils.dbunit.util.MultiSchemaDataSet;
import org.unitils.dbunit.util.MultiSchemaXmlDataSetReader;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBean;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.database.PersistentTermInitializer;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.agent.AgentDaoImpl;
import eu.etaxonomy.cdm.test.unitils.FlatFullXmlWriter;

/**
 * Abstract base class for integration testing a spring / hibernate application using
 * the unitils testing framework and dbunit.
 * <p>
 * The database being used is configured in
 * the maven module specific unitils.properties file. By default the database is a H2 in memory data base.
 * <p>
 * The H2 can be monitored during the test execution if the system property <code>h2Server</code> is given as
 * java virtual machine argument:
 * <code>-Dh2Server</code>.
 * An internal h2 database application will be started and listens at port 8082.
 * The port to listen on can be specified by passing a second argument, e.g.: <code>-Dh2Server 8083</code>.
 *
 *
 * @see <a href="http://www.unitils.org">unitils home page</a>
 *
 * @author ben.clark
 * @author a.kohlbecker (2013)
 */
@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-test.xml")
public abstract class CdmIntegrationTest extends UnitilsJUnit4 {

    private static final String PROPERTY_H2_SERVER = "h2Server";

    protected static final Logger logger = Logger.getLogger(CdmIntegrationTest.class);

    /**
     * List of the tables which are initially being populated during term loading. {@link PersistentTermInitializer}
     */
    public static final String[] termLoadingTables = new String[]{
        "DEFINEDTERMBASE",
        "DEFINEDTERMBASE_AUD",
        "DEFINEDTERMBASE_CONTINENT",
        "DEFINEDTERMBASE_REPRESENTATION",
        "DEFINEDTERMBASE_REPRESENTATION_AUD",
        "HIBERNATE_SEQUENCES",
        "RELATIONSHIPTERMBASE_INVERSEREPRESENTATION",
        "RELATIONSHIPTERMBASE_INVERSEREPRESENTATION_AUD",
        "REPRESENTATION",
        "REPRESENTATION_AUD",
        "TERMVOCABULARY",
        "TERMVOCABULARY_AUD",
        "TERMVOCABULARY_REPRESENTATION",
        "TERMVOCABULARY_REPRESENTATION_AUD"};

//	@SpringBeanByType
//	private IAgentDao agentDao;
//
//	@Before
//	public void debugExistingUsers(){
//		StringBuilder agentstr = new StringBuilder();
//		for(AgentBase agent : agentDao.list(null, null)) {
//			agentstr.append(agent.getId()).append(", ");
//		}
//		System.err.println("####" +  agentstr);
//	}

    @Before
    public void startH2Server() throws Exception {

        if(System.getProperty(PROPERTY_H2_SERVER) != null){
            try {
                List<String> args = new ArrayList<String>();
                try {
                    Integer port = Integer.parseInt(System.getProperty(PROPERTY_H2_SERVER));
                    args.add("-webPort");
                    args.add(port.toString());
                } catch (Exception e) {
                    // will start at port 8082 by default
                }
                Server.createWebServer(args.toArray(new String[]{})).start();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @TestDataSource
    protected DataSource dataSource;

    private PlatformTransactionManager transactionManager;
    protected DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();

    protected IDatabaseConnection getConnection() throws SQLException {
        IDatabaseConnection connection = null;
        try {
            /// FIXME must use unitils.properties: database.schemaNames
            connection = new DatabaseConnection(dataSource.getConnection(), "PUBLIC");

            DatabaseConfig config = connection.getConfig();

            // FIXME must use unitils.properties: org.unitils.core.dbsupport.DbSupport.implClassName
            //       & database.dialect to find configured DataTypeFactory
            config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                    new H2DataTypeFactory());
        } catch (Exception e) {
            logger.error(e);
        }
        return connection;
    }

    /**
     * Prints the data set to an output stream, using the
     * {@link FlatXmlDataSet}.
     * <p>
     * <h2>NOTE: for compatibility with unitils 3.x you may
     * want to use the {@link #printDataSetWithNull(OutputStream)}
     * method instead.</h2>
     * <p>
     * Remember, if you've just called save() or
     * update(), the data isn't written to the database until the
     * transaction is committed, and that isn't until after the
     * method exits. Consequently, if you want to test writing to
     * the database, either use the {@literal @ExpectedDataSet}
     * annotation (that executes after the test is run), or use
     * {@link CdmTransactionalIntegrationTest}.
     *
     * @param out The OutputStream to write to.
     * @see FlatFullXmlWriter
     */
    public void printDataSet(OutputStream out) {
        IDatabaseConnection connection = null;

        try {
            connection = getConnection();
            IDataSet actualDataSet = connection.createDataSet();
            FlatXmlDataSet.write(actualDataSet, out);
        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException sqle) {
                logger.error(sqle);
            }
        }
    }

    /**
     * Prints the data set to an output stream, using the
     * {@link FlatFullXmlWriter}.
     * which is a variant of the {@link org.dbunit.dataset.xml.FlatXmlWriter}. It
     * inserts '[null]' place holders for null values but skipping them.
     * This was necessary to make this xml database export compatible to the
     * {@link MultiSchemaXmlDataSetReader} which is used in Unitils since version 3.x
     * <p>
     * Remember, if you've just called save() or
     * update(), the data isn't written to the database until the
     * transaction is committed, and that isn't until after the
     * method exits. Consequently, if you want to test writing to
     * the database, either use the {@literal @ExpectedDataSet}
     * annotation (that executes after the test is run), or use
     * {@link CdmTransactionalIntegrationTest}.
     *
     * @param out The OutputStream to write to.
     * @see FlatFullXmlWriter
     */
    public void printDataSetWithNull(OutputStream out) {
        printDataSetWithNull(out, null, null);
    }

    public void printDataSetWithNull(OutputStream out, Boolean excludeTermLoadingTables, ITableFilterSimple filter) {

        if(excludeTermLoadingTables != null && excludeTermLoadingTables.equals(true)){
            ExcludeTableFilter excludeTableFilter = new ExcludeTableFilter();

            for(String tname : termLoadingTables){
                excludeTableFilter.excludeTable(tname);
            }
            filter = excludeTableFilter;
        }

        if (filter == null){
            filter = new ExcludeTableFilter();
        }

        IDatabaseConnection connection = null;
        try {
            connection = getConnection();
            DatabaseDataSet dataSet = new DatabaseDataSet(connection, false, filter);

            FlatFullXmlWriter writer = new FlatFullXmlWriter(out);
            writer.write(dataSet);
        } catch (Exception e) {
            logger.error("Error on writing dataset:", e);
        } finally {
            try {
                connection.close();
            } catch (SQLException sqle) {
                logger.error(sqle);
            }
        }
    }

    /**
     *
     * @param out
     * @param formatString can be null, otherwise a format string like eg. "&lt; %1$s /&gt;" see also {@link String#format(String, Object...)}
     */
    public void printTableNames(OutputStream out, String formatString) {
        IDatabaseConnection connection = null;
        OutputStreamWriter writer = new OutputStreamWriter(out);

        try {
            connection = getConnection();
            IDataSet actualDataSet = connection.createDataSet();
            ITableIterator tableIterator = actualDataSet.iterator();
            String tableName = null;
            while(tableIterator.next()){
                tableName = tableIterator.getTable().getTableMetaData().getTableName();
                if(formatString != null){
                    tableName = String.format(formatString, tableName);
                }
                writer.append(tableName).append("\n");
            }
        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                writer.close();
            } catch (IOException ioe) {
                logger.error(ioe);
            }
            try {
                connection.close();
            } catch (SQLException sqle) {
                logger.error(sqle);
            }
        }
    }

    /**
     * Prints the named tables to an output stream, using dbunit's
     * {@link org.dbunit.dataset.xml.FlatXmlDataSet}.
     *
     * @see {@link #printDataSet(OutputStream)}
     * @param out
     * @param tableNames
     */
    public void printDataSet(OutputStream out, String[] tableNames) {
        IDatabaseConnection connection = null;

        if(tableNames == null){
            return;
        }

        try {
            connection = getConnection();
            IDataSet actualDataSet = connection.createDataSet(tableNames);
            FlatXmlDataSet.write(actualDataSet, out);

        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException sqle) {
                logger.error(sqle);
            }
        }
    }

    /**
     * Prints the named tables to an output stream, using dbunit's
     * {@link org.dbunit.dataset.xml.FlatXmlWriter}.
     *
     * @see {@link #printDataSet(OutputStream)}
     * @param out
     * @param filter
     */
    public void printDataSet(OutputStream out, ITableFilterSimple filter) {
        if (filter == null){
            filter = new ExcludeTableFilter();
        }

        IDatabaseConnection connection = null;

        try {
            connection = getConnection();
//            FlatXmlDataSet.write(actualDataSet, out);

            IDataSet dataSet = new DatabaseDataSet(connection, false, filter);

            FlatXmlWriter writer = new FlatXmlWriter(out);
            writer.write(dataSet);

        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException sqle) {
                logger.error(sqle);
            }
        }
    }


    /**
     * Prints a dtd to an output stream, using dbunit's
     * {@link org.dbunit.dataset.xml.FlatDtdDataSet}.
     *
     * @param out The OutputStream to write to.
     * @see org.dbunit.dataset.xml.FlatDtdDataSet
     */
    public void printDtd(OutputStream out) {
        IDatabaseConnection connection = null;

        try {
            connection = getConnection();
            IDataSet actualDataSet = connection.createDataSet();
            FlatDtdDataSet.write(actualDataSet, out);
        } catch (Exception e) {
            logger.error(e);
        } finally {
            try {
                connection.close();
            } catch (SQLException sqle) {
                logger.error(sqle);
            }
        }
    }

    /**
     * Transforms a javax.xml.transform.Source to a java.lang.String (useful for comparison in
     * XmlUnit tests etc).
     *
     * @param source
     * @return
     * @throws TransformerException
     */
    public String transformSourceToString(Source source) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Result result = new StreamResult(outputStream);
        transformer.transform(source, result);

        return new String(outputStream.toByteArray());
    }

    @SpringBeanByType
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }


    @Deprecated // no longer used and for sure not needed at all
    protected void loadDataSet(InputStream dataset) {
        txDefinition.setName("CdmIntergartionTest.loadDataSet");
        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
        IDatabaseConnection connection = null;

        try {
            connection = getConnection();
            IDataSet dataSet = new FlatXmlDataSet(new InputStreamReader(dataset));

            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
        } catch (Exception e) {
            logger.error(e);
            for(StackTraceElement ste : e.getStackTrace()) {
                logger.error(ste);
            }
        } finally {
            try {
                connection.close();
            } catch (SQLException sqle) {
                logger.error(sqle);
            }
        }
        transactionManager.commit(txStatus);
    }
}
