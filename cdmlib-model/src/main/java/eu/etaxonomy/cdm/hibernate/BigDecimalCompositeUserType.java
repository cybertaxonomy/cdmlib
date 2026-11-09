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
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.spi.ValueAccess;
import org.hibernate.usertype.CompositeUserType;

import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * Multi-column mapping for {@link BigDecimal} (NUMERIC value + INTEGER scale)
 * so that the original scale is preserved on reload.
 * <p>
 * JPA {@link jakarta.persistence.AttributeConverter} cannot map multiple columns;
 * therefore this remains a Hibernate {@link CompositeUserType}.
 * <p>
 * Embeddable property order (alphabetical): {@code scale} (0), {@code value} (1).
 *
 * @author a.mueller
 */
public class BigDecimalCompositeUserType implements CompositeUserType<BigDecimal> {

    /**
     * Metadata-only embeddable; property names are sorted alphabetically for indexing.
     */
    public static class BigDecimalEmbeddable {
        Integer scale;
        BigDecimal value;
    }

    @Override
    public Object getPropertyValue(BigDecimal component, int property) throws HibernateException {
        if (component == null) {
            return null;
        }
        return switch (property) {
            case 0 -> Integer.valueOf(component.scale());
            case 1 -> component;
            default -> throw new HibernateException("Illegal property index: " + property);
        };
    }

    @Override
    public BigDecimal instantiate(ValueAccess valueAccess, SessionFactoryImplementor sessionFactory) {
        Integer scale = valueAccess.getValue(0, Integer.class);
        BigDecimal value = valueAccess.getValue(1, BigDecimal.class);
        if (value == null) {
            return null;
        }
        if (scale == null) {
            return value;
        }
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    @Override
    public Class<?> embeddable() {
        return BigDecimalEmbeddable.class;
    }

    @Override
    public Class<BigDecimal> returnedClass() {
        return BigDecimal.class;
    }

    @Override
    public boolean equals(BigDecimal x, BigDecimal y) {
        return CdmUtils.nullSafeEqual(x, y);
    }

    @Override
    public int hashCode(BigDecimal x) {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public BigDecimal deepCopy(BigDecimal value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(BigDecimal value) {
        return value;
    }

    @Override
    public BigDecimal assemble(Serializable cached, Object owner) {
        return (BigDecimal) cached;
    }

    @Override
    public BigDecimal replace(BigDecimal detached, BigDecimal managed, Object owner) {
        return detached;
    }
}
