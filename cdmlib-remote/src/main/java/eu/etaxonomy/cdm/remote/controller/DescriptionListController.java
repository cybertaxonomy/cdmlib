/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.description.TransmissionEngineDistribution;
import eu.etaxonomy.cdm.api.service.description.TransmissionEngineDistribution.AggregationMode;
import eu.etaxonomy.cdm.api.service.dto.DistributionInfoDTO;
import eu.etaxonomy.cdm.api.service.dto.DistributionInfoDTO.InfoPart;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.utility.DistributionOrder;
import eu.etaxonomy.cdm.common.JvmLimitsException;
import eu.etaxonomy.cdm.common.monitor.IRestServiceProgressMonitor;
import eu.etaxonomy.cdm.ext.geo.CondensedDistributionRecipe;
import eu.etaxonomy.cdm.ext.geo.EditGeoServiceUtilities;
import eu.etaxonomy.cdm.ext.geo.IEditGeoService;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.controller.util.ProgressMonitorUtil;
import eu.etaxonomy.cdm.remote.editor.DefinedTermBaseList;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.TermBasePropertyEditor;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@Api("description")
@RequestMapping(value = {"/description"})
public class DescriptionListController extends AbstractIdentifiableListController<DescriptionBase, IDescriptionService> {


    @Autowired
    private ITermService termService;

    @Autowired
    private IVocabularyService vocabularyService ;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private IEditGeoService geoService;

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

    protected List<String> getDescriptionInfoInitStrategy(){
        return getInitializationStrategy();
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
                    try {
                        transmissionEngineDistribution.accumulate(mode, areaPager.getRecords(), _lowerRank, _upperRank,
                                null, progressMonitorController.getMonitor(transmissionEngineMonitorUuid));
                    } catch (JvmLimitsException e) {
                        IRestServiceProgressMonitor monitor = progressMonitorController.getMonitor(transmissionEngineMonitorUuid);
                        monitor.setIsFailed(true);
                        monitor.setFeedback(e);
                    }
                }
            };
            if(priority == null) {
                priority = AbstractController.DEFAULT_BATCH_THREAD_PRIORITY;
            }
            subThread.setPriority(priority);
            subThread.start();
        }

        // send redirect "see other"
        return progressUtil.respondWithMonitor(frontendBaseUrl, processLabel, transmissionEngineMonitorUuid, false, request, response);
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

    /**
     * @param taxonUuid
     * @param parts
     *            possible values: condensedStatus, tree, mapUriParams,
     *            elements,
     * @param subAreaPreference
     * @param statusOrderPreference
     * @param hideMarkedAreasList
     * @param omitLevels
     * @param request
     * @param response
     * @param distributionOrder
     *  Default is  LABEL
     * @param recipe
     *  The recipe for creating the condensed distribution status
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @RequestMapping(value = "distributionInfoFor/{uuid}", method = RequestMethod.GET)
    public ModelAndView doGetDistributionInfo(
            @PathVariable("uuid") UUID taxonUuid,
            @RequestParam("part") Set<InfoPart> partSet,
            @RequestParam(value = "subAreaPreference", required = false) boolean subAreaPreference,
            @RequestParam(value = "statusOrderPreference", required = false) boolean statusOrderPreference,
            @RequestParam(value = "hiddenAreaMarkerType", required = false) DefinedTermBaseList<MarkerType> hideMarkedAreasList,
            @RequestParam(value = "omitLevels", required = false) Set<NamedAreaLevel> omitLevels,
            @RequestParam(value = "statusColors", required = false) String statusColorsString,
            @RequestParam(value = "distributionOrder", required = false, defaultValue="LABEL") DistributionOrder distributionOrder,
            @RequestParam(value = "recipe", required = false, defaultValue="EuroPlusMed") CondensedDistributionRecipe recipe,
            HttpServletRequest request,
            HttpServletResponse response) throws JsonParseException, JsonMappingException, IOException {

            logger.info("doGetDistributionInfo() - " + requestPathAndQuery(request));

            ModelAndView mv = new ModelAndView();

            Set<MarkerType> hideMarkedAreas = null;
            if(hideMarkedAreasList != null){
                hideMarkedAreas = hideMarkedAreasList.asSet();
            }

            EnumSet<InfoPart> parts = EnumSet.copyOf(partSet);

            Map<PresenceAbsenceTerm, Color> presenceAbsenceTermColors = EditGeoServiceUtilities.buildStatusColorMap(statusColorsString, termService, vocabularyService);

            DistributionInfoDTO dto = geoService.composeDistributionInfoFor(parts, taxonUuid,
                    subAreaPreference, statusOrderPreference, hideMarkedAreas, omitLevels,
                    presenceAbsenceTermColors, LocaleContext.getLanguages(),
                    getDescriptionInfoInitStrategy(), recipe, distributionOrder);

            mv.addObject(dto);

            return mv;
    }

}
