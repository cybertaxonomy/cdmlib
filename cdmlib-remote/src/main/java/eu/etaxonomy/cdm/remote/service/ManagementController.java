/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.service;

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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import eu.etaxonomy.cdm.database.DataSourceReloader;


@Controller("managementController")
public class ManagementController extends AbstractController
{
	Log log = LogFactory.getLog(ManagementController.class);

	@Autowired
	private DataSourceReloader datasourceLoader;
	
	private static final int DEFAULT_PAGE_SIZE = 25;

	/* 
	 * return page not found http error (404) for unknown or incorrect UUIDs
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{

			ModelAndView mv = new ModelAndView();
			String resource = getNonNullPara("resource",req);
			String op = getNonNullPara("operation",req);
			String q = getNonNullPara("q",req);
			
			log.info(String.format("Request received for manager: resource=%s op=%s ", resource, op));
			
			if(resource.equalsIgnoreCase("dataSources")){
				// get Object by UUID
				if(op.equalsIgnoreCase("reload")){
					mv.addObject("title", "CDM Community Server - Manager");
					Map<String,SimpleDriverDataSource> dataSources = datasourceLoader.reload();
					String bodyHtml = "<div><h4>Available Data Sources</h4><dl>";
					bodyHtml += "<p><i>The following data sources have been loaded:</i></p><table><th>BasePath</th><th>DataSource URI</th>";
					for(String key : dataSources.keySet()) {
						SimpleDriverDataSource ds = dataSources.get(key);
						bodyHtml += "<tr><td>"+key+"</td><td>"+ds.getUrl()+"</td></tr>";
					}
					bodyHtml += "</table><form name=\"input\" action=\"\" method=\"get\"><input type=\"submit\" value=\"Update\"></td></form></div>";
					mv.addObject("body", bodyHtml);
				}
			}
				else{
				// nothing matches
				mv.addObject("title", "CDM Community Server - Manager");
				mv.addObject("body", "<h4>Error</h4><p>Controller does not know this operation.</p>");
			}
			// set xml or json view
			mv.setViewName("htmlView");
			
		
			return mv;
		
	}
	

	/**
	 * return the value for the given parameter name as a string. 
	 * in case the parameters doesnt exist return an empty string "", not null.
	 * @param parameterName
	 * @param req
	 * @return
	 */
	private String getStringPara(String parameterName, HttpServletRequest req){
		// first try URL parameters set by org.springframework.web.servlet.handler.SimpleUrlHandlerMapping controller mapping
		Object map = req.getAttribute("ParameterizedUrlHandlerMapping.path-parameters");
		String result = null;
		if (map!=null){
			// first look into url parameters
			Map<String,String> urlParas = (Map<String, String>) map;
			result = urlParas.get(parameterName);
		}
		if (result == null){
			// alternatively try querystring parameters
			result = req.getParameter(parameterName);
		}
		return result;
	}
	private List<String> getListPara(String parameterName, HttpServletRequest req){
		ArrayList<String> list = new ArrayList<String>();
		String[] map = req.getParameterValues(parameterName);
		if(map != null){
			for(String param : map){
				list.add(param);
			}	
		}
		return list;
	}
	private String getNonNullPara(String parameterName, HttpServletRequest req){
		String val = getStringPara(parameterName, req);
		if (val==null){
			return "";
		}
		return val;
	}
	
	private Integer getIntPara(String parameterName, HttpServletRequest req){
		return getIntPara(parameterName, null, req);
	}
	
	private Integer getIntPara(String parameterName, Integer defaultValue, HttpServletRequest req){
		// first try URL parameters set by org.springframework.web.servlet.handler.SimpleUrlHandlerMapping controller mapping
		Integer result;
		String tmp = getStringPara(parameterName, req);
		try{
			result = Integer.valueOf(tmp);
		}catch (Exception e){
			result = defaultValue;
		}
		return result;
	}
	
	private Boolean getBoolPara(String parameterName, HttpServletRequest req){
		// first try URL parameters set by org.springframework.web.servlet.handler.SimpleUrlHandlerMapping controller mapping
		String tmp = getStringPara(parameterName, req);
		return Utils.isTrue(tmp);
	}



}

