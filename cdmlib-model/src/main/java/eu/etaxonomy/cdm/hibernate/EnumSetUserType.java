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
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.jadira.usertype.spi.shared.AbstractUserType;

import eu.etaxonomy.cdm.model.term.IKeyTerm;

/**
 * User type for EnumSet (#7957, open issue #10109).
 *
 * @author a.mueller
 * @since 25-02-2019
 */
public class EnumSetUserType<E extends Enum<E>>
        extends AbstractUserType
        implements UserType, ParameterizedType {

    /*
     * For current usage with hibernate Criterion see DescriptionDaoImpl.addDescriptionTypesCriterion()
     */

    private static final long serialVersionUID = 1060802925284271666L;
    @SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

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
	public EnumSet<E> nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
        String val = (String) StandardBasicTypes.STRING.nullSafeGet(rs, names, session, owner);

        EnumSet<E> result = EnumSet.noneOf(clazz);
		if(val == null) {
			return result;
		} else {
			String[] splits = val.split(SEP);
			for (String split:splits){
			    if (StringUtils.isNotEmpty(split)) {
			        @SuppressWarnings("unchecked")
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
	public void nullSafeSet(PreparedStatement statement, Object value, int index, SharedSessionContractImplementor session)
			throws HibernateException, SQLException {
		if (value == null) {
            StandardBasicTypes.STRING.nullSafeSet(statement, value, index, session);
		} else if (value instanceof String) {
		    //this happens in queries, see #hasEnumValue(...)
		    String enumStr = (String)value;
		    StandardBasicTypes.STRING.nullSafeSet(statement, enumStr, index, session);
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


	//******** FOR QUERIES *********//

	//Note: in future maybe better use spring-data Specification.toPredicate(...) (e.g. https://docs.spring.io/spring-data/jpa/docs/current/api/org/springframework/data/jpa/domain/Specification.html)

	public static <F extends Enum<F>> Predicate hasEnumValue(F enumValue, CriteriaBuilder cb, Root<?> root) {
	    if (! (enumValue instanceof IKeyTerm)) {
	        throw new IllegalArgumentException("Enum is not of type IKeyTerm and can not be handled by EnumSetUserType");
	    }
	    IKeyTerm keyValue = (IKeyTerm)enumValue;
	    Predicate p = cb.like(root.get("types"), "%#"+keyValue.getKey()+"#%");
	    return p;
    }

	public static <F extends Enum<F>> Predicate hasAllEnumValues(Set<F> enumValues, CriteriaBuilder cb, Root<?> root) {
        List<Predicate> predicates = enumValues.stream()
                .map(enumValue -> hasEnumValue(enumValue, cb, root))
                .collect(Collectors.toList());
        Predicate[] predicateArray = predicates.toArray(new Predicate[0]);
        return cb.and(predicateArray);
    }
}
