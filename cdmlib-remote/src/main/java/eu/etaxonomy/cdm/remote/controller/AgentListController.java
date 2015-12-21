// $Id$
/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.model.agent.AgentBase;

/**
 * The AgentListController class is a Spring MVC Controller.
 * <p>
 * The syntax of the mapped service URIs contains the the {datasource-name} path element.
 * The available {datasource-name}s are defined in a configuration file which
 * is loaded by the {@link UpdatableRoutingDataSource}. If the
 * UpdatableRoutingDataSource is not being used in the actual application
 * context any arbitrary {datasource-name} may be used.
 * <p>
 * Methods mapped at type level, inherited from super classes ({@link BaseController}):
 * <blockquote>
 * URI: <b>&#x002F;{datasource-name}&#x002F;agent&#x002F;</b>
 *
 * Depending on the URI parameters used this service returns
 * either a {@link Pager} on or a List of the {@link AgentBase} entities
 * identified by the <code>{agent-uuid}</code>.
 * The returned AgentBase instances are initialized by
 * the following strategy: {@link #DEFAULT_INIT_STRATEGY}
 * <p>
 * <b>URI Parameters to return a {@link Pager}:</b>
 * <ul>
 * <li><b>pageNumber</b>
 *            the number of the page to be returned, the first page has the
 *            pageNumber = 1 - <i>optional parameter</i>
 * <li><b>pageSize</b>
 *            the maximum number of entities returned per page (can be null
 *            to return all entities in a single page) - <i>optional
 *            parameter</i>
 * <li><b>type</b>
 *           Further restricts the type of entities to be returned.
 *           If null the base type <code>&lt;T&gt;</code> is being used. - <i>optional parameter</i>
 * </ul>
 * <p>
 * <b>URI Parameters to return a {@link List}:</b>
 * <ul>
 * <li><b>start</b>
 *            The offset index from the start of the list. The first entity
 *            has the index = 0 - <b><i>required parameter</i></b>
 *            The start parameter is used to distinguish between the List and Pager variants!
 * <li><b>limit</b>
 *           The maximum number of entities returned. - <i>optional parameter</i>
 * <li><b>type</b>
 *           Further restricts the type of entities to be returned.
 *           If null the base type <code>&lt;T&gt;</code> is being used. - <i>optional parameter</i>
 * </ul>
 * </blockquote>
 *
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@Api(value = "agent")
@RequestMapping(value = {"/agent"})
public class AgentListController extends IdentifiableListController<AgentBase, IAgentService> {

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.BaseListController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Override
    @Autowired
    public void setService(IAgentService service) {
        this.service = service;
    }
}