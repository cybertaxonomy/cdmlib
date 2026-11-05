/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.lsid.ExpiringResponse;
import com.ibm.lsid.LSIDException;
import com.ibm.lsid.MalformedLSIDException;
import com.ibm.lsid.server.LSIDServerException;

import eu.etaxonomy.cdm.api.service.lsid.LSIDAuthorityService;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.LSIDAuthority;
import eu.etaxonomy.cdm.test.util.LSIDMatchers;

/**
 * @author b.clark
 * @date 2009
 */
public class AuthorityControllerTest {

	private LSIDAuthorityService authorityService;

	private AuthorityController authorityController;

	private LSID lsid;
	private LSIDAuthority lsidAuthority;
	private Object source;
	private ExpiringResponse expiringResponse;

	@BeforeEach
	public void setUp() {
		try {
			lsid = new LSID("urn:lsid:example.org:taxonconcept:1");
		} catch (MalformedLSIDException e) { }

		try {
			lsidAuthority = new LSIDAuthority("fred.org");
		} catch (MalformedLSIDException e) { }

		authorityService = EasyMock.createMock(LSIDAuthorityService.class);
		authorityController = new AuthorityController();
		authorityController.setLsidAuthorityService(authorityService);
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

	@Test
    public void testGetServicesWithUnknownLSID() throws Exception {
    	LSIDServerException lse = new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");
		EasyMock.expect(authorityService.getAvailableServices(LSIDMatchers.eqLSID(lsid))).andThrow(lse);
		EasyMock.replay(authorityService);

		assertThrows(LSIDServerException.class,
				() -> authorityController.getAvailableServices(lsid));
	}

	@Test
    public void testNotifyForeignAuthority() throws Exception {
    	LSIDServerException lse = new LSIDServerException(LSIDException.METHOD_NOT_IMPLEMENTED, "FAN service not available");

    	authorityService.notifyForeignAuthority(LSIDMatchers.eqLSID(lsid), LSIDMatchers.eqLSIDAuthority(lsidAuthority));
    	EasyMock.expectLastCall().andThrow(lse);

    	EasyMock.replay(authorityService);

		assertThrows(LSIDServerException.class,
				() -> authorityController.notifyForeignAuthority(lsid, lsidAuthority));
    }

	@Test
    public void testRevokeForeignAuthority() throws Exception {
    	LSIDServerException lse = new LSIDServerException(LSIDException.METHOD_NOT_IMPLEMENTED, "FAN service not available");

    	authorityService.revokeNotificationForeignAuthority(LSIDMatchers.eqLSID(lsid), LSIDMatchers.eqLSIDAuthority(lsidAuthority));
    	EasyMock.expectLastCall().andThrow(lse);

    	EasyMock.replay(authorityService);
		assertThrows(LSIDServerException.class,
				() -> authorityController.revokeNotificationForeignAuthority(lsid, lsidAuthority));
    }
}
