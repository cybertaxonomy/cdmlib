// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import java.util.Arrays;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author l.morris
 * @date Jun 13, 2013
 *
 */
@Controller
@Api("portal_reference")
@RequestMapping(value = {"/portal/reference/{uuid}"})
public class ReferencePortalController extends ReferenceController {

    public ReferencePortalController(){
        setInitializationStrategy(Arrays.asList(new String[]{
                "$",
                "authorship.*"
             }));
    }

}
