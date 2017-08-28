/**
 *
 */
package eu.etaxonomy.cdm.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

/**
 * This software is public domain and carries NO WARRANTY.
 *
 * Patches, bug reports and feature requests welcome:
 *
 * https://bitbucket.org/ratkins/bigdecimalusertype/
 */
public class BigDecimalUserType implements UserType {

	private static final int[] SQL_TYPES = new int[] {Types.DECIMAL, Types.INTEGER};

	@Override
	public Object assemble(Serializable arg0, Object arg1) throws HibernateException {
		return deepCopy(arg0);
	}

	@Override
	public Object deepCopy(Object arg0) throws HibernateException {
		return arg0;
	}

	@Override
	public Serializable disassemble(Object arg0) throws HibernateException {
		return (Serializable) arg0;
	}

	@Override
	public boolean equals(Object arg0, Object arg1) throws HibernateException {
		return arg0.equals(arg1);
	}

	@Override
	public int hashCode(Object arg0) throws HibernateException {
		return arg0.hashCode();
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
		BigDecimal bigDecimal = (BigDecimal) StandardBasicTypes.BIG_DECIMAL.nullSafeGet(rs, names, session, owner);

//		BigDecimal bigDecimal = rs.getBigDecimal(names[0]);
		if (bigDecimal == null) {
			return null;
		}
		return bigDecimal.setScale(rs.getInt(names[1]), BigDecimal.ROUND_HALF_UP);
	}


	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
		if (value == null) {
			StandardBasicTypes.BIG_DECIMAL.nullSafeSet(st, null, index, session);
			StandardBasicTypes.INTEGER.nullSafeSet(st, null, index, session);

//			st.setNull(index, Types.DECIMAL);
//			st.setNull(index + 1, Types.INTEGER);
		} else {
			BigDecimal bdec = (BigDecimal)value;
//			st.setBigDecimal(index, bdec);
//			st.setInt(index + 1, bdec.scale());
			StandardBasicTypes.BIG_DECIMAL.nullSafeSet(st, bdec, index, session);
			StandardBasicTypes.INTEGER.nullSafeSet(st, bdec.scale(), index + 1, session);
		}
	}


	@Override
	public Object replace(Object arg0, Object arg1, Object arg2) throws HibernateException {
		return arg0;
	}

	@Override
	public Class<?> returnedClass() {
		return BigDecimal.class;
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}





}
