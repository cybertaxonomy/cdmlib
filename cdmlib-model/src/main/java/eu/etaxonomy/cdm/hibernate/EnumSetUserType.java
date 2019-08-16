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
import java.util.EnumSet;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.jadira.usertype.dateandtime.shared.spi.AbstractUserType;

import eu.etaxonomy.cdm.model.term.IKeyTerm;

/**
 * User type for EnumSet
 * @author a.mueller
 * @since 25-02-2019
 */
public class EnumSetUserType<E extends Enum<E>>
        extends AbstractUserType
        implements UserType, ParameterizedType {

    private static final long serialVersionUID = 1060802925284271666L;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EnumSetUserType.class);

    private static final String SEP = "#";


	private Class<E> clazz = null;

	public EnumSetUserType(){}

    public EnumSetUserType(Class<E> c) {
    	this.clazz = c;
    }

	@Override
	@SuppressWarnings("unchecked")
	public void setParameterValues(Properties parameters) {
		try {
			this.clazz = (Class<E>) Class.forName(parameters.getProperty("enumClass"));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static final int[] SQL_TYPES = { Types.VARCHAR };


	@Override
	public Object deepCopy(Object o) throws HibernateException {
		return o;
	}

	@Override
	public Serializable disassemble(Object value) throws HibernateException {
	    return (Serializable)value;
	}

	@Override
	public EnumSet<E> nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner)
			throws HibernateException, SQLException {
        String val = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names, session, owner);

		if(val == null) {
			return null;
		} else {
		    EnumSet<E> result = EnumSet.noneOf(clazz);
			String[] splits = val.split(SEP);
			for (String split:splits){
			    if (StringUtils.isNotEmpty(split)) {
			        E term = (E)EnumUserType.getTerm(clazz, split);
			        if (term == null){
			            throw new IllegalArgumentException(split + " is not a valid key value for enumeration " + clazz.getCanonicalName());
			        }
                    result.add(term);
                }
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
        	@SuppressWarnings("unchecked")
            EnumSet<E> enumSet = (EnumSet<E>)value;
        	String key = "#";
        	for(Enum<E> e: enumSet){
        	    key += ((IKeyTerm)e).getKey()+"#";
        	}
            StandardBasicTypes.STRING.nullSafeSet(statement, key, index, session);
        }
	}

	@Override
	public Class<E> returnedClass() {
		return clazz;
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}
}
