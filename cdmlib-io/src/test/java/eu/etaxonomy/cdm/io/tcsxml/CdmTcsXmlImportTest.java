package eu.etaxonomy.cdm.io.tcsxml;

import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class CdmTcsXmlImportTest extends CdmTransactionalIntegrationTest {
	
	@SpringBeanByType
	CdmTcsXmlImport cdmTcsXmlImport;
	
	private IImportConfigurator configurator;
	
	@Before
	public void setUp() {
		URL url = this.getClass().getResource("/eu/etaxonomy/cdm/io/tcsxml/CdmTcsXmlImortTest-input.xml");
		configurator = TcsXmlImportConfigurator.NewInstance(url.toString(), null);
	}
	
	@Test
	public void testInit() {
		assertNotNull("cdmTcsXmlImport should not be null", cdmTcsXmlImport);
	}
	
	@Test
	public void testDoInvoke() {
		cdmTcsXmlImport.invoke(configurator);
	}

}
