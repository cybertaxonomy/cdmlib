/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.TaxonNodeDtoSortMode;
import eu.etaxonomy.cdm.exception.FilterException;
import eu.etaxonomy.cdm.exception.UnpublishedException;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import eu.etaxonomy.cdm.remote.editor.RankPropertyEditor;
import io.swagger.annotations.Api;

/**
 * The ClassificationController class is a Spring MVC Controller.
 * @author a.kohlbecker
 * @since 20.03.2009
 */
@Controller
@Api("portal_classification")
@RequestMapping(value="/portal/classification")
public class ClassificationPortalListController extends AbstractIdentifiableListController<Classification,IClassificationService> {

    public static final Logger logger = Logger.getLogger(ClassificationPortalListController.class);

    private static final List<String> CLASSIFICATION_INIT_STRATEGY = Arrays.asList(new String[]{
            "reference.authorship"
    });

    private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
            "taxon.name.rank",
            "taxon.sec"
    });

    private ITaxonService taxonService;
    private ITaxonNodeService taxonNodeService;

    private ITermService termService;

    public ClassificationPortalListController() {
        setInitializationStrategy(CLASSIFICATION_INIT_STRATEGY);
    }

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
    public void setTaxonService(ITaxonService taxonService) {
        this.taxonService = taxonService;
    }

    @Autowired
    public void setTaxonNodeService(ITaxonNodeService taxonNodeService) {
        this.taxonNodeService = taxonNodeService;
    }


    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(Rank.class, new RankPropertyEditor());
    }

    /**
     * Lists all child-{@link TaxonNode}s of the specified {@link Taxon} in the {@link Classification}. The
     * a given {@link Rank} is ignored in this method but for consistency reasons it has been allowed to included it into the URI.
     * <p>
     * URI: <b>&#x002F;portal&#x002F;classification&#x002F;{treeUuid}&#x002F;childNodesOf&#x002F;{taxonUuid}</b>
     * <p>
     * <b>URI elements:</b>
     * <ul>
     * <li><b>{tree-uuid}</b> identifies the {@link Classification} by its UUID - <i>required</i>.
     * <li><b>{taxon-uuid}</b> identifies the {@link Taxon} by its UUID. - <i>required</i>.
     * </ul>
     *
     * @param response
     * @param request
     * @return a List of {@link TaxonNode} entities initialized by
     *         the {@link #NODE_INIT_STRATEGY}
     */
    @RequestMapping(
            value = {"{treeUuid}/childNodesOf/{taxonUuid}"},
            method = RequestMethod.GET)
    public List<TaxonNodeDto> getChildNodesOfTaxon(
            @PathVariable("treeUuid") UUID treeUuid,
            @PathVariable("taxonUuid") UUID taxonUuid,
            @RequestParam(value = "subtree", required = false) UUID subtreeUuid,
            @RequestParam(value = "sortMode", required = false, defaultValue = ClassificationController.DEFAULT_TAXONNODEDTO_SORT_MODE) TaxonNodeDtoSortMode sortMode,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("getChildNodesOfTaxon() " + request.getRequestURI());

        boolean includeUnpublished = NO_UNPUBLISHED;  //for now we do not allow any remote service to publish unpublished data

        List<TaxonNodeDto> children;
        try {
            children = service.listChildNodeDtosOfTaxon(taxonUuid, treeUuid, subtreeUuid,
                    includeUnpublished, null, null, sortMode, NODE_INIT_STRATEGY);
        } catch (FilterException e) {
            HttpStatusMessage.SUBTREE_FILTER_INVALID.send(response);
            return null;
        }
        return children;

    }

    @RequestMapping(
            value = {"{treeUuid}/siblingsOf/{taxonUuid}"},
            method = RequestMethod.GET)
    public List<TaxonNode> getSiblingsOfTaxon(
            @PathVariable("treeUuid") UUID classificationUuid,
            @PathVariable("taxonUuid") UUID taxonUuid,
            HttpServletRequest request,
            @SuppressWarnings("unused") HttpServletResponse response) {
        logger.info("getSiblingsOfTaxon() " + request.getRequestURI());

        boolean includeUnpublished = NO_UNPUBLISHED;
        //FIXME return pager
        List<TaxonNode> childs = service.listSiblingsOfTaxon(taxonUuid, classificationUuid, includeUnpublished, null, null, NODE_INIT_STRATEGY);
        return childs;

    }

    /**
     * Provides path of {@link TaxonNode}s from the base node to the node of the specified taxon.
     * <p>
     * URI:<b>&#x002F;portal&#x002F;classification&#x002F;{treeUuid}&#x002F;pathFrom&#x002F;{taxonUuid}&#x002F;toRank&#x002F;{rankUuid}</b>
     * <p>
     * <b>URI elements:</b>
     * <ul>
     * <li><b>{treeUuid}</b> identifies the {@link Classification} by its UUID - <i>required</i>.
     * <li><b>{taxonUuid}</b> identifies the {@link Rank}
     * <li><b>{rankUuid}</b> identifies the {@link Taxon} by its UUID. - <i>required</i>.
     * </ul>
     *
     * @param response
     * @param request
     * @return a List of {@link TaxonNode} entities initialized by
     *         the {@link #NODE_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(
            value = {"{treeUuid}/pathFrom/{taxonUuid}/toRank/{rankUuid}"},
            method = RequestMethod.GET)
    public List<TaxonNodeDto> getPathFromTaxonToRank(
            @PathVariable("treeUuid") UUID classificationUuid,
            @PathVariable("taxonUuid") UUID taxonUuid,
            @PathVariable("rankUuid") UUID rankUuid,
            @RequestParam(value = "subtree", required = false) UUID subtreeUuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        logger.info("getPathFromTaxonToRank() " + request.getRequestURI());

        boolean includeUnpublished = NO_UNPUBLISHED;

        Classification classification = service.find(classificationUuid);
        TaxonNode subtree = getSubtreeOrError(subtreeUuid, taxonNodeService, response);
        Rank rank = findRank(rankUuid);
        Taxon taxon = (Taxon) taxonService.load(taxonUuid);
        if(classification == null){
            HttpStatusMessage.UUID_INVALID.send(response, "Classification uuid does not exist.");
            return null;
        }
        try {
            List<TaxonNodeDto> result = service.loadTreeBranchDTOsToTaxon(taxon, classification, subtree, rank, includeUnpublished, NODE_INIT_STRATEGY);
            return result;
        } catch (UnpublishedException e) {
            HttpStatusMessage.ACCESS_DENIED.send(response);
            return null;
        }
    }

    /**
     * Provides path of {@link TaxonNode}s from the base node to the node of the specified taxon.
     * <p>
     * URI:<b>&#x002F;portal&#x002F;classification&#x002F;{treeUuid}&#x002F;pathFrom&#x002F;{taxonUuid}</b>
     * <p>
     * <b>URI elements:</b>
     * <ul>
     * <li><b>{treeUuid}</b> identifies the {@link Classification} by its UUID - <i>required</i>.
     * <li><b>{rankUuid}</b> identifies the {@link Taxon} by its UUID. - <i>required</i>.
     * </ul>
     *
     * @param response
     * @param request
     * @return a List of {@link TaxonNode} entities initialized by
     *         the {@link #NODE_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(
            value = {"{treeUuid}/pathFrom/{taxonUuid}"},
            method = RequestMethod.GET)
    public List<TaxonNodeDto> getPathFromTaxon(
            @PathVariable("treeUuid") UUID classificationUuid,
            @PathVariable("taxonUuid") UUID taxonUuid,
            @RequestParam(value = "subtree", required = false) UUID subtreeUuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        return getPathFromTaxonToRank(classificationUuid, taxonUuid, null, subtreeUuid, request, response);
    }


    private Rank findRank(UUID rankUuid) {
        Rank rank = null;
        if(rankUuid != null){
            DefinedTermBase<?> dt =  termService.find(rankUuid);
            if(dt instanceof Rank){
                rank = (Rank)dt;
            } else {
               throw new IllegalArgumentException("DefinedTermBase is not a Rank");
            }
        }
        return rank;
    }


}
