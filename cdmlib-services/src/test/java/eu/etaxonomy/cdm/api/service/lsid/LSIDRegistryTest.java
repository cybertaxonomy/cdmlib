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
import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import com.ibm.lsid.MalformedLSIDException;

import eu.etaxonomy.cdm.api.service.lsid.impl.LsidRegistryImpl;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class LSIDRegistryTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	LSIDRegistry lsidRegistry;
	
	private LSID lsid;
	private LSID ipniLsid;
	private LSID unknownAuthorityLsid;
	
	@Before
	public void setUp() {
		
		try {
		    lsid = new LSID("urn:lsid:example.org:names:1");
		    ipniLsid = new LSID("urn:lsid:ipni.org:names:1");
		    unknownAuthorityLsid = new LSID("urn:lsid:fred.org:dagg:1");
		} catch(MalformedLSIDException mle) {
			Assert.fail();
		}
		((LsidRegistryImpl)lsidRegistry).init();
	}
	
	@Test
	public void testGetService() {   	    		
		assertNotNull("lsidRegistry should exist",lsidRegistry);
	}
	
	@Test 
	public void testLookupDao() {
		assertNotNull("lookupDao should return a dao",lsidRegistry.lookupDAO(lsid));
		assertEquals("lookupDao should return the same dao for " + lsid + " as for " + ipniLsid,lsidRegistry.lookupDAO(lsid),lsidRegistry.lookupDAO(ipniLsid));
	}
	
	@Test 
	public void testLookupDaoWithUnknownLsid() {
		assertNull("lookupDao shoud return null for an lsid it does not recognise",lsidRegistry.lookupDAO(unknownAuthorityLsid));
	}
}
