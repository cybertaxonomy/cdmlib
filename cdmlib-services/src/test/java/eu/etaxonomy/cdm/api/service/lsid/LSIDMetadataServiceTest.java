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

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.lsid.impl.LsidRegistryImpl;
import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet("LSIDAuthorityServiceTest.testGetAvailableServices.xml")
public class LSIDMetadataServiceTest extends CdmIntegrationTest {

	@SpringBeanByType
	private LSIDMetadataService lsidMetadataService;

	@SpringBeanByType
	private LSIDRegistry lsidRegistry;
	
	private LSID lsid;
	
	@Before	
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

}
