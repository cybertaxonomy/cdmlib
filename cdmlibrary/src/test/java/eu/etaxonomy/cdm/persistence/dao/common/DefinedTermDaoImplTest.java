package eu.etaxonomy.cdm.persistence.dao.common;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.WrongTermTypeException;
import eu.etaxonomy.cdm.test.unit.CdmUnitTestBase;

public class DefinedTermDaoImplTest extends CdmUnitTestBase{
	static Logger logger = Logger.getLogger(DefinedTermDaoImplTest.class);

	@Autowired
	private IDefinedTermDao dao;
	private TermVocabulary vocabulary;
	
	@Before
	// generate enumeration for every test to play with
	public void onSetUp() throws Exception {
		this.vocabulary = new TermVocabulary("Biological subdomains","biodomain","http://myterms.org/biodomain");
		String [] repres = {"genetics","physiology","systematics","taxonomy","nomenclature"};
		for (String r : repres){
			Keyword term = new Keyword(r,r);
			vocabulary.addTerm(term);			
		}
	}

	@Test
	public void save() {
		for (DefinedTermBase dt : vocabulary){
			dao.save(dt);
		}
	}

	@Test
	public void findByTitle() {
		List<DefinedTermBase> terms = dao.findByTitle("biodomain");
		//assertTrue(terms.contains(this.vocabulary));
	}


	@Test
	public void list100() {
		assertFalse(dao.list(100,1).isEmpty());
	}	
}
