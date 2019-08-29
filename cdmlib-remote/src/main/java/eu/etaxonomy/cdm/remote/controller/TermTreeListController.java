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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.ITermTreeService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import io.swagger.annotations.Api;


@Controller
@Api("termTree")
@RequestMapping(value = {"/featureTree", "/termTree"})
public class TermTreeListController extends AbstractIdentifiableListController<TermTree, ITermTreeService> {

    @Override
    @Autowired
    public void setService(ITermTreeService service) {
        this.service = service;
    }

    @Override
    @SuppressWarnings("unchecked")
    @RequestMapping(method = RequestMethod.GET)
    public Pager<TermTree> doPage(
            @RequestParam(value = "pageNumber", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "class", required = false) Class type,
            @RequestParam(name="orderBy", defaultValue="BY_TITLE_CACHE_ASC", required=true) OrderHintPreset orderBy,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException
            {
        String requestPathAndQuery = requestPathAndQuery(request);
        if(requestPathAndQuery.contains("/featureTree")){
            logger.info(" Delegating usage of deprecated /featureTree service to /termTree");
            return doPageByTermType(TermType.Feature, pageIndex, pageSize, orderBy, request, response);
        }
        logger.info("doPage() " + requestPathAndQuery(request));
        PagerParameters pagerParameters = new PagerParameters(pageSize, pageIndex).normalizeAndValidate(response);

        if(type != null) {
            orderBy = orderBy.checkSuitableFor(type);
            // TODO how can we check in case type == null?
        }
        return service.page(type, pagerParameters.getPageSize(), pagerParameters.getPageIndex(), orderBy.orderHints(), getInitializationStrategy());
    }


    /**
     * @param termType
     *            Usually {@link TermType#Feature} or {@link TermType#Character} do make sense here.
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
    @RequestMapping(method = RequestMethod.GET, params={"termType"})
    public Pager<TermTree> doPageByTermType(
            @RequestParam(value = "termType", required = false) TermType termType,
            @RequestParam(value = "pageNumber", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(name="orderBy", defaultValue="BY_TITLE_CACHE_ASC", required=true) OrderHintPreset orderBy,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException
            {

        logger.info("doPage() " + requestPathAndQuery(request));
        PagerParameters pagerParameters = new PagerParameters(pageSize, pageIndex).normalizeAndValidate(response);

        return service.page(termType, pagerParameters.getPageSize(), pagerParameters.getPageIndex(), orderBy.orderHints(), getInitializationStrategy());
    }

    /**
     * Find IdentifiableEntity objects by name
     * <p>
     *
     * @param query
     *            the string to query for. Since the wildcard character '*'
     *            internally always is appended to the query string, a search
     *            always compares the query string with the beginning of a name.
     *            - <i>required parameter</i>
     * @param pageNumber
     *            the number of the page to be returned, the first page has the
     *            pageNumber = 1 - <i>optional parameter</i>
     * @param pageSize
     *            the maximum number of entities returned per page (can be -1
     *            to return all entities in a single page) - <i>optional parameter</i>
     * @param matchMode
     *           valid values are "EXACT", "BEGINNING", "ANYWHERE", "END" (case sensitive !!!)
     * @return a Pager on a list of {@link IdentifiableEntity}s
     * @throws IOException
     */
    @Override
    @RequestMapping(method = RequestMethod.GET, value={"findByTitle"})
    public Pager<TermTree> doFindByTitle(
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "matchMode", required = false) MatchMode matchMode,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException {

        String requestPathAndQuery = requestPathAndQuery(request);
        if(requestPathAndQuery.contains("/featureTree")){
            logger.info(" Delegating usage of deprecated /featureTree service to /termTree");
            return doFindByTitleByTermType(query, TermType.Feature, pageNumber, pageSize, matchMode, request, response);
        }
        logger.info("doFind : " + request.getRequestURI() + "?" + request.getQueryString() );

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        matchMode = matchMode != null ? matchMode : MatchMode.BEGINNING;

        return service.findByTitleWithRestrictions(null, query, matchMode, null, pagerParams.getPageSize(), pagerParams.getPageIndex(), null, initializationStrategy);

    }

    @RequestMapping(method = RequestMethod.GET, value={"findByTitle"}, params={"termType"})
    public Pager<TermTree> doFindByTitleByTermType(
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "termType", required = false) TermType termType,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "matchMode", required = false) MatchMode matchMode,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException {

        List<Restriction<?>> restrictions = service.buildTermTypeFilterRestrictions(termType);
        String requestPathAndQuery = requestPathAndQuery(request);

        logger.info("doFindByTitleByTermType() : " + requestPathAndQuery );

        PagerParameters pagerParams = new PagerParameters(pageSize, pageNumber);
        pagerParams.normalizeAndValidate(response);

        matchMode = matchMode != null ? matchMode : MatchMode.BEGINNING;

        return service.findByTitleWithRestrictions(null, query, matchMode, restrictions, pagerParams.getPageSize(), pagerParams.getPageIndex(), null, initializationStrategy);

    }

}
