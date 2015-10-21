/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 *
 */
@DataSet
public class IdentifiableDaoBaseTest extends CdmIntegrationTest {

    @SpringBeanByType
    private  TaxonDaoHibernateImpl identifiableDao;

    private UUID uuid;

    @Before
    public void setUp() {
        uuid = UUID.fromString("496b1325-be50-4b0a-9aa2-3ecd610215f2");
    }

/************ TESTS ********************************/

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase#IdentifiableDaoBase(java.lang.Class)}.
     */
    @Test
    public void testIdentifiableDaoBase() {
        assertNotNull(identifiableDao);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase#findByTitle(java.lang.String)}.
     */
    @Test
    public void testFindByTitle() {
        List<TaxonBase> results = identifiableDao.findByTitle("Lorem");
        assertNotNull("findByTitle should return a list",results);
        assertEquals("findByTitle should return one entity", 1, results.size());
        assertEquals("findByTitle should return an entity with uuid " + uuid,uuid, results.get(0).getUuid());
    }

    @Test
    public void testCountByTitle() {
        int result = identifiableDao.countByTitle("%");
        assertNotNull("findByTitle should return an integer",result);
        assertEquals("findByTitle should return 2", 2, result);

        result = identifiableDao.countByTitle("%", MatchMode.ANYWHERE, null);
        assertNotNull("findByTitle should return an integer",result);
        assertEquals("findByTitle should return 2", 2, result);

        result = identifiableDao.countByTitle("Lorem");
        assertNotNull("findByTitle should return an integer",result);
        assertEquals("findByTitle should return 1", 1, result);

        result = identifiableDao.countByTitle("Lorem", MatchMode.ANYWHERE, null);
        assertNotNull("findByTitle should return an integer",result);
        assertEquals("findByTitle should return 1", 1, result);
    }
    @Test
    public void testGetRights() {
        TaxonBase taxon = identifiableDao.findByUuid(uuid);
        assert taxon != null : "IdentifiableEntity must exist";

        List<Rights> rights = identifiableDao.getRights(taxon, null, null, null);

        assertNotNull("getRights should return a List",rights);
        assertFalse("the list should not be empty",rights.isEmpty());
        assertEquals("getRights should return 2 Rights instances",2,rights.size());
    }

    @Test
    public void testGetCredits() {
        TaxonBase taxon = identifiableDao.findByUuid(uuid);
        assert taxon != null : "IdentifiableEntity must exist";
        taxon.getCredits();

        List<Credit> credits = identifiableDao.getCredits(taxon, null, null);

        assertNotNull("getCredits should return a List",credits);
        assertFalse("the list should not be empty",credits.isEmpty());
        assertEquals("getCredits should return 3 Credit instances",3,credits.size());
    }

    @Test
    public void testCreditsOrder() {
        TaxonBase taxon = identifiableDao.findByUuid(uuid);
        assert taxon != null : "IdentifiableEntity must exist";
        List<Credit> credits = taxon.getCredits();

        assertNotNull("getCredits should return a List",credits);
        assertFalse("the list should not be empty",credits.isEmpty());
        assertEquals("getCredits should return 3 Credit instances",3,credits.size());
        assertEquals("My first credit", credits.get(0).getText());
        assertEquals("My second credit", credits.get(1).getText());
        assertEquals("My third credit", credits.get(2).getText());
    }


    @Test
    public void testSources() throws Exception {
        TaxonBase taxon = identifiableDao.findByUuid(uuid);
        assert taxon != null : "IdentifiableEntity must exist";

        List<IdentifiableSource> sources = identifiableDao.getSources(taxon, null, null,null);

        assertNotNull("getSources should return a List", sources);
        assertFalse("the list should not be empty", sources.isEmpty());
        assertEquals("getSources should return 2 OriginalSource instances",2, sources.size());
    }

    @Test
    public void testGetByLsidWithoutVersion() throws Exception {
        LSID lsid = new LSID("urn:lsid:example.org:namespace:1");
        TaxonBase<?> result = identifiableDao.find(lsid);
        assertNotNull(result);
    }

    @Test
    public void testGetByLsidWithVersionCurrent() throws Exception {
        LSID lsid = new LSID("urn:lsid:example.org:namespace:1:2");
        TaxonBase<?> result = identifiableDao.find(lsid);
        assertNotNull(result);
    }

    @Test
    public void testGetByLsidWithVersionPast() throws Exception {
        LSID lsid = new LSID("urn:lsid:example.org:namespace:1:1");
        TaxonBase<?> result = identifiableDao.find(lsid);
        assertNotNull(result);
    }


    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
