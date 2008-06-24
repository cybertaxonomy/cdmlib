/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.*;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author a.mueller
 * @created 27.05.2008
 * @version 1.0
 */
public class TermServiceImplTest {
	private static final Logger logger = Logger.getLogger(TermServiceImplTest.class);

	//@Autowired
	static ITermService service;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		CdmPersistentDataSource defaultSource = CdmPersistentDataSource.NewDefaultInstance();
		CdmApplicationController app = CdmApplicationController.NewInstance(defaultSource, DbSchemaValidation.CREATE);
		service = app.getTermService();
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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

/* ************************* TESTS *************************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TermServiceImpl#setDao(eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao)}.
	 */
	@Test
	public void testSetDao() {
		logger.info("not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TermServiceImpl#getTermByUri(java.lang.String)}.
	 */
	@Ignore //method not yet implemented
	@Test
	public void testGetTermByUri() {
		String uri = ""; 
		DefinedTermBase term = service.getTermByUri(uri);
		assertNotNull(term);
		//assertEquals(Rank.DOMAIN(), term);
		//NULL
		String uriNotExist = "";
		DefinedTermBase termNotExist = service.getTermByUri(uriNotExist);
		assertNull(termNotExist);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TermServiceImpl#getTermByUuid(java.util.UUID)}.
	 */
	@Test
	public void testGetTermByUuid() {
		// Rank.Domain
		String strUUID = "ffca6ec8-8b88-417b-a6a0-f7c992aac19b"; 
		UUID uuid = UUID.fromString(strUUID);
		DefinedTermBase term = service.getTermByUuid(uuid);
		assertNotNull(term);
		assertEquals(Rank.DOMAIN(), term);
		//NULL
		String strUUIDNotExist = "00000000-8b88-417b-a6a0-f7c992aac19c";
		UUID uuidNotExist = UUID.fromString(strUUIDNotExist);
		DefinedTermBase termNotExist = service.getTermByUuid(uuidNotExist);
		assertNull(termNotExist);
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TermServiceImpl#listTerms(java.util.UUID)}.
	 */
	@Ignore
	@Transactional
	@Test
	public void testGetVocabularyUUID() {
		//Rank
		String rankVocabularyUuid = "ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b"; 
		UUID rankUuid = UUID.fromString(rankVocabularyUuid);
		TermVocabulary<Rank> voc = service.getVocabulary(rankUuid);
		assertNotNull(voc);
		assertEquals(61, voc.getTerms().size());
		//Null
		String nullVocabularyUuid = "00000000-26e3-4e83-b47b-ca74eed40b1b"; 
		UUID nullUuid = UUID.fromString(nullVocabularyUuid);
		TermVocabulary<Rank> nullVoc = service.getVocabulary(nullUuid);
		assertNull(nullVoc);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TermServiceImpl#listVocabularies(java.lang.Class)}.
	 */
	@Ignore //method not yet implemented
	@Test
	public void testSetVocabularies() {
		logger.warn("Not yet implemented");
	}
}
