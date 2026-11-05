/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.integration.spring;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.filter.ITableFilterSimple;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.h2.tools.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.database.DataBaseTablePrinter;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * JUnit 5 / Spring Test base for CDM integration tests (Unitils {@code CdmIntegrationTest} replacement).
 */
@ExtendWith({SpringExtension.class, CdmDbUnitExtension.class})
@ContextConfiguration("classpath:/eu/etaxonomy/cdm/applicationContext-springTest.xml")
public abstract class CdmSpringIntegrationTest {

    private static final String PROPERTY_H2_SERVER = "h2Server";
    private static final String H2_SERVER_RUNNING = "h2ServerIsRunning";

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired
    private DataBaseTablePrinter dbTablePrinter;

    protected DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();

    @BeforeEach
    public void startH2Server() throws Exception {
        if (System.getProperty(PROPERTY_H2_SERVER) != null && System.getProperty(H2_SERVER_RUNNING) == null) {
            try {
                List<String> args = new ArrayList<>();
                Integer port = null;
                try {
                    port = Integer.parseInt(System.getProperty(PROPERTY_H2_SERVER));
                    args.add("-webPort");
                    args.add(port.toString());
                } catch (Exception e) {
                    // default port 8082
                }
                Server.createWebServer(args.toArray(new String[]{})).start();
                System.setProperty(H2_SERVER_RUNNING, "true");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected IDatabaseConnection getConnection() {
        try {
            IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection(), "PUBLIC");
            DatabaseConfig config = connection.getConfig();
            config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
            config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);
            return connection;
        } catch (Exception e) {
            throw new IllegalStateException("Could not create DBUnit connection", e);
        }
    }

    public void printDataSet(OutputStream out) {
        dbTablePrinter.printDataSet(out);
    }

    public void printDataSet(OutputStream out, String... includeTableNames) {
        dbTablePrinter.printDataSet(out, includeTableNames);
    }

    public void printDataSet(OutputStream out, ITableFilterSimple filter) {
        dbTablePrinter.printDataSet(out, filter);
    }

    public void printDataSet(OutputStream out, Boolean excludeTermLoadingTables,
            ITableFilterSimple excludeFilter, String[] includeTableNames) {
        dbTablePrinter.printDataSet(out, excludeTermLoadingTables, excludeFilter, includeTableNames, false);
    }

    public void printDataSetWithNull(OutputStream out) {
        dbTablePrinter.printDataSetWithNull(out);
    }

    public void printDataSetWithNull(OutputStream out, String[] includeTableNames) {
        dbTablePrinter.printDataSetWithNull(out, includeTableNames);
    }

    public void printDataSetWithNull(OutputStream out, Boolean excludeTermLoadingTables,
            ITableFilterSimple excludeFilter, String[] includeTableNames) {
        dbTablePrinter.printDataSetWithNull(out, excludeTermLoadingTables, excludeFilter, includeTableNames);
    }

    public void printDtd(OutputStream out) {
        dbTablePrinter.printDtd(out);
    }

    public void writeDbUnitDataSetFile(String[] includeTableNames) throws FileNotFoundException {
        dbTablePrinter.writeDbUnitDataSetFile(includeTableNames, this.getClass());
    }

    public void writeDbUnitDataSetFile(String[] includeTableNames, String fileAppendix, boolean withNullValues)
            throws FileNotFoundException {
        dbTablePrinter.writeDbUnitDataSetFile(includeTableNames, this.getClass(), fileAppendix, withNullValues);
    }

    protected String transformSourceToString(Source source) throws TransformerException {
        return dbTablePrinter.transformSourceToString(source);
    }

    protected <T extends CdmBase> T getEntityFromCollection(Collection<T> cdmBases, UUID uuid) {
        for (T cdmBase : cdmBases) {
            if (cdmBase.getUuid().equals(uuid)) {
                return cdmBase;
            }
        }
        return null;
    }

    protected boolean existsInCollection(Collection<? extends CdmBase> cdmBases, UUID uuid) {
        return getEntityFromCollection(cdmBases, uuid) != null;
    }

    /**
     * Optional hook to regenerate fixtures; override when needed.
     */
    public void createTestDataSet() throws FileNotFoundException {
        // default: no-op
    }
}
