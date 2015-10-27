/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate;


import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.TypeMismatchException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;
import org.jadira.usertype.dateandtime.shared.spi.AbstractUserType;

/**
 * This class maps java.net.URI to varchar(255)
 * @author a.mueller
 *
 */
public class URIUserType extends AbstractUserType implements UserType {
	private static final long serialVersionUID = -5825017496962569105L;

	/**
     * SQL type for this usertype.
     */
    private static final int[] SQL_TYPES = {Types.CLOB};

	@Override
	public Class<?> returnedClass() {
		return URI.class;
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}


	@Override
	public URI nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
		String val = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names, session, owner);

		if(val == null) {
			return null;
		} else {
            try {
			    return new URI(val);
		    } catch (URISyntaxException e) {
			    throw new TypeMismatchException(e);
		    }
		}
    }



	@Override
	public void nullSafeSet(PreparedStatement statement, Object value, int index, SessionImplementor session)
			throws HibernateException, SQLException {
		if (value == null) {
          StandardBasicTypes.STRING.nullSafeSet(statement, value, index, session);
      } else {
    	  URI uri = (URI) value;
          StandardBasicTypes.STRING.nullSafeSet(statement, uri.toString(), index, session);
      }
    }

    /**
     * @param value value being copied
     * @return copied value
     */
    @Override
    public Object deepCopy(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new URI(((URI) value).toString());
        } catch (URISyntaxException e) {
            throw new TypeMismatchException(e);
        }
    }

}
