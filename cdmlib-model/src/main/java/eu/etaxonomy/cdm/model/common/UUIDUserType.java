/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * @author a.mueller
 * @created 22.07.2008
 * @version 1.0
 */
public class UUIDUserType implements UserType {
	private static final Logger logger = Logger.getLogger(UUIDUserType.class);

	private static final int[] TYPES = { Types.VARCHAR };

	
	/**
	 * 
	 */
	public UUIDUserType() {
		// TODO Auto-generated constructor stub
	}

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		try {
			if(cached == null) {
				return null;
			} else {
			    return UUID.fromString(cached.toString());
			}
		} catch (IllegalArgumentException e) {
			throw new HibernateException(e);
		}
	}

	public Object deepCopy(Object o) throws HibernateException {
		if (o == null) {
            return null;
        }
		
		UUID uuid = (UUID) o;

        try {
			return UUID.fromString(uuid.toString());
		} catch (IllegalArgumentException e) {
			throw new HibernateException(e);
		}
	}

	public Serializable disassemble(Object value) throws HibernateException {
		if(value == null) {
			return null;
		} else {
		    UUID uuid = (UUID) value;
		    return uuid.toString();
		}
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		return (x == y) || (x != null && y != null && (x.equals(y)));
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public boolean isMutable() {
		return false;
	}

	public Object nullSafeGet(ResultSet resultSet, String[] names, Object o)
			throws HibernateException, SQLException {
        String val = (String) Hibernate.STRING.nullSafeGet(resultSet, names[0]);
		
		if(val == null) {
			return null;
		} else {

            try {
			    return UUID.fromString(val);
		    } catch (IllegalArgumentException e) {
			    throw new HibernateException(e);
		    }
		}
	}

	public void nullSafeSet(PreparedStatement preparedStatement, Object o, int index)
			throws HibernateException, SQLException {
		if (null == o) { 
            preparedStatement.setNull(index, Types.VARCHAR); 
        } else { 
        	UUID uuid = (UUID)o;
            preparedStatement.setString(index, uuid.toString()); 
        }
	}

	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	public Class returnedClass() {
		return UUID.class;
	}

	public int[] sqlTypes() {
		return TYPES;
	}
}
