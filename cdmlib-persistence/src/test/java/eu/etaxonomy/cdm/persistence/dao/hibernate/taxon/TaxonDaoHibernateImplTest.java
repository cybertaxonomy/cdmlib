package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 * @author ben.clark
 *
 */
public class TaxonDaoHibernateImplTest extends CdmIntegrationTest {
	
	@SpringBeanByType	
	private ITaxonDao taxonDao;
	
	@SpringBeanByType	
	private IReferenceDao referenceDao;
	
	private Taxon taxon;
	private ReferenceBase sec;

	@Before
	public void setUp() {
		sec = referenceDao.findById(1);
		taxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), sec);
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#TaxonDaoHibernateImpl()}.
	 */
	@Test
	public void testInit() {
		Assert.assertNotNull("Instance of ITaxonDao expected",taxonDao);
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#getRootTaxa(eu.etaxonomy.cdm.model.reference.ReferenceBase)}.
	 */
	@Test
	@DataSet
	public void testGetRootTaxa() { 
		List<Taxon> rootTaxa = taxonDao.getRootTaxa(sec);
		Assert.assertNotNull("getRootTaxa should return a List",rootTaxa);
		Assert.assertFalse("The list should not be empty",rootTaxa.isEmpty());
		Assert.assertEquals("There should be one root taxon",1, rootTaxa.size());
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#getTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.ReferenceBase)}.
	 */
	@Test
	@DataSet
	public void testGetTaxaByName() {
		List<TaxonBase> results = taxonDao.getTaxaByName("Aus", sec);
		Assert.assertNotNull("getTaxaByName should return a List",results);
		Assert.assertFalse("The list should not be empty",results.isEmpty());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl#save(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
	 */
	@Test
	@DataSet
	@ExpectedDataSet
	public void testSaveTaxon() {
		taxonDao.save(taxon);
	}
}
