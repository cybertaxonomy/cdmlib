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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;
import org.jadira.usertype.dateandtime.shared.spi.AbstractUserType;

/**
 * @author a.mueller
 * @created 22.07.2008
 * @version 2.0
 */
public class UUIDUserType  extends AbstractUserType implements UserType {
	static final long serialVersionUID = -3959049831344758708L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(UUIDUserType.class);

	private static final int[] SQL_TYPES = { Types.VARCHAR };

	@Override
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
	@Override
    public Serializable disassemble(Object value) throws HibernateException {
		if(value == null) {
			return null;
		} else {
		    UUID uuid = (UUID) value;
		    return uuid.toString();
		}
	}

	@Override
	public UUID nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
        String val = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names, session, owner);

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

	@Override
	public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {
		if (value == null) {
//            statement.setNull(index, Types.VARCHAR); old version
            StandardBasicTypes.STRING.nullSafeSet(statement, value, index, session);
        } else {
         	UUID uuid = (UUID)value;
//            statement.setString(index, uuid.toString()); //old version
            StandardBasicTypes.STRING.nullSafeSet(statement, uuid.toString(), index, session);
        }
	}


	/* (non-Javadoc)
	 * @see org.jadira.usertype.dateandtime.shared.spi.AbstractSingleColumnUserType#returnedClass()
	 */
	@Override
	public Class returnedClass() {
		return UUID.class;
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}





}
