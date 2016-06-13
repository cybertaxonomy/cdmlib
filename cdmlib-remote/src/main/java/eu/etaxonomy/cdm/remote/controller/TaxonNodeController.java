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

import io.swagger.annotations.Api;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.NodeSortMode;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;

/**
 *
 * @author a.kohlbecker
 * @date Jun 13, 2016
 *
 */
@Controller
@Api("taxonNode")
@RequestMapping(value = {"/taxonNode/{uuid}"})
public class TaxonNodeController extends AbstractController<TaxonNode, ITaxonNodeService> {


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.BaseController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Override
    @Autowired
    public void setService(ITaxonNodeService service) {
        this.service = service;
    }


    /**
     *
     * @param uuid
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(
            value = {"parent"},
            method = RequestMethod.GET)
    public TaxonNodeDto doGetParent(
            @PathVariable("uuid") UUID uuid,
            HttpServletResponse response
            ) throws IOException {

        return service.parentDto(uuid);
    }

    /**
     *
     * @param uuid
     * @param pageIndex
     * @param pageSize
     * @param sortMode
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(
            value = {"childNodes"},
            method = RequestMethod.GET)
    public Pager<TaxonNodeDto> doPageChildNodes(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "pageNumber", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value="sortMode", defaultValue="AlphabeticalOrder") NodeSortMode sortMode,
            HttpServletResponse response
            ) throws IOException {

        PagerParameters pagerParameters = new PagerParameters(pageSize, pageIndex);
        pagerParameters.normalizeAndValidate(response);

        return service.pageChildNodesDTOs(uuid, false, sortMode, pagerParameters.getPageSize(), pagerParameters.getPageIndex());
    }
}
