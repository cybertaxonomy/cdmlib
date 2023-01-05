/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.term;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dao.term.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dto.TermVocabularyDto;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.babadshanjan
 * @since 10.02.2009
 */
public class TermVocabularyDaoImplTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByType
	private ITermVocabularyDao dao;

	@Before
	public void setUp() {}

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet("/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")}
    )
    public void testListVocabularyByType() {
        //test class with no subclasses
        List<TermVocabulary> rankVocabularies = dao.listByTermType(TermType.Rank, false, null, null, null, null);
        assertFalse("There should be at least one vocabulary containing terms of type Rank", rankVocabularies.isEmpty());
        assertEquals("There should be only one vocabulary containing terms of type Rank", 1, rankVocabularies.size());

        //include subtype, but termtype has no subtype
        rankVocabularies = dao.listByTermType(TermType.Rank, true, null, null, null, null);
        assertFalse("There should be at least one vocabulary containing terms of type Rank", rankVocabularies.isEmpty());
        assertEquals("There should be only one vocabulary containing terms of type Rank", 1, rankVocabularies.size());

        //with different classes
        List<TermVocabulary> namedAreaVocabularies = dao.listByTermType(TermType.NamedArea, true, null, null, null, null);
        int subclassedSize = namedAreaVocabularies.size();
        assertEquals("There should be 4 vocabularies (TdwgAreas, Continents, Waterbody, Countries)", 4, subclassedSize);

        //with sub types
        List<TermVocabulary> scopeVocabularies = dao.listByTermType(TermType.Scope, true, null, null, null, null);
        int subtypeSize = scopeVocabularies.size();
        assertEquals("There should be 6 vocabularies (Scope, Sex, Stage, 3 x KindOfUnit)", 6, subtypeSize);

        List<TermVocabulary> scopeOnlyVocabularies = dao.listByTermType(TermType.Scope, false, null, null, null, null);
        assertEquals("Scope only vocabularies w/o subtypes should be 1", 1, scopeOnlyVocabularies.size());
        List<TermVocabulary> stageVocabularies = dao.listByTermType(TermType.Stage, false, null, null, null, null);
        assertEquals("Stage only vocabularies should be 1", 1, stageVocabularies.size());
    }


    @Test
    @DataSet("TermVocabularyDaoImplTest.testListVocabularyEmpty.xml")
    public void testListVocabularyByTypeEmpty() {
        List<TermVocabulary> emptyNamedAreas = dao.listByTermType(TermType.NamedArea, true, null, null, null, null);
        assertEquals("There should be no vocabulary, as we do not return ALL empty vocabularies of ANY type anymore", 0, emptyNamedAreas.size());

        List<TermVocabulary> languageVocabulariesAndEmpty = dao.listByTermType(TermType.Language, true, null, null, null, null);
        assertEquals("There should be 2 vocabularies, the empty one and the one that has a language term in", 2, languageVocabulariesAndEmpty.size());
    }

	@Test()
	@DataSets({
	      @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
	      @DataSet("/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")}
	)
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

	    //default titleCache
	    dao.save(newVoc);
	    newVoc.setProtectedTitleCache(true);  //make sure we use the title cache created during save by listeners
	    String emptyLabel = newVoc.getTitleCache();
	    //this value may need to be changed when the default cache generation changes
	    Assert.assertEquals("TitleCache should use default title generation", "TermVocabulary<a7a2fbe4-3a35-4ec0-b2b2-2298c3ebdf57>", emptyLabel);

	    //only German
	    newVoc.setProtectedTitleCache(false);
	    Representation newRepresentation = Representation.NewInstance("Beschreibung", "Deutsches Label", "Abk.", Language.GERMAN());
	    newVoc.addRepresentation(newRepresentation);
	    dao.saveOrUpdate(newVoc);
	    newVoc.setProtectedTitleCache(true);
	    Assert.assertEquals("German Label should be new title cache", "Deutsches Label", newVoc.getTitleCache());

	    //German and English
	    newVoc.setProtectedTitleCache(false);
        Representation englishRepresentation = Representation.NewInstance("Description", "English label", "Abbrev.", Language.DEFAULT());
        newVoc.addRepresentation(englishRepresentation);
        dao.saveOrUpdate(newVoc);
        newVoc.setProtectedTitleCache(true);
        Assert.assertEquals("English Label should be new title cache", "English label", newVoc.getTitleCache());

        //Change English label
        newVoc.setProtectedTitleCache(false);
        newVoc.setLabel("New English label");
        dao.saveOrUpdate(newVoc);
        newVoc.setProtectedTitleCache(true);
        Assert.assertEquals("English (default language) label should be new English label", "New English label", newVoc.getTitleCache());

        //Remove English
        newVoc.setProtectedTitleCache(false);
        newVoc.removeRepresentation(englishRepresentation);
        dao.saveOrUpdate(newVoc);
        newVoc.setProtectedTitleCache(true);
        Assert.assertEquals("German Label should be new title cache again as English representation is not there anymore", "Deutsches Label", newVoc.getTitleCache());

	}

	@Test
    @DataSets({
          @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
          @DataSet("/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")}
    )
	public void testFindVocabularyDtoByTermTypes(){
	    Set<TermType> termTypes = new HashSet<>();
	    termTypes.add(TermType.NamedArea);
	    List<TermVocabularyDto> vocDtos = dao.findVocabularyDtoByTermTypes(termTypes, true);
	    Assert.assertEquals(4, vocDtos.size());

	    //#9825 test deduplication
        @SuppressWarnings("unchecked")
        TermVocabulary<NamedArea> continentVoc = dao.findByUuid(NamedArea.uuidContinentVocabulary);
        continentVoc.addRepresentation(Representation.NewInstance("Kontinente", "Kontinente", "Kont.", Language.GERMAN()));
        vocDtos = dao.findVocabularyDtoByTermTypes(termTypes, true);
        Assert.assertEquals("Deduplication for representations does not work", 4, vocDtos.size());
        int repSize = vocDtos.stream().filter(voc->voc.getUuid().equals(NamedArea.uuidContinentVocabulary)).findFirst().get().getRepresentations().size();
        Assert.assertEquals("There should be 2 representations for continent vocabulary", 2, repSize);
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
