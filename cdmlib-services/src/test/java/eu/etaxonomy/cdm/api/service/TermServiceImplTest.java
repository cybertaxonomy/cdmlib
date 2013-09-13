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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @created 27.05.2008
 * @version 1.0
 */
public class TermServiceImplTest extends CdmTransactionalIntegrationTest{
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(TermServiceImplTest.class);

    @SpringBeanByType
    private ITermService termService;

    @SpringBeanByType
    private IVocabularyService vocabularyService;

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

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.TermServiceImpl#getContinentByUuid(java.util.UUID)}.
     */
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


    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.TermServiceImpl#listTerms(java.util.UUID)}.
     */
//	@Ignore
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
        Pager<SpecimenTypeDesignationStatus> results = (Pager)termService.page(SpecimenTypeDesignationStatus.class, null,null,null,null);
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
	
}
