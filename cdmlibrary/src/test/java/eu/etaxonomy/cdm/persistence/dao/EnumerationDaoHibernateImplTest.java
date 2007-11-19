package eu.etaxonomy.cdm.persistence.dao;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.CdmUnitTestBase;
import eu.etaxonomy.cdm.model.common.Enumeration;
import eu.etaxonomy.cdm.model.common.Keyword;

public class EnumerationDaoHibernateImplTest extends CdmUnitTestBase{
	@Autowired
	private IEnumerationDAO dao;
	private Enumeration enumeration;

	@Before
	// generate enumeration for every test to play with
	public void onSetUp() throws Exception {
		this.enumeration = new Enumeration();
		String [] repres = {"genetics","physiology","systematics","taxonomy","nomenclature"};
		for (String r : repres){
			Keyword term = new Keyword(r);
			enumeration.addTerm(term);			
		}
	}

	@Test
	public void testSave() {
		dao.save(this.enumeration);
		this.enumeration.addTerm(new Keyword("cladistics"));
		dao.save(this.enumeration);		
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
		assertFalse(dao.list100().isEmpty());
	}
}
