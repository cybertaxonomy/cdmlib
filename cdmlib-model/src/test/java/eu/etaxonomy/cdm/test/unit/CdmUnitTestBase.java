package eu.etaxonomy.cdm.test.unit;


import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author markus
 * Basic CDM unit testing class that incorporates the Spring Context for DI
 */
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class CdmUnitTestBase{
	private static final Logger logger = Logger.getLogger(CdmUnitTestBase.class);
}
