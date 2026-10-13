/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.hibernate.dialect;

import org.hibernate.mapping.Index;
import org.hibernate.tool.schema.spi.Exporter;

/**
 * MyISAM + UTF-8 dialect. Extends {@link MySQLDialect} (Hibernate 6) instead of the
 * legacy {@code MySQL5Dialect}, which reports MySQL 5.0 and triggers HHH000511.
 *
 * @author a.mueller
 */
public class MySQL5MyISAMUtf8Dialect extends MySQLDialect {

    private MySqlIsamIndexExporter mySqlIsamIndexExporter = new MySqlIsamIndexExporter( this );


	public MySQL5MyISAMUtf8Dialect(){
		super();
	}

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

}
