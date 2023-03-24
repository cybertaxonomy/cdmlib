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
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IIdentifiableEntityService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.dto.IdentifiedEntityDTO;
import eu.etaxonomy.cdm.api.service.dto.MarkedEntityDTO;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.editor.MatchModePropertyEditor;

/**
 * @author l.morris
 * @since 27 Mar 2012
 */
public abstract class AbstractIdentifiableListController <T extends IdentifiableEntity, SERVICE extends IIdentifiableEntityService<T>>
            extends BaseListController<T,SERVICE>  {

    private static final Logger logger = LogManager.getLogger();

    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(MatchMode.class, new MatchModePropertyEditor());
    }

	@Autowired
	private ITermService termService;

    /**
     * Find IdentifiableEntity objects by name
     * <p>
     *
     * @param query
     *            the string to query for. Since the wild-card character '*'
     *            internally always is appended to the query string, a search
     *            always compares the query string with the beginning of a name.
     *            - <i>required parameter</i>
     * @param pageIndex
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
    @RequestMapping(method = RequestMethod.GET, value={"findByTitle"})
    public Pager<T> doFindByTitle(
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "matchMode", required = false) MatchMode matchMode,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException {

        logger.info("doFind() : " + requestPathAndQuery(request) );

        PagerParameters pagerParams = new PagerParameters(pageSize, pageIndex);
        pagerParams.normalizeAndValidate(response);

        matchMode = matchMode != null ? matchMode : MatchMode.BEGINNING;

        return service.findByTitleWithRestrictions(null, query, matchMode, null, pagerParams.getPageSize(), pagerParams.getPageIndex(), null, initializationStrategy);
    }

    /**
     * List IdentifiableEntity objects by identifiers
     */
    @RequestMapping(method = RequestMethod.GET, value={"findByIdentifier"})
    public  Pager<IdentifiedEntityDTO<T>> doFindByIdentifier(
    		@RequestParam(value = "class", required = false) Class<T> type,
    		@RequestParam(value = "identifierType", required = false) String identifierTypeStr,
            @RequestParam(value = "identifier", required = false) String identifier,
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "matchMode", required = false) MatchMode matchMode,
            @RequestParam(value = "includeEntity", required = false) Boolean includeEntity,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException {

        IdentifierType identifierType = null;
    	if(StringUtils.isNotBlank(identifierTypeStr)){
    		identifierTypeStr = StringUtils.trim(identifierTypeStr);
    		UUID identifierTypeUUID = UUID.fromString(identifierTypeStr);
    		identifierType = CdmBase.deproxy(termService.find(identifierTypeUUID), IdentifierType.class);
    	}

        logger.info("doFindByIdentifier() : " + requestPathAndQuery(request) );

        PagerParameters pagerParams = new PagerParameters(pageSize, pageIndex).normalizeAndValidate(response);

        matchMode = matchMode != null ? matchMode : MatchMode.EXACT;
        boolean includeCdmEntity = includeEntity == null ||  includeEntity == true ? true : false;
        return service.findByIdentifier(type, identifier, identifierType , matchMode, includeCdmEntity, pagerParams.getPageSize(), pagerParams.getPageIndex(), initializationStrategy);
    }

    /**
     * List identifiable entities by markers
     *
     * @see AbstractIdentifiableListController#doFindByIdentifier(Class, String, String, Integer, Integer, MatchMode, Boolean, HttpServletRequest, HttpServletResponse)
     */
    @RequestMapping(method = RequestMethod.GET, value={"findByMarker"})
    public Pager<MarkedEntityDTO<T>> doFindByMarker(
            @RequestParam(value = "class", required = false) Class<T> type,
            @RequestParam(value = "markerType", required = true) UUID markerTypeUuid,
            @RequestParam(value = "value", required = false) Boolean value,
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "includeEntity", required = false, defaultValue="false") Boolean includeEntity,
            HttpServletRequest request,
            HttpServletResponse response
            )
            throws IOException {

        MarkerType markerType = null;
        if(markerTypeUuid != null){
            DefinedTermBase<?> term = CdmBase.deproxy(termService.find(markerTypeUuid), MarkerType.class);
            if (term != null && term.isInstanceOf(MarkerType.class)){
                markerType = CdmBase.deproxy(term, MarkerType.class);
            }
        }

        logger.info("doFindByMarker() " + requestPathAndQuery(request));

        PagerParameters pagerParams = new PagerParameters(pageSize, pageIndex).normalizeAndValidate(response);

        Pager<MarkedEntityDTO<T>> result = service.findByMarker(type, markerType, value, includeEntity, pagerParams.getPageSize(), pagerParams.getPageIndex(), initializationStrategy);
        return result;
    }

    /**
     * List identifiable entities by markers
     *
     * @see AbstractIdentifiableListController#doFindByIdentifier(Class, String, String, Integer, Integer, MatchMode, Boolean, HttpServletRequest, HttpServletResponse)
     */
    @RequestMapping(method = RequestMethod.GET, value={"uuidAndTitleCache"})
    public List<UuidAndTitleCache<T>> doGetUuidAndTitleCache(
            @RequestParam(value = "class", required = false) Class<T> type,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "pattern", required = false) String pattern,
            HttpServletRequest request,
            @SuppressWarnings("unused") HttpServletResponse response
            ) {

        logger.info("doGetUuidAndTitleCache() " + requestPathAndQuery(request));

        return service.getUuidAndTitleCache(type, limit, pattern);
    }

}
