/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package org.hibernate.dialect;



/**
 *  Extends MySQL5InnoDBDialect and sets the default charset to be UTF-8
 *	@author a.mueller
 *  /TODO licence
 */
public class MySQL5MyISAMUtf8Dialect extends MySQL5Dialect {

    public String getTableTypeString() {
        return " ENGINE=MYISAM DEFAULT CHARSET=utf8";
    }
    
    
	// compare org.hibernate.dialect.MySQLMyISAMDialect
    /* (non-Javadoc)
	 * @see org.hibernate.dialect.Dialect#dropConstraints()
	 */
	public boolean dropConstraints() {
		return false;
	}
	
}