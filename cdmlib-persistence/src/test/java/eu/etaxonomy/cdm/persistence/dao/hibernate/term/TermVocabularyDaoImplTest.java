/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.term;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.term.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dto.TermVocabularyDto;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

/**
 * @author a.babadshanjan
 * @since 10.02.2009
 */
public class TermVocabularyDaoImplTest extends CdmTransactionalSpringIntegrationTest {

	@Autowired
	private ITermVocabularyDao dao;

    @Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
    public void testListVocabularyByType() {
        //test class with no subclasses
        List<TermVocabulary> rankVocabularies = dao.listByTermType(TermType.Rank, false, null, null, null, null);
        assertFalse(rankVocabularies.isEmpty(), "There should be at least one vocabulary containing terms of type Rank");
        assertEquals(1, rankVocabularies.size(), "There should be only one vocabulary containing terms of type Rank");

        //include subtype, but termtype has no subtype
        rankVocabularies = dao.listByTermType(TermType.Rank, true, null, null, null, null);
        assertFalse(rankVocabularies.isEmpty(), "There should be at least one vocabulary containing terms of type Rank");
        assertEquals(1, rankVocabularies.size(), "There should be only one vocabulary containing terms of type Rank");

        //with different classes
        List<TermVocabulary> namedAreaVocabularies = dao.listByTermType(TermType.NamedArea, true, null, null, null, null);
        int subclassedSize = namedAreaVocabularies.size();
        assertEquals(4, subclassedSize, "There should be 4 vocabularies (TdwgAreas, Continents, Waterbody, Countries)");

        //with sub types
        List<TermVocabulary> scopeVocabularies = dao.listByTermType(TermType.Scope, true, null, null, null, null);
        int subtypeSize = scopeVocabularies.size();
        assertEquals(6, subtypeSize, "There should be 6 vocabularies (Scope, Sex, Stage, 3 x KindOfUnit)");

        List<TermVocabulary> scopeOnlyVocabularies = dao.listByTermType(TermType.Scope, false, null, null, null, null);
        assertEquals(1, scopeOnlyVocabularies.size(), "Scope only vocabularies w/o subtypes should be 1");
        List<TermVocabulary> stageVocabularies = dao.listByTermType(TermType.Stage, false, null, null, null, null);
        assertEquals(1, stageVocabularies.size(), "Stage only vocabularies should be 1");
    }


    @Test
    @CdmDataSet("TermVocabularyDaoImplTest.testListVocabularyEmpty.xml")
    public void testListVocabularyByTypeEmpty() {
        List<TermVocabulary> emptyNamedAreas = dao.listByTermType(TermType.NamedArea, true, null, null, null, null);
        assertEquals(0, emptyNamedAreas.size(), "There should be no vocabulary, as we do not return ALL empty vocabularies of ANY type anymore");

        List<TermVocabulary> languageVocabulariesAndEmpty = dao.listByTermType(TermType.Language, true, null, null, null, null);
        assertEquals(2, languageVocabulariesAndEmpty.size(), "There should be 2 vocabularies, the empty one and the one that has a language term in");
    }

	@Test()
	@CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
	public void testMissingTermUuids() {
		List<UUID> uuidSet = new ArrayList<>();
		uuidSet.add(Language.uuidEnglish);
		uuidSet.add(Language.uuidFrench);
		UUID uuidNotExisting = UUID.fromString("e93e8c10-d9d2-4ad6-9907-952da6d139c4");
		uuidSet.add(uuidNotExisting);
		Map<UUID, List<UUID>> uuidVocs = new HashMap<>();
		uuidVocs.put( Language.uuidLanguageVocabulary, uuidSet);
		Map<UUID, Set<UUID>> notExisting = new HashMap<>();
		Map<UUID, TermVocabulary<?>> vocabularyMap = new HashMap<>();

		dao.missingTermUuids(uuidVocs, notExisting, vocabularyMap);

		//assert missing terms
		assertEquals(Integer.valueOf(1), Integer.valueOf(notExisting.keySet().size()));
		assertEquals(Language.uuidLanguageVocabulary, notExisting.keySet().iterator().next());
		Set<UUID> missingLanguageTerms = notExisting.get(Language.uuidLanguageVocabulary );
		assertEquals(Integer.valueOf(1), Integer.valueOf(missingLanguageTerms.size()));
		assertEquals(uuidNotExisting, missingLanguageTerms.iterator().next());
	}

	@Test
    public void testTitleCacheCreation() {

	    //prepare
	    UUID vocUuid = UUID.fromString("a7a2fbe4-3a35-4ec0-b2b2-2298c3ebdf57");
	    TermVocabulary<?> newVoc = TermVocabulary.NewInstance(TermType.Modifier);
	    newVoc.setUuid(vocUuid);
	    dao.save(newVoc);

	    //default titleCache
	    newVoc.setProtectedTitleCache(true);  //make sure we use the title cache created during save by listeners
	    String emptyLabel = newVoc.getTitleCache();
	    //this value may need to be changed when the default cache generation changes
	    assertEquals("TermVocabulary<a7a2fbe4-3a35-4ec0-b2b2-2298c3ebdf57>", emptyLabel, "TitleCache should use default title generation");

	    //only German
	    newVoc.setProtectedTitleCache(false);
	    Representation newRepresentation = Representation.NewInstance("Beschreibung", "Deutsches Label", "Abk.", Language.GERMAN());
	    newVoc.addRepresentation(newRepresentation);
        newVoc = dao.save(newVoc); //test titleCache generation with save
	    newVoc.setProtectedTitleCache(true);
	    assertEquals("Deutsches Label", newVoc.getTitleCache(), "German Label should be new title cache");

	    //German and English
	    newVoc.setProtectedTitleCache(false);
        Representation englishRepresentation = Representation.NewInstance("Description", "English label", "Abbrev.", Language.DEFAULT());
        newVoc.addRepresentation(englishRepresentation);
        newVoc = dao.save(newVoc); //test titleCache generation with save
        newVoc.setProtectedTitleCache(true);
        assertEquals("English label", newVoc.getTitleCache(), "English Label should be new title cache");

        //Change English label
        newVoc.setProtectedTitleCache(false);
        newVoc.setLabel("New English label");
        newVoc = dao.merge(newVoc); //test titleCache generation with merge
        newVoc.setProtectedTitleCache(true);
        assertEquals("New English label", newVoc.getTitleCache(), "English (default language) label should be new English label");

        //Remove English
        newVoc.setProtectedTitleCache(false);
        newVoc.removeRepresentation(englishRepresentation);
        newVoc = dao.merge(newVoc);  //test titleCache generation with merge
        newVoc.setProtectedTitleCache(true);
        assertEquals("Deutsches Label", newVoc.getTitleCache(), "German Label should be new title cache again as English representation is not there anymore");
	}

	@Test
    @CdmDataSet(cleanSweep = true, value = {
            "/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml",
            "/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml"
    })
	public void testFindVocabularyDtoByTermTypes(){
	    Set<TermType> termTypes = new HashSet<>();
	    termTypes.add(TermType.NamedArea);
	    List<TermVocabularyDto> vocDtos = dao.findVocabularyDtoByTermTypes(termTypes, true);
	    assertEquals(4, vocDtos.size());

	    //#9825 test deduplication
        @SuppressWarnings("unchecked")
        TermVocabulary<NamedArea> continentVoc = dao.findByUuid(NamedArea.uuidContinentVocabulary);
        continentVoc.addRepresentation(Representation.NewInstance("Kontinente", "Kontinente", "Kont.", Language.GERMAN()));
        vocDtos = dao.findVocabularyDtoByTermTypes(termTypes, true);
        assertEquals(4, vocDtos.size(), "Deduplication for representations does not work");
        int repSize = vocDtos.stream().filter(voc->voc.getUuid().equals(NamedArea.uuidContinentVocabulary)).findFirst().get().getRepresentations().size();
        assertEquals(2, repSize, "There should be 2 representations for continent vocabulary");
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
