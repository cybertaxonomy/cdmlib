// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @date 25.11.2010
 *
 */
public class DescriptionElementDaoHibernateImplTest extends CdmTransactionalIntegrationTest{
	private static final Logger logger = Logger.getLogger(DescriptionElementDaoHibernateImplTest.class);
	
	@SpringBeanByType
	IDescriptionElementDao descriptionElementDao;
	
	private UUID uuidSingleTextData = UUID.fromString("31a0160a-51b2-4565-85cf-2be58cb561d6");
	private UUID uuidDobuleTextData = UUID.fromString("50f6b799-3585-40a7-b69d-e7be77b2651a");
	
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
	@DataSet //(value="DescriptionElementDaoHibernateImplTest.xml")
	@ExpectedDataSet
	public void testPersistMultiLanguageString(){
//		int count = descriptionElementDao.count(TextData.class);
//		Assert.assertTrue("There must exist TextData", count > 0);
		
		//test read
		TextData doubleLanguageTextData = (TextData)descriptionElementDao.findByUuid(uuidDobuleTextData);
		Map<Language, LanguageString> multiLangText = doubleLanguageTextData.getMultilanguageText();
		Assert.assertEquals("There should be exactly 2 languageText in the multilanguageText", 2, multiLangText.size());
//		Assert.assertTrue("The language should be English", multiLangText.containsKey(Language.ENGLISH()));
		Language eng = Language.ENGLISH();
		System.out.println("English: " + eng.getLabel() + "("+ eng.getId() + ", " + eng.getUuid() + ")");

		for (Language lang: multiLangText.keySet()){
			System.out.println(lang.getLabel() + "("+ lang.getId() + ", " + lang.getUuid() + ")");
			boolean equal = lang.equals(eng);
			System.out.println(equal);
		}
		//An Niels: verstehst du das ?
		boolean contains = multiLangText.keySet().contains(eng);
		String et = doubleLanguageTextData.getText(eng);
		LanguageString englishText = multiLangText.get(eng);
		Assert.assertNotNull("English text should exist", englishText);
		Assert.assertEquals("The English text should be correct", "Praesent vitae turpis vitae sapien sodales sagittis.", englishText.getText());
		LanguageString czechText = multiLangText.get(Language.CZECH());
		Assert.assertNotNull("Czech", czechText);
		Assert.assertEquals("The Czech text should be correct", "A Czech translation for Praesent ...", czechText.getText());
		
		
		//test write
		TextData singleLanguageTextData = (TextData)descriptionElementDao.findByUuid(uuidSingleTextData);
		multiLangText = singleLanguageTextData.getMultilanguageText();
		Assert.assertEquals("There should be exactly 1 languageText in the multilanguageText", 1, multiLangText.size());
		Assert.assertTrue("The language should be English", multiLangText.containsKey(Language.ENGLISH()));
		singleLanguageTextData.putText("Ein test auf deutsch", Language.GERMAN());
		Assert.assertEquals("There should be exactly 2 languageText in the multilanguageText", 2, multiLangText.size());
		String germanText = singleLanguageTextData.getText(Language.GERMAN());
		Assert.assertNotNull("German text should exist", germanText);
		
		LanguageString germanLanguageText = singleLanguageTextData.getLanguageText(Language.GERMAN());
		Assert.assertNotNull("German language text should exist", germanLanguageText);
		
		descriptionElementDao.saveOrUpdate(singleLanguageTextData);
		
		
		
		setComplete();
		endTransaction();
		try {
			printDataSet(System.out, new String[]{"LanguageString", "DescriptionElementBase", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING", "DEFINEDTERMBASE"});
		} catch(Exception e) { 
			logger.warn(e);
		} 
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

}
