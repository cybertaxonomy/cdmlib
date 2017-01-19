/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.model.common.User;

/**
 * @author a.kohlbecker
 * @date Oct 11, 2016
 *
 */
@Controller
@Api("user")
@RequestMapping(value = {"/user"})
public class UserController extends AbstractController<User, IUserService> {

    /**
     * {@inheritDoc}
     */
    @Override
    @Autowired
    public void setService(IUserService service) {
        this.service = service;
    }

    @RequestMapping(value="me", method=RequestMethod.GET)
    public Principal doGetCurrentUser(Principal principal) {
        return principal;
    }



}
