/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.babadshanjan
 * @created 09.02.2009
 * @version 1.0
 */
public class DescriptionServiceImplTest extends CdmIntegrationTest {
	private static Logger logger = Logger
			.getLogger(DescriptionServiceImplTest.class);
	
	@SpringBeanByType
	private IDescriptionService service;

	@Test
	public void testGetDefaultFeatureVocabulary() {
		
		service.getDefaultFeatureVocabulary();
	}
}
