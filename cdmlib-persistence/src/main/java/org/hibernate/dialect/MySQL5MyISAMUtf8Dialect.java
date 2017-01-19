/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package org.hibernate.dialect;

import java.sql.Types;



/**
 *  Extends MySQL5InnoDBDialect and sets the default charset to be UTF-8
 *	@author a.mueller
 *  /TODO licence
 */
public class MySQL5MyISAMUtf8Dialect extends MySQL5Dialect {

	public MySQL5MyISAMUtf8Dialect(){
		super();
		//see http://dev.mysql.com/doc/refman/5.0/en/numeric-type-overview.html
		registerColumnType(Types.BOOLEAN, "bit");
	}
	
    @Override
	public String getTableTypeString() {
        return " ENGINE=MYISAM DEFAULT CHARSET=utf8";
    }
    
    
	// compare org.hibernate.dialect.MySQLMyISAMDialect
    @Override
	public boolean dropConstraints() {
		return false;
	}
	
}
