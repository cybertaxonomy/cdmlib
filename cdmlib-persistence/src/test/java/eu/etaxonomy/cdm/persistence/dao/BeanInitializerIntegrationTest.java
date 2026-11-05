/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTest;

@CdmDataSet
public class BeanInitializerIntegrationTest extends CdmTransactionalSpringIntegrationTest {

	@Autowired
	private ITaxonNameDao taxonNameDao;

	@Autowired
	private IDescriptionElementDao descriptionElementDao;

	private UUID sphingidaeUuid;
	private UUID textDataUuid;
	private AuditEvent previousAuditEvent;

	@BeforeEach
	public void setUp() {
		sphingidaeUuid = UUID.fromString("9640a158-2bdb-4cbc-bff6-8f77e781f86b");
		textDataUuid = UUID.fromString("31a0160a-51b2-4565-85cf-2be58cb561d6");
		previousAuditEvent = new AuditEvent();
		previousAuditEvent.setRevisionNumber(1024);
		previousAuditEvent.setUuid(UUID.fromString("1f868a29-9127-4634-90c2-5024cc46be9d"));
		AuditEventContextHolder.clearContext(); // By default we're in the current view (i.e. view == null)
	}

	@AfterEach
	public void tearDown() {
		AuditEventContextHolder.clearContext();
	}

	/**
	 * Basic behavior here - we want to be able to initialize properties of this
	 * entity
	 */
	@Test
	@Disabled //FIXME homotypicalGroup is initialized even though it shouldn't
	//this seems to be a general feature when using hibernate criteria which is the case here.
	public void testInitializeManyToOneProperty() {
		List<String> propertyPaths = new ArrayList<>();
		propertyPaths.add("nomenclaturalSource.citation");

		TaxonName sphingidae = taxonNameDao.load(sphingidaeUuid, propertyPaths);
		setComplete();
		endTransaction();

		assertNotNull(sphingidae, "Sphingidae should not be null");
		//TODO this does not fail due to property path, but it does even not fail without property path which is unexpected
		//there is a very similar test (AdvancedBeanInitializerTest.testTitleAndNameCacheAutoInitializer) which loads the taxon
		//and not the name and there it works.
		assertTrue(Hibernate.isInitialized(sphingidae.getNomenclaturalReference()), "TaxonName.nomenclaturalSource.citation should be initialized");

		assertFalse(Hibernate.isInitialized(sphingidae.getHomotypicalGroup()), "TaxonName.homotypicalGroup should not be initialized");
	}

	/**
	 * Slightly more advanced - what happens if we try to initialize
	 * a non-existent property - i.e. if we retrieve an object which
	 * might be one of many subclasses, and try to initialize a
	 * property belonging to another subclass.
	 *
	 * The bean initialization code should silently catch the
	 * MethodNotFound exception (otherwise we would not be able to initialize
	 * any properties of a subclass).
	 */
	@Test
	public void testInitializeManyToOneSubclassProperty() {
		List<String> propertyPaths = new ArrayList<>();
		propertyPaths.add("combinationAuthorship");
		propertyPaths.add("hybridRelationships");

		TaxonName sphingidae = taxonNameDao.load(sphingidaeUuid, propertyPaths);
		setComplete();
		endTransaction();

		assertNotNull(sphingidae, "Sphingidae should not be null");
		assertTrue(Hibernate.isInitialized(sphingidae.getCombinationAuthorship()), "TaxonName.combinationAuthorship should be initialized");
	}

	/**
	 * Because java.util.Map is not an instanceof java.util.Collection, we need to
	 * add an extra clause to DefaultBeanInitializer to catch Map properties
	 */
	@Test
	@Disabled //FIXME disabled since getMultilanguageText() fails when session is closed !!!!
	public void testInitializeMapProperty() {
		List<String> propertyPaths = new ArrayList<>();
		propertyPaths.add("multilanguageText");
		propertyPaths.add("multilanguageText.language");

		TextData textData = (TextData)descriptionElementDao.load(textDataUuid, propertyPaths);
		setComplete();
		endTransaction();

		assertNotNull(textData, "textData should not be null");

		assertTrue(Hibernate.isInitialized(textData.getMultilanguageText()), "TextData.multilanguageText should be initialized");
		assertFalse(textData.getMultilanguageText().isEmpty(), "TextData.multilanguageText should not be empty");
		LanguageString languageString = textData.getMultilanguageText().values().iterator().next();
		assertTrue(Hibernate.isInitialized(languageString.getLanguage()), "LanguageString.language should be initialized");
	}

	/**
	 * Interesting bug in envers where the three entity (object, parent and mapkey) query was not correct.
	 * Also Hibernate.initialize does not initalize *-to-Many relationships in envers as envers proxies dont implement
	 * HibernateProxy etc.
	 */
	@Test
	@Disabled //FIXME disabled since getMultilanguageText() fails when session is closed !!!!
	public void testInitializeMapInPriorView() {
		AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
		List<String> propertyPaths = new ArrayList<>();
		propertyPaths.add("multilanguageText");
		propertyPaths.add("multilanguageText.language");

		TextData textData = (TextData)descriptionElementDao.load(textDataUuid, propertyPaths);
		setComplete();
		endTransaction();

		assertNotNull(textData, "textData should not be null");
		assertTrue(Hibernate.isInitialized(textData.getMultilanguageText()), "TextData.multilanguageText should be initialized");
		assertFalse(textData.getMultilanguageText().isEmpty(), "TextData.multilanguageText should not be empty");
		LanguageString languageString = textData.getMultilanguageText().values().iterator().next();
		assertTrue(Hibernate.isInitialized(languageString.getLanguage()), "LanguageString.language should be initialized");
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}