/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service.lsid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.api.service.lsid.impl.LsidRegistryImpl;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmServicesTransactionalSpringIntegrationTest;

@CdmDataSet("LSIDAuthorityServiceTest.testGetAvailableServices.xml")
public class LSIDMetadataServiceTest extends CdmServicesTransactionalSpringIntegrationTest {

	@Autowired
	private LSIDMetadataService lsidMetadataService;

	@Autowired
	private LSIDRegistry lsidRegistry;

	private LSID lsid;

	@BeforeEach
	public void setUp() throws Exception {

		lsid = new LSID("example.org", "taxonconcepts", "1", null);
	    ((LsidRegistryImpl)lsidRegistry).init();
	}

	@Test
	public void testInit()	{
		assertNotNull("lsidMetadataService should exist",lsidMetadataService);
	}

	@Test
	public void testGetMetadataWithKnownLSID() throws Exception {
		IIdentifiableEntity identifiableEntity = lsidMetadataService.getMetadata(lsid);
		assertNotNull("getMetadata should return an IdentifiableEntity",identifiableEntity);
		assertEquals("the object should have an lsid equal to the lsid supplied",lsid.getAuthority(),identifiableEntity.getLsid().getAuthority());
		assertEquals("the object should have an lsid equal to the lsid supplied",lsid.getNamespace(),identifiableEntity.getLsid().getNamespace());
		assertEquals("the object should have an lsid equal to the lsid supplied",lsid.getObject(),identifiableEntity.getLsid().getObject());
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
