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
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.DataSets;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.TermServiceImpl.TermMovePosition;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.model.term.OrderedTerm;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 * @since 27.05.2008
 */
public class TermServiceImplTest extends CdmTransactionalIntegrationTest{

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private IVocabularyService vocabularyService;

    @SpringBeanByType
    private ITaxonService taxonService;

/* ************************* TESTS *************************************************/

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.TermServiceImpl#getTermByUri(java.lang.String)}.
     */
    @Ignore //second part of test throws unexpected exception & also first part fails since language(406)
    //is also not found here, for an explanation see comment below
    @Test
    /* @DataSet
     * WARNING:
     *    the dataset contains records for DEFINEDTERMBASE,DEFINEDTERMBASE_REPRESENTATION and REPRESENTAION
     *    and thus will cause unitils to empty the according tables, thus all terms etc will be deleted for the
     *    following tests, thus it might be a good idea moving this test to the end
     */
    public void testGetTermByUri() {
        String uriStr = "http://any.uri.com";
        URI uri = URI.create(uriStr);
        DefinedTermBase<?> term = termService.getByUri(uri);
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
        DefinedTermBase<?> termNotExist = termService.getByUri(uriNotExist);
        assertNull(termNotExist);
    }

    @Test
    /* @DataSet
     * WARNING:
     *    the dataset contains records for DEFINEDTERMBASE,DEFINEDTERMBASE_REPRESENTATION and REPRESENTAION
     *    and thus will cause unitils empty the according tables
     */
    public void testGetTermByUuid() {
        // Rank.Domain
        String strUUID = "ffca6ec8-8b88-417b-a6a0-f7c992aac19b";
        UUID uuid = UUID.fromString(strUUID);
        DefinedTermBase<?> term = termService.find(uuid);
        assertNotNull(term);
        assertEquals(Rank.DOMAIN(), term);
        //NULL
        String strUUIDNotExist = "00000000-8b88-417b-a6a0-f7c992aac19c";
        UUID uuidNotExist = UUID.fromString(strUUIDNotExist);
        DefinedTermBase<?> termNotExist = termService.find(uuidNotExist);
        assertNull(termNotExist);
    }

    @Test
    /* @DataSet
     * WARNING:
     *    the dataset contains records for DEFINEDTERMBASE,DEFINEDTERMBASE_REPRESENTATION and REPRESENTAION
     *    and thus will cause unitils empty the according tables
     */
    public void testGetVocabularyUUID() {
        //Rank
        String rankVocabularyUuid = "ef0d1ce1-26e3-4e83-b47b-ca74eed40b1b";
        UUID rankUuid = UUID.fromString(rankVocabularyUuid);
        TermVocabulary<Rank> voc = vocabularyService.find(rankUuid);
        assertNotNull(voc);
        assertEquals(62, voc.getTerms().size());
        //Null
        String nullVocabularyUuid = "00000000-26e3-4e83-b47b-ca74eed40b1b";
        UUID nullUuid = UUID.fromString(nullVocabularyUuid);
        TermVocabulary<Rank> nullVoc = vocabularyService.find(nullUuid);
        assertNull(nullVoc);
    }

    @Test
    /* @DataSet
     * WARNING:
     *    the dataset contains records for DEFINEDTERMBASE,DEFINEDTERMBASE_REPRESENTATION and REPRESENTAION
     *    and thus will cause unitils empty the according tables
     */
    public void testGetAreaByTdwgAbbreviation(){
        String tdwgAbbreviation = "GER-OO";
        NamedArea germany = termService.getAreaByTdwgAbbreviation(tdwgAbbreviation);
        assertEquals(tdwgAbbreviation, germany.getRepresentation(Language.DEFAULT()).getAbbreviatedLabel());
    }

    @Test
    /* @DataSet
     * WARNING:
     *    the dataset contains records for DEFINEDTERMBASE,DEFINEDTERMBASE_REPRESENTATION and REPRESENTAION
     *    and thus will cause unitils empty the according tables
     */
    public void testListTerms() {
        Pager<SpecimenTypeDesignationStatus> results = termService.page(SpecimenTypeDesignationStatus.class, null,null,null,null);
        assertNotNull("Results should not be null",results);
    }

    @Ignore
    @Test
    public void testTitleCacheUpdate(){
    	String uuid = "ae787603-3070-4298-9ca6-4cbe73378122";
    	UUID fromString = UUID.fromString(uuid);
    	DefinedTermBase<?> termBase = termService.find(fromString);

    	// change label
    	String expectedTitleCache = termBase.getLabel() + "append";
    	termBase.setLabel(expectedTitleCache);

    	commitAndStartNewTransaction(null);

    	termBase = termService.find(fromString);
    	assertEquals("Title cache did not update after setting the label and saving the term", expectedTitleCache, termBase.getTitleCache());

    	// add new representation for default language
    	String expecteTitleCacheAfterRepresentationChange = "new label";
    	Representation representation = termBase.getRepresentation(Language.DEFAULT());
    	representation.setLabel(expecteTitleCacheAfterRepresentationChange);
    	termBase.addRepresentation(representation);

    	//this will create another termBase in the DB which has the same UUID -> test failure
//    	termBase.addRepresentation(Representation.NewInstance(expecteTitleCacheAfterRepresentationChange, "", "", Language.DEFAULT()));////new Representation(expecteTitleCacheAfterRepresentationChange, "", "", Language.DEFAULT()));


    	commitAndStartNewTransaction(null);

    	termBase = termService.find(fromString);
    	assertEquals("Title cache did not update after adding a new representation for default language and saving the term", expecteTitleCacheAfterRepresentationChange, termBase.getTitleCache());
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testDeleteTerms(){
    	final String[] tableNames = new String[]{
                "DefinedTermBase","Representation"};

    	//test State
    	commitAndStartNewTransaction(tableNames);
    	TermVocabulary<State> vocStates = TermVocabulary.NewInstance(TermType.State,
    	        State.class, "Test States", null, null, null);
    	vocStates.addTerm(State.NewInstance("green", "green", "gn"));
    	vocabularyService.save(vocStates);
    	Pager<DefinedTermBase> term = termService.findByRepresentationText("green", DefinedTermBase.class, null, null);
    	if (term.getCount() != 0){
    		DeleteResult result = termService.delete(term.getRecords().get(0));
    		assertTrue(result.isOk());
    		commitAndStartNewTransaction(tableNames);
       	}

    	//create identifier type vocabulary
    	TermVocabulary<IdentifierType> vocDna = TermVocabulary.NewInstance(TermType.IdentifierType,
    	        IdentifierType.class, "Test identifier type", null, null, null);
    	vocDna.addTerm(IdentifierType.NewInstance("test", "identifier", "t"));
    	vocabularyService.save(vocDna);

    	vocDna = vocabularyService.find(vocDna.getUuid());
    	Set<IdentifierType> terms = vocDna.getTerms();
    	IdentifierType termBase =terms.iterator().next();
    	termService.delete(termBase, null);
    	//commitAndStartNewTransaction(tableNames);
    	termBase =  (IdentifierType)termService.load(termBase.getUuid());
    	assertNull(termBase);

    	//TermVocabulary<DefinedTerm> voc = TermVocabulary.NewInstance(TermType.Feature, "TestFeatures", null, null, null);
        vocDna.addTerm(IdentifierType.NewInstance("test", "identifier", "t"));
        vocabularyService.save(vocDna);

        vocDna = vocabularyService.find(vocDna.getUuid());
        terms = vocDna.getTerms();
        termBase =terms.iterator().next();
        termBase = (IdentifierType)termService.load(termBase.getUuid());
        IBotanicalName testName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        Taxon testTaxon = Taxon.NewInstance(testName,null);
        testTaxon.addIdentifier("Test", termBase);
        taxonService.save(testTaxon);
        termService.delete(termBase, null);
        //commitAndStartNewTransaction(tableNames);
        termBase =  (IdentifierType)termService.load(termBase.getUuid());
        assertNotNull(termBase);
    }

    @Test
    @DataSets({
        @DataSet(loadStrategy=CleanSweepInsertLoadStrategy.class, value="/eu/etaxonomy/cdm/database/ClearDB_with_Terms_DataSet.xml"),
        @DataSet(value="/eu/etaxonomy/cdm/database/TermsDataSet-with_auditing_info.xml")
    })
    public void testMoveTerm(){
    //	final String[] tableNames = new String[]{
    //                "DefinedTermBase","Representation"};

    	commitAndStartNewTransaction();
    	TermVocabulary<OrderedTerm> vocTest = OrderedTermVocabulary.NewOrderedInstance(TermType.DnaMarker,
    	        OrderedTerm.class, "Test Term Vocabulary", null, null, null);
    	vocTest.addTerm(OrderedTerm.NewInstance(TermType.DnaMarker, "test1", "marker1", "t1"));
    	vocTest = vocabularyService.save(vocTest);

    	OrderedTermVocabulary<OrderedTerm> vocDna = OrderedTermVocabulary.NewOrderedInstance(TermType.DnaMarker,
    	        OrderedTerm.class, "Test DNA marker", null, null, null);
    	vocDna.addTerm(OrderedTerm.NewInstance(TermType.DnaMarker, "test", "marker", "t"));
    	vocDna.addTerm(OrderedTerm.NewInstance(TermType.DnaMarker, "test2", "marker2", "t2"));
    	vocDna.addTerm(OrderedTerm.NewInstance(TermType.DnaMarker, "test3", "marker3", "t3"));
    	vocDna = vocabularyService.save(vocDna);
    	/*
    	 * 					vocDna
    	 *   marker	   marker2	   marker3
    	 *
    	 */

    	Iterator<OrderedTerm> termsTest = vocTest.getTerms().iterator();
    	Iterator<OrderedTerm> termsDna = vocDna.getTerms().iterator();

    	TermDto termToMove = TermDto.fromTerm(termsTest.next());
    	OrderedTerm termBase = (OrderedTerm)termService.load(termToMove.getUuid());
    	assertTrue(termBase.getOrderIndex() == 1);
    	UUID markerUuid = termsDna.next().getUuid();
    	TermDto secondTermDto = TermDto.fromTerm(termsDna.next());
    	UUID secondTermUuid = secondTermDto.getUuid();

    	//move to other vocabulary
    	termService.moveTerm(termToMove, vocDna.getUuid());

    	/*
    	 * 		vocDna
    	 * marker		marker2		marker3		marker1
    	 */
    	//commitAndStartNewTransaction(tableNames);
    	termBase = (OrderedTerm)termService.load(termToMove.getUuid());
    	assertNotNull(termBase);
    	assertTrue(termBase.getVocabulary().getUuid().equals(vocDna.getUuid()));

    	vocTest = vocabularyService.load(vocDna.getUuid());
    	//include marker1 in marker
    	termService.moveTerm(termToMove, markerUuid);
    	//commitAndStartNewTransaction(tableNames);
    	termBase = (OrderedTerm)termService.load(termToMove.getUuid());
    	OrderedTerm marker = (OrderedTerm) termService.load(markerUuid);
    	assertTrue(marker.getIncludes().size() == 1);
    	assertNotNull(termBase);
    	//termToMove is included in and fourth term of vocabulary -> orderIndex == 4
    	assertTrue(termBase.getOrderIndex() == 4);

    	//marker2 should be moved behind marker
    	termService.moveTerm(secondTermDto, markerUuid, TermMovePosition.AFTER);
    	OrderedTerm marker2 = (OrderedTerm)termService.load(secondTermUuid);
    	marker = (OrderedTerm) termService.load(markerUuid);
    	assertTrue("marker2 should be behind marker", marker2.getOrderIndex() == marker.getOrderIndex() + 1);
    }

    /**
     * This test has been implemented to reproduce a potential bug in java runtime.
     * The HashMap used to implement the MultilanguageText failed in jre1.6_11 b03 win32 to
     * to find existing Language keys.
     * <p>
     * Initially this test was implemented in <code>TextDataTest#testPreferredLanguageString()</code> but
     * failed to reproduce the bug in this environment. Therefore it has been additionally implemented as
     * integration test.
     * <p>
     * see https://dev.e-taxonomy.eu/redmine/issues/804
     */
    @Test
    public void testPreferredLanguageString() {
        TextData textData = TextData.NewInstance("testText", Language.DEFAULT(), TextFormat.NewInstance());
        List<Locale> locales = Arrays.asList(Locale.GERMAN, Locale.ENGLISH);
        List<Language> languages = termService.getLanguagesByLocale(Collections.enumeration(locales));
        assertNotNull(textData.getPreferredLanguageString(languages));
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
