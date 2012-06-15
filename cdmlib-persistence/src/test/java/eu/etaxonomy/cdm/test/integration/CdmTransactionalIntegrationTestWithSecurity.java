package eu.etaxonomy.cdm.test.integration;

import org.unitils.spring.annotation.SpringApplicationContext;

@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-securityTest.xml")
public abstract class CdmTransactionalIntegrationTestWithSecurity extends
		CdmTransactionalIntegrationTest {

}
