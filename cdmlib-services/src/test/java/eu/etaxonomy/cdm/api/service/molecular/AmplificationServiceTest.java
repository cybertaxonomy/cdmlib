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

import org.apache.log4j.Logger;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author pplitzner
 * @date 31.03.2014
 *
 */
public class AmplificationServiceTest  extends CdmTransactionalIntegrationTest {
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AmplificationServiceTest.class);

    @SpringBeanByType
    private IAmplificationService amplificationService;


}
