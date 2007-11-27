package eu.etaxonomy.cdm.persistence.dao;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import eu.etaxonomy.cdm.model.common.Enumeration;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.test.unit.CdmUnitTestBase;


public class EnumerationDaoHibernateImplTest extends CdmUnitTestBase{
	static Logger logger = Logger.getLogger(EnumerationDaoHibernateImplTest.class);

	@Autowired
	private IEnumerationDAO dao;
	private Enumeration enumeration;

	@Before
	// generate enumeration for every test to play with
	public void onSetUp() throws Exception {
		logger.debug(EnumerationDaoHibernateImplTest.class.getSimpleName() + " setup()");
		this.enumeration = new Enumeration("Biological subdomains","biodomain","http://myterms.org/biodomain");
		String [] repres = {"genetics","physiology","systematics","taxonomy","nomenclature"};
		for (String r : repres){
			Keyword term = new Keyword(r,r,null);
			enumeration.addTerm(term);			
		}
	}

	@Test
	public void testSave() {
		dao.saveOrUpdate(this.enumeration);
		this.enumeration.addTerm(new Keyword("cladistics","cladistics",null));
		dao.saveOrUpdate(this.enumeration);		
	}

	@Test
	public void testFindById() {
		// fail("Not yet implemented");
	}

	@Test
	public void testFindString() {
		List<Enumeration> myEnum = dao.find("biodomain");
		assertTrue(myEnum.contains(this.enumeration));
	}

	@Test
	public void testExists() {
		// fail("Not yet implemented");
	}

	@Test
	public void testList100() {
		assertFalse(dao.list(100,1).isEmpty());
	}
}
