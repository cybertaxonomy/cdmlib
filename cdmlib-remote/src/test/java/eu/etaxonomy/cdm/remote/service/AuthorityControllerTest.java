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

import com.ibm.lsid.ExpiringResponse;
import com.ibm.lsid.LSIDException;
import com.ibm.lsid.MalformedLSIDException;
import com.ibm.lsid.server.LSIDServerException;

import eu.etaxonomy.cdm.api.service.lsid.LSIDAuthorityService;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.LSIDAuthority;
import eu.etaxonomy.cdm.test.util.LSIDMatchers;

public class AuthorityControllerTest extends UnitilsJUnit4 {

	@Mock
	@InjectInto(property = "lsidAuthorityService")
	private LSIDAuthorityService authorityService;

	@TestedObject
	private AuthorityController authorityController;

	private LSID lsid;
	private LSIDAuthority lsidAuthority;
	private Object source;
	private ExpiringResponse expiringResponse;

	@Before
	public void setUp() {
		try {
			lsid = new LSID("urn:lsid:example.org:taxonconcept:1");
		} catch (MalformedLSIDException e) { }

		try {
			lsidAuthority = new LSIDAuthority("fred.org");
		} catch (MalformedLSIDException e) { }

		authorityService = org.easymock.classextension.EasyMock.createMock(LSIDAuthorityService.class);
		authorityController = new AuthorityController();
		source = new Object();
		expiringResponse = new ExpiringResponse(source, null);
   }

	@Test
	public void testAuthorityWSDL() throws Exception {
		EasyMock.expect(authorityService.getAuthorityWSDL()).andReturn(expiringResponse);
	    EasyMock.replay(authorityService);

		ModelAndView modelAndView = authorityController.getAvailableServices();

		EasyMock.verify(authorityService);
		ModelAndViewAssert.assertViewName(modelAndView, "Authority.wsdl");
		ModelAndViewAssert.assertModelAttributeValue(modelAndView, "source", source);
	}

	@Test
    public void testGetServices() throws Exception {
		EasyMock.expect(authorityService.getAvailableServices(LSIDMatchers.eqLSID(lsid))).andReturn(expiringResponse);
		EasyMock.replay(authorityService);

		ModelAndView modelAndView = authorityController.getAvailableServices(lsid);

		EasyMock.verify(authorityService);
		ModelAndViewAssert.assertViewName(modelAndView, "Services.wsdl");
		ModelAndViewAssert.assertModelAttributeValue(modelAndView, "source",source);
	}

	@Test(expected = LSIDServerException.class)
    public void testGetServicesWithUnknownLSID() throws Exception {
    	LSIDServerException lse = new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");
		EasyMock.expect(authorityService.getAvailableServices(LSIDMatchers.eqLSID(lsid))).andThrow(lse);
		EasyMock.replay(authorityService);

		authorityController.getAvailableServices(lsid);
	}

	@Test(expected = LSIDServerException.class)
    public void testNotifyForeignAuthority() throws Exception {
    	LSIDServerException lse = new LSIDServerException(LSIDException.METHOD_NOT_IMPLEMENTED, "FAN service not available");

    	authorityService.notifyForeignAuthority(LSIDMatchers.eqLSID(lsid), LSIDMatchers.eqLSIDAuthority(lsidAuthority));
    	EasyMock.expectLastCall().andThrow(lse);

    	EasyMock.replay(authorityService);

		authorityController.notifyForeignAuthority(lsid, lsidAuthority);
    }

	@Test(expected = LSIDServerException.class)
    public void testRevokeForeignAuthority() throws Exception {
    	LSIDServerException lse = new LSIDServerException(LSIDException.METHOD_NOT_IMPLEMENTED, "FAN service not available");

    	authorityService.revokeNotificationForeignAuthority(LSIDMatchers.eqLSID(lsid), LSIDMatchers.eqLSIDAuthority(lsidAuthority));
    	EasyMock.expectLastCall().andThrow(lse);

    	EasyMock.replay(authorityService);
		authorityController.revokeNotificationForeignAuthority(lsid, lsidAuthority);
    }
}
