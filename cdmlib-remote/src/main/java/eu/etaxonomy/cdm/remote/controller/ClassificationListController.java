// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.model.taxon.Classification;
import io.swagger.annotations.Api;

/**
 * @author n.hoffmann
 * @created Apr 8, 2010
 */
@Controller
@Api("classification")
@RequestMapping(value = {"/classification"})
public class ClassificationListController extends IdentifiableListController<Classification,IClassificationService> {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ClassificationListController.class);


    protected static final List<String> DEFAULT_INIT_STRATEGY = Arrays.asList(new String []{
            "reference.authorship"
    });


    @Override
    @Autowired
    public void setService(IClassificationService service) {
        this.service = service;
    }

}
