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
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(UUIDUserType.class);

	private static final int[] TYPES = { Types.VARCHAR };

	/* (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#assemble(java.io.Serializable, java.lang.Object)
	 */
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

	/* (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#deepCopy(java.lang.Object)
	 */
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

	/* (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#disassemble(java.lang.Object)
	 */
	public Serializable disassemble(Object value) throws HibernateException {
		if(value == null) {
			return null;
		} else {
		    UUID uuid = (UUID) value;
		    return uuid.toString();
		}
	}

	/* (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#equals(java.lang.Object, java.lang.Object)
	 */
	public boolean equals(Object x, Object y) throws HibernateException {
		return (x == y) || (x != null && y != null && (x.equals(y)));
	}

	/* (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#hashCode(java.lang.Object)
	 */
	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	/* (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#isMutable()
	 */
	public boolean isMutable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], java.lang.Object)
	 */
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

	/* (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int)
	 */
	public void nullSafeSet(PreparedStatement preparedStatement, Object o, int index)
			throws HibernateException, SQLException {
		if (null == o) { 
            preparedStatement.setNull(index, Types.VARCHAR); 
        } else { 
        	UUID uuid = (UUID)o;
            preparedStatement.setString(index, uuid.toString()); 
        }
	}

	/* (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#replace(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	public Object replace(Object original, Object target, Object owner)
			throws HibernateException {
		return original;
	}

	/* (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#returnedClass()
	 */
	public Class returnedClass() {
		return UUID.class;
	}

	/* (non-Javadoc)
	 * @see org.hibernate.usertype.UserType#sqlTypes()
	 */
	public int[] sqlTypes() {
		return TYPES;
	}
}
