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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.editor.CdmTypePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.DefinedTermBaseList;
import eu.etaxonomy.cdm.remote.editor.TermBaseListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.TermBasePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDListPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UuidList;
import io.swagger.annotations.Api;

/**
 * TODO write controller documentation
 *
 * @author a.kohlbecker
 * @since 24.03.2009
 */
@Controller
@Api("descriptionElement")
@RequestMapping(value = {"/descriptionElement"})
public class DescriptionElementListController {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private ITermService termService;

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    public ProgressMonitorController progressMonitorController;

    protected IDescriptionService service;

    protected static final List<String> DESCRIPTION_ELEMENT_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "multilanguageText"
    });

    protected List<String> getInitializationStrategy() {
        return DESCRIPTION_ELEMENT_INIT_STRATEGY;
    }

    private String requestPathAndQuery(HttpServletRequest request) {
        return AbstractController.requestPathAndQuery(request);
    }

    @Autowired
    public void setService(IDescriptionService service) {
        this.service = service;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
        binder.registerCustomEditor(UuidList.class, new UUIDListPropertyEditor());
        binder.registerCustomEditor(DefinedTermBaseList.class, new TermBaseListPropertyEditor<Feature>(termService));
        binder.registerCustomEditor(NamedAreaLevel.class, new TermBasePropertyEditor<NamedAreaLevel>(termService));
        binder.registerCustomEditor(Rank.class, new TermBasePropertyEditor<Rank>(termService));
        binder.registerCustomEditor(Class.class, new CdmTypePropertyEditor());
    }

    /**
     * Requires the query parameter "descriptionType" to be present
     */
    @RequestMapping(value = "byFeature", method = RequestMethod.GET) // mapped as absolute path, see CdmAntPathMatcher
    public Pager<? extends DescriptionElementBase> doPageDescriptionElementsByFeature(
            @RequestParam(value = "features", required = false) DefinedTermBaseList<Feature> features,
            @RequestParam(value = "descriptionType", required = true) Class<? extends DescriptionBase> descriptionType,
            @RequestParam(value = "type", required = false) Class<? extends DescriptionElementBase> type,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doPageDescriptionElementsByFeature : " + requestPathAndQuery(request));

        PagerParameters pagerParams = new PagerParameters(pageSize, pageIndex);
        pagerParams.normalizeAndValidate(response);

        if(features == null){
            features = new DefinedTermBaseList<Feature>();
        }

        Pager<? extends DescriptionElementBase> pager = service.pageDescriptionElements(null, descriptionType, features.asSet(),
                type, pagerParams.getPageSize(), pagerParams.getPageIndex(), getInitializationStrategy());

        return pager;
    }

    /**
     * Requires the query parameter "taxon"  to be present
     */
    @RequestMapping(value = "byTaxon", method = {RequestMethod.GET, RequestMethod.POST}) // mapped as absolute path, see CdmAntPathMatcher
    public <T extends DescriptionElementBase> Pager<T> doGetDescriptionElementsForTaxon(
            @RequestParam(value = "taxon", required = true) UUID taxon_uuid,
            @RequestParam(value = "features", required = false) DefinedTermBaseList<Feature> features,
            @RequestParam(value = "type", required = false) Class<T> type,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("getDescriptionElementsForTaxon : " + requestPathAndQuery(request));

        PagerParameters pagerParams = new PagerParameters(pageSize, pageIndex);
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
                pageIndex, getInitializationStrategy());
        return pager;
    }

   @RequestMapping(value = "find", method = RequestMethod.GET) // mapped as absolute path, see CdmAntPathMatcher
   public Pager<? extends DescriptionElementBase> doFindDescriptionElements(
           @RequestParam(value = "query", required = true) String queryString,
           @RequestParam(value = "type", required = false) Class<? extends DescriptionElementBase> type,
           @RequestParam(value = "pageSize", required = false) Integer pageSize,
           @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
           @RequestParam(value = "matchMode", required = false) MatchMode matchMode,
           HttpServletRequest request,
           HttpServletResponse response
           )
            throws IOException {

       logger.info("doFindDescriptionElements : " + request.getRequestURI() + "?" + request.getQueryString() );

       PagerParameters pagerParams = new PagerParameters(pageSize, pageIndex);
       pagerParams.normalizeAndValidate(response);

       Pager<? extends DescriptionElementBase> pager = service.searchElements(type, queryString, pageSize, pageIndex, null, getInitializationStrategy());

       return pager;
   }
}