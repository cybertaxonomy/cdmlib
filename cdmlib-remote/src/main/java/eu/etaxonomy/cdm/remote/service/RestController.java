/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.service;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import eu.etaxonomy.cdm.datagenerator.TaxonGenerator;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao;
import eu.etaxonomy.cdm.persistence.dao.common.ITitledDao.MATCH_MODE;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceSTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceTO;
import eu.etaxonomy.cdm.remote.dto.ResultSetPageSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;
import eu.etaxonomy.cdm.remote.dto.TreeNode;
import eu.etaxonomy.cdm.remote.view.XmlView;
import eu.etaxonomy.cdm.remote.service.Utils;


/**
 * Controller to generate the Home Page basics to be rendered by a view.
 * It extends the convenience class AbstractController that encapsulates most
 * of the drudgery involved in handling HTTP requests.
 */
@Controller("restController")
public class RestController extends AbstractController
{
	Log log = LogFactory.getLog(RestController.class);

	@Autowired
	private ICdmService service;

	/* 
	 * return page not found http error (404) for unknown or incorrect UUIDs
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		try{
			ModelAndView mv = new ModelAndView();
			String action = getNonNullPara("action",req);
			String op = getNonNullPara("operation",req);
			String dto = getNonNullPara("dto",req);
			String uuid = getNonNullPara("uuid",req);
			String sec = getNonNullPara("sec",req);
			String q = getNonNullPara("q",req);
			
			Enumeration<Locale> locales = req.getLocales();
			
			log.info(String.format("Request received: act=%s op=%s dto=%s uuid=%s sec=%s", action, op, dto, uuid, sec));
			
			if(action==""){
				// get Object by UUID
				if(dto.equalsIgnoreCase("name")){
					NameTO n = service.getName( getUuid(uuid), locales);
					mv.addObject(n);
				}else if(dto.equalsIgnoreCase("taxon")){
					TaxonTO t = service.getTaxon(getUuid(uuid), locales);
					mv.addObject(t);
				}else if(dto.equalsIgnoreCase("ref")){
					ReferenceTO r = service.getReference(getUuid(uuid), locales);
					mv.addObject(r);
				}else if(dto.equalsIgnoreCase("whatis")){
					//TODO: somehow the whatis url path is not delegated to this controller ?!#!??
					Object cl = service.whatis(getUuid(uuid));
					mv.addObject(cl);
				}
			}else if(action.equalsIgnoreCase("simple")){
				Set<UUID> uuids = getUuids(uuid);
				if(dto.equalsIgnoreCase("name")){
					List<NameSTO> n = service.getSimpleNames(uuids, locales);
					mv.addObject(n);
				}else if(dto.equalsIgnoreCase("taxon")){
					if(op.equalsIgnoreCase("acceptedfor")){
						List<TaxonSTO> t = service.getAcceptedTaxon(getUuid(uuid), locales);
						mv.addObject(t);
					} else {
						List<TaxonSTO> t = service.getSimpleTaxa(uuids, locales);
						mv.addObject(t);
					}
				}else if(dto.equalsIgnoreCase("ref")){
					List<ReferenceSTO> r = service.getSimpleReferences(uuids, locales);
					mv.addObject(r);
				}
			}else if(action.equalsIgnoreCase("find")){
				//
				// retrieve meaningful parameters
				UUID u = null;
				try {
					u = getUuid(sec);
				} catch (CdmObjectNonExisting e) {
					log.warn("Concept sec reference UUID is not valid. Ignore");
				}
				Set<UUID> higherTaxa = new HashSet<UUID>();
				// TODO: take higher taxa UUIDs from "higherTaxa"
				//
				
				MATCH_MODE matchMode = null;
				try{
					String matchModeStr = getStringPara("mode",req);
					matchMode = MATCH_MODE.valueOf(matchModeStr.toUpperCase());
				} catch(Exception e){
					matchMode = MATCH_MODE.BEGINNING;
				}
//				if(matchMode == null){
//				}
				
				Boolean onlyAccepted = getBoolPara("onlyAccepted",req);
				if (onlyAccepted==null){
					onlyAccepted=false;
				};
				Integer page = getIntPara("page",req);
				if (page==null){
					page=1;
				};
				Integer pagesize = getIntPara("pagesize",req);
				if (pagesize==null){
					pagesize=25;
				};
				//
				// search for taxa
				Object obj = service.findTaxa(q, u, higherTaxa, matchMode, onlyAccepted, page, pagesize, locales);
				mv.addObject(obj);
			}else if(action.equalsIgnoreCase("taxonomy")){
				List results = null; 
				if(op.equalsIgnoreCase("children")){
					results = service.getChildrenTaxa(getUuid(uuid));
				}
				else if(op.equalsIgnoreCase("parents")){
					results = service.getParentTaxa(getUuid(uuid));
				}
				else if(op.equalsIgnoreCase("root")){
					UUID u = null;
					if(sec != null && sec.length() == 36 ){
						try {
							u = getUuid(sec);
						} catch (CdmObjectNonExisting e) {
							log.warn("Concept sec reference UUID is not valid. Ignore");
						}
					}
					results = service.getRootTaxa(u);
				}
				mv.addObject( (List)results );
			}else if(action.equalsIgnoreCase("insert")){
				// insert test data.
				//
				// TODO: THIS OPERATION IS FOR TESTING ONLY AND SHOULD BE REMOVED !!!
				//
				Taxon t = TaxonGenerator.getTestTaxon();
				service.saveTaxon(t);
				mv.addObject("status", "Test data inserted");
			}else{
				// nothing matches
				mv.addObject("status", "Controller does not know this operation");
			}
			// set xml or json view
			mv.setViewName(getLogicalView(req));
			return mv;
		}catch(CdmObjectNonExisting e){
			sendNonExistingUuidError(resp, e);
			return null;
		}
	}
	
	/**
	 * return a proper UUID for a string resembling a UUID
	 * If the uuid string is not a valid UUID, a CdmObjectNonExisting exception is thrown
	 * @param uuid
	 * @return
	 * @throws CdmObjectNonExisting
	 */
	private UUID getUuid(String uuid) throws CdmObjectNonExisting{
		UUID u=null;
		try{
			u = UUID.fromString(uuid);
		}catch(IllegalArgumentException e){
			throw new CdmObjectNonExisting(uuid);
		}
		return u;
	}
	/**
	 * Turns a string of uuids concatenated with comma characters '<code>,</code>' into a Set of UUID instances
	 * @param uuid
	 * @return
	 */
	private Set<UUID> getUuids(String uuid) {
		String [] temp = uuid.trim().split(",");
		Set<UUID> uuids = new HashSet<UUID>();
		for (String u : temp){
			try {
				uuids.add(getUuid(u));
			} catch (CdmObjectNonExisting e) {
				logger.warn(u+" is not valid UUID!");
			}
		}
		return uuids;
	}
	
	private void sendNonExistingUuidError(HttpServletResponse resp, CdmObjectNonExisting e) throws IOException{
		resp.sendError(404, e.getMessage() );		
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
	private String getNonNullPara(String parameterName, HttpServletRequest req){
		String val = getStringPara(parameterName, req);
		if (val==null){
			return "";
		}
		return val;
	}
	private Integer getIntPara(String parameterName, HttpServletRequest req){
		// first try URL parameters set by org.springframework.web.servlet.handler.SimpleUrlHandlerMapping controller mapping
		Integer result;
		String tmp = getStringPara(parameterName, req);
		try{
			result = Integer.valueOf(tmp);
		}catch (Exception e){
			result = null;
		}
		return result;
	}
	private Boolean getBoolPara(String parameterName, HttpServletRequest req){
		// first try URL parameters set by org.springframework.web.servlet.handler.SimpleUrlHandlerMapping controller mapping
		String tmp = getStringPara(parameterName, req);
		return Utils.isTrue(tmp);
	}
	/**
	 * Return logical spring view name to be used for rendering
	 * Read http request parameter "Accept" and decide whether to use JSON or XML for the response.
	 * Defaults to XML in case no matching header can be identified.
	 * @param request
	 * @return
	 */
	private String getLogicalView(HttpServletRequest request){
		String DEFAULT_VIEW = "xmlView";
		String ctype = request.getHeader("Accept");
		String[] ctypes = ctype.split("[,;]");
		for (String ct : ctypes){
			if (ct.endsWith("json")){
				return "jsonView";
			}else if (ct.endsWith("xml")){
				return "xmlView";
			}
		}
		return DEFAULT_VIEW;
	}


}

