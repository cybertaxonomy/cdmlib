package eu.etaxonomy.cdm.remote.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.unitils.UnitilsJUnit4;
import org.unitils.easymock.annotation.Mock;
import org.unitils.inject.annotation.InjectInto;
import org.unitils.inject.annotation.TestedObject;

import com.ibm.lsid.LSIDException;
import com.ibm.lsid.MalformedLSIDException;
import com.ibm.lsid.server.LSIDServerException;

import eu.etaxonomy.cdm.api.service.lsid.LSIDDataService;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.test.util.LSIDMatchers;

public class DataControllerTest extends UnitilsJUnit4 {
	
	@Mock
	@InjectInto(property = "lsidDataService")
	LSIDDataService dataService;
	
	@TestedObject
    private DataController dataController;
	
	private static String expectedData = "Acherontia Laspeyres 1809 sensu example.org 2007";
	private InputStream inputStream;
	private LSID lsid;
	private MockHttpServletResponse response;


	@Before
    public void setUp() {
		try {
			lsid = new LSID("urn:lsid:example.org:taxonconcepts:1");
		} catch (MalformedLSIDException e) { }
		
    	dataController = new DataController();
    	inputStream = new ByteArrayInputStream(expectedData.getBytes());
    	dataService = EasyMock.createMock(LSIDDataService.class);
    	response = new MockHttpServletResponse();
    }
	
	@Test
    public void testGetData() throws Exception {
    	EasyMock.expect(dataService.getData(LSIDMatchers.eqLSID(lsid))).andReturn(inputStream);
		EasyMock.replay(dataService);

		ModelAndView modelAndView = dataController.getData(lsid,response);
		EasyMock.verify(dataService); 
		
		Assert.assertNull(modelAndView);
		Assert.assertEquals(response.getContentAsString(),expectedData);
	}
    
	@Test(expected = LSIDServerException.class)
    public void testGetDataWithUnknownLSID() throws Exception {;
    	LSIDServerException lse = new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");
    	
    	EasyMock.expect(dataService.getData(LSIDMatchers.eqLSID(lsid))).andThrow(lse);
		EasyMock.replay(dataService);
		
		dataController.setLsidDataService(dataService);
        dataController.getData(lsid, response);
	}
}
