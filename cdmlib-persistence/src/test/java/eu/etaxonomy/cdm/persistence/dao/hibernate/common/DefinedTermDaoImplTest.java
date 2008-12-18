package eu.etaxonomy.cdm.persistence.dao.hibernate.common;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class DefinedTermDaoImplTest extends CdmIntegrationTest {

	@SpringBeanByType
	private IDefinedTermDao dao;
	
	private UUID uuid;
	private UUID armUuid;
	private UUID northernEuropeUuid;
	private UUID middleEuropeUuid;
	private UUID westTropicalAfricaUuid;
	private Set<NamedArea> namedAreas;
	
	@Before
	public void setUp() {
		uuid = UUID.fromString("d6781519-ec60-4afa-b5ea-375f4d2a1729");
		armUuid = UUID.fromString("7a0fde13-26e9-4382-a5c9-5640fc2b3334");
		northernEuropeUuid = UUID.fromString("22524ba2-6e57-4b71-89ab-89fc50fba6b4");
		middleEuropeUuid = UUID.fromString("d292f237-da3d-408b-93a1-3257a8c80b97");
		westTropicalAfricaUuid = UUID.fromString("931164ad-ec16-4133-afab-bdef25d67636");
		namedAreas = new HashSet<NamedArea>();
	}

	@Test
	public void findByTitle() throws Exception {
		List<DefinedTermBase> terms = dao.findByTitle("nomenclature");
		assertNotNull("findByTitle should return a List", terms);
		assertEquals("findByTitle should return one term ",terms.size(),1);
		assertEquals("findByTitle should return a term with uuid " + uuid,terms.get(0).getUuid(),uuid);
	}

	/**
	 * FIXME Should list() be tested in CdmEntityDaoBaseTest?
	 * Also - how is this list sorted? Should we supply an enum that allows
	 * the list to be sorted by different fields (titleCache? label? text? uri?)
	 */
	@Test
	public void listOneTerm() {
		List<DefinedTermBase> terms = dao.list(1,2017);
		assertNotNull("list should return a list",terms);
		assertEquals("list should return one term",1, terms.size());
		assertEquals("list should return one term with uuid " + uuid, uuid,terms.get(0).getUuid());		
	}
	
	@Test
	public void listManyTerms() {
		List<DefinedTermBase> terms = dao.list(5,2013);
		assertNotNull("list should return a list",terms);
		assertEquals("list should return five terms",5, terms.size());
		assertEquals("list should return a term with uuid " + uuid + " at position 5", uuid,terms.get(4).getUuid());	
	}
	
	@Test
	public void getTermByUUID() {
		DefinedTermBase term = dao.findByUuid(uuid);
		assertNotNull("findByUuid should return a term",term);
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
		    
		    List<NamedArea> includes = dao.getIncludes(namedAreas, null, null);
		    
		    assertNotNull("getIncludes should return a List",includes);
		    assertFalse("The list should not be empty",includes.isEmpty());
		    assertEquals("getIncludes should return 9 NamedArea entities",9,includes.size());
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
		    
		    List<NamedArea> partOf = dao.getPartOf(namedAreas, null, null);
		    
		    assertNotNull("getPartOf should return a List",partOf);
		    assertFalse("The list should not be empty",partOf.isEmpty());
		    assertEquals("getPartOf should return 2 NamedArea entities",2,partOf.size());
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
}
