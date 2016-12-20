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
 *  This is a copy of the H2CorrectedDialect in cdmlib-persistence which is needed
 *  by the DdlCreator. Once H2CorrectedDialect is removed from cdmlib-persistence
 *  we can also remove this class.
 */
public class H2CorrectedDialectTest extends H2Dialect {

	 public H2CorrectedDialectTest() {
	        super();
	        registerColumnType(Types.FLOAT, "double"); //do we really want this


	 }
}
