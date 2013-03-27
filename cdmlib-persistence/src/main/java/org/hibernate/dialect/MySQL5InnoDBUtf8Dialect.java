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

import org.hibernate.dialect.MySQL5InnoDBDialect;

/**
 *  Extends MySQL5InnoDBDialect and sets the default charset to be UTF-8
 *	@author a.mueller
 *  /TODO licence
 */
public class MySQL5InnoDBUtf8Dialect extends MySQL5InnoDBDialect {

    
	public MySQL5InnoDBUtf8Dialect(){
		super();
		registerColumnType(Types.BOOLEAN, "bit");
	}
	
	public String getTableTypeString() {
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8";
    }
    
}