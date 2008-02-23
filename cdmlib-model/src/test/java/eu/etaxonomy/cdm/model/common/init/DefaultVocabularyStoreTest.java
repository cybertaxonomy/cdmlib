/**
 * 
 */
package eu.etaxonomy.cdm.model.common.init;

import static org.junit.Assert.*;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.WrongTermTypeException;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.test.suite.CdmTestSuite;

/**
 * @author AM
 *
 */
public class DefaultVocabularyStoreTest {
	static Logger logger = Logger.getLogger(DefaultVocabularyStoreTest.class);

	static private DefaultVocabularyStore defaultVocabularyStore;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		defaultVocabularyStore = new DefaultVocabularyStore();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

/*********************** TEST *************************************************/
	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.init.DefaultVocabularyStore#DEFAULT_LANGUAGE()}.
	 */
	@Test
	public void testDEFAULT_LANGUAGE() {
		UUID uuidEnglish = UUID.fromString("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		assertEquals(uuidEnglish, defaultVocabularyStore.DEFAULT_LANGUAGE().getUuid());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.init.DefaultVocabularyStore#getTermByUuid(java.util.UUID)}.
	 */
	@Test
	public void testGetTermByUuid() {
		UUID uuidForm = UUID.fromString("0461281e-458a-47b9-8d41-19a3d39356d5");
		assertEquals(Rank.FORM(), defaultVocabularyStore.getTermByUuid(uuidForm));
		assertEquals(null, defaultVocabularyStore.getTermByUuid(null));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.init.DefaultVocabularyStore#getVocabularyByUuid(java.util.UUID)}.
	 */
	@Test
	public void testGetVocabularyByUuid() {
		TermVocabulary voc = new TermVocabulary();
		try {
			voc.addTerm(new Rank());
		} catch (WrongTermTypeException e) {
			fail();
		}
		defaultVocabularyStore.saveOrUpdate(voc);
		TermVocabulary voc2 = defaultVocabularyStore.getVocabularyByUuid(voc.getUuid());
		assertEquals(voc, voc2);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.init.DefaultVocabularyStore#saveOrUpdate(eu.etaxonomy.cdm.model.common.TermVocabulary)}.
	 */
	@Test
	public void testSaveOrUpdate() {
		TermVocabulary voc = new TermVocabulary();
		try {
			voc.addTerm(new Rank());
		} catch (WrongTermTypeException e) {
			fail();
		}
		defaultVocabularyStore.saveOrUpdate(voc);
		assertEquals(voc, defaultVocabularyStore.getVocabularyByUuid(voc.getUuid()));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.init.DefaultVocabularyStore#loadBasicTerms()}.
	 */
	@Test
	public void testLoadBasicTerms() {
		assertTrue(defaultVocabularyStore.loadBasicTerms());
	}

}
