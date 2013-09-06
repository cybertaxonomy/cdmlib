/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.babadshanjan
 * @created 10.02.2009
 */
public class TermVocabularyDaoImplTest extends CdmIntegrationTest {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TermVocabularyDaoImplTest.class);

	@SpringBeanByType
	private ITermVocabularyDao dao;

	@Before
	public void setUp() {
	}

	@Test
	public void testListVocabularyByClass() {
		//test class with no subclasses
		List<TermVocabulary<? extends Rank>> rankVocabularies = dao.listByTermClass(Rank.class, false, false, null, null, null, null);
		assertFalse("There should be at least one vocabulary containing terms of class Rank",rankVocabularies.isEmpty());
		assertEquals("There should be only one vocabulary containing terms of class Rank",1,rankVocabularies.size());
		
		
		rankVocabularies = dao.listByTermClass(Rank.class, true, false, null, null, null, null);
		assertFalse("There should be at least one vocabulary containing terms of class Rank",rankVocabularies.isEmpty());
		assertEquals("There should be only one vocabulary containing terms of class Rank",1,rankVocabularies.size());
		
		//with subclasses
		List<TermVocabulary<? extends NamedArea>> namedAreaVocabularies = dao.listByTermClass(NamedArea.class, true, false, null, null, null, null);
		int subclassedSize = namedAreaVocabularies.size();
		assertEquals("There should be 3 vocabularies (TdwgAreas, Continents, WaterbodyOrCountries)", 4, subclassedSize);
		
		List<TermVocabulary<? extends NamedArea>> namedAreaOnlyVocabularies = dao.listByTermClass(NamedArea.class, false, false, null, null, null, null);
		List<TermVocabulary<? extends WaterbodyOrCountry>> countryVocabularies = dao.listByTermClass(WaterbodyOrCountry.class, false, false, null, null, null, null);
		int sumOfSingleSizes = namedAreaOnlyVocabularies.size() + countryVocabularies.size();
		assertEquals("number of NamedArea and subclasses should be same as sum of all single vocabularies", subclassedSize, sumOfSingleSizes);

	}
	
	@Test
	@DataSet("TermVocabularyDaoImplTest.testListVocabularyEmpty.xml")
	public void testListVocabularyByClassEmpty() {
		//test include empty
		List<TermVocabulary<? extends NamedArea>> namedAreaVocabulariesAndEmpty = dao.listByTermClass(NamedArea.class, true, true, null, null, null, null);
		assertEquals("There should be 1 vocabulary (the empty one)", 1, namedAreaVocabulariesAndEmpty.size());

		List<TermVocabulary<? extends Language>> languageVocabulariesAndEmpty = dao.listByTermClass(Language.class, true, true, null, null, null, null);
		assertEquals("There should be 2 vocabularies, the empty one and the one that has a language term in", 2, languageVocabulariesAndEmpty.size());
	}	
	
	@Test
	@DataSet("TermVocabularyDaoImplTest.testListVocabularyEmpty.xml")
	public void testListVocabularyEmpty() {
		//test class with no subclasses
		List<TermVocabulary> emptyVocs = dao.listEmpty(null, null, null, null);
		assertFalse("There should be at least one vocabulary containing no terms",emptyVocs.isEmpty());
		assertEquals("There should be only one vocabulary containing terms of class Rank",1,emptyVocs.size());
		UUID uuidEmptyVoc = UUID.fromString("f253962f-d787-4b16-b2d2-e645da73ae4f");
		assertEquals("The empty vocabulary should be the one defined", uuidEmptyVoc, emptyVocs.get(0).getUuid());
	}
	
	
}
