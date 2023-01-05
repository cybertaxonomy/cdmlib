/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.cache;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@Ignore
public class CdmPermanentCacheBaseTest extends CdmIntegrationTest {

	private static final Logger logger = LogManager.getLogger();

	@SpringBeanByType
	private CdmPermanentCacheBase cdmCacherBase;

    @SpringBeanByType
    private IReferenceService referenceService;

    @SpringBeanByType
    private ITaxonService taxonService;

	@Test
	public void testLanguageCache() {
		Language defaultLanguage = Language.getDefaultLanguage();

		Language defaultLanguageInCache = (Language)cdmCacherBase.getFromCache(defaultLanguage.getUuid());
		Assert.assertEquals("Loaded Language Term should match Language Term in Cache",defaultLanguage,defaultLanguageInCache);

		Language language = Language.getLanguageFromUuid(Language.uuidFrench);
		Language languageInCache = (Language)cdmCacherBase.getFromCache(language.getUuid());
		Assert.assertEquals("Loaded Language Term should match Language Term in Cache",language,languageInCache);

		// Following test is just to make sure no exception is raised when saving a taxon corresponding
		// to a taxon name with no name cache to begin with
		Reference sec = ReferenceFactory.newDatabase();
        referenceService.save(sec);
		Taxon taxon = Taxon.NewInstance(TaxonNameFactory.NewNonViralInstance(Rank.SERIES()), sec);
        taxon.setTitleCache("Tax" + "CdmCacher", true);
        taxonService.save(taxon);
        INonViralName nvn = taxon.getName();
        String nameCache = nvn.getNameCache();
        logger.warn("name cache : " + nameCache);
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
