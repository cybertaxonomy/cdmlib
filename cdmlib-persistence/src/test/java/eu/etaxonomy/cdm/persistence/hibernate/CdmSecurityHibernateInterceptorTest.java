/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author a.kohlbecker
 * @since 18.10.2017
 *
 */
public class CdmSecurityHibernateInterceptorTest extends Assert {


    @Test
    public void testUnprotectedCacheFields_unprotected() {
        CdmSecurityHibernateInterceptor interceptor = new CdmSecurityHibernateInterceptor();
        Object[] previousState = new Object[]{
                new Boolean(false),
                ""

        };
        Object[] currentState = new Object[]{
                new Boolean(false),
                null

        };

        String[] propertyNames = new String[]{
                "protectedTitleCache",
                "titleCache"
        };
        Collection<? extends String> excludes = interceptor.unprotectedCacheFields(currentState, previousState, propertyNames);
        assertEquals(2, excludes.size());
        assertTrue(excludes.contains("titleCache"));
        assertTrue(excludes.contains("protectedTitleCache"));
    }

    @Test
    public void testUnprotectedCacheFields_protected() {
        CdmSecurityHibernateInterceptor interceptor = new CdmSecurityHibernateInterceptor();
        Object[] previousState = new Object[]{
                new Boolean(true),
                ""

        };
        Object[] currentState = new Object[]{
                new Boolean(true),
                null

        };

        String[] propertyNames = new String[]{
                "protectedTitleCache",
                "titleCache"
        };
        Collection<? extends String> excludes = interceptor.unprotectedCacheFields(currentState, previousState, propertyNames);
        assertEquals(0, excludes.size());
    }

    @Test
    public void testUnprotectedCacheFields_currentStateDifferent() {
        CdmSecurityHibernateInterceptor interceptor = new CdmSecurityHibernateInterceptor();
        Object[] previousState = new Object[]{
                new Boolean(false),
                ""

        };
        Object[] currentState = new Object[]{
                new Boolean(true),
                null

        };

        String[] propertyNames = new String[]{
                "protectedTitleCache",
                "titleCache"
        };
        Collection<? extends String> excludes = interceptor.unprotectedCacheFields(currentState, previousState, propertyNames);
        assertEquals(0, excludes.size());
    }

}
