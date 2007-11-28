package eu.etaxonomy.cdm.persistence.dao;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.WrongTermTypeException;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.test.unit.CdmUnitTestBase;


public class EnumerationDaoHibernateImplTest extends CdmUnitTestBase{
	static Logger logger = Logger.getLogger(EnumerationDaoHibernateImplTest.class);

	@Autowired
	private IEnumerationDAO dao;
	private TermVocabulary enumeration;

	@Before
	// generate enumeration for every test to play with
	public void onSetUp() throws Exception {
		logger.debug(EnumerationDaoHibernateImplTest.class.getSimpleName() + " setup()");
		this.enumeration = new TermVocabulary("Biological subdomains","biodomain","http://myterms.org/biodomain");
		String [] repres = {"genetics","physiology","systematics","taxonomy","nomenclature"};
		for (String r : repres){
			Keyword term = new Keyword(r,r,null);
			enumeration.addTerm(term);			
		}
	}

	//@Test
	public void testSave() {
		dao.saveOrUpdate(this.enumeration);
		try {
			this.enumeration.addTerm(new Keyword("cladistics","cladistics",null));
		} catch (WrongTermTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dao.saveOrUpdate(this.enumeration);		
	}

	//@Test
	public void testFindString() {
		List<TermVocabulary> myEnum = dao.find("biodomain");
		assertTrue(myEnum.contains(this.enumeration));
	}

	@Test
	public void loadTerms() {
		try {
			//dao.loadTerms(Rank.class, "Rank.csv", true);
			dao.loadDefaultTerms(SynonymRelationshipType.class);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoDefinedTermClassException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testList100() {
		assertFalse(dao.list(100,1).isEmpty());
	}
}
