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
import java.util.Map;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.model.relational.QualifiedNameImpl;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Index;
import org.hibernate.tool.schema.internal.StandardIndexExporter;

/**
 * This index exporter extends the {@link StandardIndexExporter}
 * by using a maximum prefix length of 255 for text based indexes.
 * <p>
 * Needed for MyISAM (max key length 1000 bytes) when indexing
 * {@code VARCHAR} columns longer than that allow (e.g. {@code titleCache}
 * with length 800). See #9058.
 * <p>
 * Since Hibernate 5.6 schema export calls
 * {@link #getSqlCreateStrings(Index, Metadata, SqlStringGenerationContext)}
 * instead of the deprecated 2-argument variant; this class overrides the
 * 3-argument method accordingly.
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
     * Based on Hibernate 5.6 StandardIndexExporter. Changes marked as such.
     */
    @Override
    public String[] getSqlCreateStrings(Index index, Metadata metadata, SqlStringGenerationContext context) {
        final JdbcEnvironment jdbcEnvironment = metadata.getDatabase().getJdbcEnvironment();
        final String tableName = context.format( index.getTable().getQualifiedTableName() );

        final String indexNameForCreation;
        if ( dialect.qualifyIndexName() ) {
            indexNameForCreation = context.format(
                    new QualifiedNameImpl(
                            index.getTable().getQualifiedTableName().getCatalogName(),
                            index.getTable().getQualifiedTableName().getSchemaName(),
                            jdbcEnvironment.getIdentifierHelper().toIdentifier( index.getQuotedName( dialect ) )
                    )
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
        final Iterator<Column> columnItr = index.getColumnIterator();
        final Map<Column, String> columnOrderMap = index.getColumnOrderMap();
        while ( columnItr.hasNext() ) {
            final Column column = columnItr.next();
            if ( first ) {
                first = false;
            }
            else {
                buf.append( ", " );
            }
            //*** CHANGED *******/
            // Prefix long varchar indexes so MyISAM's 1000-byte key limit is not exceeded
            // (utf8: 255*3=765; titleCache is mapped with length 800).
            // Note: column.getLength() may report 255 even for longer mapped columns.
            String length = column.getLength() > 254 ? "(255)" : "";
            buf.append( column.getQuotedName( dialect ) + length );
            //*** DEGNAHC *******/
            if ( columnOrderMap.containsKey( column ) ) {
                buf.append( " " ).append( columnOrderMap.get( column ) );
            }
        }
        buf.append( ")" );
        return new String[] { buf.toString() };
    }

    @Override
    public String[] getSqlDropStrings(Index index, Metadata metadata, SqlStringGenerationContext context) {
        if ( !dialect.dropConstraints() ) {
            return NO_COMMANDS;
        }

        final String tableName = context.format( index.getTable().getQualifiedTableName() );

        final String indexNameForCreation;
        if ( dialect.qualifyIndexName() ) {
            indexNameForCreation = StringHelper.qualify( tableName, index.getName() );
        }
        else {
            indexNameForCreation = index.getName();
        }

        return new String[] { "drop index " + indexNameForCreation };
    }
}
