package eu.etaxonomy.cdm.api.cache;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

import eu.etaxonomy.cdm.api.cache.CdmCacher;

public class CdmCacherTest extends CdmIntegrationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmCacherTest.class);

	@SpringBeanByType
	private CdmCacher cdmCacher;
	
	@Test
	public void testLanguageCache() {
		Language defaultLanguage = Language.getDefaultLanguage();	
		
		Language defaultLanguageInCache = (Language)cdmCacher.getFromCache(defaultLanguage.getUuid());
		Assert.assertEquals("Loaded Language Term should match Language Term in Cache",defaultLanguage,defaultLanguageInCache);		
		
		Language language = Language.getLanguageFromUuid(Language.uuidFrench);
		Language languageInCache = (Language)cdmCacher.getFromCache(language.getUuid());
		Assert.assertEquals("Loaded Language Term should match Language Term in Cache",language,languageInCache);
	}

}
