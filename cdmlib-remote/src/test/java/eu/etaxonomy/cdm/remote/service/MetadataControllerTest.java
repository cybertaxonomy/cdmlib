package eu.etaxonomy.cdm.remote.service;


import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.unitils.UnitilsJUnit4;
import org.unitils.easymock.annotation.Mock;
import org.unitils.inject.annotation.InjectInto;
import org.unitils.inject.annotation.TestedObject;

import com.ibm.lsid.LSIDException;
import com.ibm.lsid.MalformedLSIDException;
import com.ibm.lsid.MetadataResponse;
import com.ibm.lsid.server.LSIDServerException;

import eu.etaxonomy.cdm.api.service.lsid.LSIDMetadataService;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.test.util.LSIDMatchers;

public class MetadataControllerTest extends UnitilsJUnit4 {
	
	@Mock
	@InjectInto(property = "lsidMetadataService")
	private LSIDMetadataService metadataService;
	
	@TestedObject
    private MetadataController metadataController;
    
    private LSID lsid;
    private String[] acceptedFormats;
    
	@Before
	public void setUp() {
		metadataController = new MetadataController();
		metadataService = EasyMock.createMock(LSIDMetadataService.class);
		try {
			lsid = new LSID("urn:lsid:fred.org:dagg:1");
		} catch (MalformedLSIDException e) { }
		acceptedFormats = new String[]{MetadataResponse.RDF_FORMAT};
	}
	
	@Test
    public void testGetMetadata() throws Exception {
		MetadataResponse metadataResponse = new MetadataResponse(null, null);
		EasyMock.expect(metadataService.getMetadata(LSIDMatchers.eqLSID(lsid), EasyMock.aryEq(acceptedFormats))).andReturn(metadataResponse);
		EasyMock.replay(metadataService);
		
		
		ModelAndView modelAndView = metadataController.getMetadata(lsid, "application/xml+rdf");
		
		EasyMock.verify(metadataService);
		ModelAndViewAssert.assertViewName(modelAndView, "Metadata.rdf");		
		ModelAndViewAssert.assertModelAttributeValue(modelAndView,"metadataResponse",metadataResponse);
	}
    
	@Test(expected = LSIDServerException.class)
    public void testGetMetadataWithoutAcceptedFormat() throws Exception {
    	acceptedFormats = new String[]{MetadataResponse.N3_FORMAT,MetadataResponse.XMI_FORMAT};
    	LSIDServerException lse = new LSIDServerException(LSIDServerException.NO_METADATA_AVAILABLE_FOR_FORMATS,"No metadata found for given format");
    	EasyMock.expect(metadataService.getMetadata(LSIDMatchers.eqLSID(lsid), EasyMock.aryEq(acceptedFormats))).andThrow(lse);
    	EasyMock.replay(metadataService);
    	
		metadataController.getMetadata(lsid, "application/n3,application/xml+xmi");
	}
    
	@Test(expected = LSIDServerException.class)
    public void testGetMetadataWithUnknownLSID() throws Exception {
 
    	LSIDServerException lse = new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");
    	EasyMock.expect(metadataService.getMetadata(LSIDMatchers.eqLSID(lsid), EasyMock.aryEq(acceptedFormats))).andThrow(lse);
    	EasyMock.replay(metadataService);

        metadataController.getMetadata(lsid, "application/xml+rdf");
	}
}
