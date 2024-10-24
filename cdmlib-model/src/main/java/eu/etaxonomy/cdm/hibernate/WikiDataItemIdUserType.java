/**
* Copyright (C) 2024 EDIT
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;
import org.jadira.usertype.spi.shared.AbstractUserType;

import eu.etaxonomy.cdm.model.common.WikiDataItemId;

/**
 * Hibernate user type for the {@link WikiDataItemIdUserType} class.
 *
 * @author muellera
 * @since 17.10.2024
 */
public class WikiDataItemIdUserType extends AbstractUserType implements UserType {

    private static final long serialVersionUID = -6053467304358358334L;

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private static final int[] SQL_TYPES = { Types.VARCHAR };

    @Override
    public Object deepCopy(Object o) throws HibernateException {
        if (o == null) {
            return null;
        }
        try {
            WikiDataItemId wikiDataItemId = (WikiDataItemId) o;
            return wikiDataItemId;
        } catch (Exception e) {
            throw new HibernateException(e);
        }
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        if(value == null) {
            return null;
        } else {
            WikiDataItemId wikiDataItemId = (WikiDataItemId) value;
            return wikiDataItemId.asURI();  //TO be on the safe side. We could also use wikiDataItemId.getDigitsOnly()
        }
    }

    @Override
    public WikiDataItemId nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {

        String val = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names, session, owner);
        if(val == null) {
            return null;
        } else {
            try {
                return WikiDataItemId.fromString(val);
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
            WikiDataItemId wikiDataItemId = (WikiDataItemId)value;
            StandardBasicTypes.STRING.nullSafeSet(statement, wikiDataItemId.getIdentifierWithQ(), index, session);
        }
    }

    @Override
    public Class<?> returnedClass() {
        return WikiDataItemId.class;
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }
}