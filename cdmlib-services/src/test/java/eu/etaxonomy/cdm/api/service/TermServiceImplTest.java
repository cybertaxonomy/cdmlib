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

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
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
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermServiceImplTest.class);

	@SpringBeanByType
	private ITermService service;
	
	@SpringBeanByType
	private IVocabularyService vocabularyService;

/* ************************* TESTS *************************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TermServiceImpl#getTermByUri(java.lang.String)}.
	 */
//	@Ignore //second part of test throws unexpected exception & also first part fails since language(406) 
	//is also not found here
	@Test
	@DataSet
	public void testGetTermByUri() {
		String uriStr = "http://any.uri.com"; 
		URI uri = URI.create(uriStr);
		DefinedTermBase<?> term = service.getByUri(uri);
		assertNotNull(term);
		//for testing only
//		TermVocabulary<?> voc = term.getVocabulary();
//		service.saveOrUpdate(term);
//		List<MarkerType> list = service.listByTermClass(MarkerType.class, null, null, null, null);
		
		//NULL
		//FIXME throws object not found exception. Wants to load term.voc(11).representation(496).language(124) which does not exist
		//I do not understand where the vocabulary data comes from (checked persistence TermsDataSet-with_auditing_info.xml) but somehow this does not apply
		String uriNotExistStr = "http://www.notExisting.com";
		URI uriNotExist = URI.create(uriNotExistStr);
		DefinedTermBase<?> termNotExist = service.getByUri(uriNotExist);
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
		DefinedTermBase<?> term = service.find(uuid);
		assertNotNull(term);
		assertEquals(Rank.DOMAIN(), term);
		//NULL
		String strUUIDNotExist = "00000000-8b88-417b-a6a0-f7c992aac19c";
		UUID uuidNotExist = UUID.fromString(strUUIDNotExist);
		DefinedTermBase<?> termNotExist = service.find(uuidNotExist);
		assertNull(termNotExist);
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TermServiceImpl#listTerms(java.util.UUID)}.
	 */
//	@Ignore
	@Test
	public void testGetVocabularyUUID() {
		//Rank
		String rankVocabularyUuid = "ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b"; 
		UUID rankUuid = UUID.fromString(rankVocabularyUuid);
		TermVocabulary<Rank> voc = vocabularyService.find(rankUuid);
		assertNotNull(voc);
		assertEquals(66, voc.getTerms().size());
		//Null
		String nullVocabularyUuid = "00000000-26e3-4e83-b47b-ca74eed40b1b"; 
		UUID nullUuid = UUID.fromString(nullVocabularyUuid);
		TermVocabulary<Rank> nullVoc = vocabularyService.find(nullUuid);
		assertNull(nullVoc);
	}

	
	@Test
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
