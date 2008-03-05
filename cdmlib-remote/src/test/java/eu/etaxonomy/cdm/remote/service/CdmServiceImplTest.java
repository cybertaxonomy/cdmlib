package eu.etaxonomy.cdm.remote.service;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.datagenerator.TaxonGenerator;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.taxon.TaxonDaoHibernateImplTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
@TransactionConfiguration(defaultRollback=true)
public class CdmServiceImplTest {
	private static final Logger logger = Logger.getLogger(CdmServiceImplTest.class);
	
	@Autowired
	private  ICdmService service;

	//@Test
	public void testGetName() {
		fail("Not yet implemented");
	}

	//@Test
	public void testGetSimpleName() {
		fail("Not yet implemented");
	}

	//@Test
	public void testGetTaxon() {
		fail("Not yet implemented");
	}

	//@Test
	public void testGetSimpleTaxon() {
		fail("Not yet implemented");
	}

	//@Test
	public void testGetChildrenTaxa() {
		fail("Not yet implemented");
	}

	//@Test
	public void testGetParentTaxa() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testSaveTaxon(){
		Taxon t = TaxonGenerator.getTestTaxon();
		service.saveTaxon(t);
	}
}
