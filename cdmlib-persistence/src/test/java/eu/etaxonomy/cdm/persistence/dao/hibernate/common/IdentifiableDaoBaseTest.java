/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 *
 */
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
	@DataSet
	public void testFindByTitle() {
		List<TaxonBase> results = identifiableDao.findByTitle("Lorem");
		assertNotNull("findByTitle should return a list",results);
		assertEquals("findByTitle should return one entity", 1, results.size());
		assertEquals("findByTitle should return an entity with uuid " + uuid,uuid, results.get(0).getUuid());
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
