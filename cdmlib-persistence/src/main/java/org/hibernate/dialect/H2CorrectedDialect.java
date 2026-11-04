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
 * Thin H2 dialect wrapper for CDM.
 * <p>
 * With H2 &gt;= 2.1 (compatible with Hibernate 6's {@link H2Dialect}), the former
 * TIMESTAMP/TIME WITH TIME ZONE remapping for H2 1.4 is no longer needed.
 * Keeps the historic FLOAT→{@ mapping used by CDM schemas.
 *
 * @see <a href="https://hibernate.atlassian.net/browse/HHH-1598">HHH-1598</a>
 * @author a.mueller
 */
public class H2CorrectedDialect extends H2Dialect {

	@Override
	protected String columnType(int sqlTypeCode) {
		if (sqlTypeCode == Types.FLOAT) {
			return "double";
		}
		return super.columnType(sqlTypeCode);
	}
}
