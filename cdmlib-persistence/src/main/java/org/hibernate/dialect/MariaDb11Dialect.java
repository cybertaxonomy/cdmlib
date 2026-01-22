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
 * Hibernate Dialect for mariadb. For now, this is mostly of {@link MySQL5MyISAMUtf8Dialect}
 * but using {@link MariaDB103Dialect} as base class.
 *
 * @author muellera
 * @since 14.01.2026
 */
public class MariaDb11Dialect extends MariaDB103Dialect {

    private MySqlIsamIndexExporter mySqlIsamIndexExporter = new MySqlIsamIndexExporter( this );

    public MariaDb11Dialect() {

    }

    //works only starting with hibernate 5.6+
//    @Override
//    protected boolean supportsReservedKeywordQuerying() {
//        return false;
//    }

    //workaround
    @Override
    public IdentifierHelper buildIdentifierHelper(
            IdentifierHelperBuilder builder,
            DatabaseMetaData dbMetaData) throws SQLException {

        // Verhindert die Abfrage der RESERVED-Spalte
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
