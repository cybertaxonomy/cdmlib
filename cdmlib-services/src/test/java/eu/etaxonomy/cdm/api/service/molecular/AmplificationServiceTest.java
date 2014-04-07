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
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.model.molecular.DnaSample;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author pplitzner
 * @date 31.03.2014
 *
 */
public class AmplificationServiceTest  extends CdmTransactionalIntegrationTest {
    private static final Logger logger = Logger.getLogger(AmplificationServiceTest.class);

    @SpringBeanByType
    private IAmplificationService amplificationService;


    @Test
    public void testMoveSingleRead(){
        DnaSample dnaSample = DnaSample.NewInstance();
        Amplification amplificationA = Amplification.NewInstance(dnaSample);
        Amplification amplificationB = Amplification.NewInstance(dnaSample);
        SingleRead singleRead = SingleRead.NewInstance();

        amplificationA.addSingleRead(singleRead);
        amplificationService.moveSingleRead(amplificationA, amplificationB, singleRead);

        assertTrue("SingleRead was not correctly removed from origin", amplificationA.getSingleReads().isEmpty());
        assertEquals("SingleRead was not correctly moved to target", 1, amplificationB.getSingleReads().size());
        assertEquals("Moved SingleRead is not equal to origin SingleRead", singleRead, amplificationB.getSingleReads().iterator().next());

    }
}
