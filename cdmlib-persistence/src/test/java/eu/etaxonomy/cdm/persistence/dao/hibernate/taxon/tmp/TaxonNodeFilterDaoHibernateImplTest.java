/**
 *
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.tmp;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.tmp.TaxonNodeFilter;
import eu.etaxonomy.cdm.persistence.dao.taxon.tmp.TaxonNodeFilterDaoHibernateImpl;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @date 2014/06/13
 *
 */
public class TaxonNodeFilterDaoHibernateImplTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private ITaxonNodeDao taxonNodeDao;

    @SpringBeanByType
    private IClassificationDao classificationDao;

    @SpringBeanByType
    private TaxonNodeFilterDaoHibernateImpl filterDao;





	private Classification classification1;
	private TaxonNode node1;
	private TaxonNode node2;
	private TaxonNode node3;
	private TaxonNode node4;
	private TaxonNode node5;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		classification1 = Classification.NewInstance("TestClassification");
		Reference citation = null;
		String microCitation = null;
		Taxon taxon1 = Taxon.NewInstance(null, null);
		Taxon taxon2 = Taxon.NewInstance(null, null);
		Taxon taxon3 = Taxon.NewInstance(null, null);
		Taxon taxon4 = Taxon.NewInstance(null, null);
		Taxon taxon5 = Taxon.NewInstance(null, null);
		node1 = classification1.addChildTaxon(taxon1, citation, microCitation);
		node2 = classification1.addChildTaxon(taxon2, citation, microCitation);
		node3 = node1.addChildTaxon(taxon3, citation, microCitation);
		node4 = node3.addChildTaxon(taxon4, citation, microCitation);
		node5 = node3.addChildTaxon(taxon5, citation, microCitation);
		classificationDao.save(classification1);

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.taxon.tmp.TaxonNodeFilterDaoHibernateImpl#listUuids(eu.etaxonomy.cdm.persistence.dao.taxon.tmp.TaxonNodeFilter)}.
	 */
	@Test
	public void testListUuids() {
		Classification classification = classificationDao.findByUuid(classification1.getUuid());
		TaxonNodeFilter filter = new TaxonNodeFilter(node1);
		List<UUID> listUuid = filterDao.listUuids(filter);
		Assert.assertEquals("All 4 children should be returned", 4, listUuid.size());
		Assert.assertTrue(listUuid.contains(node4.getUuid()));
		Assert.assertFalse(listUuid.contains(node2.getUuid()));
		Assert.assertFalse(listUuid.contains(classification.getRootNode().getUuid()));


		filter = new TaxonNodeFilter(classification.getRootNode());
		listUuid = filterDao.listUuids(filter);
		//FIXME still unclear if (empty) root node should be part of the result
		Assert.assertEquals("All 6 children should be returned", 6, listUuid.size());

		filter = new TaxonNodeFilter(node3);
		listUuid = filterDao.listUuids(filter);
		Assert.assertEquals("All 3 children should be returned", 3, listUuid.size());

		filter.or(node2);
		listUuid = filterDao.listUuids(filter);
		Assert.assertEquals("All 3 children and node 2 should be returned", 4, listUuid.size());
		Assert.assertTrue(listUuid.contains(node2.getUuid()));

		filter = new TaxonNodeFilter(node1).not(node4);
		listUuid = filterDao.listUuids(filter);
		Assert.assertEquals("Node and 2 children but not node4 should be returned", 3, listUuid.size());
		Assert.assertFalse(listUuid.contains(node4.getUuid()));





	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
