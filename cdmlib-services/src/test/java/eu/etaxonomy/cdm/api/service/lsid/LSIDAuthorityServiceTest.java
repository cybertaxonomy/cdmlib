/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.lsid;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.lsid.ExpiringResponse;
import com.ibm.lsid.server.LSIDServerException;

import eu.etaxonomy.cdm.api.service.lsid.impl.LsidRegistryImpl;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesSpringIntegrationTest;

@CdmDataSet("LSIDAuthorityServiceTest.testGetAvailableServices.xml")
public class LSIDAuthorityServiceTest extends CdmServicesSpringIntegrationTest {

	@Autowired
	private LSIDAuthorityService lsidAuthorityService;

	@Autowired
	private LSIDRegistry lsidRegistry;

	private LSID knownLsid;
	private LSID unknownLsid;

	@BeforeEach
	public void setUp() throws Exception {
		unknownLsid = new LSID("fred.org", "dagg", "1", null);
		knownLsid = new LSID("example.org", "taxonconcepts", "1", null);
	    XMLUnit.setIgnoreWhitespace(true);
	    ((LsidRegistryImpl)lsidRegistry).init();
	}

	@Test
	public void testInit()	{
		assertNotNull("lsidAuthorityService should exist",lsidAuthorityService);
	}

	@Test
	public void testGetAuthorityWSDL() throws Exception {
		ExpiringResponse expiringResponse = lsidAuthorityService.getAuthorityWSDL();
		String resource = "/eu/etaxonomy/cdm/api/service/lsid/LSIDAuthorityServiceTest.testGetAuthorityWSDL-result.wsdl";
		String result = transformSourceToString((Source) expiringResponse.getValue());

		assertXMLEqual("getAuthorityWSDL should return an xml source equal to the test resource",
		        new InputStreamReader(this.getClass().getResourceAsStream(resource)),
		        new StringReader(result));
	}

	/**
	 * Unfortunately, the ordering of the services switches round between linux and windows.
	 * This is why we ignore this test failure.
	 *
	 * @throws Exception
	 */
	@Test
	@Disabled
	public void testGetAvailableServicesWithKnownLSID() throws Exception {
		ExpiringResponse expiringResponse = lsidAuthorityService.getAvailableServices(knownLsid);

		String resource = "/eu/etaxonomy/cdm/api/service/lsid/LSIDAuthorityServiceTest.testGetAvailableServicesWithKnownLSID-result.wsdl";
		String result = transformSourceToString((Source) expiringResponse.getValue());
		assertXMLEqual("getAvailableServices should return an xml source equal to the test resource",new InputStreamReader(this.getClass().getResourceAsStream(resource)),new StringReader(result));
	}

	@Test
	public void testGetAvailableServicesWithUnknownLSID() throws Exception {
		assertThrows(LSIDServerException.class, () -> lsidAuthorityService.getAvailableServices(unknownLsid));
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
