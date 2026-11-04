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
 * Copy of {@link H2CorrectedDialect} for {@code cdmlib-test} / {@code DdlCreator}
 * (avoids a hard dependency cycle on cdmlib-persistence).
 */
public class H2CorrectedDialectTest extends H2Dialect {

	@Override
	protected String columnType(int sqlTypeCode) {
		if (sqlTypeCode == Types.FLOAT) {
			return "double";
		}
		return super.columnType(sqlTypeCode);
	}
}
