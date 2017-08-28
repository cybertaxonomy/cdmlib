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
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;
import org.jadira.usertype.dateandtime.shared.spi.AbstractUserType;

import eu.etaxonomy.cdm.common.DOI;

/**
 * Hibernate user type for the {@link DOI} class.
 * @author a.mueller
 * @created 05.09.2013
 */
public class DOIUserType  extends AbstractUserType implements UserType {
	private static final long serialVersionUID = 2227841000128722278L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DOIUserType.class);

	private static final int[] SQL_TYPES = { Types.VARCHAR };

	@Override
	public Object deepCopy(Object o) throws HibernateException {
		if (o == null) {
            return null;
        }

		DOI doi = (DOI) o;

        try {
			return DOI.fromString(doi.toString());
		} catch (IllegalArgumentException e) {
			throw new HibernateException(e);
		}
	}


	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		if(value == null) {
			return null;
		} else {
		    DOI doi = (DOI) value;
		    return doi.toString();
		}
	}

	@Override
	public DOI nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
        String val = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names, session, owner);

		if(val == null) {
			return null;
		} else {

            try {
			    return DOI.fromString(val);
		    } catch (IllegalArgumentException e) {
			    throw new HibernateException(e);
		    }
		}
	}

	@Override
	public void nullSafeSet(PreparedStatement statement, Object value, int index, SharedSessionContractImplementor session)
			throws HibernateException, SQLException {
		if (value == null) {
            StandardBasicTypes.STRING.nullSafeSet(statement, value, index, session);
        } else {
        	DOI doi = (DOI)value;
            StandardBasicTypes.STRING.nullSafeSet(statement, doi.toString(), index, session);
        }
	}

	@Override
	public Class returnedClass() {
		return DOI.class;
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}





}
