/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@Api("name")
@RequestMapping(value = {"/name"})
public class NameListController extends AbstractIdentifiableListController<TaxonNameBase, INameService> {

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.BaseListController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Override
    @Autowired
    public void setService(INameService service) {
        this.service = service;
    }

   @RequestMapping(method = RequestMethod.GET, value={"findTitleCache"})
    public Pager<TaxonNameBase> doFindTitleCache(
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "matchMode", required = false) MatchMode matchMode,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException {

        logger.info("doFind : " + request.getRequestURI() + "?" + request.getQueryString() );

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        return service.findTitleCache(null, query, pagerParams.getPageSize(), pagerParams.getPageIndex(), null, matchMode);

    }

    @RequestMapping(value = "findByName", method = RequestMethod.GET)
    public Pager<TaxonNameBase> doFindByName(
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "matchMode", required = false) MatchMode matchMode, HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        PagerParameters pagerParameters = new PagerParameters(pageSize, pageNumber);
        pagerParameters.normalizeAndValidate(response);

        return service.findByTitle(TaxonNameBase.class, query, matchMode, null, pageSize, pageNumber, null, getInitializationStrategy());
    }

}
