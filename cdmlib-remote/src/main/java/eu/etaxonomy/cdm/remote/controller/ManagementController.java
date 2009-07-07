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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import eu.etaxonomy.cdm.database.DataSourceReloader;
import eu.etaxonomy.cdm.remote.service.Utils;


//@Controller
@RequestMapping(value = {"/cdmmanager/*/*"})
public class ManagementController
{
	Log log = LogFactory.getLog(ManagementController.class);

	@Autowired
	private DataSourceReloader datasoucrceLoader;
	
	private static final int DEFAULT_PAGE_SIZE = 25;

	/* 
	 * return page not found http error (404) for unknown or incorrect UUIDs
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@RequestMapping(value = { "/cdmmanager/datasources/list" }, method = RequestMethod.GET)
	protected ModelAndView doList(HttpServletRequest request, HttpServletResponse respone) throws Exception {
		
		return doReload(request, respone);

	}
	
	@RequestMapping(value = { "/cdmmanager/datasources/reload" }, method = RequestMethod.GET)
	public ModelAndView doReload(HttpServletRequest request, HttpServletResponse respone) throws Exception {
		
		Map<String, SimpleDriverDataSource> dataSources = datasoucrceLoader.reload();
		
		ModelAndView mv = new ModelAndView();

		mv.addObject("title", "CDM Community Server - Manager");
		String bodyHtml = "<div><h4>Available Data Sources</h4><dl>";
		bodyHtml += "<p><i>The following data sources have been loaded:</i></p><table><th>BasePath</th><th>DataSource URI</th>";
		for (String key : dataSources.keySet()) {
			SimpleDriverDataSource ds = dataSources.get(key);
			bodyHtml += "<tr><td>" + key + "</td><td>" + ds.getUrl() + "</td></tr>";
		}
		bodyHtml += "</table><form name=\"input\" action=\"\" method=\"get\"><input type=\"submit\" value=\"Update\"></td></form></div>";
		mv.addObject("body", bodyHtml);
		mv.setViewName("htmlView");

		return mv;
	}



}

