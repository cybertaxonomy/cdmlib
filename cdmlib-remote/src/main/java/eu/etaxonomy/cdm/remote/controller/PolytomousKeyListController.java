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

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;

/**
 * @author a.kohlbecker
 * @date 24.03.2011
 *
 */
@Controller
@Api("polytomousKey")
@RequestMapping(value = {"/polytomousKey"})
public class PolytomousKeyListController extends AbstractIdentifiableListController<PolytomousKey, IPolytomousKeyService> {

    public static final Logger logger = Logger.getLogger(PolytomousKeyListController.class);

    private ITaxonService taxonService;

    @Override
    @Autowired
    public void setService(IPolytomousKeyService service) {
        this.service = service;
    }

    @Autowired
    public void setService(ITaxonService taxonService) {
        this.taxonService = taxonService;
    }

    @RequestMapping(
            params = {"findByTaxonomicScope"},
            method = RequestMethod.GET)
    public Pager<PolytomousKey> doFindByTaxonomicScope(
            @RequestParam(value = "findByTaxonomicScope") UUID taxonUuid,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response)throws IOException {

        logger.info("doFindByTaxonomicScope: " + request.getRequestURI() + request.getQueryString());

        PagerParameters pagerParameters = new PagerParameters(pageSize, pageNumber);
        pagerParameters.normalizeAndValidate(response);


        TaxonBase taxon = taxonService.find(taxonUuid);
        Pager<PolytomousKey> pager = service.findByTaxonomicScope(taxon, pagerParameters.getPageSize(), pagerParameters.getPageIndex(), null, null);
        return pager;
    }

}

