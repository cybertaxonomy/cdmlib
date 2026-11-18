/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.integration.spring;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableMetaData;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.opentest4j.AssertionFailedError;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * DBUnit helpers for Spring-based CDM integration tests.
 */
public final class CdmDbUnitSupport {

    public static final String CLEAR_DB_DATASET = "eu/etaxonomy/cdm/database/ClearDBDataSet.xml";
    public static final String SCHEMA = "PUBLIC";

    private CdmDbUnitSupport() {
    }

    public static IDatabaseConnection connection(DataSource dataSource) throws Exception {
        Connection jdbc = dataSource.getConnection();
        IDatabaseConnection connection = new DatabaseConnection(jdbc, SCHEMA);
        DatabaseConfig config = connection.getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
        config.setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, false);
        // CDM FlatXml fixtures often use empty attribute values
        config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);
        return connection;
    }

    public static IDataSet loadFlatXml(Resource resource) throws Exception {
        try (InputStream in = resource.getInputStream()) {
            FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
            builder.setColumnSensing(true);
            IDataSet dataSet = builder.build(in);
            ReplacementDataSet replacement = new ReplacementDataSet(dataSet);
            replacement.addReplacementObject("[null]", null);
            return replacement;
        }
    }

    public static void cleanInsert(DataSource dataSource, Resource... datasets) throws Exception {
        execute(dataSource, DatabaseOperation.CLEAN_INSERT, datasets);
    }

    public static void refresh(DataSource dataSource, Resource... datasets) throws Exception {
        execute(dataSource, DatabaseOperation.REFRESH, datasets);
    }

    private static void execute(DataSource dataSource, DatabaseOperation operation, Resource... datasets)
            throws Exception {
        IDatabaseConnection connection = connection(dataSource);
        try {
            // FlatXml fixture order is not always FK-safe. Leave RI off (Unitils dropped FKs;
            // spring test JDBC URL also sets REFERENTIAL_INTEGRITY=FALSE).
            java.sql.Connection jdbc = connection.getConnection();
            jdbc.createStatement().execute("SET REFERENTIAL_INTEGRITY FALSE");
            // Old Jailer fixtures omit columns that are NOT NULL in current Hibernate DDL
            // (e.g. DescriptionBase.types, TaxonNode.status). Defaults match entity defaults.
            applyFixtureColumnDefaults(jdbc);
            for (Resource dataset : datasets) {
                try {
                    operation.execute(connection, loadFlatXml(dataset));
                } catch (Exception ex) {
                    String url;
                    try {
                        url = connection.getConnection().getMetaData().getURL();
                    } catch (Exception ignored) {
                        url = "<unknown>";
                    }
                    Throwable root = ex;
                    while (root.getCause() != null) {
                        root = root.getCause();
                    }
                    throw new IllegalStateException(
                            "DBUnit " + operation + " failed for " + dataset
                                    + " on " + url + " — " + root.getMessage(),
                            ex);
                }
            }
        } finally {
            closeQuietly(connection);
        }
    }

    public static void cleanSweepInsert(DataSource dataSource, Resource... datasets) throws Exception {
        Resource[] all = new Resource[datasets.length + 1];
        all[0] = new ClassPathResource(CLEAR_DB_DATASET);
        System.arraycopy(datasets, 0, all, 1, datasets.length);
        cleanInsert(dataSource, all);
    }

    public static void assertEquals(DataSource dataSource, Resource expected) throws Exception {
        IDatabaseConnection connection = connection(dataSource);
        try {
            IDataSet expectedDataSet = loadFlatXml(expected);
            IDataSet actualDataSet = connection.createDataSet(expectedDataSet.getTableNames());
            // Unitils ExpectedDataSet semantics (subset match):
            // - only tables/columns from the expected FlatXml
            // - each expected row must match some actual row on non-null expected cells
            // - extra actual rows are ignored ("other records are not taken into account")
            // - omitted XML attributes → null → ignored for that cell
            for (String tableName : expectedDataSet.getTableNames()) {
                ITable expectedTable = expectedDataSet.getTable(tableName);
                ITable actualTable = actualDataSet.getTable(tableName);
                ITable filteredActual = DefaultColumnFilter.includedColumnsTable(
                        actualTable, expectedTable.getTableMetaData().getColumns());
                assertExpectedRowsPresent(expectedTable, filteredActual);
            }
        } finally {
            closeQuietly(connection);
        }
    }

    private static void assertExpectedRowsPresent(ITable expected, ITable actual) throws Exception {
        ITableMetaData meta = expected.getTableMetaData();
        // Unitils: empty expected table ⇒ actual table must be empty
        if (expected.getRowCount() == 0) {
            if (actual.getRowCount() != 0) {
                throw new AssertionFailedError(
                        "Expected table to be empty but found " + actual.getRowCount()
                                + " rows for table " + meta.getTableName());
            }
            return;
        }
        Column[] columns = meta.getColumns();
        for (int expectedRow = 0; expectedRow < expected.getRowCount(); expectedRow++) {
            if (findMatchingActualRow(expected, expectedRow, actual, columns) >= 0) {
                continue;
            }
            StringBuilder wanted = new StringBuilder();
            for (Column column : columns) {
                Object value = expected.getValue(expectedRow, column.getColumnName());
                if (value != null) {
                    if (wanted.length() > 0) {
                        wanted.append(", ");
                    }
                    wanted.append(column.getColumnName()).append('=').append(value);
                }
            }
            throw new AssertionFailedError(
                    "Missing expected row in table " + meta.getTableName()
                            + " (expected row " + expectedRow + "): " + wanted);
        }
    }

    private static int findMatchingActualRow(ITable expected, int expectedRow, ITable actual,
            Column[] columns) throws Exception {
        for (int actualRow = 0; actualRow < actual.getRowCount(); actualRow++) {
            if (rowMatches(expected, expectedRow, actual, actualRow, columns)) {
                return actualRow;
            }
        }
        return -1;
    }

    private static boolean rowMatches(ITable expected, int expectedRow, ITable actual, int actualRow,
            Column[] columns) throws Exception {
        for (Column column : columns) {
            Object expectedValue = expected.getValue(expectedRow, column.getColumnName());
            if (expectedValue == null) {
                continue;
            }
            Object actualValue = actual.getValue(actualRow, column.getColumnName());
            if (expectedValue.equals(actualValue)) {
                continue;
            }
            if (String.valueOf(expectedValue).equals(String.valueOf(actualValue))) {
                continue;
            }
            return false;
        }
        return true;
    }

    private static void applyFixtureColumnDefaults(Connection jdbc) {
        executeQuietly(jdbc, "ALTER TABLE DESCRIPTIONBASE ALTER COLUMN TYPES SET DEFAULT '#'");
        executeQuietly(jdbc, "ALTER TABLE TAXONNODE ALTER COLUMN STATUS SET DEFAULT 'INC'");
        // Jailer/Unitils fixtures often use bare "<LanguageString id='…'/>" (or similar)
        // only to include a table in CLEAN_INSERT. Current Hibernate DDL has UUID NOT NULL.
        relaxUuidNotNull(jdbc);
    }

    private static void relaxUuidNotNull(Connection jdbc) {
        try {
            try (java.sql.ResultSet rs = jdbc.getMetaData().getColumns(null, SCHEMA, "%", "UUID")) {
                while (rs.next()) {
                    if (rs.getInt("NULLABLE") == java.sql.DatabaseMetaData.columnNoNulls) {
                        String table = rs.getString("TABLE_NAME");
                        executeQuietly(jdbc, "ALTER TABLE " + table + " ALTER COLUMN UUID SET NULL");
                    }
                }
            }
        } catch (SQLException ignored) {
            // ignore
        }
    }

    private static void executeQuietly(Connection jdbc, String sql) {
        try {
            jdbc.createStatement().execute(sql);
        } catch (SQLException ignored) {
            // table/column may not exist in this schema
        }
    }

    private static void closeQuietly(IDatabaseConnection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
            // ignore
        }
    }
}
