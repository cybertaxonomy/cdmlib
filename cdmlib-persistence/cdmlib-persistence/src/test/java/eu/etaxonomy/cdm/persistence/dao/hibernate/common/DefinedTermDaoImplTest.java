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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Hibernate;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

public class DefinedTermDaoImplTest extends CdmIntegrationTest {

	@SpringBeanByType
	private IDefinedTermDao dao;
	
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

	@Test
	public void findByTitle() throws Exception {
		List<DefinedTermBase> terms = dao.findByTitle("Diagnosis");
		assertNotNull("findByTitle should return a List", terms);
		assertEquals("findByTitle should return one term ",terms.size(),1);
		assertEquals("findByTitle should return Feature.DIAGNOSIS",terms.get(0),Feature.DIAGNOSIS());
	}
	
	@Test
	public void getTermByUUID() {
		DefinedTermBase term = dao.findByUuid(uuid);
		assertNotNull("findByUuid should return a term",term);
		assertEquals("findByUuid should return Feature.UNKNOWN",Feature.UNKNOWN(),term);
	}

	
	@Test
	public void getLanguageByIso2() {
		Language lang = dao.getLanguageByIso("arm");
		assertEquals("getLanguageByIso should return the correct Language instance",lang.getUuid(), armUuid);
	}
	
	@Test
	public void getLanguageByIso1() {
		Language lang = dao.getLanguageByIso("hy");
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
	 public void testListInitialization() {
		 AuditEventContextHolder.getContext().setAuditEvent(auditEvent);
		 List<OrderHint> orderHints = new ArrayList<OrderHint>();
		 orderHints.add(new OrderHint("titleCache",SortOrder.ASCENDING));
		 
		 List<String> propertyPaths = new ArrayList<String>();
		 propertyPaths.add("representations");
		 propertyPaths.add("representations.language");
		 List<DefinedTermBase> extensionTypes = dao.list(ExtensionType.class,null, null, orderHints, propertyPaths);
		 
		 
		 assertTrue(Hibernate.isInitialized(extensionTypes.get(0).getRepresentations()));
		 Set<Representation> representations = extensionTypes.get(0).getRepresentations();
		 for(Representation representation : representations) {
			 assertTrue(Hibernate.isInitialized(representation.getLanguage()));
		 }
	 }
}
