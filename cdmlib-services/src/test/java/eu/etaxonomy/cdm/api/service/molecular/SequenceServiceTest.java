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
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;


/**
 * @author pplitzner
 * @since 31.03.2014
 *
 */
public class SequenceServiceTest extends CdmTransactionalIntegrationTest {
    private static final Logger logger = Logger.getLogger(SequenceServiceTest.class);

    @SpringBeanByType
    private ISequenceService sequenceService;

    @Test
    public void testMoveSingleRead(){
        Sequence sequenceA = Sequence.NewInstance("");
        Sequence sequenceB = Sequence.NewInstance("");
        SingleRead singleRead = SingleRead.NewInstance();

        sequenceA.addSingleRead(singleRead);
        sequenceService.moveSingleRead(sequenceA, sequenceB, singleRead);

        assertTrue("SingleRead was not correctly removed from origin", sequenceA.getSingleReads().isEmpty());
        assertEquals("SingleRead was not correctly moved to target", 1, sequenceB.getSingleReads().size());
        assertEquals("Moved SingleRead is not equal to origin SingleRead", singleRead, sequenceB.getSingleReads().iterator().next());
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub
        
    }
}
