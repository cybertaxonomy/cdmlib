/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @since Jul 12, 2019
 *
 */
@Controller
@Api(value = "portal_typeDesignation")
@RequestMapping(value = {"/portal/typedesignation/{uuid}"})
public class TypeDesignationPortalController extends TypeDesignationController {

    public static final List<String> DEFAULT_INIT_STRATEGY = Arrays.asList(new String []{
            "typeName.$",
            "typeSpecimen",
            "typeStatus.representations",
            "typifiedNames.nomenclaturalReference.authorship.$",
            "typifiedNames.nomenclaturalReference.inReference.authorship.$",
            "typifiedNames.nomenclaturalReference.inReference.inReference.authorship.$",
            "typeSpecimen.media",
            "registrations.institution",
            "text",
            "designationSource.citation.authorship.$",
            "designationSource.links.description",
            "sources.links.description", // other sources
            "sources.citation.authorship.$" // other sources
    });

    public TypeDesignationPortalController() {
        setInitializationStrategy(DEFAULT_INIT_STRATEGY);
    }


}
