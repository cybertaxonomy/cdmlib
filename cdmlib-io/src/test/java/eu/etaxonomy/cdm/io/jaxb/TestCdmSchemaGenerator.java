/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.test.integration.spring.CdmIoTransactionalSpringIntegrationTest;

@Disabled // only used for development, thus we exclude this from running in the suite: IGNORE
public class TestCdmSchemaGenerator extends CdmIoTransactionalSpringIntegrationTest {

	@Test
	public void testSchemaGeneration() throws Exception {

		CdmSchemaGenerator cdmSchemaGenerator = new CdmSchemaGenerator();
		cdmSchemaGenerator.writeSchema();
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
