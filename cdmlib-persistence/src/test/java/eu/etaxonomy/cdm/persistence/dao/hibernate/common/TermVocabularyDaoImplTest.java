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

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import com.mchange.util.AssertException;

import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.location.Continent;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.babadshanjan
 * @created 10.02.2009
 * @version 1.0
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
		List<TermVocabulary<? extends Rank>> rankVocabularies = dao.listByTermClass(Rank.class, false, null, null, null, null);
		assertFalse("There should be at least one vocabulary containing terms of class Rank",rankVocabularies.isEmpty());
		assertEquals("There should be only one vocabulary containing terms of class Rank",1,rankVocabularies.size());
		
		
		rankVocabularies = dao.listByTermClass(Rank.class, true, null, null, null, null);
		assertFalse("There should be at least one vocabulary containing terms of class Rank",rankVocabularies.isEmpty());
		assertEquals("There should be only one vocabulary containing terms of class Rank",1,rankVocabularies.size());
		
		//with subclasses
		List<TermVocabulary<? extends NamedArea>> namedAreaVocabularies = dao.listByTermClass(NamedArea.class, true, null, null, null, null);
		int subclassedSize = namedAreaVocabularies.size();
		assertEquals("There should be 3 vocabularies (TdwgAreas, Continents, WaterbodyOrCountries)", 3, subclassedSize);
		
		List<TermVocabulary<? extends NamedArea>> namedAreaOnlyVocabularies = dao.listByTermClass(NamedArea.class, false, null, null, null, null);
		List<TermVocabulary<? extends TdwgArea>> tdwgVocabularies = dao.listByTermClass(TdwgArea.class, false, null, null, null, null);
		List<TermVocabulary<? extends WaterbodyOrCountry>> countryVocabularies = dao.listByTermClass(WaterbodyOrCountry.class, false, null, null, null, null);
		List<TermVocabulary<? extends Continent>> continentVocabularies = dao.listByTermClass(Continent.class, false, null, null, null, null);
		int sumOfSingleSizes = namedAreaOnlyVocabularies.size() + tdwgVocabularies.size() + countryVocabularies.size() + continentVocabularies.size();
		assertEquals("number of NamedArea and subclasses should be same as sum of all single vocabularies", subclassedSize, sumOfSingleSizes);
		
	}
	
}
