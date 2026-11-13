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
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @since Aug 07, 2014
 */
//@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-test.xml")
public class VocabularyServiceImplTest extends CdmTransactionalIntegrationTest{

	@SpringBeanByType
	private IVocabularyService vocabularyService;

	@Before
	public void setUp() throws Exception {}

    @Test
    public void testGetRankVocabulary() {
        List<TermVocabulary<Rank>> rankVocabularyList = vocabularyService.findByTermType(TermType.Rank, null);
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

    @Test
    public void testGetTypeDesignationVocabulary() {
        List<TermVocabulary<SpecimenTypeDesignationStatus>> typeDesignationVocabularyList =
        		vocabularyService.findByTermType(TermType.SpecimenTypeDesignationStatus, null);
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

    /**
     * Property path as used by {@code VocabularyController} for term loading.
     */
    @Test
    public void testLoadWithTermsAndIncludesPropertyPath() {
        TermVocabulary<Feature> vocabulary = TermVocabulary.NewInstance(
                TermType.Feature, Feature.class, "Voc load propertyPath test", "VocLoadTest", null, null);
        Feature parent = Feature.NewInstance("Parent feature", "Parent", "par");
        Feature child = Feature.NewInstance("Child feature", "Child", "ch");
        vocabulary.addTerm(parent);
        vocabulary.addTerm(child);
        parent.addIncludes(child);

        UUID vocabularyUuid = vocabularyService.save(vocabulary).getUuid();
        UUID parentUuid = parent.getUuid();
        UUID childUuid = child.getUuid();
        commitAndStartNewTransaction();

        // without init strategy: terms.includes stays lazy after session close
        TermVocabulary<?> withoutPath = vocabularyService.load(vocabularyUuid);
        DefinedTermBase<?> parentWithoutPath = findTerm(withoutPath, parentUuid);
        assertNotNull(parentWithoutPath);
        commitAndStartNewTransaction();
        try {
            parentWithoutPath.getIncludes().size();
            fail("terms.includes should not be accessible without property path after session close");
        } catch (LazyInitializationException expected) {
            // expected
        }

        List<String> propertyPaths = Arrays.asList(new String[]{
                "terms.$",
                "terms.includes.$",
                "includes"
        });
        TermVocabulary<?> loaded = vocabularyService.load(vocabularyUuid, propertyPaths);
        assertNotNull(loaded);
        assertTrue("terms should be initialized by terms.$", Hibernate.isInitialized(loaded.getTerms()));
        assertEquals(2, loaded.getTerms().size());

        DefinedTermBase<?> parentWithPath = findTerm(loaded, parentUuid);
        assertNotNull(parentWithPath);
        Set<? extends DefinedTermBase<?>> includes = parentWithPath.getIncludes();
        assertEquals(1, includes.size());
        DefinedTermBase<?> included = includes.iterator().next();
        assertEquals(childUuid, included.getUuid());
        // $ initializes to-one associations of the included term (e.g. vocabulary)
        assertTrue(Hibernate.isInitialized(included.getVocabulary()));

        // associations must remain usable after the session is closed
        commitAndStartNewTransaction();
        assertEquals(2, loaded.getTerms().size());
        assertEquals(1, parentWithPath.getIncludes().size());
        assertEquals(childUuid, parentWithPath.getIncludes().iterator().next().getUuid());
        assertEquals(vocabularyUuid, included.getVocabulary().getUuid());
    }

    private DefinedTermBase<?> findTerm(TermVocabulary<?> vocabulary, UUID termUuid) {
        for (DefinedTermBase<?> term : vocabulary.getTerms()) {
            if (termUuid.equals(term.getUuid())) {
                return term;
            }
        }
        return null;
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
