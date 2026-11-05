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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

/**
 * @author pplitzner
 * @since 31.03.2014
 */
public class SequenceServiceTest extends CdmServicesTransactionalSpringIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private ISequenceService sequenceService;

    @Test
    public void testMoveSingleRead(){
        Sequence sequenceA = Sequence.NewInstance("");
        Sequence sequenceB = Sequence.NewInstance("");
        SingleRead singleRead = SingleRead.NewInstance();

        sequenceA.addSingleRead(singleRead);
        sequenceService.moveSingleRead(sequenceA, sequenceB, singleRead);

        assertTrue(sequenceA.getSingleReads().isEmpty(), "SingleRead was not correctly removed from origin");
        assertEquals(1, sequenceB.getSingleReads().size(), "SingleRead was not correctly moved to target");
        assertEquals(singleRead, sequenceB.getSingleReads().iterator().next(), "Moved SingleRead is not equal to origin SingleRead");
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}