/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.integration;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.filter.ITableFilterSimple;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.h2.tools.Server;
import org.junit.Before;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.unitils.UnitilsJUnit4;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.dbunit.util.MultiSchemaXmlDataSetReader;
import org.unitils.orm.hibernate.annotation.HibernateSessionFactory;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.database.DataBaseTablePrinter;

/**
 * Abstract base class for integration testing a spring / hibernate application using
 * the unitils testing framework and dbunit.
 *
 * <h2>Data base server</h2>
 * The database being used is configured in
 * the maven module specific unitils.properties file. By default the database is a H2 in memory data base.
 * <p>
 * The H2 can be monitored during the test execution if the system property <code>h2Server</code> is given as
 * java virtual machine argument:
 * <code>-Dh2Server</code>.
 * An internal h2 database application will be started and listens at port 8082.
 * The port to listen on can be specified by passing a second argument, e.g.: <code>-Dh2Server 8083</code>.
 *
 * <h2>Creating create DbUnit dataset files</h2>
 * In order to create DbUnit datasets  for integration tests it is highly recommended method to use the
 * {@link #writeDbUnitDataSetFile(String[])} method.
 *
 * From {@link http://www.unitils.org/tutorial-database.html}, by default every test is executed in a transaction,
 * which is committed at the end of the test. This can be disabled using @Transactional(TransactionMode.DISABLED)
 *
 * @see <a href="http://www.unitils.org">unitils home page</a>
 *
 * @author ben.clark
 * @author a.kohlbecker (2013)
 */
@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-test.xml")
// @HibernateSessionFactory is only needed for test phases like afterTestTearDown
// which are configured to run without a spring application context.
// for further details, see /cdmlib-test/src/main/resources/eu/etaxonomy/cdm/hibernate-test.cfg.xml
@HibernateSessionFactory({"/eu/etaxonomy/cdm/hibernate.cfg.xml", "/eu/etaxonomy/cdm/hibernate-test.cfg.xml"})
public abstract class CdmIntegrationTest extends UnitilsJUnit4 {
    protected static final Logger logger = Logger.getLogger(CdmIntegrationTest.class);

    private static final String PROPERTY_H2_SERVER = "h2Server";
    private static final String H2_SERVER_RUNNING = "h2ServerIsRunning";

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
        "TERMBASE_INVERSEREPRESENTATION",
        "TERMBASE_INVERSEREPRESENTATION_AUD",
        "REPRESENTATION",
        "REPRESENTATION_AUD",
        "TERMVOCABULARY",
        "TERMVOCABULARY_AUD",
        "TERMVOCABULARY_REPRESENTATION",
        "TERMVOCABULARY_REPRESENTATION_AUD"};

    @TestDataSource
    protected DataSource dataSource;

    private PlatformTransactionManager transactionManager;

    @SpringBeanByType
    private DataBaseTablePrinter dbTablePrinter;

    protected DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();

    @SpringBeanByType
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }


    protected IDatabaseConnection getConnection() {
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

//  @SpringBeanByType
//  private IAgentDao agentDao;
//
//  @Before
//  public void debugExistingUsers(){
//      StringBuilder agentstr = new StringBuilder();
//      for(AgentBase agent : agentDao.list(null, null)) {
//          agentstr.append(agent.getId()).append(", ");
//      }
//      System.err.println("####" +  agentstr);
//  }

    /**
     * How to use:
     * <ol>
     * <li>Add <code>-Dh2Server</code> as jvm argument to activate.</li>
     * <li>uncomment <code>database.url= jdbc:h2:tcp://localhost/~/cdm</code> in <code>/cdmlib-test/src/main/resources/unitils.properties</code></li>
     * @throws Exception
     */
    @Before
    public void startH2Server() throws Exception {

        if(System.getProperty(PROPERTY_H2_SERVER) != null && System.getProperty(H2_SERVER_RUNNING) == null){
            try {
                // printing to System.out, so that developers get feedback always
                System.out.println("####################################################");
                System.out.println("  Starting h2 web server ...");
                List<String> args = new ArrayList<String>();
                Integer port = null;
                try {
                    port = Integer.parseInt(System.getProperty(PROPERTY_H2_SERVER));
                    args.add("-webPort");
                    args.add(port.toString());
                } catch (Exception e) {
                    // the default port is 8082
                }
                Server.createWebServer(args.toArray(new String[]{})).start();
                System.setProperty(H2_SERVER_RUNNING, "true");
                System.out.println("  you can connect to the h2 web server by opening");
                System.out.println("  http://localhost:" + (port != null ? port : "8082") + "/ in your browser");
                System.out.println("#####################################################");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
     * @see {@link DataBaseTablePrinter}
     * @param out The OutputStream to write to.
     * @see FlatFullXmlWriter
     */
    public void printDataSet(OutputStream out) {
        dbTablePrinter.printDataSet(out);
    }

    /**
     * Prints the data set to an output stream, using the
     * {@link FlatFullXmlWriter}.
     * which is a variant of the {@link org.dbunit.dataset.xml.FlatXmlWriter}. It
     * inserts '[null]' place holders for null values instead of skipping them.
     * This was necessary to make this xml database export compatible to the
     * {@link MultiSchemaXmlDataSetReader} which is used in Unitils since version 3.x
     * <p>
     * @see {@link DataBaseTablePrinter}
     * @param out out The OutputStream to write to.
     * @param includeTableNames
     */
    public void printDataSetWithNull(OutputStream out, String[] includeTableNames) {
        dbTablePrinter.printDataSetWithNull(out, includeTableNames);
    }

    /**
     * Prints the data set to an output stream, using the
     * {@link FlatFullXmlWriter}.
     * which is a variant of the {@link org.dbunit.dataset.xml.FlatXmlWriter}. It
     * inserts '[null]' place holders for null values instead of skipping them.
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
     *@see {@link DataBaseTablePrinter}
     * @param out The OutputStream to write to.
     * @see FlatFullXmlWriter
     */
    public void printDataSetWithNull(OutputStream out) {
        dbTablePrinter.printDataSetWithNull(out);
    }

    /**
     * @param out
     * @param excludeTermLoadingTables
     * @param excludeFilter the tables to be <em>excluded</em>
     */
    public void printDataSetWithNull(OutputStream out, Boolean excludeTermLoadingTables,
            ITableFilterSimple excludeFilterOrig, String[] includeTableNames) {
        dbTablePrinter.printDataSetWithNull(out, excludeTermLoadingTables, excludeFilterOrig, includeTableNames);
    }

    /**
     *
     * @param out
     * @param formatString can be null, otherwise a format string like eg. "&lt; %1$s /&gt;" see also {@link String#format(String, Object...)}
     */
    public void printTableNames(OutputStream out, String formatString) {
        dbTablePrinter.printTableNames(out, formatString);
    }

    /**
     * Prints the named tables to an output stream, using dbunit's
     * {@link org.dbunit.dataset.xml.FlatXmlDataSet}.
     * <p>
     * <h2>NOTE: for compatibility with unitils 3.x you may
     * want to use the {@link #printDataSetWithNull(OutputStream)}
     * method instead.</h2>
     *
     * <p>
     * Remember, if you've just called save() or
     * update(), the data isn't written to the database until the
     * transaction is committed, and that isn't until after the
     * method exits. Consequently, if you want to test writing to
     * the database, either use the {@literal @ExpectedDataSet}
     * annotation (that executes after the test is run), or use
     * {@link CdmTransactionalIntegrationTest}.
     *
     * @see {@link DataBaseTablePrinter}
     *
     * @see {@link #printDataSet(OutputStream)}
     * @see FlatFullXmlWriter
     *
     * @param out
     * 		the OutputStream to write the XML to
     * @param includeTableNames
     * 		the names of tables to print (should be in upper case letters)
     */
    public void printDataSet(OutputStream out, String[] includeTableNames) {
        dbTablePrinter.printDataSet(out, includeTableNames);

    }

    /**
     * Prints the named tables to an output stream, using dbunit's
     * {@link org.dbunit.dataset.xml.FlatXmlWriter}.
     * <p>
     * <h2>NOTE: for compatibility with unitils 3.x you may
     * want to use the {@link #printDataSetWithNull(OutputStream)}
     * method instead.</h2>
     *
     * <p>
     * Remember, if you've just called save() or
     * update(), the data isn't written to the database until the
     * transaction is committed, and that isn't until after the
     * method exits. Consequently, if you want to test writing to
     * the database, either use the {@literal @ExpectedDataSet}
     * annotation (that executes after the test is run), or use
     * {@link CdmTransactionalIntegrationTest}.
     *
     * @see {@link DataBaseTablePrinter}
     *
     * @see {@link #printDataSet(OutputStream)}
     * @see FlatFullXmlWriter
     * @param out
     * @param filter
     */
    public void printDataSet(OutputStream out, ITableFilterSimple filter) {
        dbTablePrinter.printDataSet(out, filter);
    }


    /**
     * Prints a dtd to an output stream, using dbunit's
     * {@link org.dbunit.dataset.xml.FlatDtdDataSet}.
     *
     * @param out The OutputStream to write to.
     * @see org.dbunit.dataset.xml.FlatDtdDataSet
     */
    public void printDtd(OutputStream out) {
        dbTablePrinter.printDtd(out);
    }

    /**
     * <b>WARNING</b> read this doc before using this method.
     * <p>
     * This is the recommended method to create DbUnit data set for integration tests.
     * This method will create the DbUnit data set file for the specific test class
     * it has been called from. This happens in full compliance with the DbUnit conventions,
     * so that the test class will immediately be able to use the
     * newly created file during the next run.
     * This also means that using <code>writeDbUnitDataSetFile()</code>
     * in a test class <b>will overwrite the existing data set file</b>. This is not
     * considered to be harmful since we expect that any development always is
     * backed up by a VCS like git, svn and therefore recovery of overwritten
     * data source files should not cause any problems.
     * <p>
     * Writes a  DbUnit dataset file from the current data base connection using the
     * {@link FlatFullXmlWriter} which is a variant of the
     * {@link org.dbunit.dataset.xml.FlatXmlWriter}. It
     * inserts <code>'[null]'</code> place holders for null values instead of skipping them.
     *
     * see {@link DataBaseTablePrinter}
     *
     * @param includeTableNames
     * @throws FileNotFoundException
     */
    public void writeDbUnitDataSetFile(String[] includeTableNames) throws FileNotFoundException {
        dbTablePrinter.writeDbUnitDataSetFile(includeTableNames, this.getClass());
    }

    /**
     *
     * Extension of method mentioned in "see also" where you can specify an appendix for the
     * generated DbUnit data set file.
     *
     * see {@link DataBaseTablePrinter}
     *
     * @param includeTableNames
     * @param fileAppendix the appendix of the generated DbUnit dataset file
     * @throws FileNotFoundException
     * @see #writeDbUnitDataSetFile(String[])
     */
    public void writeDbUnitDataSetFile(String[] includeTableNames, String fileAppendix) throws FileNotFoundException {
        dbTablePrinter.writeDbUnitDataSetFile(includeTableNames, this.getClass(), fileAppendix);
    }

    /**
     * see {@link DataBaseTablePrinter#transformSourceToString(Source)
     */
    protected String transformSourceToString(Source source) throws TransformerException {
        return dbTablePrinter.transformSourceToString(source);
    }


    /**
     * This is the common method to create test data xml files for integration tests.
     *
     * With {@link CdmTransactionalIntegrationTestExample#createTestDataSet} an example
     * implementation of this method is provided.
     *
     * @throws FileNotFoundException
     */
    public abstract void createTestDataSet() throws FileNotFoundException;
}
