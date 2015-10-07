/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;
import org.jadira.usertype.dateandtime.shared.spi.AbstractUserType;

import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment.Shift;

/**
 * Hibernate user type for arrays of int(eger).
 * @author a.mueller
 * @created 03.12.2014
 */
public class ShiftUserType  extends AbstractUserType implements UserType {
	private static final long serialVersionUID = -2507496252811101383L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ShiftUserType.class);

	private static final String SHIFT_SEPARATOR = ";";
	private static final String ATTR_SEPARATOR = ",";

	private static final int[] SQL_TYPES = { Types.CLOB };

	@Override
	public Object deepCopy(Object o) throws HibernateException {
		return o;  //do we need more?
	}


	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		if(value == null) {
			return null;
		} else {
			Shift[] ints = (Shift[]) value;
		    String result = "";
		    for (Shift shift : ints){
		    	if (shift != null){  //null should never happen, but to be on the safe side
		    	    result += SHIFT_SEPARATOR + String.valueOf(shift.position);
		    	    result += ATTR_SEPARATOR + String.valueOf(shift.shift);
		    	}
		    }
		    if (result.length() > 0){
		    	result = result.substring(1);
		    }
		    return result;
		}
	}

	@Override
	public Shift[] nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
        String val = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names, session, owner);

		if(val == null) {
			return null;
		} else {
            try {
            	Shift[] result = nullSafeGet(val);
			    return result;
		    } catch (IllegalArgumentException e) {
			    throw new HibernateException(e);
		    }
		}
	}


	protected Shift[] nullSafeGet(String val) {
		if (val.length() == 0){
			return new Shift[0];
		}else{
			String[] splits = val.split(SHIFT_SEPARATOR);
			Shift[] result = new Shift[splits.length];
			for (int i = 0; i< splits.length ; i++){
				result[i] = new Shift();
				String[] split = splits[i].split(ATTR_SEPARATOR);
				result[i].position = Integer.valueOf(split[0]);
				result[i].shift = Integer.valueOf(split[1]);
			}
			return result;
		}
	}

	@Override
	public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {
		if (value == null) {
            StandardBasicTypes.STRING.nullSafeSet(statement, value, index, session);
        } else {
        	String str = (String)disassemble(value);
            StandardBasicTypes.STRING.nullSafeSet(statement, str, index, session);
        }
	}

	@Override
	public Class<?> returnedClass() {
		return Shift[].class;
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

}
