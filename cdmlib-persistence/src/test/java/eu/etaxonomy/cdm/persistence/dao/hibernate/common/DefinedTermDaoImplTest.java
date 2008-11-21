package eu.etaxonomy.cdm.persistence.dao.hibernate.common;


import static org.junit.Assert.*;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.WrongTermTypeException;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.test.unit.CdmUnitTestBase;

public class DefinedTermDaoImplTest extends CdmUnitTestBase{
	static Logger logger = Logger.getLogger(DefinedTermDaoImplTest.class);

	@Autowired
	private IDefinedTermDao dao;

	@Test
	public void save() throws WrongTermTypeException {
		TermVocabulary<DefinedTermBase> vocabulary	= new TermVocabulary<DefinedTermBase>("Biological subdomains","biodomain",null, "http://myterms.org/biodomain");
		String [] repres = {"genetics","physiology","systematics","taxonomy","nomenclature"};
		for (String r : repres){
			Keyword term = Keyword.NewInstance(r,r, null);
			vocabulary.addTerm(term);			
		}
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
		//fail();
		logger.warn("findByTitle - Not yet implemented");
		//assertTrue(terms.contains(this.vocabulary));
	}

	@Test
	public void listOneTerm() {
		logger.debug("TEST: List 1 defined term");
		List<DefinedTermBase> terms = dao.list(3, 3);
		for (DefinedTermBase dt:terms){
			logger.debug("Loaded term: "+dt.toString());
		}
	}
	
	@Test
	public void getTermByUUID() {
		String strUuid = "eeaea868-c4c1-497f-b9fe-52c9fc4aca53";
		logger.debug("TEST: getTermByUUID " + strUuid);
		DefinedTermBase dt = dao.findByUuid(UUID.fromString(strUuid));
		//logger.debug("Loaded term: "+dt.toString());
	}
	
	@Test
	public void listManyTerms() {
		logger.debug("TEST: List 10 defined terms");
		List<DefinedTermBase> terms = dao.list(10, 0);
		for (DefinedTermBase dt:terms){
			logger.debug("Loaded term: "+dt.toString());
		}
	}
	
	@Test
	public void getLanguageByIso() {
		Language lang = dao.getLanguageByIso("arm");
		logger.debug("Result for 'arm': " + lang.toString());
		assertEquals(lang.toString(), "arm");
		lang = dao.getLanguageByIso("hy");
		logger.debug("Result for 'hy': " + lang.toString());
		assertEquals(lang.toString(), "arm");
		lang = dao.getLanguageByIso("a");
		assertNull(lang);
		lang = dao.getLanguageByIso("abcd");
		assertNull(lang);
	}
}
