// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.database.UpdatableRoutingDataSource;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.Annotation;

/**
 * The AgentController class is a Spring MVC Controller.
 * <p>
 * The syntax of the mapped service URIs contains the the {datasource-name} path element.
 * The available {datasource-name}s are defined in a configuration file which
 * is loaded by the {@link UpdatableRoutingDataSource}. If the
 * UpdatableRoutingDataSource is not being used in the actual application
 * context any arbitrary {datasource-name} may be used.
 * <p>
 * Methods mapped at type level, inherited from super classes ({@link BaseController}):
 * <blockquote>
 * URI: <b>&#x002F;{datasource-name}&#x002F;agent&#x002F;name&#x002F;{agent-uuid}</b>
 * 
 * Get the {@link AgentBase} instance identified by the <code>{agent-uuid}</code>.
 * The returned AgentBase is initialized by
 * the default initialization strategy: {@link #DEFAULT_INIT_STRATEGY}
 * </blockquote>
 * <blockquote>
 * URI: <b>&#x002F;{datasource-name}&#x002F;agent&#x002F;name&#x002F;{agent-uuid}&#x002F;annotation</b>
 * 
 * Returns a {@link Pager} on the {@link Annotation}s for the {@link AgentBase} instance identified by the
 * <code>{agent-uuid}</code>.
 * The returned AgentBase instances are initialized by
 * the following strategy: {@link #ANNOTATION_INIT_STRATEGY}
 * </blockquote>
 * 
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@RequestMapping(value = {"/agent/{uuid}"})
public class AgentController extends AnnotatableController<AgentBase, IAgentService>
{

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(IAgentService service) {
		this.service = service;
	}

}
