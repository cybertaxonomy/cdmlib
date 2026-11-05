/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.molecular;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * @author pplitzner
 * @since 31.03.2014
 */
public class PrimerServiceTest extends CdmServicesTransactionalSpringIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private IPrimerService primerService;

    @Test
    public void testGetPrimerUuidAndTitleCache(){
        String primerLabel = "MatK";
        Primer primer = Primer.NewInstance(primerLabel);
        UUID uuid = primerService.save(primer).getUuid();

        List<UuidAndTitleCache<Primer>> primerUuidAndTitleCache = primerService.getPrimerUuidAndTitleCache();
        assertEquals(1, primerUuidAndTitleCache.size(), "Number of Primers in DB is incorrect.");
        UuidAndTitleCache<Primer> uuidAndTitleCache = primerUuidAndTitleCache.iterator().next();
        assertEquals(uuid, uuidAndTitleCache.getUuid(), "UUID is incorrect.");
        assertEquals(primerLabel, uuidAndTitleCache.getTitleCache(), "Label is incorrect.");
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}