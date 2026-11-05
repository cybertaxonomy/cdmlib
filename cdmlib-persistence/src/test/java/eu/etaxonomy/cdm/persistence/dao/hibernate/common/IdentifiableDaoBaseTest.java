/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmSpringIntegrationTest;

/**
 * @author a.mueller
 */
@CdmDataSet
public class IdentifiableDaoBaseTest extends CdmSpringIntegrationTest {

    @Autowired
    private  TaxonDaoHibernateImpl identifiableDao;

    private UUID uuid;

    @BeforeEach
    public void setUp() {
        uuid = UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2");
    }

/************ TESTS ********************************/

    @Test
    public void testIdentifiableDaoBase() {
        assertNotNull(identifiableDao);
    }

    @Test
    public void testFindByTitle() {
        List<TaxonBase> results = identifiableDao.findByTitle("Lorem");
        assertNotNull(results, "findByTitle should return a list");
        assertEquals(1, results.size(), "findByTitle should return one entity");
        assertEquals(uuid, results.get(0).getUuid(), "findByTitle should return an entity with uuid " + uuid);
    }

    @Test
    public void testCountByTitle() {
        long result = identifiableDao.countByTitle("%");
        assertNotNull(result, "findByTitle should return an integer");
        assertEquals(2, result, "findByTitle should return 2");

        result = identifiableDao.countByTitle("%", MatchMode.ANYWHERE, null);
        assertNotNull(result, "findByTitle should return an integer");
        assertEquals(2, result, "findByTitle should return 2");

        result = identifiableDao.countByTitle("Lorem");
        assertNotNull(result, "findByTitle should return an integer");
        assertEquals(1, result, "findByTitle should return 1");

        result = identifiableDao.countByTitle("Lorem", MatchMode.ANYWHERE, null);
        assertNotNull(result, "findByTitle should return an integer");
        assertEquals(1, result, "findByTitle should return 1");
    }

    @Test
    public void testGetCredits() {
        TaxonBase taxon = identifiableDao.findByUuid(uuid);
        assert taxon != null : "IdentifiableEntity must exist";
        taxon.getCredits();

        List<Credit> credits = identifiableDao.getCredits(taxon, null, null);

        assertNotNull(credits, "getCredits should return a List");
        assertFalse(credits.isEmpty(), "the list should not be empty");
        assertEquals(3, credits.size(), "getCredits should return 3 Credit instances");
    }

    @Test
    public void testCreditsOrder() {
        TaxonBase taxon = identifiableDao.findByUuid(uuid);
        assert taxon != null : "IdentifiableEntity must exist";
        List<Credit> credits = taxon.getCredits();

        assertNotNull(credits, "getCredits should return a List");
        assertFalse(credits.isEmpty(), "the list should not be empty");
        assertEquals(3, credits.size(), "getCredits should return 3 Credit instances");
        assertEquals("My first credit", credits.get(0).getText());
        assertEquals("My second credit", credits.get(1).getText());
        assertEquals("My third credit", credits.get(2).getText());
    }

    @Test
    public void testSources() throws Exception {
        TaxonBase taxon = identifiableDao.findByUuid(uuid);
        assert taxon != null : "IdentifiableEntity must exist";

        List<IdentifiableSource> sources = identifiableDao.getSources(taxon, null, null, null);

        assertNotNull(sources, "getSources should return a List");
        assertFalse(sources.isEmpty(), "the list should not be empty");
        assertEquals(2, sources.size(), "getSources should return 2 OriginalSource instances");
    }

    @Test
    public void testGetByLsidWithoutVersion() throws Exception {
        LSID lsid = new LSID("urn:lsid:example.org:namespace:1");
        TaxonBase result = identifiableDao.find(lsid);
        assertNotNull(result);
    }

    @Test
    public void testGetByLsidWithVersionCurrent() throws Exception {
        LSID lsid = new LSID("urn:lsid:example.org:namespace:1:2");
        TaxonBase result = identifiableDao.find(lsid);
        assertNotNull(result);
    }

    @Test
    public void testGetByLsidWithVersionPast() throws Exception {
        LSID lsid = new LSID("urn:lsid:example.org:namespace:1:1");
        TaxonBase result = identifiableDao.find(lsid);
        assertNotNull(result);
    }

    @Test
    public void testGetUuidAndTitleCache() {
        List<UuidAndTitleCache<TaxonBase>> result = identifiableDao.getUuidAndTitleCache(TaxonBase.class, 10, "Lore*");
        assertEquals(1, result.size());
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
