package eu.etaxonomy.cdm.remote.service;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import eu.etaxonomy.cdm.datagenerator.TaxonGenerator;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.dto.TreeNode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/applicationContext.xml"})
@TransactionConfiguration(defaultRollback=true)
public class CdmServiceImplTest {
	private static final Logger logger = Logger.getLogger(CdmServiceImplTest.class);
	
	@Autowired
	private  ICdmService service;

	@Test
	public void testGetName() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetSimpleName() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetTaxon() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetSimpleTaxon() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetChildrenTaxa() {
		logger.warn("Not yet implemented");
	}
	
	@Test
	public void testGetRootTaxa() {
		/*
		logger.warn("HACKED TEST: using static uuid");
		String uuid_str = "fd5d06eb-06ff-4ffd-9878-1b375765d539";
		UUID uuid = UUID.fromString(uuid_str);
		try {
			List<TreeNode> root = service.getRootTaxa(uuid);
			assertNotNull(root);
			Assert.assertTrue(root.size() > 0);
		} catch (CdmObjectNonExisting e) {
			logger.error(e.getMessage());
		}
		*/
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetParentTaxa() {
		logger.warn("Not yet implemented");
	}
	
	@Test
	public void testSaveTaxon(){
		Taxon t = TaxonGenerator.getTestTaxon();
		service.saveTaxon(t);
	}
}
