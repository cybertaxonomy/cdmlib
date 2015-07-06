// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.molecular;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author pplitzner
 * @date 31.03.2014
 *
 */
public class PrimerServiceTest extends CdmTransactionalIntegrationTest {
    private static final Logger logger = Logger.getLogger(PrimerServiceTest.class);

    @SpringBeanByType
    private IPrimerService primerService;

    @Test
    public void testGetPrimerUuidAndTitleCache(){
        String primerLabel = "MatK";
        Primer primer = Primer.NewInstance(primerLabel);
        UUID uuid = primerService.save(primer).getUuid();

        List<UuidAndTitleCache<Primer>> primerUuidAndTitleCache = primerService.getPrimerUuidAndTitleCache();
        assertEquals("Number of Primers in DB is incorrect.", 1, primerUuidAndTitleCache.size());
        UuidAndTitleCache<Primer> uuidAndTitleCache = primerUuidAndTitleCache.iterator().next();
        assertEquals("UUID is incorrect.", uuid, uuidAndTitleCache.getUuid());
        assertEquals("Label is incorrect.", primerLabel, uuidAndTitleCache.getTitleCache());
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
