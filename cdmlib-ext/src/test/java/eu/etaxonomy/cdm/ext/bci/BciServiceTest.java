/**
 * Copyright (C) 2010 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.ext.bci;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.ext.ipni.IIpniService;
import eu.etaxonomy.cdm.model.occurrence.Collection;

/**
 * @author a.mueller
 */
public class BciServiceTest {

	private IBciServiceWrapper service1;

	@BeforeEach
	public void setUp() throws Exception {
		service1 = new BciServiceWrapper();
	}

// ******************************* TESTS ******************************************************/

	@Test
	public void testBciService() {
		assertNotNull(service1, "Service should not be null");
		assertNotNull(service1.getServiceUrl(IIpniService.ADVANCED_NAME_SERVICE_URL), "URL1 should not be null");
	}

	@Test
	@Disabled // service is under refactoring
	public void testGetCollectionsByCode(){
		ICdmRepository repository = null;
		List<Collection> collectionList = service1.getCollectionsByCode("BG", repository);
		//expected web service result: urn:lsid:biocol.org:col:15727	http://biocol.org/urn:lsid:biocol.org:col:15727	University of Bergen Herbarium

		assertEquals(1, collectionList.size(), "There should be exactly 1 result for 'BG'");
		Collection collection = collectionList.get(0);
		//title cache
		assertEquals("University of Bergen Herbarium", collection.getName(), "Name for BG should be 'University of Bergen Herbarium'");
		assertEquals("urn:lsid:biocol.org:col:15727", collection.getLsid().getLsid(), "LSID should be urn:lsid:biocol.org:col:15727");

		collectionList = service1.getCollectionsByCode("CM", repository);
		assertEquals(3, collectionList.size(), "There should be exactly 3 result for 'CM'. If not try http://www.biocol.org/rest/lookup/code/CM");
	}

	@Test
	public void testGetServiceUrl() {
		assertNotNull(service1, "Service should not be null");
		assertNotNull(service1.getServiceUrl(IBciServiceWrapper.LOOKUP_CODE_REST), "URL1 should not be null");
	}
}