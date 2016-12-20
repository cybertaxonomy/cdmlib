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
 *  Extends H2Dialect and registers Types.BIT for boolean and Types.FLOAT as double.
 *  This is a work around for a known bug in the H2Dialect
 *  
 *  @see http://opensource.atlassian.com/projects/hibernate/browse/HHH-1598
 *	@author a.mueller
 */
public class HSQLCorrectedDialect extends HSQLDialect {

	 public HSQLCorrectedDialect() {
	        super();
	        registerColumnType(Types.BIT, "boolean");
	        registerColumnType(Types.FLOAT, "double");
	        
	        
	 }

    
}
