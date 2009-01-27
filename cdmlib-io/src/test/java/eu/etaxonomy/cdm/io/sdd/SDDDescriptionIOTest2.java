package eu.etaxonomy.cdm.io.sdd;

import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class SDDDescriptionIOTest2 extends CdmTransactionalIntegrationTest {
	
	@SpringBeanByType
	SDDDescriptionIO sddDescriptionIo;
	
	private IImportConfigurator configurator;
	
	@Before
	public void setUp() {
		URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/sdd/SDD-Test-Simple.xml");
		configurator = SDDImportConfigurator.NewInstance(url.toString(), null);
	}
	
	@Test
	public void testInit() {
		assertNotNull("sddDescriptionIo should not be null",sddDescriptionIo);
	}
	
	@Test
	public void testDoInvoke() {
		sddDescriptionIo.doInvoke(configurator, null);
	}

}
