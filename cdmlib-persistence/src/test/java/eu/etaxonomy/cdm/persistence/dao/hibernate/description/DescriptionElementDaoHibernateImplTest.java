/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.dbunit.datasetfactory.impl.MultiSchemaXmlDataSetFactory;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author a.mueller
 \* @since 25.11.2010
 *
 */
public class DescriptionElementDaoHibernateImplTest extends CdmTransactionalIntegrationTest {
	private static final Logger logger = Logger.getLogger(DescriptionElementDaoHibernateImplTest.class);

	@SpringBeanByType
	IDescriptionElementDao descriptionElementDao;

	@SpringBeanByType
    IDescriptionDao descriptionDao;


	@SpringBeanByType
	IDefinedTermDao termDao;

	@SpringBeanByType
	ITaxonDao taxonDao;



	private final UUID uuidSingleTextData = UUID.fromString("31a0160a-51b2-4565-85cf-2be58cb561d6");
	private final UUID uuidDobuleTextData = UUID.fromString("50f6b799-3585-40a7-b69d-e7be77b2651a");

	private final boolean printDatasets = false;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

// ***************************** TESTS ************************************/



	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#countMedia(eu.etaxonomy.cdm.model.description.DescriptionElementBase)}.
	 */
	@Test
	public void testCountMedia() {
		logger.warn("Not yet implemented");
	}


	/**
	 * See #2114
	 */
	@Test
	@DataSet // (value="DescriptionElementDaoHibernateImplTest.xml")
	public void testRetrieveMultiLanguageString(){
//		int count = descriptionElementDao.count(TextData.class);
//		Assert.assertTrue("There must exist TextData", count > 0);

		//test read
		TextData textDataTwo = (TextData)descriptionElementDao.findByUuid(uuidDobuleTextData);
		Assert.assertEquals("There should be exactly 2 languageText in the multilanguageText", 2, textDataTwo.size());
		Assert.assertTrue("One language should be English", textDataTwo.containsKey(Language.ENGLISH()));
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
		Assert.assertNotNull("English text should exist", englishText);
		Assert.assertEquals("The English text should be correct", "Praesent vitae turpis vitae sapien sodales sagittis.", englishText.getText());
		LanguageString czechText = textDataTwo.getLanguageText(Language.CZECH());
		Assert.assertNotNull("Czech", czechText);
		Assert.assertEquals("The Czech text should be correct", "A Czech translation for Praesent ...", czechText.getText());
	}

	/**
	 * See #2114
	 */
	@Test
	@DataSet //(value="DescriptionElementDaoHibernateImplTest.xml")
	@ExpectedDataSet
	public void testPersistMultiLanguageString(){

		//test write
		TextData singleLanguageTextData = (TextData)descriptionElementDao.findByUuid(uuidSingleTextData);
		Map<Language, LanguageString> multiLangText = singleLanguageTextData.getMultilanguageText();
		Assert.assertEquals("There should be exactly 1 languageText in the multilanguageText", 1, multiLangText.size());
		Assert.assertTrue("The language should be English", multiLangText.containsKey(Language.ENGLISH()));
		singleLanguageTextData.putText(Language.GERMAN(), "Ein test auf deutsch");
		Assert.assertEquals("There should be exactly 2 languageText in the multilanguageText", 2, singleLanguageTextData.size());
		String germanText = singleLanguageTextData.getText(Language.GERMAN());
		Assert.assertNotNull("German text should exist", germanText);

		LanguageString germanLanguageText = singleLanguageTextData.getLanguageText(Language.GERMAN());
		Assert.assertNotNull("German language text should exist", germanLanguageText);

		singleLanguageTextData.putText(Language.ENGLISH(), singleLanguageTextData.getText(Language.ENGLISH()));
		descriptionElementDao.saveOrUpdate(singleLanguageTextData);

		setComplete(); endTransaction();
		try {if (printDatasets){printDataSet(System.out, new String[]{"LanguageString", "DescriptionElementBase", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING"});}
		} catch(Exception e) { logger.warn(e);}
	}

	/**
	 * See #2114
	 */
	@Test
	@DataSet //(value="DescriptionElementDaoHibernateImplTest.xml")
	@ExpectedDataSet
	public void testChangeLanguageString(){

		//test write
		TextData singleLanguageTextData = (TextData)descriptionElementDao.findByUuid(uuidSingleTextData);
		Map<Language, LanguageString> multiLangText = singleLanguageTextData.getMultilanguageText();
		Assert.assertEquals("There should be exactly 1 languageText in the multilanguageText", 1, multiLangText.size());
		Assert.assertTrue("The language should be English", multiLangText.containsKey(Language.ENGLISH()));

		singleLanguageTextData.putText(Language.ENGLISH(), "A new English text");
		descriptionElementDao.saveOrUpdate(singleLanguageTextData);

		setComplete(); endTransaction();
		try {if (printDatasets){printDataSet(System.out, new String[]{"LanguageString", "DescriptionElementBase", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING"});}
		} catch(Exception e) { logger.warn(e);}
	}

	/**
	 * See #2114
	 */
	@Test
	@DataSet //(value="DescriptionElementDaoHibernateImplTest.xml")
	@ExpectedDataSet
	public void testRemoveLanguageString(){

		//test write
		TextData textDataTwo = (TextData)descriptionElementDao.findByUuid(uuidDobuleTextData);
		Assert.assertEquals("There should be exactly 2 languageText in the multilanguageText", 2, textDataTwo.size());

		Assert.assertTrue("The language should be English", textDataTwo.containsKey(Language.ENGLISH()));

		textDataTwo.removeText(Language.ENGLISH());
		Assert.assertEquals("There should be only 1 language left", 1, textDataTwo.size());
		descriptionElementDao.saveOrUpdate(textDataTwo);

		setComplete(); endTransaction();
		try {if (printDatasets){printDataSet(System.out, new String[]{"LanguageString", "DescriptionElementBase", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING"});}
		} catch(Exception e) { logger.warn(e);}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#count(java.lang.Class, java.lang.String)}.
	 */
	@Test
	public void testCountClassOfQextendsDescriptionElementBaseString() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#getMedia(eu.etaxonomy.cdm.model.description.DescriptionElementBase, java.lang.Integer, java.lang.Integer, java.util.List)}.
	 */
	@Test
	public void testGetMedia() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#search(java.lang.Class, java.lang.String, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)}.
	 */
	@Test
	public void testSearch() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#purgeIndex()}.
	 */
	@Test
	public void testPurgeIndex() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#rebuildIndex()}.
	 */
	@Test
	public void testRebuildIndex() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#optimizeIndex()}.
	 */
	@Test
	public void testOptimizeIndex() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#count(java.lang.String)}.
	 */
	@Test
	public void testCountString() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.description.DescriptionElementDaoImpl#suggestQuery(java.lang.String)}.
	 */
	@Test
	public void testSuggestQuery() {
		logger.warn("Not yet implemented");
	}

	@Test  //test cascading for modifying text (and others)
	@DataSet( loadStrategy=CleanSweepInsertLoadStrategy.class)
	@ExpectedDataSet(factory=MultiSchemaXmlDataSetFactory.class)
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
