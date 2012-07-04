package eu.etaxonomy.cdm.test.integration;

import org.junit.After;
import org.junit.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitils.spring.annotation.SpringApplicationContext;

@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-securityTest.xml")
public abstract class CdmTransactionalIntegrationTestWithSecurity extends  CdmTransactionalIntegrationTest {


    @After
    @Before
    public void clearAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }


}
