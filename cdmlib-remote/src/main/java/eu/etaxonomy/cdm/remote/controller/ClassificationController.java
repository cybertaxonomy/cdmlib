/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.TaxonNodeDtoSortMode;
import eu.etaxonomy.cdm.api.service.dto.GroupedTaxonDTO;
import eu.etaxonomy.cdm.api.service.dto.TaxonInContextDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.compare.taxon.TaxonNodeSortMode;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.editor.RankPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import io.swagger.annotations.Api;

/**
 * @author a.kohlbecker
 * @since 03.06.2010
 */
@Controller
@Api("classification")
@RequestMapping(value = {"/classification/{uuid}"})
public class ClassificationController extends AbstractIdentifiableController<Classification,IClassificationService> {

    private static final Logger logger = LogManager.getLogger();

    private ITermService termService;
    private ITaxonNodeService taxonNodeService;

    public static final String DEFAULT_TAXONNODEDTO_SORT_MODE = "RankAndAlphabeticalOrder";

    @Override
    @Autowired
    public void setService(IClassificationService service) {
        this.service = service;
    }

    @Autowired
    public void setTermService(ITermService termService) {
        this.termService = termService;
    }

    @Autowired
    public void setTaxonNodeService(ITaxonNodeService taxonNodeService) {
        this.taxonNodeService = taxonNodeService;
    }
    protected ITaxonNodeService getTaxonNodeService() {
        return this.taxonNodeService;
    }


    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(Rank.class, new RankPropertyEditor());
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
    }

    private List<String> NODE_INIT_STRATEGY(){
        return Arrays.asList(new String[]{"taxon.name"});
    }

    @RequestMapping(
            value = {"childNodes"},
            method = RequestMethod.GET)
    public List<TaxonNodeDto> getChildNodes(
            @PathVariable("uuid") UUID classificationUuid,
            @RequestParam(value = "subtree", required = false) UUID subtreeUuid,
            @RequestParam(value = "sortMode", required = false, defaultValue = DEFAULT_TAXONNODEDTO_SORT_MODE) TaxonNodeDtoSortMode sortMode,
            HttpServletRequest request,
            HttpServletResponse response
            ) throws IOException {

        return getChildNodesAtRank(classificationUuid, null, subtreeUuid, sortMode, request, response);
    }

    @RequestMapping(
            value = {"childNodesAt/{rankUuid}"},
            method = RequestMethod.GET)
    public List<TaxonNodeDto> getChildNodesAtRank(
            @PathVariable("uuid") UUID classificationUuid,
            @PathVariable("rankUuid") UUID rankUuid,
            @RequestParam(value = "subtree", required = false) UUID subtreeUuid,
            @RequestParam(value = "sortMode", required = false, defaultValue = DEFAULT_TAXONNODEDTO_SORT_MODE) TaxonNodeDtoSortMode sortMode,
            HttpServletRequest request,
            HttpServletResponse response
            ) throws IOException {

        logger.info("getChildNodesAtRank() - " + request.getRequestURI());

        Classification classification = service.find(classificationUuid);

        if(classification == null) {
            HttpStatusMessage.UUID_NOT_FOUND.send(response, "Classification not found using " + classificationUuid);
            return null;
        }

        TaxonNode subtree = getSubtreeOrError(subtreeUuid, taxonNodeService, response);

        Rank rank = findRank(rankUuid);

        boolean includeUnpublished = NO_UNPUBLISHED;
//        long start = System.currentTimeMillis();
        List<TaxonNodeDto> rootNodes = service.listRankSpecificRootNodeDtos(classification, subtree, rank,
                includeUnpublished, null, null, sortMode, NODE_INIT_STRATEGY());
//        System.err.println("service.listRankSpecificRootNodes() " + (System.currentTimeMillis() - start));

        return rootNodes;
    }

   @RequestMapping(
           value = {"childNodesByTaxon/{taxonUuid}"},
           method = RequestMethod.GET)
   public Pager<TaxonNodeDto> doPageChildNodesByTaxon(
           @PathVariable("uuid") UUID classificationUuid,
           @PathVariable("taxonUuid") UUID taxonUuid,
           @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
           @RequestParam(value = "pageSize", required = false) Integer pageSize,
           @RequestParam(value = "sortMode", defaultValue = DEFAULT_TAXONNODEDTO_SORT_MODE) TaxonNodeSortMode sortMode,
           @RequestParam(value = "doSynonyms", defaultValue = "false") Boolean doSynonyms,
           HttpServletResponse response
           ) throws IOException {

       boolean includeUnpublished = NO_UNPUBLISHED;  //for now we do not allow any remote service to publish unpublished data

       PagerParameters pagerParameters = new PagerParameters(pageSize, pageIndex);
       pagerParameters.normalizeAndValidate(response);

//       service.startTransaction();
       boolean recursive = false;
       UUID taxonNodeUuid = service.getTaxonNodeUuidByTaxonUuid(classificationUuid, taxonUuid) ;
       if (taxonNodeUuid == null){
           HttpStatusMessage.UUID_NOT_FOUND.send(response);
           return null;
       }
       Pager<TaxonNodeDto> pager = taxonNodeService.pageChildNodesDTOs(taxonNodeUuid, recursive, includeUnpublished,
               doSynonyms, sortMode, pageSize, pageIndex);
//       service.commitTransaction()

       return pager;
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

    /**
     * @param classificationUuid
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(
            value = {"groupedTaxaByMarker"},
            method = RequestMethod.GET)
    public List<GroupedTaxonDTO> getGroupedTaxaByMarkedParents(
            @PathVariable("uuid") UUID classificationUuid,
            @RequestParam(value = "taxonUuids", required = true) UuidList taxonUuids,
            @RequestParam(value = "markerTypeUuid", required = false) UUID markerTypeUuid,
            @RequestParam(value = "flag", required = false) Boolean flag,

            HttpServletRequest request,
            HttpServletResponse response
            ) throws IOException {

        logger.info("getGroupedTaxaByHigherTaxon() - " + request.getRequestURI());

        Classification classification = service.find(classificationUuid);
        if(classification == null) {
            response.sendError(404 , "Classification not found using " + classificationUuid );
            return null;
        }

        MarkerType markerType = findMarkerType(markerTypeUuid);
//        long start = System.currentTimeMillis();
        List<GroupedTaxonDTO> result = service.groupTaxaByMarkedParents(taxonUuids, classificationUuid, markerType, flag);
//        System.err.println("service.listRankSpecificRootNodes() " + (System.currentTimeMillis() - start));

        return result;
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

    private MarkerType findMarkerType(UUID markerTypeUuid) {
        MarkerType markerType = null;
        if(markerTypeUuid != null){
            DefinedTermBase<?> definedTermBase =  termService.find(markerTypeUuid);
            if(definedTermBase instanceof MarkerType){
                markerType = (MarkerType) definedTermBase;
            } else {
               throw new IllegalArgumentException("DefinedTermBase is not a MarkerType");
            }
        }
        return markerType;
    }


   @RequestMapping(
           value = {"taxonInContext/{taxonUuid}"},
           method = RequestMethod.GET)
   public TaxonInContextDTO getTaxonInContext(
           @PathVariable("uuid") UUID classificationUuid,
           @PathVariable("taxonUuid") UUID taxonUuid,
           @RequestParam(value = "doChildren", defaultValue = "false") Boolean doChildren,
           @RequestParam(value = "doSynonyms", defaultValue = "false") Boolean doSynonyms,
           @RequestParam(value = "sortMode", defaultValue = DEFAULT_TAXONNODEDTO_SORT_MODE) TaxonNodeSortMode sortMode,
           @RequestParam(value = "ancestorMarker", required = false) List<UUID> ancestorMarkers,
           HttpServletResponse response
           ) throws IOException {

       try {
           boolean includeUnpublished = NO_UNPUBLISHED;  //for now we do not allow any remote service to publish unpublished data
           TaxonInContextDTO taxonInContextDTO = service.getTaxonInContext(classificationUuid, taxonUuid, doChildren, includeUnpublished,
                   doSynonyms, ancestorMarkers, sortMode);
           return taxonInContextDTO;
       } catch (EntityNotFoundException e) {
           HttpStatusMessage.UUID_NOT_FOUND.send(response);
           return null;
       }
   }

   @RequestMapping(value = { "classificationRootNode" }, method = RequestMethod.GET)
   public ModelAndView getClassificationRootNode(
           @PathVariable("uuid") UUID uuid,
           @SuppressWarnings("unused") HttpServletRequest request,
           @SuppressWarnings("unused") HttpServletResponse response) {

       ModelAndView mv = new ModelAndView();
       mv.addObject(service.getRootNode(uuid));
       return mv;
   }


}
