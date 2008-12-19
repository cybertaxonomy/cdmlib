/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.OriginalSource;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
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

/************ TESTS *******************************
 * @throws FileNotFoundException */
	
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
	public void testGetRights() {
		TaxonBase taxon = identifiableDao.findByUuid(uuid);
		assert taxon != null : "IdentifiableEntity must exist";
		
		List<Rights> rights = identifiableDao.getRights(taxon, null, null);
		
		assertNotNull("getRights should return a List",rights);
		assertFalse("the list should not be empty",rights.isEmpty());
		assertEquals("getRights should return 2 Rights instances",2,rights.size());
	}
	
	@Test
	public void testSources() {
		TaxonBase taxon = identifiableDao.findByUuid(uuid);
		assert taxon != null : "IdentifiableEntity must exist";
		
		List<OriginalSource> sources = identifiableDao.getSources(taxon, null, null);

		assertNotNull("getSources should return a List", sources);
		assertFalse("the list should not be empty", sources.isEmpty());
		assertEquals("getSources should return 2 OriginalSource instances",2, sources.size());
	}

//	@Test
//	TODO - implement this later
//	public void testGetByLSID() throws Exception {
//		LSID lsid = new LSID("urn:lsid:cate-project.org:taxonconcepts:1");
//		TaxonBase result = taxonDAO.find(lsid);
//		
//		Assert.assertNotNull(result);
//	}
//	
}
