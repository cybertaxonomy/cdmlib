/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.service;

import io.swagger.annotations.Api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.lsid.ExpiringResponse;
import com.ibm.lsid.LSIDException;
import com.ibm.lsid.server.LSIDServerException;

import eu.etaxonomy.cdm.api.service.lsid.LSIDAuthorityService;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.LSIDAuthority;
import eu.etaxonomy.cdm.remote.editor.LSIDAuthorityPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.LSIDPropertyEditor;

/**
 * Controller which accepts incoming requests to the LSIDAuthorityService
 * This controller has three methods. Using ControllerClassNameHandlerMapping means that
 * any requests to /authority/ get handled by this controller.
 *
 * To allow the Spring DispatcherServlet to handle /authority/, /authority/notify/ and
 * /authority/revoke/ directly, you need to add the following mappings to your web.xml
 *
 * <servlet-mapping>
 *   <servlet-name>${servlet.name}</servlet-name>
 *   <url-pattern>/authority/</url-pattern>
 * </servlet-mapping>
 * <servlet-mapping>
 *   <servlet-name>${servlet.name}</servlet-name>
 *   <url-pattern>/authority/revoke/</url-pattern>
 * </servlet-mapping>
 * <servlet-mapping>
 *   <servlet-name>${servlet.name}</servlet-name>
 *   <url-pattern>/authority/notify/</url-pattern>
 * </servlet-mapping>
 *
 * You also need to use some kind of MethodNameResolver - AuthorityMethodNameResolver
 * maps the request to the correct method using the request URL.
 *
 * @author ben
 * @author Ben Szekely (<a href="mailto:bhszekel@us.ibm.com">bhszekel@us.ibm.com</a>)
 * @see org.springframework.web.servlet.mvc.support.ControllerClassNameHandlerMapping
 * @see org.cateproject.controller.interceptor.lsid.AuthorityMethodNameResolver
 * @see com.ibm.lsid.server.servlet.AuthorityServlet
 */
@Controller
@Api(value="lsid_authority",
    description="Controller which accepts incoming requests to the LSIDAuthorityService.")
public class AuthorityController {
    private static Log log = LogFactory.getLog(AuthorityController.class);
    private LSIDAuthorityService lsidAuthorityService;

    @Autowired
    public void setLsidAuthorityService(LSIDAuthorityService lsidAuthorityService) {
        this.lsidAuthorityService = lsidAuthorityService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LSID.class, new LSIDPropertyEditor());
        binder.registerCustomEditor(LSIDAuthority.class, new LSIDAuthorityPropertyEditor());
    }

    /**
     * Get the available services of this LSIDAuthority as a wsdl document
     *
     * @return ModelAndView containing the WSDL as a javax.xml.transform.Source, view name 'Authority.wsdl'
     * @throws LSIDServerException
     * @see javax.xml.transform.Source
     */
    @RequestMapping(value="/authority/",params="!lsid", method = RequestMethod.GET) // mapped as absolute path, see CdmAntPathMatcher
    public ModelAndView getAvailableServices() throws LSIDServerException {
        ExpiringResponse expiringResponse = lsidAuthorityService.getAuthorityWSDL();
        return new ModelAndView("Authority.wsdl","source",expiringResponse.getValue());
    }

    /**
     * Get the available services for a given lsid
     *
     * @return ModelAndView containing the WSDL as a javax.xml.transform.Source, view name 'Services.wsdl'
     * @param LSID the lsid to query the service with
     * @throws LSIDServerException
     * @see javax.xml.transform.Source
     */
    @RequestMapping(value="/authority/",params="lsid", method = RequestMethod.GET)
    public ModelAndView getAvailableServices(@RequestParam("lsid")LSID lsid) throws LSIDServerException {
        ExpiringResponse expiringResponse = lsidAuthorityService.getAvailableServices(lsid);
        return new ModelAndView("Services.wsdl","source",expiringResponse.getValue());
    }

    /**
     * Notify the authority that another authority resolves the object with the given identifier
     *
     * @param lsid the LSID to notify the authority about
     * @param authorityName the foreign authority
     * @return ModelAndView (null)
     * @throws LSIDServerException
     */
    @RequestMapping(value="/authority/notify/",params={"lsid","authorityName"}, method = RequestMethod.GET)
    public ModelAndView notifyForeignAuthority(@RequestParam("lsid")LSID lsid,
                                               @RequestParam("authorityName")LSIDAuthority lsidAuthority) throws LSIDServerException {
        lsidAuthorityService.notifyForeignAuthority(lsid,lsidAuthority);
        return null;
    }

    /**
     * Maps to the notify path without the required params
     *
     * @throws LSIDServerException
     */
    @RequestMapping(value="/authority/notify/", method = RequestMethod.GET)
    public ModelAndView notifyForeignAuthority() throws LSIDException {
        throw new LSIDException(LSIDException.INVALID_METHOD_CALL, "You must supply an lsid and an lsidAuthority");
    }

    /**
     * Notify the authority that another authority no longer resolves the object with the given identifier
     *
     * @param lsid the LSID to notify the authority about
     * @param authorityName the foreign authority
     * @return ModelAndView (null)
     * @throws LSIDServerException
     */
    @RequestMapping(value="/authority/revoke/",params={"lsid","authorityName"}, method = RequestMethod.GET)
    public ModelAndView revokeNotificationForeignAuthority(@RequestParam("lsid")LSID lsid,
                                                           @RequestParam("authorityName")LSIDAuthority lsidAuthority) throws LSIDServerException {
        lsidAuthorityService.revokeNotificationForeignAuthority(lsid,lsidAuthority);
        return null;
    }

    /**
     * Maps to the revoke path without the required params
     *
     * @throws LSIDServerException
     */
    @RequestMapping(value="/authority/revoke/", method = RequestMethod.GET)
    public ModelAndView revokeNotificationForeignAuthority() throws LSIDException {
        throw new LSIDException(LSIDException.INVALID_METHOD_CALL, "You must supply an lsid and an lsidAuthority");
    }
}
