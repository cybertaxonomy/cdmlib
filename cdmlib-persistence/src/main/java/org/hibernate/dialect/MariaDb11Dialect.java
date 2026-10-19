/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.hibernate.dialect;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.hibernate.engine.jdbc.env.spi.IdentifierHelper;
import org.hibernate.engine.jdbc.env.spi.IdentifierHelperBuilder;
import org.hibernate.mapping.Index;
import org.hibernate.tool.schema.spi.Exporter;

/**
 * Hibernate Dialect for MariaDB. Same MyISAM/UTF-8 behaviour as
 * {@link MySQL5MyISAMUtf8Dialect}, based on {@link MariaDBDialect}
 * (replaces removed {@code MariaDB103Dialect} in Hibernate 6.6+).
 *
 * @author muellera
 * @since 14.01.2026
 */
public class MariaDb11Dialect extends MariaDBDialect {

    private final MySqlIsamIndexExporter mySqlIsamIndexExporter = new MySqlIsamIndexExporter(this);

    public MariaDb11Dialect() {
        super(DatabaseVersion.make(11, 0));
    }

    @Override
    public IdentifierHelper buildIdentifierHelper(
            IdentifierHelperBuilder builder,
            DatabaseMetaData dbMetaData) throws SQLException {

        // Avoid querying the RESERVED column from DatabaseMetaData
        builder.setAutoQuoteKeywords(false);

        return super.buildIdentifierHelper(builder, dbMetaData);
    }

//*********************** copied from MySQL5MyISAMUtf8Dialect *************/

    @Override
    public String getTableTypeString() {
        return " ENGINE=MYISAM DEFAULT CHARSET=utf8";
    }

    @Override
    public Exporter<Index> getIndexExporter() {
        return mySqlIsamIndexExporter;
    }

    // compare org.hibernate.dialect.MySQLMyISAMDialect
    @Override
    public boolean dropConstraints() {
        return false;
    }
// ********************** end copied from ... ***********************/

}
