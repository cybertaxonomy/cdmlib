/**
 * 
 */
package eu.etaxonomy.cdm.remote.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.application.CdmApplicationRemoteController;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.remote.service.RemoteTermInitializer;

/**
 * @author j.koch
 *
 */
public class TestRemoteTermInitializer {

	private static RemoteTermInitializer remoteTermInitializer;
	private static CdmApplicationRemoteController appCtr;
	
	@BeforeClass
	public static void setUp(){
		Resource applicationContextResource = new ClassPathResource("/eu/etaxonomy/cdm/remotingApplicationContext.xml");
		appCtr = CdmApplicationRemoteController.NewInstance(applicationContextResource, null);
		remoteTermInitializer = new RemoteTermInitializer();
		remoteTermInitializer.setVocabularyService(appCtr.getVocabularyService());
	}
	
	@Test
	public void testInit() {
		assertNotNull("TermInitializer should exist",remoteTermInitializer);
	}
	
//	@Test
//	public void testFirstPass() {
//		Map<UUID, DefinedTermBase> persistedTerms = new HashMap<UUID, DefinedTermBase>();
//		remoteTermInitializer.firstPass(VocabularyEnum.Rank, persistedTerms);
//	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.DefaultTermInitializer#initialize()}.
	 */
	@Test
	//@Ignore // does not run yet in a test suite as the Language.DEFAULT() is not null then
	public void testInitialize() {
		assertNull("At the beginning of the initialization test the default language should still be null but is not", Language.DEFAULT());
		remoteTermInitializer.initialize();
		assertNotNull("TermInitializer should exist",remoteTermInitializer);
		assertNotNull("TermInitializer should have initialized Language.DEFAULT",Language.DEFAULT());
		assertEquals("Language.DEFAULT should equal Language.ENGLISH",Language.DEFAULT(),Language.ENGLISH());
		TermVocabulary<Language> voc = Language.DEFAULT().getVocabulary();
		assertNotNull("language for language vocabulary representation was null but must be default language", voc.getRepresentation(Language.DEFAULT()));	
	}
	
	@Test
	public void testGetRepresentations() {
		assertNotNull("Rank.SPECIES() should not be null", Rank.SPECIES());
		assertFalse("Rank.SPECIES().getRepresentations() should not be empty",Rank.SPECIES().getRepresentations().isEmpty());
		assertEquals("Rank.SPECIES().getLabel() should return \"Species\"","Species",Rank.SPECIES().getLabel());
		
	}
}
