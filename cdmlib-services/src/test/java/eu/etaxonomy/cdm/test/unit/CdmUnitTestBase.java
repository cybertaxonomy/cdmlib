package eu.etaxonomy.cdm.test.unit;

import org.junit.runner.RunWith;
import org.springframework.test.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author markus
 * Basic CDM unit testing class that incorporates the Spring Context for DI
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/eu/etaxonomy/cdm/applicationContext.xml"})
@TransactionConfiguration(defaultRollback=true)
@Transactional
public abstract class CdmUnitTestBase{
	
}
