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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.dto.GroupedTaxonDTO;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.remote.editor.RankPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;

/**
 * @author a.kohlbecker
 * @date 03.06.2010
 *
 */
@Controller
@Api("classification")
@RequestMapping(value = {"/classification/{uuid}"})
public class ClassificationController extends BaseController<Classification,IClassificationService> {


    private ITermService termService;

    @Override
    @Autowired
    public void setService(IClassificationService service) {
        this.service = service;
    }

    @Autowired
    public void setTermService(ITermService termService) {
        this.termService = termService;
    }


    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(Rank.class, new RankPropertyEditor());
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
    }

    private List<String> NODE_INIT_STRATEGY(){
        return Arrays.asList(new String[]{
            "taxon.name"
    });}

    /**
     * @param classificationUuid
     * @param response
     * @return
     * @throws IOException
     *
     */
    @RequestMapping(
            value = {"childNodes"},
            method = RequestMethod.GET)
    public List<TaxonNode> getChildNodes(
            @PathVariable("uuid") UUID classificationUuid,
            HttpServletRequest request,
            HttpServletResponse response
            ) throws IOException {

        return getChildNodesAtRank(classificationUuid, null, request, response);
    }

    @RequestMapping(
            value = {"childNodesAt/{rankUuid}"},
            method = RequestMethod.GET)
    public List<TaxonNode> getChildNodesAtRank(
            @PathVariable("uuid") UUID classificationUuid,
            @PathVariable("rankUuid") UUID rankUuid,
            HttpServletRequest request,
            HttpServletResponse response
            ) throws IOException {

        logger.info("getChildNodesAtRank() - " + request.getRequestURI());

        Classification classification = service.find(classificationUuid);

        if(classification == null) {
            response.sendError(404 , "Classification not found using " + classificationUuid );
            return null;
        }
        Rank rank = findRank(rankUuid);

//        long start = System.currentTimeMillis();
        List<TaxonNode> rootNodes = service.listRankSpecificRootNodes(classification, rank, null, null, NODE_INIT_STRATEGY());
//        System.err.println("service.listRankSpecificRootNodes() " + (System.currentTimeMillis() - start));

        return rootNodes;
    }

    private Rank findRank(UUID rankUuid) {
        Rank rank = null;
        if(rankUuid != null){
            DefinedTermBase<?> definedTermBase =  termService.find(rankUuid);
            if(definedTermBase instanceof Rank){
                rank = (Rank) definedTermBase;
            } else {
               throw new IllegalArgumentException("DefinedTermBase is not a Rank");
            }
        }
        return rank;
    }

    /**
     * @param classificationUuid
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(
            value = {"groupedTaxa"},
            method = RequestMethod.GET)
    public List<GroupedTaxonDTO> getGroupedTaxaByHigherTaxon(
            @PathVariable("uuid") UUID classificationUuid,
            @RequestParam(value = "taxonUuids", required = true) UuidList taxonUuids,
            @RequestParam(value = "minRankUuid", required = false) UUID minRankUuid,
            @RequestParam(value = "maxRankUuid", required = false) UUID maxRankUuid,
            HttpServletRequest request,
            HttpServletResponse response
            ) throws IOException {

        logger.info("getGroupedTaxaByHigherTaxon() - " + request.getRequestURI());

        Classification classification = service.find(classificationUuid);
        if(classification == null) {
            response.sendError(404 , "Classification not found using " + classificationUuid );
            return null;
        }


        Rank minRank = findRank(minRankUuid);
        Rank maxRank = findRank(maxRankUuid);

//        long start = System.currentTimeMillis();
        List<GroupedTaxonDTO> result = service.groupTaxaByHigherTaxon(taxonUuids, classificationUuid, minRank, maxRank);
//        System.err.println("service.listRankSpecificRootNodes() " + (System.currentTimeMillis() - start));

        return result;
    }
}
