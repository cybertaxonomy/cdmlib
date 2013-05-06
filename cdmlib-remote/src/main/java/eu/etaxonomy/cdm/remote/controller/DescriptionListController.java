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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.IFeatureTreeService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.description.TransmissionEngineDistribution;
import eu.etaxonomy.cdm.api.service.description.TransmissionEngineDistribution.AggregationMode;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.remote.controller.util.ProgressMonitorUtil;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@RequestMapping(value = {"/description"})
public class DescriptionListController extends IdentifiableListController<DescriptionBase, IDescriptionService> {

	@Autowired
	private IFeatureTreeService featureTreeService;

    @Autowired
    private ITermService termService;

    @Autowired
    public TransmissionEngineDistribution transmissionEngineDistribution;

    @Autowired
    public ProgressMonitorController progressMonitorController;

    /**
     * There should only be one longtime processes
     * therefore the according progress monitor uuid is stored in
     * this static field.
     */
    private static UUID transmissionEngineMonitorUuid = null;

	private static final List<String> FEATURETREE_INIT_STRATEGY = Arrays.asList(
			new String[]{
				"representations",
				"root.feature.representations",
				"root.children.feature.representations"
			});

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.BaseListController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Override
	@Autowired
	public void setService(IDescriptionService service) {
		this.service = service;
	}

	@RequestMapping(method = RequestMethod.GET, value="/featureTree")
	public List<FeatureTree> doGetFeatureTrees(HttpServletRequest request, HttpServletResponse response) throws IOException {

		List<FeatureTree> obj = featureTreeService.list(null,null,null,null,FEATURETREE_INIT_STRATEGY);
		return obj;
	}


    @RequestMapping(value = { "accumulateDistributions" }, method = RequestMethod.GET)
    public ModelAndView doAccumulateDistributions(
            @RequestParam(value= "mode", required = true) final AggregationMode mode,
            @RequestParam(value = "frontendBaseUrl", required = false) String frontendBaseUrl,
            @RequestParam(value = "priority", required = false) Integer priority,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doAccumulateDistributions()" + request.getServletPath());

//        transmissionEngineDistribution.updatePriorities();

        String processLabel = "accumulating distributions";

        ProgressMonitorUtil progressUtil = new ProgressMonitorUtil(progressMonitorController);

        final List<String> term_init_strategy = Arrays.asList(new String []{
                "representations"
        });

        if (!progressMonitorController.isMonitorRunning(transmissionEngineMonitorUuid)) {
            transmissionEngineMonitorUuid = progressUtil.registerNewMonitor();
            Thread subThread = new Thread() {
                @Override
                public void run() {
                    Pager<NamedArea> areaPager = termService.list(NamedAreaLevel.TDWG_LEVEL3(), (NamedAreaType) null,
                            null, null, (List<OrderHint>) null, term_init_strategy);
                    transmissionEngineDistribution.accumulate(mode, areaPager.getRecords(), Rank.SUBSPECIES(), Rank.GENUS(),
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
}