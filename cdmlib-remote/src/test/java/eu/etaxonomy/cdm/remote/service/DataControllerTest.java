/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.lsid.LSIDException;
import com.ibm.lsid.MalformedLSIDException;
import com.ibm.lsid.server.LSIDServerException;

import eu.etaxonomy.cdm.api.service.lsid.LSIDDataService;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.test.util.LSIDMatchers;

public class DataControllerTest {

	private LSIDDataService dataService;

    private DataController dataController;

	private static String expectedData = "Acherontia Laspeyres 1809 sensu example.org 2007";
	private InputStream inputStream;
	private LSID lsid;
	private MockHttpServletResponse response;


	@BeforeEach
    public void setUp() {
		try {
			lsid = new LSID("urn:lsid:example.org:taxonconcepts:1");
		} catch (MalformedLSIDException e) { }

    	dataController = new DataController();
    	inputStream = new ByteArrayInputStream(expectedData.getBytes());
    	dataService = EasyMock.createMock(LSIDDataService.class);
    	dataController.setLsidDataService(dataService);
    	response = new MockHttpServletResponse();
    }

	@Test
    public void testGetData() throws Exception {
    	EasyMock.expect(dataService.getData(LSIDMatchers.eqLSID(lsid))).andReturn(inputStream);
		EasyMock.replay(dataService);

		ModelAndView modelAndView = dataController.getData(lsid,response);
		EasyMock.verify(dataService);

		assertNull(modelAndView);
		assertEquals(expectedData, response.getContentAsString());
	}

	@Test
    public void testGetDataWithUnknownLSID() throws Exception {
    	LSIDServerException lse = new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");

    	EasyMock.expect(dataService.getData(LSIDMatchers.eqLSID(lsid))).andThrow(lse);
		EasyMock.replay(dataService);

        assertThrows(LSIDServerException.class,
        		() -> dataController.getData(lsid, response));
	}
}
