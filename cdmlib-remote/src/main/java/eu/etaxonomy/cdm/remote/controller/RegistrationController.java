/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IRegistrationService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 24.03.2009
 */

@Controller
@Api("registration")
@RequestMapping(value = {"/registration"})
public class RegistrationController extends BaseController<Registration, IRegistrationService>
{

    public static final Logger logger = Logger.getLogger(RegistrationController.class);

    public RegistrationController(){
        setInitializationStrategy(Arrays.asList(new String[]{
                "$",
                "name.$",
                "typeDesignations.$"
             }));
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Autowired
    @Override
    public void setService(IRegistrationService service) {
        this.service = service;
    }

    @Override
    @RequestMapping(value="{uuid}", method = RequestMethod.GET)
    public Registration doGet(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        Registration reg = super.doGet(uuid, request, response);
        if(reg != null){
            if(userHelper.userIsAutheticated() && userHelper.userIsAnnonymous() && !reg.getStatus().equals(RegistrationStatus.PUBLISHED)) {
                // completely hide the fact that there is a registration
                HttpStatusMessage.create("No such Registration", HttpServletResponse.SC_NO_CONTENT).send(response);
            }
        }
        return reg;
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "identifier", value = "The persitent identifier of the Registration.", required = true, dataType = "string", paramType = "path"),
    })
    @ApiOperation(value = "Finds Registration by persitent identifier.",
        notes = "The identifier passed as paramter must be unique in the database otherwise the server will responde with the HTTP error code: " + HttpServletResponse.SC_PRECONDITION_FAILED
    )
    @RequestMapping(value="identifier/**", method = RequestMethod.GET)
    public Registration doGetByIdentifier(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetByIdentifier() " + requestPathAndQuery(request));

        String identifier = readPathParameter(request, "/registration/identifier/");

        Pager<Registration> regPager = service.pageByIdentifier(identifier, 0, 2, getInitializationStrategy());

        if(regPager.getCount() == 1){
            return regPager.getRecords().get(0);
        } else if(regPager.getCount() > 1){
            HttpStatusMessage.create("The identifier " + identifier + " refrences multiple registrations", HttpServletResponse.SC_PRECONDITION_FAILED).send(response);
            return null; // never reached, due to previous send()
        } else {
            return null;
        }
    }


    @ApiImplicitParams({
        @ApiImplicitParam(name = "identifier", value = "The persitent identifier of the Registration", required = true, dataType = "string", paramType = "path"),
    })
    @RequestMapping(value="identifier/**", method = RequestMethod.GET, params={"validateUniqueness"})
    public Pager<Registration> doPageByIdentifier(
            @RequestParam(value = "pageNumber", required = true) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doPageByIdentifier() " + requestPathAndQuery(request));

        String identifier = readPathParameter(request, "/registration/identifier/");

        Pager<Registration> regPager = service.pageByIdentifier(identifier, pageIndex, pageSize, getInitializationStrategy());
        return regPager;
    }





}
