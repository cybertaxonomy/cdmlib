/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.lsid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.api.service.lsid.impl.LsidRegistryImpl;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesSpringIntegrationTest;

@CdmDataSet
public class LSIDRegistryTest extends CdmServicesSpringIntegrationTest {

	@Autowired
	private LSIDRegistry lsidRegistry;

	private LSID lsid;
	private LSID ipniLsid;
	private LSID unknownAuthorityLsid;

	@BeforeEach
	public void setUp() {

		try {
		    lsid = new LSID("urn:lsid:example.org:names:1");
		    ipniLsid = new LSID("urn:lsid:ipni.org:names:1");
		    unknownAuthorityLsid = new LSID("urn:lsid:fred.org:dagg:1");
		} catch(MalformedLSIDException mle) {
			fail();
		}
		((LsidRegistryImpl)lsidRegistry).init();
	}

	@Test
	public void testGetService() {
		assertNotNull(lsidRegistry, "lsidRegistry should exist");
	}

	@Test
	public void testLookupDao() {
		assertNotNull(lsidRegistry.lookupDAO(lsid), "lookupDao should return a dao");
		assertEquals(lsidRegistry.lookupDAO(lsid), lsidRegistry.lookupDAO(ipniLsid), "lookupDao should return the same dao for " + lsid + " as for " + ipniLsid);
	}

	@Test
	public void testLookupDaoWithUnknownLsid() {
		assertNull(lsidRegistry.lookupDAO(unknownAuthorityLsid), "lookupDao shoud return null for an lsid it does not recognise");
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}