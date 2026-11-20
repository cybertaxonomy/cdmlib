/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmExpectedDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

/**
 * @author a.mueller
 * @since 25.11.2010
 */
public class DescriptionElementDaoHibernateImplTest
        extends CdmTransactionalSpringIntegrationTest {

    private static final Logger logger = LogManager.getLogger();

    private final UUID uuidSingleTextData = UUID.fromString("31a0160a-51b2-4565-85cf-2be58cb561d6");
    private final UUID uuidDobuleTextData = UUID.fromString("50f6b799-3585-40a7-b69d-e7be77b2651a");

    private final boolean printDatasets = false;

    @Autowired
	private IDescriptionElementDao descriptionElementDao;

	@Autowired
	private IDefinedTermDao termDao;

// ***************************** TESTS ************************************/

	@Test
	public void testCountMedia() {
		logger.warn("testCountMedia not yet implemented");
	}

	/**
	 * See #2114
	 */
	@Test
	@CdmDataSet // (value="DescriptionElementDaoHibernateImplTest.xml")
	public void testRetrieveMultiLanguageString(){
//		int count = descriptionElementDao.count(TextData.class);
//		assertTrue(count > 0, "There must exist TextData");

		//test read
		TextData textDataTwo = (TextData)descriptionElementDao.findByUuid(uuidDobuleTextData);
		assertEquals(2, textDataTwo.size(), "There should be exactly 2 languageText in the multilanguageText");
		assertTrue(textDataTwo.containsKey(Language.ENGLISH()), "One language should be English");
		Language eng = Language.ENGLISH();
//		System.out.println("English: " + eng.getLabel() + "("+ eng.getId() + ", " + eng.getUuid() + ")");
//
//		for (Language lang: multiLangText.keySet()){
//			System.out.println(lang.getLabel() + "("+ lang.getId() + ", " + lang.getUuid() + ")");
//			boolean equal = lang.equals(eng);
//			System.out.println(equal);
//		}
//		boolean contains = multiLangText.keySet().contains(eng);
//		String et = doubleLanguageTextData.getText(eng);
		LanguageString englishText = textDataTwo.getLanguageText(eng);
		assertNotNull(englishText, "English text should exist");
		assertEquals("Praesent vitae turpis vitae sapien sodales sagittis.", englishText.getText(), "The English text should be correct");
		LanguageString czechText = textDataTwo.getLanguageText(Language.CZECH());
		assertNotNull(czechText, "Czech");
		assertEquals("A Czech translation for Praesent ...", czechText.getText(), "The Czech text should be correct");
	}

	/**
	 * See #2114
	 */
	@Test
	@CdmDataSet //(value="DescriptionElementDaoHibernateImplTest.xml")
	@CdmExpectedDataSet
	public void testPersistMultiLanguageString(){

		//test write
		TextData singleLanguageTextData = (TextData)descriptionElementDao.findByUuid(uuidSingleTextData);
		Map<Language, LanguageString> multiLangText = singleLanguageTextData.getMultilanguageText();
		assertEquals(1, multiLangText.size(), "There should be exactly 1 languageText in the multilanguageText");
		assertTrue(multiLangText.containsKey(Language.ENGLISH()), "The language should be English");
		singleLanguageTextData.putText(Language.GERMAN(), "Ein test auf deutsch");
		assertEquals(2, singleLanguageTextData.size(), "There should be exactly 2 languageText in the multilanguageText");
		String germanText = singleLanguageTextData.getText(Language.GERMAN());
		assertNotNull(germanText, "German text should exist");

		LanguageString germanLanguageText = singleLanguageTextData.getLanguageText(Language.GERMAN());
		assertNotNull(germanLanguageText, "German language text should exist");

		singleLanguageTextData.putText(Language.ENGLISH(), singleLanguageTextData.getText(Language.ENGLISH()));

		setComplete(); endTransaction();
		try {if (printDatasets){printDataSet(System.out, new String[]{"LanguageString", "DescriptionElementBase", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING"});}
		} catch(Exception e) { logger.warn(e);}
	}

	/**
	 * See #2114
	 */
	@Test
	@CdmDataSet //(value="DescriptionElementDaoHibernateImplTest.xml")
	@CdmExpectedDataSet
	public void testChangeLanguageString(){

		//test write
		TextData singleLanguageTextData = (TextData)descriptionElementDao.findByUuid(uuidSingleTextData);
		Map<Language, LanguageString> multiLangText = singleLanguageTextData.getMultilanguageText();
		assertEquals(1, multiLangText.size(), "There should be exactly 1 languageText in the multilanguageText");
		assertTrue(multiLangText.containsKey(Language.ENGLISH()), "The language should be English");

		singleLanguageTextData.putText(Language.ENGLISH(), "A new English text");

		setComplete(); endTransaction();
		try {if (printDatasets){printDataSet(System.out, new String[]{"LanguageString", "DescriptionElementBase", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING"});}
		} catch(Exception e) { logger.warn(e);}
	}

	/**
	 * See #2114
	 */
	@Test
	@CdmDataSet //(value="DescriptionElementDaoHibernateImplTest.xml")
	@CdmExpectedDataSet
	public void testRemoveLanguageString(){

		//test write
		TextData textDataTwo = (TextData)descriptionElementDao.findByUuid(uuidDobuleTextData);
		assertEquals(2, textDataTwo.size(), "There should be exactly 2 languageText in the multilanguageText");

		assertTrue(textDataTwo.containsKey(Language.ENGLISH()), "The language should be English");

		textDataTwo.removeText(Language.ENGLISH());
		assertEquals(1, textDataTwo.size(), "There should be only 1 language left");

		setComplete(); endTransaction();
		try {if (printDatasets){printDataSet(System.out, new String[]{"LanguageString", "DescriptionElementBase", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING"});}
		} catch(Exception e) { logger.warn(e);}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#count(java.lang.Class, java.lang.String)}.
	 */
	@Test
	public void testCountClassOfQextendsDescriptionElementBaseString() {
		logger.warn("testCountClassOfQextendsDescriptionElementBaseString not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#getMedia(eu.etaxonomy.cdm.model.description.DescriptionElementBase, java.lang.Integer, java.lang.Integer, java.util.List)}.
	 */
	@Test
	public void testGetMedia() {
		logger.warn("testGetMedia not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#search(java.lang.Class, java.lang.String, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)}.
	 */
	@Test
	public void testSearch() {
		logger.warn("testSearch not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#purgeIndex()}.
	 */
	@Test
	public void testPurgeIndex() {
		logger.warn("testPurgeIndex not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#rebuildIndex()}.
	 */
	@Test
	public void testRebuildIndex() {
		logger.warn("testRebuildIndex not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#optimizeIndex()}.
	 */
	@Test
	public void testOptimizeIndex() {
		logger.warn("testOptimizeIndex not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#count(java.lang.String)}.
	 */
	@Test
	public void testCountString() {
		logger.warn("testCountString not yet implemented");
	}

	@Test  //test cascading for modifying text (and others)
	@CdmDataSet(cleanSweep = true)
	@CdmExpectedDataSet
	public void testSaveCategoricalData(){
		UUID uuidDummyState = UUID.fromString("881b9c80-626d-47a6-b308-a63ee5f4178f");
		State state = (State)termDao.findByUuid(uuidDummyState);
		CategoricalData categoricalData = CategoricalData.NewInstance();
		categoricalData.setUuid(UUID.fromString("5c3f2340-f675-4d50-af96-89a2a12993b8"));
		categoricalData.setFeature(Feature.DESCRIPTION());
		StateData stateData = StateData.NewInstance(state);
		stateData.setUuid(UUID.fromString("04b9190d-d4ab-4c3a-8dec-8293dc820ddc"));
		stateData.putModifyingText(Language.ENGLISH(), "test modifier");
		LanguageString langString = stateData.getModifyingText().get(Language.ENGLISH());
		langString.setUuid(UUID.fromString("53a91bd4-d758-47ec-a385-94799bdb9f32"));
		categoricalData.addStateData(stateData);
		DefinedTerm modifier = DefinedTerm.SEX_FEMALE();
		System.out.println(modifier.getId());
		stateData.addModifier(modifier);

		descriptionElementDao.save(categoricalData);
		commit();
//		commitAndStartNewTransaction(new String[]{"Hibernate_sequences","DescriptionElementBase","DescriptionElementBase_StateData","StateData_DefinedTermBase", "StateData", "StateData_LanguageString", "LanguageString"});
	}



    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
