package eu.etaxonomy.cdm;

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
//ApplicationContext will be loaded from "/applicationContext.xml" and "/applicationContext-test.xml"
//in the root of the classpath
@ContextConfiguration(locations={"/applicationContext-test.xml"})
@TransactionConfiguration(transactionManager="testTransactionManager", defaultRollback=false)
@Transactional
public abstract class CdmUnitTestBase{
	
}
