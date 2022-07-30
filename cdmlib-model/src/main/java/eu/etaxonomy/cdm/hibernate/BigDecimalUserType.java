
package eu.etaxonomy.cdm.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.usertype.UserType;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * Hibernate {@link UserType} for BigDecimal with correct handling for scale.
 * Correct means that a BigDecimal with scale 2 will be stored and reloaded
 * with scale 2 even if the defined scale in the database has a higher scale defined.
 *
 * E.g. "1.32" is persisted as exactly this BigDecimal even if scale of the column is
 * defined with scale = 4. The default {@link BigDecimalType} stores and reloads it
 * as 1.3200 which is a difference when handling e.g. measurement values as it looses the
 * information about the exactness of the data.<BR><BR>
 *
 * Usage example with annotations (with type already declared in according package-info.java):<BR>
 *
 * <BR>@Columns(columns={@Column(name="xxx", precision = 18, scale = 9), @Column(name="xxx_scale")})
 * <BR>@Type(type="bigDecimalUserType")
 * <BR><BR>
 *
 * This class has been originally copied and adapted from
 * https://bitbucket.org/ratkins/bigdecimalusertype/src/default/<BR><BR>
 */
public class BigDecimalUserType implements UserType {

	private static final int[] SQL_TYPES = new int[] {
	        Types.NUMERIC, //for some reason Types.Decimal does not exist at least in MySQL Dialect
	        Types.INTEGER};

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
	    return CdmUtils.nullSafeEqual(arg0, arg1);
	}

	@Override
	public int hashCode(Object arg0) throws HibernateException {
	    assert (arg0 != null);
	    return arg0.hashCode();
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
		BigDecimal bigDecimal = (BigDecimal) StandardBasicTypes.BIG_DECIMAL.nullSafeGet(rs, names, session, owner);
		if (bigDecimal == null) {
			return null;
		}
		return bigDecimal.setScale(rs.getInt(names[1]), BigDecimal.ROUND_HALF_UP);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
		if (value == null) {
			StandardBasicTypes.BIG_DECIMAL.nullSafeSet(st, null, index, session);
			StandardBasicTypes.INTEGER.nullSafeSet(st, null, index+1, session);
		} else {
			BigDecimal bdec = (BigDecimal)value;
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
