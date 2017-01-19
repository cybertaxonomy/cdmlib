//package eu.etaxonomy.cdm.hibernate;
//
//import static org.junit.Assert.*;
//
//import org.junit.Test;
//
//import static org.hamcrest.Matchers.*;
//import static org.junit.Assert.*;
//import static org.mockito.Mockito.*;
//
//import java.math.*;
//import java.sql.*;
//
//import org.hibernate.usertype.*;
//import org.junit.*;
//import org.junit.runner.*;
//import org.mockito.*;
//import org.mockito.runners.*;
//
///**
// * This software is public domain and carries NO WARRANTY.
// *
// * Patches, bug reports and feature requests welcome:
// *
// * https://bitbucket.org/ratkins/bigdecimalusertype/
// */
//@RunWith(MockitoJUnitRunner.class)
//public class BigDecimalUserTypeTest {
//	
//	@Mock ResultSet rs;
//	@Mock PreparedStatement st;
//	
//	private UserType userType;
//
//	@Before
//	public void setUp() throws Exception {
//		userType = new BigDecimalUserType();
//	}
//	
//	@Test
//	public void testNotMutable() throws Exception {
//		assertThat(userType.isMutable(), equalTo(false));
//	}
//	
//	@Test
//	public void testDeepCopyReturnsSelf() throws Exception {
//		BigDecimal foo = new BigDecimal("1.0");
//		assertThat((BigDecimal) userType.deepCopy(foo), sameInstance(foo));
//	}
//	
//	@Test
//	public void testReturnedClassIsBigDecimal() throws Exception {
////		The Eclipse compiler passes this, the jdk on the command line one doesn't
////		assertThat(userType.returnedClass(), equalTo(BigDecimal.class));
//		assertTrue(userType.returnedClass() == BigDecimal.class);
//	}
//	
//	@Test
//	public void testEqualsDelegatesToBigDecimalEquals() throws Exception {
//		assertThat(userType.equals(new BigDecimal("1.0"), new BigDecimal("1.0")), equalTo(true));
//		assertThat(userType.equals(new BigDecimal("1.0"), new BigDecimal("2.0")), equalTo(false));
//		assertThat(userType.equals(new BigDecimal("1.0"), new BigDecimal("1.00")), equalTo(false));
//	}
//	
//	@Test
//	public void testHashCodeDelegatesToBigDecimalHashCode() throws Exception {
//		assertThat(userType.hashCode(new BigDecimal("1.0")), equalTo(new BigDecimal("1.0").hashCode()));
//		assertThat(userType.hashCode(new BigDecimal("1.00")), not(equalTo(new BigDecimal("1.0").hashCode())));
//		assertThat(userType.hashCode(new BigDecimal("1.0")), not(equalTo(new BigDecimal("2.0").hashCode())));
//	}
//	
//	@Test
//	public void testAssembleReturnsSelf() throws Exception {
//		BigDecimal foo = new BigDecimal("1.0");
//		assertThat((BigDecimal) userType.assemble(foo, null), sameInstance(foo));
//	}
//	
//	@Test
//	public void testDisassembleReturnsSelf() throws Exception {
//		BigDecimal foo = new BigDecimal("1.0");
//		assertThat((BigDecimal) userType.disassemble(foo), sameInstance(foo));
//		assertThat((BigDecimal) userType.disassemble(null), equalTo((BigDecimal) null));
//	}
//	
//	@Test
//	public void testReplaceReturnsSelf() throws Exception {
//		BigDecimal foo = new BigDecimal("1.0");
//		assertThat((BigDecimal) userType.replace(foo, null, null), sameInstance(foo));
//		assertThat((BigDecimal) userType.disassemble(null), equalTo((BigDecimal) null));
//	}
//	
//	@Test
//	public void testSqlTypesAreNumberAndInteger() throws Exception {
//		assertThat(userType.sqlTypes().length, equalTo(2));
//		assertThat(userType.sqlTypes()[0], equalTo(Types.DECIMAL));
//		assertThat(userType.sqlTypes()[1], equalTo(Types.INTEGER));
//	}
//	
//	@Test
//	public void testNullSafeGet() throws Exception {
//		when(rs.getBigDecimal("a")).thenReturn(new BigDecimal("1.000000"));
//		when(rs.getInt("b")).thenReturn(1);
//		
//		assertThat((BigDecimal) userType.nullSafeGet(rs, new String[] {"a", "b"}, null), equalTo(new BigDecimal("1.0")));
//	}
//
//	@Test
//	public void testNullSafeGetWithNull() throws Exception {
//		when(rs.getBigDecimal("a")).thenReturn(null);
//		when(rs.getInt("b")).thenReturn(0);
//		
//		assertThat((BigDecimal) userType.nullSafeGet(rs, new String[] {"a", "b"}, null), equalTo((BigDecimal) null));
//	}
//	
//	@Test
//	public void testNullSafeSet() throws Exception {
//		userType.nullSafeSet(st, new BigDecimal("1.000"), 17);
//		
//		verify(st).setBigDecimal(17, new BigDecimal("1.000"));
//		verify(st).setInt(18, 3);
//	}
//
//	@Test
//	public void testNullSafeSetWithNull() throws Exception {
//		userType.nullSafeSet(st, null, 17);
//		
//		verify(st).setNull(17, Types.DECIMAL);
//		verify(st).setNull(18, Types.INTEGER);
//	}
//}
