// $Id$
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.wordnik.swagger.annotations.Api;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.description.TransmissionEngineDistribution;
import eu.etaxonomy.cdm.api.service.description.TransmissionEngineDistribution.AggregationMode;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.controller.util.ProgressMonitorUtil;
import eu.etaxonomy.cdm.remote.editor.DefinedTermBaseList;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.TermBasePropertyEditor;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@Api("description")
@RequestMapping(value = {"/description"})
public class DescriptionListController extends IdentifiableListController<DescriptionBase, IDescriptionService> {


    @Autowired
    private ITermService termService;

    @Autowired
    private ITaxonService taxonService;


    @Autowired
    public TransmissionEngineDistribution transmissionEngineDistribution;

    @Autowired
    public ProgressMonitorController progressMonitorController;

    protected static final List<String> DESCRIPTION_ELEMENT_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "multilanguageText",
    });

    /**
     * There should only be one longtime processes
     * therefore the according progress monitor uuid is stored in
     * this static field.
     */
    private static UUID transmissionEngineMonitorUuid = null;


    @Override
    @Autowired
    public void setService(IDescriptionService service) {
        this.service = service;
    }

    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(DefinedTermBaseList.class, new TermBaseListPropertyEditor<Feature>(termService));
        binder.registerCustomEditor(NamedAreaLevel.class, new TermBasePropertyEditor<NamedAreaLevel>(termService));
        binder.registerCustomEditor(Rank.class, new TermBasePropertyEditor<Rank>(termService));
    }


    /**
     * Runs the {@link TransmissionEngineDistribution} in a separate Thread and
     * responds with a redirect to a progress monitor REST service end point.
     * <p>
     *
     * @param mode
     *      one of <code>byAreas</code>, <code>byRanks</code>,
     *      <code>byAreasAndRanks</code>
     * @param frontendBaseUrl
     *      the cdm server instance base URL, this is needed for the a
     *      proper redirect URL when the service is running behind a
     *      reverse HTTP proxy
     * @param priority
     *      the priority for the Thread to spawn, see
     *      {@link Thread#setPriority(int)}, defaults to 3
     * @param targetAreaLevel
     *      The level of target areas to project the distributions to.
     * @param lowerRank
     * @param upperRank
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value = { "accumulateDistributions" }, method = RequestMethod.GET)
    public ModelAndView doAccumulateDistributions(
            @RequestParam(value= "mode", required = true) final AggregationMode mode,
            @RequestParam(value = "frontendBaseUrl", required = false) String frontendBaseUrl,
            @RequestParam(value = "priority", required = false) Integer priority,
            @RequestParam(value = "targetAreaLevel", required = true) final NamedAreaLevel targetAreaLevel,
            @RequestParam(value = "lowerRank", required = false) Rank lowerRank,
            @RequestParam(value = "upperRank", required = false) Rank upperRank,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doAccumulateDistributions()" + request.getRequestURI());

//        transmissionEngineDistribution.updatePriorities();

        String processLabel = "accumulating distributions";

        final Rank _lowerRank = lowerRank != null ? lowerRank : Rank.UNKNOWN_RANK(); // this is the lowest rank
        final Rank _upperRank = upperRank != null ? upperRank : Rank.GENUS();

        ProgressMonitorUtil progressUtil = new ProgressMonitorUtil(progressMonitorController);

        final List<String> term_init_strategy = Arrays.asList(new String []{
                "representations"
        });

        if (!progressMonitorController.isMonitorRunning(transmissionEngineMonitorUuid)) {
            transmissionEngineMonitorUuid = progressUtil.registerNewMonitor();
            Thread subThread = new Thread() {
                @Override
                public void run() {
                    Pager<NamedArea> areaPager = termService.list(targetAreaLevel, (NamedAreaType) null,
                            null, null, (List<OrderHint>) null, term_init_strategy);
                    transmissionEngineDistribution.accumulate(mode, areaPager.getRecords(), _lowerRank, _upperRank,
                            null, progressMonitorController.getMonitor(transmissionEngineMonitorUuid));
                }
            };
            if(priority == null) {
                priority = AbstractController.DEFAULT_BATCH_THREAD_PRIORITY;
            }
            subThread.setPriority(priority);
            subThread.start();
        }

        // send redirect "see other"
        return progressUtil.respondWithMonitor(frontendBaseUrl, request, response, processLabel, transmissionEngineMonitorUuid);
    }

    /**
    *
    * @param queryString
    * @param type
    * @param pageSize
    * @param pageNumber
    * @param matchMode
    * @param request
    * @param response
    * @return
    * @throws IOException
    */
   @RequestMapping(value = "//descriptionElement/find", method = RequestMethod.GET) // mapped as absolute path, see CdmAntPathMatcher
   public Pager<DescriptionElementBase> doFindDescriptionElements(
           @RequestParam(value = "query", required = true) String queryString,
           @RequestParam(value = "type", required = false) Class<? extends DescriptionElementBase> type,
           @RequestParam(value = "pageSize", required = false) Integer pageSize,
           @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
           @RequestParam(value = "matchMode", required = false) MatchMode matchMode,
           HttpServletRequest request,
           HttpServletResponse response
           )
            throws IOException {

       logger.info("doFindDescriptionElements : "  + requestPathAndQuery(request) );

       PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
       pagerParams.normalizeAndValidate(response);

       Pager<DescriptionElementBase> pager = service.searchElements(type, queryString, pageSize, pageNumber, null, getInitializationStrategy());

       return pager;
   }

    /**
     * Requires the query parameter "descriptionType" to be present
     *
     * @param features
     * @param descriptionType
     * @param type
     * @param pageSize
     * @param pageNumber
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "//descriptionElement/byFeature", method = RequestMethod.GET) // mapped as absolute path, see CdmAntPathMatcher
    public Pager<DescriptionElementBase> doPageDescriptionElementsByFeature(
            @RequestParam(value = "features", required = false) DefinedTermBaseList<Feature> features,
            @RequestParam(value = "descriptionType", required = true) Class<? extends DescriptionBase> descriptionType,
            @RequestParam(value = "type", required = false) Class<? extends DescriptionElementBase> type,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doPageDescriptionElementsByFeature : " + requestPathAndQuery(request));

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        if(features == null){
            features = new DefinedTermBaseList<Feature>();
        }

        Pager<DescriptionElementBase> pager = service.pageDescriptionElements(null, descriptionType, features.asSet(),
                type, pagerParams.getPageSize(), pagerParams.getPageIndex(), getInitializationStrategy());

        return pager;
    }

    /**
     * Requires the query parameter "taxon"  to be present
     *
     * @param taxon_uuid
     * @param features
     * @param type
     * @param pageSize
     * @param pageNumber
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "//descriptionElement/byTaxon", method = {RequestMethod.GET, RequestMethod.POST}) // mapped as absolute path, see CdmAntPathMatcher
    public <T extends DescriptionElementBase> Pager<T> getDescriptionElementsForTaxon(
            @RequestParam(value = "taxon", required = true) UUID taxon_uuid,
            @RequestParam(value = "features", required = false) DefinedTermBaseList<Feature> features,
            @RequestParam(value = "type", required = false) Class<T> type,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("getDescriptionElementsForTaxon : " + requestPathAndQuery(request));

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        Taxon taxon = null;
        if( taxon_uuid!= null){
            try {
                taxon = (Taxon) taxonService.load(taxon_uuid);
            } catch (Exception e) {
                HttpStatusMessage.UUID_NOT_FOUND.send(response);
            }
        }

        Pager<T> pager = service.pageDescriptionElementsForTaxon(taxon, features != null ? features.asSet() : null, type, pageSize,
                pageNumber, DESCRIPTION_ELEMENT_INIT_STRATEGY);
        return pager;
    }

    @RequestMapping(value = "namedAreasInUse", method = RequestMethod.GET)
    public Pager<TermDto> doPageNamedAreasInUse(
            @RequestParam(value = "includeAllParents", required = false) boolean includeAllParents,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doPageNamedAreasInUse : " + requestPathAndQuery(request));

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        Pager<TermDto> pager = service.pageNamedAreasInUse(includeAllParents, pagerParams.getPageSize(), pagerParams.getPageIndex());

        localizeTerms(pager);

        return pager;
    }
}