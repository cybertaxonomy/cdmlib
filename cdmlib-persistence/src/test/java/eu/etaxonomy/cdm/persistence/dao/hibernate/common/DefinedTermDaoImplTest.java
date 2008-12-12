package eu.etaxonomy.cdm.persistence.dao.hibernate.common;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class DefinedTermDaoImplTest extends CdmIntegrationTest {

	@SpringBeanByType
	private IDefinedTermDao dao;
	
	private UUID uuid;
	private UUID armUuid;
	
	@Before
	public void setUp() {
		uuid = UUID.fromString("c0a18ec0-c838-11dd-ad8b-0800200c9a66");
		armUuid = UUID.fromString("7a0fde13-26e9-4382-a5c9-5640fc2b3334");
	}

	@Test
	public void findByTitle() {
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
		List<DefinedTermBase> terms = dao.list(1,489);
		assertNotNull("list should return a list",terms);
		assertEquals("list should return one term",1, terms.size());
		assertEquals("list should return one term with uuid " + uuid, uuid,terms.get(0).getUuid());		
	}
	
	@Test
	public void listManyTerms() {
		List<DefinedTermBase> terms = dao.list(5,485);
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
}
