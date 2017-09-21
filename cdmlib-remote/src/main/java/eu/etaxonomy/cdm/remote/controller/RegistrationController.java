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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IRegistrationService;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@Api("registration")
@RequestMapping(value = {"/registration/{uuid}", "/registration/{localID}"})
public class RegistrationController extends BaseController<Registration, IRegistrationService>
{

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
    public Registration doGet(@PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        Registration reg = super.doGet(uuid, request, response);
        if(reg != null){
            if(userIsAutheticated() && userIsAnnonymous() && !reg.getStatus().equals(RegistrationStatus.PUBLISHED)) {
                // completely hide the fact that there is a registration
                HttpStatusMessage.create("No such Registration", HttpServletResponse.SC_NO_CONTENT).send(response);
            }
        }
        return reg;
    }

 /*
    @Override
    public Registration doGetbyLocalId(@PathVariable("localID") Integer localID,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        Registration reg = super.doGet(localID, request, response);
        if(reg != null){
            if(userIsAutheticated() && userIsAnnonymous() && !reg.getStatus().equals(RegistrationStatus.PUBLISHED)) {
                // completely hide the fact that there is a registration
                HttpStatusMessage.create("No such Registration", HttpServletResponse.SC_NO_CONTENT);
            }
        }
        return reg;
    }
*/

}
