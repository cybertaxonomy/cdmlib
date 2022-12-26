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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.editor.CdmTypePropertyEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;

/**
 * @author a.kohlbecker
 * @since 22.07.2009
 */
public abstract class BaseListController <T extends CdmBase, SERVICE extends IService<T>> extends AbstractListController<T, SERVICE> {

    public static final Logger logger = LogManager.getLogger();

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
        binder.registerCustomEditor(Class.class, new CdmTypePropertyEditor());
    }

    /**
     * NOTE: The indices for pages are 0-based see {@link Pager}
     *
     * @param pageIndex
     *            the index of the page to be returned, the first page has the
     *            pageIndex = 0 - <i>optional parameter</i>. Defaults to 0 if
     *            set to <code>NULL</code>.
     * @param pageSize
     *            the maximum number of entities returned per page.
     *            The {@link #DEFAULT_PAGE_SIZE} will be used if pageSize is set to
     *            <code>null</code> - <i>optional parameter</i>
     * @param type
     *            Further restricts the type of entities to be returned.
     *            If null the base type <code>&lt;T&gt;</code> is being used. - <i>optional parameter</i>
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.GET)
    public Pager<T> doPage(
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "class", required = false) Class type,
            @RequestParam(name="orderBy", defaultValue="BY_TITLE_CACHE_ASC", required=true) OrderHintPreset orderBy,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException
            {

        logger.info("doPage() " + requestPathAndQuery(request));
        PagerParameters pagerParameters = new PagerParameters(pageSize, pageIndex).normalizeAndValidate(response);

        if(type != null) {
            orderBy = orderBy.checkSuitableFor(type);
            // TODO how can we check in case type == null?
        }
        return service.page(type, pagerParameters.getPageSize(), pagerParameters.getPageIndex(), orderBy.orderHints(), getInitializationStrategy());
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, params={"restriction"})
    public Pager<T> doPageByRestrictions(
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "class", required = false) Class type,
            @RequestParam(value = "restriction", required = true) List<Restriction<?>> restrictions,
            @RequestParam(value = "initStrategy", required = true) List<String> initStrategy,
            @RequestParam(name="orderBy", defaultValue="BY_TITLE_CACHE_ASC", required=true) OrderHintPreset orderBy,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException
            {

        // NOTE: for testing with httpi and jq:
        // http GET :8080/portal/taxon.json restriction=='{"propertyName":"name.titleCache","matchMode":"EXACT","values":["Eunotia krammeri Metzeltin & Lange-Bert."]}' initStrategy=name.titleCache | jq '.records[].name.titleCache'
        logger.info("doPageByRestrictions() " + requestPathAndQuery(request));
        PagerParameters pagerParameters = new PagerParameters(pageSize, pageIndex).normalizeAndValidate(response);

        if(type != null) {
            orderBy = orderBy.checkSuitableFor(type);
        }

        return pageByRestrictions(type, initStrategy, orderBy, pagerParameters, new ArrayList<>(restrictions));
    }

    /**
     * This method can be overwritten by subclasses, for example to apply additional filtering like for the publish flag.
     *
     * @param type
     * @param initStrategy
     * @param orderBy
     * @param pagerParameters
     * @param restrictions
     * @return
     */
    protected Pager<T> pageByRestrictions(Class<T> type, List<String> initStrategy, OrderHintPreset orderBy,
            PagerParameters pagerParameters, ArrayList<Restriction<?>> restrictions) {
        return service.page(type, restrictions, pagerParameters.getPageSize(), pagerParameters.getPageIndex(), orderBy.orderHints(), initStrategy);
    }

//    /**
//     * Parameter less method to be used as default when request without parameter are made. Otherwise
//     * the nameless methods {@link #doPage(Integer, Integer, Class)} and {@link #doList(Integer, Integer, Class)}
//     * are ambigous.
//     * @return
//     * @throws IOException
//     */
//    @RequestMapping(method = RequestMethod.GET)
//    public Pager<T> doPage(HttpServletRequest request, HttpServletResponse response) throws IOException{
//        return doPage(null, null, null, request, response);
//    }

    /**
     * @param start
     *            The offset index from the start of the list. The first entity
     *            has the index = 0 - <i>required parameter</i>
     * @param limit
     *            The maximum number of entities returned. - <i>optional parameter</i>
     *            If limit is set to a value < 1 all entities will be returned
     * @param type
     *            Further restricts the type of entities to be returned.
     *            If null the base type <code>&lt;T&gt;</code> is being used. - <i>optional parameter</i>
     * @return a List of entities
     */
    @RequestMapping(method = RequestMethod.GET, params = "start")
    public List<T> doList(
            @RequestParam(value = "start", required = true) Integer start,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "class", required = false) Class<T> type,
            HttpServletRequest request,
            @SuppressWarnings("unused") HttpServletResponse response) {

        if (request != null){
            logger.info("doList() " + requestPathAndQuery(request));
        }

        //if(start == null){ start = 0;}
        if(limit == null){
            limit = PagerParameters.DEFAULT_PAGESIZE;
        }else if(limit < 1){
            limit = null;
        }
        return service.list(type, limit, start, null, getInitializationStrategy());
    }

    // this is a copy from BaseController, should be unified
    protected TaxonNode getSubtreeOrError(UUID subtreeUuid, ITaxonNodeService taxonNodeService, HttpServletResponse response) throws IOException {
        TaxonNode subtree = null;
        if (subtreeUuid != null){
            subtree = taxonNodeService.find(subtreeUuid);
            if(subtree == null) {
                response.sendError(404 , "Taxon node for subtree not found: " + subtreeUuid );
                //will not happen
                return null;
            }
        }
        return subtree;
    }

    // this is a copy from BaseController, should be unified
    protected Classification getClassificationOrError(UUID classificationUuid,
            IClassificationService classificationService, HttpServletResponse response) throws IOException {
        Classification classification = null;
        if (classificationUuid != null){
            classification = classificationService.find(classificationUuid);
            if(classification == null) {
                response.sendError(404 , "Classification not found: " + classificationUuid );
                //will not happen
                return null;
            }
        }
        return classification;
    }

  /* TODO
   @RequestMapping(method = RequestMethod.POST)
  public T doPost(@ModelAttribute("object") T object, BindingResult result) {
        validator.validate(object, result);
        if (result.hasErrors()) {
                // set http status code depending upon what happened, possibly return
            // the put object and errors so that they can be rendered into a suitable error response
        } else {
          // should set the status to 201 created  and "Location" header to "/resource/uuid"
          service.save(object);
        }
  }
  */
}
