/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.integration.spring;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * Applies the H2 DDL script once (replaces Unitils dbmaintainer schema update).
 * <p>
 * Also drops non-PK NOT NULL constraints (Unitils {@code disableValueConstraints}),
 * so FlatXml fixtures may omit columns that are NOT NULL in production DDL.
 */
public class CdmTestSchemaInitializer implements InitializingBean {

    private static final String SCHEMA = "PUBLIC";

    private DataSource dataSource;
    private Resource script;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setScript(Resource script) {
        this.script = script;
    }

    @Override
    public void afterPropertiesSet() {
        if (schemaAlreadyPresent()) {
            // Multiple Spring Test contexts may share the same H2 mem URL
            // (e.g. services vs services-security); skip re-applying DDL.
            return;
        }
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(script);
        populator.setContinueOnError(false);
        populator.setSeparator(";");
        DatabasePopulatorUtils.execute(populator, dataSource);
        disableNotNullConstraints();
    }

    private boolean schemaAlreadyPresent() {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                        "select count(*) from INFORMATION_SCHEMA.TABLES "
                                + "where TABLE_SCHEMA = '" + SCHEMA
                                + "' AND TABLE_NAME = 'AGENTBASE'")) {
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Unitils {@code H2DbSupport.disableValueConstraints()} equivalent for Spring tests.
     */
    private void disableNotNullConstraints() {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            List<String> tables = queryStrings(statement,
                    "select TABLE_NAME from INFORMATION_SCHEMA.TABLES "
                            + "where TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA = '" + SCHEMA + "'");
            for (String table : tables) {
                Set<String> primaryKeys = new HashSet<>(queryStrings(statement,
                        "select kcu.COLUMN_NAME from INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc "
                                + "join INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu "
                                + "on tc.CONSTRAINT_SCHEMA = kcu.CONSTRAINT_SCHEMA "
                                + "and tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME "
                                + "and tc.TABLE_SCHEMA = kcu.TABLE_SCHEMA "
                                + "and tc.TABLE_NAME = kcu.TABLE_NAME "
                                + "where tc.CONSTRAINT_TYPE = 'PRIMARY KEY' AND tc.TABLE_NAME = '"
                                + table + "' AND tc.TABLE_SCHEMA = '" + SCHEMA + "'"));
                List<String> notNullColumns = queryStrings(statement,
                        "select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS "
                                + "where IS_NULLABLE = 'NO' AND TABLE_NAME = '" + table
                                + "' AND TABLE_SCHEMA = '" + SCHEMA + "'");
                for (String column : notNullColumns) {
                    if (primaryKeys.contains(column)) {
                        continue;
                    }
                    statement.executeUpdate("alter table \"" + SCHEMA + "\".\"" + table
                            + "\" alter column \"" + column + "\" set null");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to disable NOT NULL constraints for CDM tests", e);
        }
    }

    private static List<String> queryStrings(Statement statement, String sql) throws SQLException {
        List<String> values = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                values.add(rs.getString(1));
            }
        }
        return values;
    }
}
