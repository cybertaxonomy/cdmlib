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
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IIdentifiableEntityService;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.dto.common.StringResultDTO;
import eu.etaxonomy.cdm.remote.editor.MatchModePropertyEditor;

/**
 * @author a.mueller
 * @since 06 Oct 2016
 */
public abstract class AbstractIdentifiableController <T extends IdentifiableEntity, SERVICE extends IIdentifiableEntityService<T>>
        extends BaseController<T,SERVICE>  {

    private static final Logger logger = LogManager.getLogger();

    @InitBinder
    @Override
    public void initBinder(WebDataBinder binder) {
        super.initBinder(binder);
        binder.registerCustomEditor(MatchMode.class, new MatchModePropertyEditor());
    }

    /**
     * List identifiable entities by markers
     *
     * @see AbstractIdentifiableListController#doFindByIdentifier(Class, String, String, Integer, Integer, MatchMode, Boolean, HttpServletRequest, HttpServletResponse)
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value={"titleCache"})
    public StringResultDTO doGetTitleCache(
            @PathVariable("uuid") UUID uuid,
            @RequestParam(value = "refresh", defaultValue= "false", required = false) Boolean refresh,
            HttpServletRequest request,
            @SuppressWarnings("unused") HttpServletResponse response
            )
            throws IOException {

        if (logger.isDebugEnabled()){logger.info("doGetTitleCache  : " + request.getRequestURI() + "?" + request.getQueryString() );}

        String result = service.getTitleCache(uuid, refresh);

        return new StringResultDTO(result);
    }
}
