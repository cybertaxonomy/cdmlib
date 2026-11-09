/**
 * Copyright (C) 2020 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.hibernate;

import java.math.BigDecimal;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.spi.ValueAccess;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link BigDecimalCompositeUserType}.
 *
 * @author a.mueller
 * @since 27.04.2020
 */
public class BigDecimalCompositeUserTypeTest {

    private BigDecimalCompositeUserType userType;

    @Before
    public void setUp() {
        userType = new BigDecimalCompositeUserType();
    }

    @Test
    public void testNotMutable() {
        Assert.assertFalse(userType.isMutable());
    }

    @Test
    public void testDeepCopyReturnsSelf() {
        BigDecimal foo = new BigDecimal("1.0");
        Assert.assertSame(foo, userType.deepCopy(foo));
    }

    @Test
    public void testPropertyValueAndInstantiate() {
        BigDecimal value = new BigDecimal("1.320");
        Assert.assertEquals(Integer.valueOf(3), userType.getPropertyValue(value, 0));
        Assert.assertEquals(value, userType.getPropertyValue(value, 1));

        ValueAccess access = new ValueAccess() {
            @Override
            public Object[] getValues() {
                return new Object[] { Integer.valueOf(2), new BigDecimal("1.32") };
            }
        };
        BigDecimal restored = userType.instantiate(access, (SessionFactoryImplementor) null);
        Assert.assertEquals(2, restored.scale());
        Assert.assertEquals(new BigDecimal("1.32"), restored);
    }

    @Test
    public void testReturnedClass() {
        Assert.assertEquals(BigDecimal.class, userType.returnedClass());
    }
}
