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
 * @author a.kohlbecker
 * @date 14.10.2010
 *
 */
@Controller
@Api("portal_derivedUnitFacade")
@RequestMapping(value = {"/portal/derivedUnitFacade/{uuid}"})
public class DerivedUnitFacadePortalController extends
        DerivedUnitFacadeController {

    public DerivedUnitFacadePortalController(){
        super();
        setInitializationStrategy(Arrays.asList(new String []{
            "*",
            "titleCache",
//			"gatheringEvent.*",
            "ecologyAll",
            "plantDescriptionAll",

//			"fieldUnit.*",
            "fieldObjectMedia",

//			"derivedUnit.*",
            "derivedUnitMedia",
            "derivedUnitDefinitions",

            "exactLocation.referenceSystem"


        }));
    }

}
