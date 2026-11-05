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

import org.dbunit.Assertion;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
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
        IDatabaseConnection connection = connection(dataSource);
        try {
            // FlatXml fixture order is not always FK-safe (Unitils disabled constraints too)
            connection.getConnection().createStatement().execute("SET REFERENTIAL_INTEGRITY FALSE");
            try {
                for (Resource dataset : datasets) {
                    DatabaseOperation.CLEAN_INSERT.execute(connection, loadFlatXml(dataset));
                }
            } finally {
                connection.getConnection().createStatement().execute("SET REFERENTIAL_INTEGRITY TRUE");
            }
        } finally {
            closeQuietly(connection);
        }
    }

    public static void cleanSweepInsert(DataSource dataSource, Resource dataset) throws Exception {
        cleanInsert(dataSource, new ClassPathResource(CLEAR_DB_DATASET), dataset);
    }

    public static void assertEquals(DataSource dataSource, Resource expected) throws Exception {
        IDatabaseConnection connection = connection(dataSource);
        try {
            IDataSet expectedDataSet = loadFlatXml(expected);
            IDataSet actualDataSet = connection.createDataSet(expectedDataSet.getTableNames());
            Assertion.assertEquals(expectedDataSet, actualDataSet);
        } finally {
            closeQuietly(connection);
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
