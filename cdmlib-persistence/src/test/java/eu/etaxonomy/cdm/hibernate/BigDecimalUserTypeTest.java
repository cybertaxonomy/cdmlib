/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.sql.Types;

import org.hibernate.usertype.UserType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * Test for {@link BigDecimalUserType}
 *
 * The test class has originally been copied and adapted from
 * https://bitbucket.org/ratkins/bigdecimalusertype/src/default/BigDecimalUserTypeTest.java
 *
 * @author a.mueller
 * @since 27.04.2020
 */
public class BigDecimalUserTypeTest extends CdmTransactionalIntegrationTest {

//    @Mock ResultSet rs;
//    @Mock PreparedStatement st;

    private UserType userType;

    @Before
    public void setUp() throws Exception {
        userType = new BigDecimalUserType();
    }

    @Test
    public void testNotMutable() throws Exception {
        Assert.assertFalse(userType.isMutable());
    }

    @Test
    public void testDeepCopyReturnsSelf() throws Exception {
        BigDecimal foo = new BigDecimal("1.0");
        Assert.assertSame(userType.deepCopy(foo), foo);
    }

    @Test
    public void testReturnedClassIsBigDecimal() throws Exception {
        Assert.assertEquals(userType.returnedClass(), BigDecimal.class);
        Assert.assertTrue(userType.returnedClass() == BigDecimal.class);
    }

    @Test
    public void testEqualsDelegatesToBigDecimalEquals() throws Exception {
        Assert.assertTrue(userType.equals(new BigDecimal("1.0"), new BigDecimal("1.0")));
        Assert.assertFalse(userType.equals(new BigDecimal("1.0"), new BigDecimal("2.0")));
        Assert.assertFalse(userType.equals(new BigDecimal("1.0"), new BigDecimal("1.00")));
    }

    @Test
    public void testHashCodeDelegatesToBigDecimalHashCode() throws Exception {
        Assert.assertEquals(userType.hashCode(new BigDecimal("1.0")), new BigDecimal("1.0").hashCode());
        Assert.assertNotEquals(userType.hashCode(new BigDecimal("1.00")), new BigDecimal("1.0").hashCode());
        Assert.assertNotEquals(userType.hashCode(new BigDecimal("1.0")), new BigDecimal("2.0").hashCode());
    }

    @Test
    public void testAssembleReturnsSelf() throws Exception {
        BigDecimal foo = new BigDecimal("1.0");
        Assert.assertSame(userType.assemble(foo, null), foo);
    }

    @Test
    public void testDisassembleReturnsSelf() throws Exception {
        BigDecimal foo = new BigDecimal("1.0");
        Assert.assertSame(userType.disassemble(foo), foo);
        Assert.assertEquals(userType.disassemble(null), (BigDecimal) null);
    }

    @Test
    public void testReplaceReturnsSelf() throws Exception {
        BigDecimal foo = new BigDecimal("1.0");
        Assert.assertSame(userType.replace(foo, null, null), foo);
        Assert.assertEquals(userType.disassemble(null), (BigDecimal) null);
    }

    @Test
    public void testSqlTypesAreNumberAndInteger() throws Exception {
        Assert.assertEquals(userType.sqlTypes().length, 2);
        Assert.assertEquals(userType.sqlTypes()[0], Types.NUMERIC);   //Types.DECIMAL does not work (tested for MySQL)
        Assert.assertEquals(userType.sqlTypes()[1], Types.INTEGER);
    }

    //TODO update tests from original file
//    @Test
//    public void testNullSafeGet() throws Exception {
//        when(rs.getBigDecimal("a")).thenReturn(new BigDecimal("1.000000"));
//        when(rs.getInt("b")).thenReturn(1);
//
//        assertThat((BigDecimal) userType.nullSafeGet(rs, new String[] {"a", "b"}, null), equalTo(new BigDecimal("1.0")));
//    }
//
//    @Test
//    public void testNullSafeGetWithNull() throws Exception {
//        when(rs.getBigDecimal("a")).thenReturn(null);
//        when(rs.getInt("b")).thenReturn(0);
//
//        assertThat((BigDecimal) userType.nullSafeGet(rs, new String[] {"a", "b"}, null), equalTo((BigDecimal) null));
//    }
//
//    @Test
//    public void testNullSafeSet() throws Exception {
//        userType.nullSafeSet(st, new BigDecimal("1.000"), 17);
//
//        verify(st).setBigDecimal(17, new BigDecimal("1.000"));
//        verify(st).setInt(18, 3);
//    }
//
//    @Test
//    public void testNullSafeSetWithNull() throws Exception {
//        userType.nullSafeSet(st, null, 17);
//
//        verify(st).setNull(17, Types.NUMERIC);
//        verify(st).setNull(18, Types.INTEGER);
//    }


    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
