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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.wordnik.swagger.annotations.Api;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.editor.DefinedTermBaseList;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;

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
@Api("portal_description")
@RequestMapping(value = {"/portal/description"})
public class DescriptionListPortalController extends IdentifiableListController<DescriptionBase, IDescriptionService> {

   public static final Logger logger = Logger.getLogger(DescriptionListPortalController.class);

    @Autowired
    private ITaxonService taxonService;

    protected static final List<String> DESCRIPTION_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "elements.$",
            "elements.annotations",
            "elements.markers",
            "elements.stateData.$",
            "elements.sources.citation.authorship",
            "elements.sources.nameUsedInSource",
            "elements.multilanguageText",
            "elements.media",
            "elements.kindOfUnit"
    });

    protected static final List<String> DESCRIPTION_ELEMENT_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "annotations",
            "markers",
            "stateData.$",
            "statisticalValues.*",
            "sources.citation.authorship",
            "sources.nameUsedInSource",
            "multilanguageText",
            "media",
            "name.$",
            "name.rank.representations",
            "name.status.type.representations",
            "taxon2.name"
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
        binder.registerCustomEditor(DefinedTermBaseList.class, new TermBaseListPropertyEditor<Feature>(termService));
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
   @RequestMapping(value = "//portal/descriptionElement/find", method = RequestMethod.GET) // mapped as absolute path, see CdmAntPathMatcher
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
   @RequestMapping(value = "//portal/descriptionElement/byFeature", method = {RequestMethod.GET, RequestMethod.POST}) // mapped as absolute path, see CdmAntPathMatcher
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

       Pager pager = service.pageDescriptionElements(null, descriptionType, features.asSet(),
               type, pagerParams.getPageSize(), pagerParams.getPageIndex(), DESCRIPTION_ELEMENT_INIT_STRATEGY);

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
   @RequestMapping(value = "//portal/descriptionElement/byTaxon", method = {RequestMethod.GET, RequestMethod.POST}) // mapped as absolute path, see CdmAntPathMatcher
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
       //TODO it seems as if the InitializationStrategy is not appropriate here !!!
       //   see #3728 (DescriptionListPortalController.getDescriptionElementsForTaxon() seems to be using in-appropriate init strategy)
       if (logger.isDebugEnabled()){logger.debug("get pager ...");}
       Pager<T> pager = service.pageDescriptionElementsForTaxon(
               taxon,
               (features != null ? features.asSet() : null),
               type,
               pagerParams.getPageSize(),
               pagerParams.getPageIndex(),
               DESCRIPTION_ELEMENT_INIT_STRATEGY
              );
       if (logger.isDebugEnabled()){logger.debug("get pager - DONE");}
       return pager;
   }
}