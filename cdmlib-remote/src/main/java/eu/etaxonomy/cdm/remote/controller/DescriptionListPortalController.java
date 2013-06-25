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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.editor.TermBasePropertyEditor;

/**
 * IMPORTANT:
 *
 * This controller is mostly a 1:1 copy of the DescriptionListController
 * and this provides identical end points which only differ in the depth of the
 * object graphs returned.
 * An exception is the doAccumulateDistributions() method, which is not repeated
 * here
 *
 * @author a.kohlbecker
 * @date Jun 25, 2013
 *
 */
@Controller
@RequestMapping(value = {"/portal/description"})
public class DescriptionListPortalController extends IdentifiableListController<DescriptionBase, IDescriptionService> {

    protected static final List<String> DESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "elements.$",
            "elements.sources.citation.authorTeam.$",
            "elements.sources.nameUsedInSource.originalNameString",
            "elements.area.level",
            "elements.modifyingText",
            "elements.states.*",
            "elements.media",
    });


    public DescriptionListPortalController() {
        super();
        setInitializationStrategy(DESCRIPTION_INIT_STRATEGY);
    }


    @Autowired
    private ITermService termService;

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.BaseListController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Override
    @Autowired
    public void setService(IDescriptionService service) {
        this.service = service;
    }

    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(Feature.class, new TermBasePropertyEditor<Feature>(termService));
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
   @RequestMapping(value = "/portal/descriptionElement/find", method = RequestMethod.GET)
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

       logger.info("doFindDescriptionElements : " + request.getRequestURI() + "?" + request.getQueryString() );

       PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
       pagerParams.normalizeAndValidate(response);

       Pager<DescriptionElementBase> pager = service.searchElements(type, queryString, pageSize, pageNumber, null, getInitializationStrategy());

       return pager;
   }

   @RequestMapping(value = "/portal/descriptionElement", method = RequestMethod.GET)
   public Pager<DescriptionElementBase> doPageDescriptionElementsByFeature(
           @RequestParam(value = "feature", required = true) Feature feature,
           @RequestParam(value = "type", required = false) Class<? extends DescriptionElementBase> type,
           @RequestParam(value = "pageSize", required = false) Integer pageSize,
           @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
           HttpServletRequest request,
           HttpServletResponse response
           )
            throws IOException {

       logger.info("doPageDescriptionElementsByFeature : " + request.getRequestURI() + "?" + request.getQueryString() );

       PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
       pagerParams.normalizeAndValidate(response);

       Set<Feature> features = new HashSet<Feature>(1);
       features.add(feature);

       Pager<DescriptionElementBase> pager = service.getDescriptionElements(null, features, type, pagerParams.getPageSize(), pagerParams.getPageIndex(), getInitializationStrategy());

       return pager;
   }
}