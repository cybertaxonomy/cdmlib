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
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.mueller
 *
 */
public class NameServiceImplTest2 extends CdmIntegrationTest {
	private static final Logger logger = Logger.getLogger(NameServiceImplTest2.class);

	@SpringBeanByType
	private INameService service;
	
/* ******************** TESTS ********************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#setDao(eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao)}.
	 */
	@Ignore
	@Test
	public void testSetDao() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#setVocabularyDao(eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao)}.
	 */
	@Ignore
	@Test
	public void testSetVocabularyDao() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#NameServiceImpl()}.
	 */
	@Ignore
	@Test
	public void testNameServiceImpl() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getNamesByName(java.lang.String)}.
	 */
	@Ignore
	@Test
	public void testGetNamesByName() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getTaxonNameByUuid(java.util.UUID)}.
	 */
	@Ignore
	@Test
	public void testGetTaxonNameByUuid() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#saveTaxonName(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Ignore
	@Test
	public void testSaveTaxonName() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#saveTaxonNameAll(java.util.Collection)}.
	 */
	@Ignore
	@Test
	public void testSaveTaxonNameAll() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#removeTaxon(eu.etaxonomy.cdm.model.name.TaxonNameBase)}.
	 */
	@Ignore
	@Test
	public void testRemoveTaxon() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getAllNames(int, int)}.
	 */
	@Ignore
	@Test
	public void testGetAllNames() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getRankVocabulary()}.
	 */
	@Ignore
	@Test
	public void testGetRankVocabulary() {
		OrderedTermVocabulary<Rank> rankVocabulary = service.getRankVocabulary();
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
	@Ignore
	@Test
	public void testGetTypeDesignationVocabulary() {
		OrderedTermVocabulary<TypeDesignationStatus> typeDesignationVocabulary = 
			service.getTypeDesignationVocabulary();
		assertNotNull(typeDesignationVocabulary);
		assertEquals(62, typeDesignationVocabulary.size());
		TypeDesignationStatus highestType = typeDesignationVocabulary.getHighestTerm();
		assertEquals(TypeDesignationStatus.EPITYPE(), highestType);
		assertEquals(TypeDesignationStatus.HOLOTYPE(), typeDesignationVocabulary.getNextLowerTerm(highestType));
		assertSame(TypeDesignationStatus.EPITYPE(), highestType);
		assertSame(TypeDesignationStatus.HOLOTYPE(), typeDesignationVocabulary.getNextLowerTerm(highestType));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#generateTitleCache()}.
	 */
	@Ignore
	@Test
	public void testGenerateTitleCache() {
		logger.warn("Not yet implemented");
	}

}
