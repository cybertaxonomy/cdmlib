/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.jaxb;

import org.junit.Test;

import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class TestCdmSchemaGenerator extends CdmTransactionalIntegrationTest {
	
	@Test
	public void testSchemaGeneration() throws Exception {
		
		CdmSchemaGenerator cdmSchemaGenerator = new CdmSchemaGenerator();
		cdmSchemaGenerator.writeSchema();
	}

}
