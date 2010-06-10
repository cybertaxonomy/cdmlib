/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 * @created 27.05.2008
 * @version 1.0
 */
public class TermServiceImplTest extends CdmIntegrationTest {
	private static final Logger logger = Logger.getLogger(TermServiceImplTest.class);

	@SpringBeanByType
	private ITermService service;
	
	@SpringBeanByType
	private IVocabularyService vocabularyService;

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
		DefinedTermBase term = service.getByUri(uri);
		assertNotNull(term);
		//assertEquals(Rank.DOMAIN(), term);
		//NULL
		String uriNotExist = "";
		DefinedTermBase termNotExist = service.getByUri(uriNotExist);
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
		DefinedTermBase term = service.find(uuid);
		assertNotNull(term);
		assertEquals(Rank.DOMAIN(), term);
		//NULL
		String strUUIDNotExist = "00000000-8b88-417b-a6a0-f7c992aac19c";
		UUID uuidNotExist = UUID.fromString(strUUIDNotExist);
		DefinedTermBase termNotExist = service.find(uuidNotExist);
		assertNull(termNotExist);
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TermServiceImpl#listTerms(java.util.UUID)}.
	 */
	@Ignore
	@Test
	public void testGetVocabularyUUID() {
		//Rank
		String rankVocabularyUuid = "ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b"; 
		UUID rankUuid = UUID.fromString(rankVocabularyUuid);
		TermVocabulary<Rank> voc = vocabularyService.find(rankUuid);
		assertNotNull(voc);
		assertEquals(61, voc.getTerms().size());
		//Null
		String nullVocabularyUuid = "00000000-26e3-4e83-b47b-ca74eed40b1b"; 
		UUID nullUuid = UUID.fromString(nullVocabularyUuid);
		TermVocabulary<Rank> nullVoc = vocabularyService.find(nullUuid);
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
	
	@Test
	@Ignore //FIXME ignoring just for today 9.6.2010 a.kohlbecker !!!!!!!!!!!!!!!!!!!!!
	public void testGetAreaByTdwgAbbreviation(){
		String tdwgAbbreviation = "GER-OO";
		NamedArea germany = service.getAreaByTdwgAbbreviation(tdwgAbbreviation);
		assertEquals(tdwgAbbreviation, germany.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel());
	}
	
	@Test
	public void testListTerms() {
		Pager<SpecimenTypeDesignationStatus> results = (Pager)service.page(SpecimenTypeDesignationStatus.class, null,null,null,null);
		assertNotNull("Results should not be null",results);
	}
}
