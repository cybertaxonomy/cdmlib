package eu.etaxonomy.cdm.io.sdd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class SDDDescriptionIOTest extends CdmTransactionalIntegrationTest {
	
	@SpringBeanByType
	SDDDescriptionIO sddDescriptionIo;
	
	@SpringBeanByType
	INameService nameService;
	
	private IImportConfigurator configurator;
	
	@Before
	public void setUp() {
		URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/sdd/SDDDescriptionIOTest-input.xml");
		configurator = SDDImportConfigurator.NewInstance(url.toString(), null);
	}
	
	@Test
	public void testInit() {
		assertNotNull("sddDescriptionIo should not be null",sddDescriptionIo);
		assertNotNull("nameService should not be null", nameService);
	}
	
	@Test
	public void testDoInvoke() {
		sddDescriptionIo.doInvoke(configurator, null);
		assertEquals("Number of TaxonNames should be 1", 1, nameService.count());
	}

}
