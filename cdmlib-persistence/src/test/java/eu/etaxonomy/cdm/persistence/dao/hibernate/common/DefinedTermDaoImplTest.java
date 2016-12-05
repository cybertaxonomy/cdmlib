/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class DefinedTermDaoImplTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByType
	private IDefinedTermDao dao;

	@SpringBeanByType
    private ITermVocabularyDao vocabularyDao;

	private UUID uuid;
	private UUID armUuid;
	private UUID northernEuropeUuid;
	private UUID middleEuropeUuid;
	private UUID westTropicalAfricaUuid;
	private Set<NamedArea> namedAreas;
	private AuditEvent auditEvent;

	@Before
	public void setUp() {
		uuid = UUID.fromString("910307f1-dc3c-452c-a6dd-af5ac7cd365c");
		armUuid = UUID.fromString("7a0fde13-26e9-4382-a5c9-5640fc2b3334");
		northernEuropeUuid = UUID.fromString("22524ba2-6e57-4b71-89ab-89fc50fba6b4");
		middleEuropeUuid = UUID.fromString("d292f237-da3d-408b-93a1-3257a8c80b97");
		westTropicalAfricaUuid = UUID.fromString("931164ad-ec16-4133-afab-bdef25d67636");
		namedAreas = new HashSet<NamedArea>();
		auditEvent = new AuditEvent();
		auditEvent.setUuid(UUID.fromString("6456c23d-6424-42dc-a240-36d34e77b249"));
		auditEvent.setRevisionNumber(1001);
		AuditEventContextHolder.clearContext();
	}

	@After
	public void cleantUp() {
		AuditEventContextHolder.clearContext();
	}

	@Test
	public void findByTitle() throws Exception {
		List<DefinedTermBase> terms = dao.findByTitle("Diagnosis");
		assertNotNull("findByTitle should return a List", terms);
		assertEquals("findByTitle should return one term ",terms.size(),1);
		assertEquals("findByTitle should return Feature.DIAGNOSIS",terms.get(0),Feature.DIAGNOSIS());
	}

	@Test
	public void getTermByUUID() {
		DefinedTermBase<?> term = dao.findByUuid(uuid);
		assertNotNull("findByUuid should return a term",term);
		assertEquals("findByUuid should return Feature.UNKNOWN",Feature.UNKNOWN(),term);
	}


	@Test
	public void getLanguageByIso2() {
		Language lang = dao.getLanguageByIso("arm");
		assertNotNull(lang);
		assertEquals("getLanguageByIso should return the correct Language instance",lang.getUuid(), armUuid);
	}

	@Test
	public void getLanguageByIso1() {
		Language lang = dao.getLanguageByIso("hy");
		assertNotNull(lang);
		assertEquals("getLanguageByIso should return the correct Language instance",lang.getUuid(), armUuid);
	}

	@Test
	public void getLanguageByMalformedIso1() {
		Language lang = dao.getLanguageByIso("a");
		assertNull("getLanguageByIso should return null for this malformed Iso \'a\'",lang);
	}

	@Test
	public void getLanguageByMalformedIso2() {
		Language lang = dao.getLanguageByIso("abcd");
		assertNull("getLanguageByIso should return null for this malformed Iso \'abcd\'",lang);
	}

	@Test
	public void getDefinedTermByIdInVocabulary(){
		UUID tdwgVocUuid = NamedArea.uuidTdwgAreaVocabulary;
		List<NamedArea> list = dao.getDefinedTermByIdInVocabulary("GER", tdwgVocUuid, NamedArea.class, null, null);
		assertNotNull("Method should return a result", list);
		assertEquals("Method should return exactly 1 area", 1, list.size());
		NamedArea area = list.get(0);
		assertEquals("GER", area.getIdInVocabulary());
		assertEquals(tdwgVocUuid, area.getVocabulary().getUuid());
	}

	 @Test
	 public void testGetIncludes() {
		    NamedArea northernEurope = (NamedArea)dao.findByUuid(northernEuropeUuid);
		    assert northernEurope != null : "NamedArea must exist";
		    namedAreas.add(northernEurope);

		    List<String> propertyPaths = new ArrayList<String>();
		    propertyPaths.add("level");

		    List<NamedArea> includes = dao.getIncludes(namedAreas, null, null,propertyPaths);

		    assertNotNull("getIncludes should return a List",includes);
		    assertFalse("The list should not be empty",includes.isEmpty());
		    assertEquals("getIncludes should return 9 NamedArea entities",9,includes.size());
		    assertTrue("NamedArea.level should be initialized",Hibernate.isInitialized(includes.get(0).getLevel()));
	 }

	 @Test
	 public void countIncludes() {
		 NamedArea northernEurope = (NamedArea)dao.findByUuid(northernEuropeUuid);
		 assert northernEurope != null : "NamedArea must exist";
		 namedAreas.add(northernEurope);

		 int numberOfIncludes = dao.countIncludes(namedAreas);
		 assertEquals("countIncludes should return 9",9, numberOfIncludes);

	 }

	 @Test
	 public void testGetPartOf() {
		    NamedArea northernEurope = (NamedArea)dao.findByUuid(northernEuropeUuid);
		    NamedArea middleEurope = (NamedArea)dao.findByUuid(middleEuropeUuid);
		    NamedArea westTropicalAfrica = (NamedArea)dao.findByUuid(westTropicalAfricaUuid);
		    assert northernEurope != null : "NamedArea must exist";
		    assert middleEurope != null : "NamedArea must exist";
		    assert westTropicalAfrica != null : "NamedArea must exist";
		    namedAreas.add(northernEurope);
		    namedAreas.add(middleEurope);
		    namedAreas.add(westTropicalAfrica);

		    List<String> propertyPaths = new ArrayList<String>();
		    propertyPaths.add("level");

		    List<NamedArea> partOf = dao.getPartOf(namedAreas, null, null,propertyPaths);

		    assertNotNull("getPartOf should return a List",partOf);
		    assertFalse("The list should not be empty",partOf.isEmpty());
		    assertEquals("getPartOf should return 2 NamedArea entities",2,partOf.size());
		    assertTrue("NamedArea.level should be initialized",Hibernate.isInitialized(partOf.get(0).getLevel()));
	 }

	 @Test
	 public void countPartOf() {
		 NamedArea northernEurope = (NamedArea)dao.findByUuid(northernEuropeUuid);
		 NamedArea middleEurope = (NamedArea)dao.findByUuid(middleEuropeUuid);
		 NamedArea westTropicalAfrica = (NamedArea)dao.findByUuid(westTropicalAfricaUuid);
		 assert northernEurope != null : "NamedArea must exist";
		 assert middleEurope != null : "NamedArea must exist";
		 assert westTropicalAfrica != null : "NamedArea must exist";

		 namedAreas.add(northernEurope);
		 namedAreas.add(middleEurope);
		 namedAreas.add(westTropicalAfrica);

		 int numberOfPartOf = dao.countPartOf(namedAreas);
		 assertEquals("countPartOf should return 2",2,numberOfPartOf);
	 }

	 @Test
	 // NOTE: if this test is failing see
	 //       http://dev.e-taxonomy.eu/trac/changeset/13291/trunk/cdmlib/cdmlib-persistence/src/test/resources/eu/etaxonomy/cdm/persistence/dao/hibernate/dataset.dtd
	 public void testListInitialization() {
		 AuditEventContextHolder.getContext().setAuditEvent(auditEvent);
		 List<OrderHint> orderHints = new ArrayList<OrderHint>();
		 orderHints.add(new OrderHint("titleCache",SortOrder.ASCENDING));

		 List<String> propertyPaths = new ArrayList<String>();
		 propertyPaths.add("representations");
		 propertyPaths.add("representations.language");
		 List<ExtensionType> extensionTypes = dao.list(ExtensionType.class,null, null, orderHints, propertyPaths);


		 assertTrue(Hibernate.isInitialized(extensionTypes.get(0).getRepresentations()));
		 Set<Representation> representations = extensionTypes.get(0).getRepresentations();
		 //TODO currently the representations list is empty, is this wanted? If not,
		 //we should first check, if the list is not empty and then iterate.
		 //Why is it empty? Does ExtensionType not have representations?
		 for(Representation representation : representations) {
			 assertTrue(Hibernate.isInitialized(representation.getLanguage()));
		 }
	 }

	 @Test
	 public void testTitleCacheCreation() {

	     //prepare
	     @SuppressWarnings("unchecked")
	     TermVocabulary<DefinedTerm> newVoc = TermVocabulary.NewInstance(TermType.Modifier);
	     UUID vocUuid = UUID.fromString("6ced4c45-9c1b-4053-9dc3-6b8c51d286ed");
	     newVoc.setUuid(vocUuid);
	     UUID termUuid = UUID.fromString("2ab69720-c06c-4cfc-8928-d2ae6f1e4a48");
	     DefinedTerm newModifier = DefinedTerm.NewModifierInstance("Test Modifier Description", "English Modifier", "TM");
	     Representation englishRepresentation = newModifier.getRepresentations().iterator().next();
	     newModifier.setUuid(termUuid);
	     newVoc.addTerm(newModifier);
	     vocabularyDao.save(newVoc);

         //only English
	     newModifier.setProtectedTitleCache(false);
	     newModifier.setProtectedTitleCache(true);
         Assert.assertEquals("English Label should be the title cache", "English Modifier", newModifier.getTitleCache());

         //Change English label
         newModifier.setProtectedTitleCache(false);
         newModifier.setLabel("New English label");
         dao.saveOrUpdate(newModifier);
         newModifier.setProtectedTitleCache(true);
         Assert.assertEquals("English (default language) label should still be the title cache", "New English label", newModifier.getTitleCache());

         //Add German
         newModifier.setProtectedTitleCache(false);
         Representation newRepresentation = Representation.NewInstance("Beschreibung", "Deutscher Modifier", "Abk.", Language.GERMAN());
         newModifier.addRepresentation(newRepresentation);
         dao.saveOrUpdate(newModifier);
         newModifier.setProtectedTitleCache(true);
         Assert.assertEquals("English (default language) label should still be the title cache", "New English label", newModifier.getTitleCache());

         //Remove English
         newModifier.setProtectedTitleCache(false);
         newModifier.removeRepresentation(englishRepresentation);
         dao.saveOrUpdate(newModifier);
         newVoc.setProtectedTitleCache(true);
         Assert.assertEquals("German Label should be the new title cache again as English representation is not there anymore", "Deutscher Modifier", newModifier.getTitleCache());
    }

	 @Test
	 public void testListByTermType(){

	     TermType termType = TermType.Modifier;

	     List<DefinedTermBase> existingList = this.dao.listByTermType(termType, null, null, null, null);
	     int nExisting = existingList.size();
	     int nExistingTerms = this.dao.list(DefinedTerm.class, null, null, null, null).size();


	     //prepare
         @SuppressWarnings("unchecked")
         TermVocabulary<DefinedTerm> newVoc = TermVocabulary.NewInstance(termType);
         UUID vocUuid = UUID.fromString("6ced4c45-9c1b-4053-9dc3-6b8c51d286ed");
         newVoc.setUuid(vocUuid);
         UUID termUuid = UUID.fromString("2ab69720-c06c-4cfc-8928-d2ae6f1e4a48");
         DefinedTerm newModifier = DefinedTerm.NewModifierInstance("Test Modifier Description", "English Modifier", "TM");
         newModifier.setUuid(termUuid);
         newVoc.addTerm(newModifier);
         vocabularyDao.save(newVoc);
         this.commitAndStartNewTransaction(null);

         //assert 1 more
         int nNow = this.dao.listByTermType(termType, null, null, null, null).size();
         Assert.assertEquals("There should be exactly 1 more term now", nExisting + 1 , nNow);
         int nTermsNow = this.dao.list(DefinedTerm.class, null, null, null, null).size();
         Assert.assertEquals("There should be exactly 1 more term now", nExistingTerms + 1 , nTermsNow);
         this.commitAndStartNewTransaction(null);

         //Add German representation
         Representation newRepresentation = Representation.NewInstance("Beschreibung", "Deutscher Modifier", "Abk.", Language.GERMAN());
         newModifier.addRepresentation(newRepresentation);
         dao.saveOrUpdate(newModifier);
         this.commitAndStartNewTransaction(null);

         nNow = this.dao.listByTermType(termType, null, null, null, null).size();
         Assert.assertEquals("There should still be only one more term (but with 2 representations)", nExisting + 1 , nNow);
         nTermsNow = this.dao.list(DefinedTerm.class, null, null, null, null).size();
         Assert.assertEquals("There should be exactly 1 more term now", nExistingTerms + 1 , nTermsNow);


         List<DefinedTerm> languages = this.dao.listByTermType(TermType.Language, null, null, null, null);
         Assert.assertNotNull(languages);
         Assert.assertEquals(485, languages.size());
	 }


    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}