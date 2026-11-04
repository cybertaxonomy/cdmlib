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

import org.hibernate.type.SqlTypes;

/**
 *  Extends H2Dialect and registers Types.BIT for boolean and Types.FLOAT as double.
 *  This is a work around for a known bug in the H2Dialect
 *
 *  Also adapts Hibernate 6's H2Dialect (targets H2 &gt;= 2.1) to the project's H2 1.4.x:
 *  no native TIMESTAMP/TIME WITH TIME ZONE support.
 *
 *  @see http://opensource.atlassian.com/projects/hibernate/browse/HHH-1598
 *	@author a.mueller
 */
public class H2CorrectedDialect extends H2Dialect {

	@Override
	protected String columnType(int sqlTypeCode) {
		if (sqlTypeCode == Types.FLOAT) {
			return "double"; //do we really want this
		}
		// H2 1.4.x cannot parse "timestamp($p) with time zone" / "time($p) with time zone"
		if (sqlTypeCode == SqlTypes.TIMESTAMP_WITH_TIMEZONE
				|| sqlTypeCode == SqlTypes.TIMESTAMP_UTC) {
			return columnType(SqlTypes.TIMESTAMP);
		}
		if (sqlTypeCode == SqlTypes.TIME_WITH_TIMEZONE
				|| sqlTypeCode == SqlTypes.TIME_UTC) {
			return columnType(SqlTypes.TIME);
		}
		return super.columnType(sqlTypeCode);
	}

	@Override
	public TimeZoneSupport getTimeZoneSupport() {
		return TimeZoneSupport.NONE;
	}

	@Override
	public boolean supportsTemporalLiteralOffset() {
		return false;
	}

	@Override
	public boolean supportsTimeLiteralOffset() {
		return false;
	}
}
