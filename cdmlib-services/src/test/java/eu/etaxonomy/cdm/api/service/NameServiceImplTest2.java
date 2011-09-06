/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 *
 */
@Ignore
public class NameServiceImplTest2 extends CdmIntegrationTest {
	private static final Logger logger = Logger.getLogger(NameServiceImplTest2.class);

	@SpringBeanByType
	private INameService nameService;
	
/* ******************** TESTS ********************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#setDao(eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao)}.
	 */
	@Test
	public void testSetDao() {
//		Assert.assertNotNull(((NameServiceImpl)nameService).dao);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#setVocabularyDao(eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao)}.
	 */
	@Test
	public void testSetVocabularyDao() {
//		Assert.assertNotNull(( (NameServiceImpl)nameService).vocabularyDao);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getNamesByName(java.lang.String)}.
	 */
	@Test
	public void testGetNamesByName() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getTaxonNameByUuid(java.util.UUID)}.
	 */
	@Test
	public void testGetTaxonNameByUuid() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#saveTaxonName(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Test
	public void testSaveTaxonName() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#saveTaxonNameAll(java.util.Collection)}.
	 */
	@Test
	public void testSaveTaxonNameAll() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#removeTaxon(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Test
	public void testRemoveTaxon() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getAllNames(int, int)}.
	 */
	@Test
	public void testGetAllNames() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getRankVocabulary()}.
	 */
	@Test
	public void testGetRankVocabulary() {
		//TODO move test to vocabulary service
		OrderedTermVocabulary<Rank> rankVocabulary = nameService.getRankVocabulary();
		assertNotNull(rankVocabulary);
		assertEquals(62, rankVocabulary.size());
		Rank highestRank = rankVocabulary.getHighestTerm();
		assertEquals(Rank.EMPIRE(), highestRank);
		assertEquals(Rank.DOMAIN(), rankVocabulary.getNextLowerTerm(highestRank));
		assertSame(Rank.EMPIRE(), highestRank);
		assertSame(Rank.DOMAIN(), rankVocabulary.getNextLowerTerm(highestRank));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getTypeDesignationVocabulary()}.
	 */
	@Test
	public void testGetTypeDesignationVocabulary() {
		//TODO move test to vocabulary service
		OrderedTermVocabulary<SpecimenTypeDesignationStatus> typeDesignationVocabulary = 
			nameService.getSpecimenTypeDesignationVocabulary();
		assertNotNull(typeDesignationVocabulary);
		assertEquals(62, typeDesignationVocabulary.size());
		SpecimenTypeDesignationStatus highestType = typeDesignationVocabulary.getHighestTerm();
		assertEquals(SpecimenTypeDesignationStatus.EPITYPE(), highestType);
		assertEquals(SpecimenTypeDesignationStatus.HOLOTYPE(), typeDesignationVocabulary.getNextLowerTerm(highestType));
		assertSame(SpecimenTypeDesignationStatus.EPITYPE(), highestType);
		assertSame(SpecimenTypeDesignationStatus.HOLOTYPE(), typeDesignationVocabulary.getNextLowerTerm(highestType));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
	 */
	@Test
	public void testGenerateTitleCache() {
		logger.warn("Not yet implemented");
	}

}
