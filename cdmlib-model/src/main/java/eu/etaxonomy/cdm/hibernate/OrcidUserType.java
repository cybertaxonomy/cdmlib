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
import org.jadira.usertype.spi.shared.AbstractUserType;

import eu.etaxonomy.cdm.model.agent.ORCID;

/**
 * Hibernate user type for the {@link ORCID} class.
 * @author a.mueller
 * @since 08.11.2018
 */
public class OrcidUserType  extends AbstractUserType implements UserType {

    private static final long serialVersionUID = -1274015438727972344L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(OrcidUserType.class);

	private static final int[] SQL_TYPES = { Types.VARCHAR };

	@Override
	public Object deepCopy(Object o) throws HibernateException {
		if (o == null) {
            return null;
        }
        try {
            ORCID orcid = (ORCID) o;
			return orcid;
		} catch (Exception e) {
			throw new HibernateException(e);
		}
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
		if(value == null) {
			return null;
		} else {
		    ORCID orcid = (ORCID) value;
		    return orcid.asURI();  //TO be on the safe side. We could also use orcid.getDigitsOnly()
		}
	}

	@Override
	public ORCID nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
        String val = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names, session, owner);

		if(val == null) {
			return null;
		} else {
            try {
			    return ORCID.fromString(val);
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
            ORCID orcid = (ORCID)value;
            StandardBasicTypes.STRING.nullSafeSet(statement, orcid.getDigitsOnly(), index, session);
        }
	}

	@Override
	public Class<?> returnedClass() {
		return ORCID.class;
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}
}
