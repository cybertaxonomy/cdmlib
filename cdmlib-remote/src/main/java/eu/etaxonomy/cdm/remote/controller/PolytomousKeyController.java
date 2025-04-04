/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @since 24.03.2011
 */
@Controller
@Api("polytomousKey")
@RequestMapping(value = {"/polytomousKey/{uuid}"})
public class PolytomousKeyController extends AbstractIdentifiableController<PolytomousKey, IPolytomousKeyService> {

    private static final Logger logger = LogManager.getLogger();

    private static final List<String> KEY_INIT_STRATEGY = Arrays.asList(new String[]{
            "annotations.annotationType.includes",
            "sources.citation.$"
    });

    @Override
    @Autowired
    public void setService(IPolytomousKeyService service) {
        this.service = service;
    }

    @Override
    protected  <CDM_BASE extends CdmBase> List<String> complementInitStrategy(@SuppressWarnings("unused") Class<CDM_BASE> clazz, List<String> pathProperties) {
        List<String> result = new ArrayList<>();
        result.addAll(KEY_INIT_STRATEGY);
        result.addAll(pathProperties);
        return result;
    }


}