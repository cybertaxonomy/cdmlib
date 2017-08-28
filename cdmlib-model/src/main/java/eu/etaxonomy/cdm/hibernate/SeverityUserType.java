/**
* Copyright (C) 2009 EDIT
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

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.Severity;


/**
 * A hibernate {@code UserType} for persisting {@link Severity} instances.
 *
 * @see EntityConstraintViolation
 *
 * @author ayco_holleman
 *
 */
public class SeverityUserType implements UserType {

	@Override
	public int[] sqlTypes(){
		return new int[] { java.sql.Types.VARCHAR };
	}


	@SuppressWarnings("rawtypes")
	@Override
	public Class returnedClass(){
		return Severity.class;

	}


	@Override
	public boolean equals(Object x, Object y) throws HibernateException{
		if (x == null) {
			if (y == null) {
				return true;
			}
			return false;
		}
		if (y == null) {
			return false;
		}
		return ((Severity) x).equals(y);
	}


	@Override
	public int hashCode(Object x) throws HibernateException{
		return x.getClass().hashCode();
	}


	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException{
		String severity = rs.getString(names[0]);
		return rs.wasNull() ? null : Severity.forName(severity);
	}


	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException{
		st.setString(index, value == null ? null : value.toString());
	}


	@Override
	public Object deepCopy(Object value) throws HibernateException{
		return value;
	}


	@Override
	public boolean isMutable(){
		return false;
	}


	@Override
	public Serializable disassemble(Object value) throws HibernateException{
		return null;
	}


	@Override
	public Object assemble(Serializable cached, Object owner) throws HibernateException{
		return null;
	}


	@Override
	public Object replace(Object original, Object target, Object owner) throws HibernateException{
		return original;
	}

}
