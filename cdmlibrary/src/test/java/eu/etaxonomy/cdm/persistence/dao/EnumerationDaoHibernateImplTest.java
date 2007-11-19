package eu.etaxonomy.cdm.persistence.dao;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.CdmUnitTestBase;
import eu.etaxonomy.cdm.model.common.*;

public class EnumerationDaoHibernateImplTest extends CdmUnitTestBase{
	private static IEnumerationDAO dao;
	private static Enumeration myStaticEnum = new Enumeration();
	private Enumeration enumeration;

	public static IEnumerationDAO getDao() {
		return dao;
	}

	// to be set by spring
	public static void setDao(IEnumerationDAO dao) {
		EnumerationDaoHibernateImplTest.dao = dao;
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String [] repres = {"real","fake","original"};
		for (String r : repres){
			Keyword term = new Keyword(r);
			myStaticEnum.addTerm(term);			
		}
	}

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
