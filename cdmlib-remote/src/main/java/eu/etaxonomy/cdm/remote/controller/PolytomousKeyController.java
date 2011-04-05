// $Id$
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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.model.description.PolytomousKey;

/**
 * @author a.kohlbecker
 * @date 24.03.2011
 *
 */
@Controller
@RequestMapping(value = {"/polytomousKey/{uuid}"})
public class PolytomousKeyController extends BaseController<PolytomousKey, IPolytomousKeyService> {
	public static final Logger logger = Logger.getLogger(PolytomousKeyController.class);
	
	@Autowired
	public void setService(IPolytomousKeyService service) {
		this.service = service;
	}
	
	/**
	 * @param uuid
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * @Deprecated ONLY FOR TESTING PURPOSES
	 */
	@RequestMapping(value = {"loadWithNodes"})
	public ModelAndView doLoadWithNodes(
			@PathVariable("uuid") UUID uuid,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		logger.info("doLoadWithNodes() - " + request.getServletPath());

		ModelAndView mv = new ModelAndView();
		PolytomousKey key = service.loadWithNodes(uuid, null, null);
		mv.addObject(key); 
		return mv;
		
	}
	
}
	
