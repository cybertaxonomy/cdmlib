/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.remote.config.OaiPmhCacheConfiguration;
import eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataPrefix;
import eu.etaxonomy.cdm.remote.dto.oaipmh.ResumptionToken;

/**
 * Guarantees put/get/remove semantics formerly provided via springmodules-cache
 * + Ehcache 2, now via Spring Cache + Ehcache 3.
 *
 * @author a.mueller
 * @since 26.11.2026
 */
public class ResumptionTokenCacheTest {

    private AnnotationConfigApplicationContext ctx;
    private ResumptionTokenCache cache;

    @BeforeEach
    public void setUp() {
        ctx = new AnnotationConfigApplicationContext(OaiPmhCacheConfiguration.class);
        cache = ctx.getBean(ResumptionTokenCache.class);
    }

    @AfterEach
    public void tearDown() {
        if (ctx != null) {
            ctx.close();
        }
    }

    @Test
    public void springCacheManagerBeanIsEhcache3Backed() {
        CacheManager cm = ctx.getBean(
                OaiPmhCacheConfiguration.SPRING_CACHE_MANAGER_BEAN, CacheManager.class);
        assertNotNull(cm.getCache(ResumptionTokenCache.CACHE_NAME));
    }

    @Test
    public void putGetRoundTripPreservesTransientFields() {
        ResumptionToken token = newToken("token-1");
        cache.put(token.getValue(), token);

        ResumptionToken loaded = cache.get(token.getValue());
        assertNotNull(loaded);
        assertEquals(token.getValue(), loaded.getValue());
        assertEquals(MetadataPrefix.OAI_DC, loaded.getMetadataPrefix());
        assertNotNull(loaded.getFrom());
        assertNotNull(loaded.getUntil());
        assertEquals(BigInteger.valueOf(25), loaded.getCompleteListSize());
        assertTrue(cache.contains(token.getValue()));
    }

    @Test
    public void getMissingReturnsNull() {
        assertNull(cache.get("does-not-exist"));
        assertFalse(cache.contains("does-not-exist"));
    }

    @Test
    public void removeEvictsEntry() {
        ResumptionToken token = newToken("token-2");
        cache.put(token.getValue(), token);
        assertNotNull(cache.get(token.getValue()));

        cache.remove(token.getValue());
        assertNull(cache.get(token.getValue()));
        assertFalse(cache.contains(token.getValue()));
    }

    @Test
    public void putOverwritesExisting() {
        ResumptionToken first = newToken("token-3");
        cache.put(first.getValue(), first);

        ResumptionToken second = newToken("token-3");
        second.setCursor(BigInteger.valueOf(99));
        cache.put(second.getValue(), second);

        ResumptionToken loaded = cache.get("token-3");
        assertNotNull(loaded);
        assertEquals(BigInteger.valueOf(99), loaded.getCursor());
    }

    private static ResumptionToken newToken(String value) {
        List<String> records = Arrays.asList("a", "b", "c", "d", "e");
        Pager<String> pager = new DefaultPagerImpl<>(0, 25, 5, records);
        ZonedDateTime from = ZonedDateTime.parse("2020-01-01T00:00:00Z");
        ZonedDateTime until = ZonedDateTime.parse("2020-12-31T23:59:59Z");
        ResumptionToken token = new ResumptionToken(pager, from, until, MetadataPrefix.OAI_DC, null);
        token.setValue(value);
        return token;
    }
}
