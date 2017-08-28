/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.hibernate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;
import org.jadira.usertype.dateandtime.shared.spi.AbstractUserType;
import org.joda.time.Partial;



/**
 * Persist {@link org.joda.time.Partial} via hibernate.
 * This is a preliminary implementation that fulfills the needs of CDM but does not fully store a Partial.
 * Only year, month and day is stored
 * @author a.mueller
 * @created 11.11.2008
 * @version 2.0
 */
public class PartialUserType extends AbstractUserType implements UserType /* extends AbstractSingleColumnUserType<Partial, String, ColumnMapper<Partial,String>> implements UserType */ {
	private static final long serialVersionUID = -5323104403077597869L;

	private static final Logger logger = Logger.getLogger(PartialUserType.class);

	//not required
	public final static PartialUserType INSTANCE = new PartialUserType();

	private static final int[] SQL_TYPES = new int[]{
	    Types.VARCHAR,
	};


	@Override
	public Temporal nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		String partial = (String)StandardBasicTypes.STRING.nullSafeGet(rs, names, session, owner);
		Temporal result = null;
		if (partial == null || partial.length() != 8) {
			return null;
		}
		Integer year = Integer.valueOf(partial.substring(0,4));
		Integer month = Integer.valueOf(partial.substring(4,6));
		Integer day = Integer.valueOf(partial.substring(6,8));

		if (year != 0){
			result = Year.of(year);
		}
		if (month != 0){
			result = YearMonth.of(year, month);
		}
		if (day != 0){
			result = LocalDate.of(year, month, day);
		}
		return result;
	}

	@Override
	public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index,
	        SharedSessionContractImplementor session) throws HibernateException, SQLException {
		if (value == null){
			StandardBasicTypes.STRING.nullSafeSet(preparedStatement, null, index, session);
		}else {
			Temporal p = ((Temporal) value);
			StandardBasicTypes.STRING.nullSafeSet(preparedStatement, partialToString(p), index, session);
		}
	}

	/**
	 * @param p
	 * @return an ISO 8601 like time representations of the form yyyyMMdd
	 */
	public static String partialToString(Temporal p) {
		//FIXME reduce code by use org.joda.time.format.ISODateTimeFormat.basicDate() instead ?
		//      for a date with unknown day this will produce e.g. 195712??
		//
		String strYear = getNullFilledString(p, ChronoField.YEAR,4);
		String strMonth = getNullFilledString(p, ChronoField.MONTH_OF_YEAR,2);
		String strDay = getNullFilledString(p, ChronoField.DAY_OF_MONTH,2);
		String result = strYear + strMonth + strDay;
		return result;
	}

	private static String getNullFilledString(Temporal partial, ChronoField type, int count){
		String nul = "0000000000";
		if (! partial.isSupported(type)){
			return nul.substring(0, count);
		}else{
			int value = partial.get(type);
			String result = String.valueOf(value);
			if (result.length() > count){
				logger.error("value to long");
				result = result.substring(0, count);
			}else if (result.length() < count){
				result = nul.substring(0, count - result.length()) +  result;
			}
			return result;
		}
	}

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }

        return new Partial((Partial)value);
    }

	@Override
	public int[] sqlTypes() {
		// TODO Auto-generated method stub
		return SQL_TYPES;
	}

	@Override
	public Class returnedClass() {
		// TODO Auto-generated method stub
		return null;
	}

}

