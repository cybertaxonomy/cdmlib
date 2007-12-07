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
	private TermVocabulary<DefinedTermBase> vocabulary;
	
	@Before
	// generate enumeration for every test to play with
	public void onSetUp() throws Exception {
		this.vocabulary = new TermVocabulary<DefinedTermBase>("Biological subdomains","biodomain","http://myterms.org/biodomain");
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
		List<DefinedTermBase> terms = dao.findByTitle("En");
		logger.debug("Results: "+terms.size());
		for (DefinedTermBase dt:terms){
			logger.debug(dt.toString());
		}
		//assertTrue(terms.contains(this.vocabulary));
	}

	@Test
	public void listOneTerm() {
		logger.debug("TEST: List 1 defined term");
		List<DefinedTermBase> terms = dao.list(1, 1);
		for (DefinedTermBase dt:terms){
			logger.debug("Loaded term: "+dt.toString());
		}
	}
	
	@Test
	public void getTermByUUID() {
		logger.debug("TEST: getTermByUUID eeaea868-c4c1-497f-b9fe-52c9fc4aca53");
		DefinedTermBase dt = dao.findByUuid("eeaea868-c4c1-497f-b9fe-52c9fc4aca53");
		logger.debug("Loaded term: "+dt.toString());
	}
	
	@Test
	public void listManyTerms() {
		logger.debug("TEST: List 10 defined terms");
		List<DefinedTermBase> terms = dao.list(10, 0);
		for (DefinedTermBase dt:terms){
			logger.debug("Loaded term: "+dt.toString());
		}
	}
}
