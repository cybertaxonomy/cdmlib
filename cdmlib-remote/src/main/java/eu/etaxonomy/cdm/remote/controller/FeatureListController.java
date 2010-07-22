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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * TODO write controller documentation
 * 
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@RequestMapping(value = {"/feature/", "/feature/{uuid}"}) //FIXME refactor type mappings
public class FeatureListController extends BaseController<DescriptionBase, IDescriptionService>
{
	@Autowired
	private ITermService termService;

	private static final List<String> FEATURE_INIT_STRATEGY = Arrays.asList(new String[]{"representations"});
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(IDescriptionService service) {
		this.service = service;
	}
	
	@RequestMapping()
	@Deprecated
	public DescriptionBase doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		return null;
	}
	
	
	/**
	 * TODO write controller method documentation
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET)
	public List<Feature> doGetFeatures(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		List<Feature> obj = (List)termService.list(Feature.class,null,null,null,FEATURE_INIT_STRATEGY);
		return obj;
	}

}
