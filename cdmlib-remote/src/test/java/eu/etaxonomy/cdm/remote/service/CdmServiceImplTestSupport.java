package eu.etaxonomy.cdm.remote.service;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/applicationContext.xml"})
@TransactionConfiguration(defaultRollback=true)
public class CdmServiceImplTestSupport {

	@Test
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

}
