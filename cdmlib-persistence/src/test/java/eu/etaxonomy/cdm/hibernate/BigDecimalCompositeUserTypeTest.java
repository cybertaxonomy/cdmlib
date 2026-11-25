/**
 * Copyright (C) 2020 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;

import org.hibernate.metamodel.spi.ValueAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link BigDecimalCompositeUserType}.
 *
 * @author a.mueller
 * @since 27.04.2020
 */
public class BigDecimalCompositeUserTypeTest {

    private BigDecimalCompositeUserType userType;

    @BeforeEach
    public void setUp() {
        userType = new BigDecimalCompositeUserType();
    }

    @Test
    public void testNotMutable() {
        assertFalse(userType.isMutable());
    }

    @Test
    public void testDeepCopyReturnsSelf() {
        BigDecimal foo = new BigDecimal("1.0");
        assertSame(foo, userType.deepCopy(foo));
    }

    @Test
    public void testPropertyValueAndInstantiate() {
        BigDecimal value = new BigDecimal("1.320");
        assertEquals(Integer.valueOf(3), userType.getPropertyValue(value, 0));
        assertEquals(value, userType.getPropertyValue(value, 1));

        ValueAccess access = new ValueAccess() {
            @Override
            public Object[] getValues() {
                return new Object[] { Integer.valueOf(2), new BigDecimal("1.32") };
            }
        };
        BigDecimal restored = userType.instantiate(access);
        assertEquals(2, restored.scale());
        assertEquals(new BigDecimal("1.32"), restored);
    }

    @Test
    public void testReturnedClass() {
        assertEquals(BigDecimal.class, userType.returnedClass());
    }
}
