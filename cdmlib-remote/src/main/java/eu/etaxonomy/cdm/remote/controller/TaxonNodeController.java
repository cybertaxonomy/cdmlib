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
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.compare.taxon.TaxonNodeSortMode;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @since Jun 13, 2016
 */
@Controller
@Api("taxonNode")
@RequestMapping(value = {"/taxonNode/{uuid}"})
//NOTE by AM: it seems that TaxonNodeController does not inherit from BaseController
//            because doGet method return type is a TaxonNodeDTO not TaxonNode as expected
//            by BaseController
public class TaxonNodeController extends AbstractController<TaxonNode, ITaxonNodeService> {

    private static final Logger logger = LogManager.getLogger();

    @Override
    @Autowired
    public void setService(ITaxonNodeService service) {
        this.service = service;
    }

    @RequestMapping(
            value = {"parent"},
            method = RequestMethod.GET)
    public TaxonNodeDto doGetParent(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            @SuppressWarnings("unused") HttpServletResponse response
            ) {

        logger.info("doGetParent() " + requestPathAndQuery(request));
        return service.parentDto(uuid);
    }

    @RequestMapping(
            method = RequestMethod.GET)
    public TaxonNodeDto doGet(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response
            ) throws IOException {

        logger.info("doGet() " + requestPathAndQuery(request));
        TaxonNodeDto dto = service.dto(uuid);
        if(dto == null){
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
        }
        return dto;
    }

    @RequestMapping(
            value = {"childNodes"},
            method = RequestMethod.GET)
    public Pager<TaxonNodeDto> doPageChildNodes(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value="sortMode", defaultValue = ClassificationController.DEFAULT_TAXONNODEDTO_SORT_MODE) TaxonNodeSortMode sortMode,
            @RequestParam(value="doSynonyms", defaultValue = "false") Boolean doSynonyms,
            HttpServletResponse response
            ) throws IOException {

        boolean includeUnpublished = NO_UNPUBLISHED;  //for now we do not allow any remote service to publish unpublished data

        PagerParameters pagerParameters = new PagerParameters(pageSize, pageIndex);
        pagerParameters.normalizeAndValidate(response);

        return service.pageChildNodesDTOs(uuid, false, includeUnpublished, doSynonyms, sortMode, pagerParameters.getPageSize(), pagerParameters.getPageIndex());
    }
}
