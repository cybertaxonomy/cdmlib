/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.hibernate.dialect;

import java.util.Iterator;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.QualifiedNameImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Index;
import org.hibernate.tool.schema.internal.StandardIndexExporter;

/**
 * This index exporter extends the {@link StandardIndexExporter}
 * by using a maximum size of 255 for text based indexes.
 *
 * @author a.mueller
 * @since 09.06.2020
 */
public class MySqlIsamIndexExporter extends StandardIndexExporter {

    private final Dialect dialect;

    public MySqlIsamIndexExporter(Dialect dialect) {
        super(dialect);
        this.dialect = dialect;
    }

    /*
     * This is a copy from StandardIndexExported. The changes
     * are marked as such
     */
    @Override
    public String[] getSqlCreateStrings(Index index, Metadata metadata) {
        final JdbcEnvironment jdbcEnvironment = metadata.getDatabase().getJdbcEnvironment();
        final String tableName = jdbcEnvironment.getQualifiedObjectNameFormatter().format(
                index.getTable().getQualifiedTableName(),
                dialect
        );

        final String indexNameForCreation;
        if ( dialect.qualifyIndexName() ) {
            indexNameForCreation = jdbcEnvironment.getQualifiedObjectNameFormatter().format(
                    new QualifiedNameImpl(
                            index.getTable().getQualifiedTableName().getCatalogName(),
                            index.getTable().getQualifiedTableName().getSchemaName(),
                            jdbcEnvironment.getIdentifierHelper().toIdentifier( index.getName() )
                    ),
                    jdbcEnvironment.getDialect()
            );
        }
        else {
            indexNameForCreation = index.getName();
        }
        final StringBuilder buf = new StringBuilder()
                .append( "create index " )
                .append( indexNameForCreation )
                .append( " on " )
                .append( tableName )
                .append( " (" );

        boolean first = true;
        int colCount = index.getColumnSpan();
        Iterator<Column> columnItr = index.getColumnIterator();
        while ( columnItr.hasNext() ) {
            final Column column = columnItr.next();
            if ( first ) {
                first = false;
            }
            else {
                buf.append( ", " );
            }
            //*** CHANGED *******/
            String length = column.getLength()>254?"(255)":"";  //for some reason column.getLength() return 255 even if the column is defined with length 800
            buf.append( ( column.getQuotedName( dialect )+length) );
            //*** DEGNAHC *******/
        }
        buf.append( ")" );
        return new String[] { buf.toString() };
    }
}
