/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

@DataSet
public class BeanInitializerIntegration extends CdmTransactionalIntegrationTest {

	@SpringBeanByType
	ITaxonNameDao taxonNameDao;

	@SpringBeanByType
	IDescriptionElementDao descriptionElementDao;

	UUID sphingidaeUuid;
	UUID textDataUuid;
	AuditEvent previousAuditEvent;

	@Before
	public void setUp() {
		sphingidaeUuid = UUID.fromString("9640a158-2bdb-4cbc-bff6-8f77e781f86b");
		textDataUuid = UUID.fromString("31a0160a-51b2-4565-85cf-2be58cb561d6");
		previousAuditEvent = new AuditEvent();
		previousAuditEvent.setRevisionNumber(1024);
		previousAuditEvent.setUuid(UUID.fromString("1f868a29-9127-4634-90c2-5024cc46be9d"));
		AuditEventContextHolder.clearContext(); // By default we're in the current view (i.e. view == null)
	}

	@After
	public void tearDown() {
		AuditEventContextHolder.clearContext();
	}

	/**
	 * Basic behavior here - we want to be able to initialize properties of this
	 * entity
	 */
	@Test
	@Ignore //FIXME homotypicalGroup is initialized even if it shouldn't
	public void testInitializeManyToOneProperty() {
		List<String> propertyPaths = new ArrayList<String>();
		propertyPaths.add("nomenclaturalReference");

		TaxonNameBase sphingidae = taxonNameDao.load(sphingidaeUuid, propertyPaths);
		setComplete();
		endTransaction();

		assertNotNull("Sphingidae should not be null",sphingidae);
		assertFalse("TaxonNameBase.homotypicalGroup should not be initialized",Hibernate.isInitialized(sphingidae.getHomotypicalGroup()));
		assertTrue("TaxonNameBase.nomenclaturalReference should be initialized",Hibernate.isInitialized(sphingidae.getNomenclaturalReference()));
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

		TaxonNameBase<?,?> sphingidae = taxonNameDao.load(sphingidaeUuid, propertyPaths);
		setComplete();
		endTransaction();

		assertNotNull("Sphingidae should not be null",sphingidae);
		assertTrue("TaxonNameBase.nomenclaturalReference should be initialized",Hibernate.isInitialized(sphingidae.getCombinationAuthorship()));
	}

	/**
	 * Because java.util.Map is not an instanceof java.util.Collection, we need to
	 * add an extra clause to DefaultBeanInitializer to catch Map properties
	 */
	@Test
	@Ignore //FIXME disabled since getMultilanguageText() fails when session is closed !!!!
	public void testInitializeMapProperty() {
		List<String> propertyPaths = new ArrayList<String>();
		propertyPaths.add("multilanguageText");
		propertyPaths.add("multilanguageText.language");

		TextData textData = (TextData)descriptionElementDao.load(textDataUuid, propertyPaths);
		setComplete();
		endTransaction();

		assertNotNull("textData should not be null",textData);

		assertTrue("TextData.multilanguageText should be initialized",Hibernate.isInitialized(textData.getMultilanguageText()));
		assertFalse("TextData.multilanguageText should not be empty",textData.getMultilanguageText().isEmpty());
		LanguageString languageString = textData.getMultilanguageText().values().iterator().next();
		assertTrue("LanguageString.language should be initialized",Hibernate.isInitialized(languageString.getLanguage()));
	}

	/**
	 * Interesting bug in envers where the three entity (object, parent and mapkey) query was not correct.
	 * Also Hibernate.initialize does not initalize *-to-Many relationships in envers as envers proxies dont implement
	 * HibernateProxy etc.
	 */
	@Test
	@Ignore //FIXME disabled since getMultilanguageText() fails when session is closed !!!!
	public void testInitializeMapInPriorView() {
		AuditEventContextHolder.getContext().setAuditEvent(previousAuditEvent);
		List<String> propertyPaths = new ArrayList<String>();
		propertyPaths.add("multilanguageText");
		propertyPaths.add("multilanguageText.language");

		TextData textData = (TextData)descriptionElementDao.load(textDataUuid, propertyPaths);
		setComplete();
		endTransaction();

		assertNotNull("textData should not be null",textData);
		assertTrue("TextData.multilanguageText should be initialized",Hibernate.isInitialized(textData.getMultilanguageText()));
		assertFalse("TextData.multilanguageText should not be empty",textData.getMultilanguageText().isEmpty());
		LanguageString languageString = textData.getMultilanguageText().values().iterator().next();
		assertTrue("LanguageString.language should be initialized",Hibernate.isInitialized(languageString.getLanguage()));
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
