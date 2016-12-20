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
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @created Aug 07, 2014
 */
//@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-test.xml")
public class VocabularyServiceImplTest extends CdmTransactionalIntegrationTest{

	@SpringBeanByType
	private IVocabularyService vocabularyService;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {}


    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getRankVocabulary()}.
     */
    @Test
//    @Ignore //FIXME assertSame does not work yet
    public void testGetRankVocabulary() {
        List<TermVocabulary<Rank>> rankVocabularyList = vocabularyService.findByTermType(TermType.Rank);
        assertTrue(rankVocabularyList.size() == 1);
        OrderedTermVocabulary<Rank> rankVocabulary = (OrderedTermVocabulary<Rank>)rankVocabularyList.get(0);
        assertNotNull(rankVocabulary);
        assertEquals(62, rankVocabulary.size());
        Rank highestRank = rankVocabulary.getHighestTerm();
        assertEquals(Rank.EMPIRE(), highestRank);
        assertEquals(Rank.DOMAIN(), rankVocabulary.getNextLowerTerm(highestRank));
//        assertSame(Rank.EMPIRE(), highestRank);  //as we do not use second level cache this is not required
//        assertSame(Rank.DOMAIN(), rankVocabulary.getNextLowerTerm(highestRank)); //as we do not use second level cache this is not required
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.NameServiceImpl#getTypeDesignationVocabulary()}.
     */
    @Test
//    @Ignore  //not yet correctly implemented
    public void testGetTypeDesignationVocabulary() {
        List<TermVocabulary<SpecimenTypeDesignationStatus>> typeDesignationVocabularyList =
        		vocabularyService.findByTermType(TermType.SpecimenTypeDesignationStatus);
        assertTrue(typeDesignationVocabularyList.size() == 1);
        OrderedTermVocabulary<SpecimenTypeDesignationStatus> typeDesignationVocabulary = (OrderedTermVocabulary<SpecimenTypeDesignationStatus>)typeDesignationVocabularyList.get(0);

        assertNotNull(typeDesignationVocabulary);
        assertEquals(16, typeDesignationVocabulary.size());
        SpecimenTypeDesignationStatus highestType = typeDesignationVocabulary.getHighestTerm();
        assertEquals(SpecimenTypeDesignationStatus.HOLOTYPE(), highestType);
        assertEquals(SpecimenTypeDesignationStatus.LECTOTYPE(), typeDesignationVocabulary.getNextLowerTerm(highestType));
//      assertSame(SpecimenTypeDesignationStatus.EPITYPE(), highestType);   //as we do not use second level cache this is not required
//      assertSame(SpecimenTypeDesignationStatus.HOLOTYPE(), typeDesignationVocabulary.getNextLowerTerm(highestType));   //as we do not use second level cache this is not required
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
