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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.database.NamedContextHolder;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.remote.dto.FeatureTO;
import eu.etaxonomy.cdm.remote.dto.FeatureTreeTO;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;


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
	
	private static final int DEFAULT_PAGE_SIZE = 25;

	/* 
	 * return page not found http error (404) for unknown or incorrect UUIDs
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		try{
			ModelAndView mv = new ModelAndView();
			String basepath = getNonNullPara("basepath",req);
			String action = getNonNullPara("action",req);
			String op = getNonNullPara("operation",req);
			String dto = getNonNullPara("dto",req);
			String uuid = getNonNullPara("uuid",req);
			String sec = getNonNullPara("sec",req);
			String q = getNonNullPara("q",req);
			Integer pageNumber = getIntPara("pageNumber", 0 , req);
			
			Enumeration<Locale> locales = req.getLocales();
			
			log.info(String.format("Request received for %s: act=%s op=%s dto=%s uuid=%s sec=%s", basepath, action, op, dto, uuid, sec));
			
			NamedContextHolder.setContextKey(basepath);

			/* ----------------------------------------
			 * FIXME test implementation !!!!!! works OK :) however compeletly
			 * misplaced in here, when moving also reconsider service.getDataSource()
			 * which was implemented for testing only
			 */
//			AbstractDataSource ads = service.getDataSource();
//			DataSourceLoader.updateRoutingDataSource(ads);
			// ---------------------------------------- 
			
			if(action==""){
				// get Object by UUID
				if(dto.equalsIgnoreCase("name")){
					NameTO n = service.getName( getUuid(uuid), locales);
					mv.addObject(n);
				}else if(dto.equalsIgnoreCase("taxon")){
					UUID taxonUuid = getUuid(uuid);
					
					String ftree = this.getStringPara("ftree", req);
					
					UUID featureTreeUuid = null;
					if(ftree != null){
						featureTreeUuid = getUuid(ftree);
					}
					
					TaxonTO t = service.getTaxon(taxonUuid, featureTreeUuid, locales);
					mv.addObject(t);
				}else if(dto.equalsIgnoreCase("reference")){
					if(op.equals("list")){
						
						Pager<ReferenceBase> pager = service.listReferences(DEFAULT_PAGE_SIZE, pageNumber);
						mv.addObject(pager);
					} else {
						ReferenceBase r = service.getReference(getUuid(uuid), locales);
						mv.addObject(r);						
					}
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
						Hashtable<String, List<TaxonSTO>> t = service.getAcceptedTaxa(uuids, locales);
						mv.addObject(t);
					} else {
						List<TaxonSTO> t = service.getSimpleTaxa(uuids, locales);
						mv.addObject(t);
					}
				}else if(dto.equalsIgnoreCase("reference")){
					List<ReferenceSTO> r = service.getSimpleReferences(uuids, locales);
					mv.addObject(r);
				}
			}else if(action.equalsIgnoreCase("find")){
				//
				// TODO handle multiple sec
				Set<UUID> secundum = null;
				try {
					secundum = new HashSet<UUID>();
					List<String> secs = getListPara("sec", req);
					if(secs != null){
						for (String secString : secs){
							secundum.add(getUuid(secString));
						}
					}	
					log.info(secundum);
				} catch (CdmObjectNonExisting e) {
					log.warn("Concept sec reference UUID is not valid. Ignore");
				}
				
				Set<UUID> higherTaxa = new HashSet<UUID>();
				// TODO: take higher taxa UUIDs from "higherTaxa"
				//
				
				MatchMode matchMode = null;
				try{
					String matchModeStr = getStringPara("mode",req);
					matchMode = MatchMode.valueOf(matchModeStr.toUpperCase());
				} catch(Exception e){
					matchMode = MatchMode.BEGINNING;
				}				
				
				String featureTree = getStringPara("feature", req); 
				logger.info("FeatureTree: " + featureTree);
				
				Boolean onlyAccepted = getBoolPara("onlyAccepted",req);
				if (onlyAccepted==null){
					onlyAccepted=false;
				};
				Integer page = getIntPara("page", 1, req);
				page--;
				Integer pagesize = getIntPara("pagesize", DEFAULT_PAGE_SIZE, req);
				
				//
				// search for taxa
				
				Object obj = service.findTaxa(q, secundum, higherTaxa, matchMode, onlyAccepted, page, pagesize, locales);
				mv.addObject(obj);
			}else if(action.equalsIgnoreCase("taxonomy")){
				List results = null; 
				if(op.equalsIgnoreCase("children")){
					results = service.getChildTaxa(getUuid(uuid));
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
			}else if(action.equalsIgnoreCase("features")){
				logger.info("Feature Request.");
				if(op != null && op.equals("tree")){
					// return a list of feature trees stored in database
					List<FeatureTreeTO> featureTree = service.getFeatureTrees(locales);
					mv.addObject(featureTree);
				}else{
					// return a list of features this community store supports
					List<FeatureTO> feature = service.getFeatures(locales);
					mv.addObject(feature);
				}
				
			}

//FIXME commented out below, since refactoring is urgently needed see ticket#593 http://dev.e-taxonomy.eu/trac/ticket/593
//			else if(action.equalsIgnoreCase("annotations")){
//			
//				logger.info("Annotation action requested.");
//				
//				UUID annotatableEntityUuid = getUuid(uuid);
//				
//				String requestMethod = req.getMethod();
//				
//				if(requestMethod.equalsIgnoreCase("GET")){
//					logger.info("Processing GET request");
//					AnnotationTO annotation = service.getAnnotation(annotatableEntityUuid, locales);
//					mv.addObject(annotation);					
//				}else if(requestMethod.equalsIgnoreCase("POST")){
//					
//					String annotationText = req.getParameter("annotation");
//					// TODO set locale
//					logger.info("Processing POST request");
//					
//					Annotation annotation = Annotation.NewInstance(annotationText, null);
//					
//					service.saveAnnotation(annotatableEntityUuid, annotation);
//					//log.info(service.saveAnnotation(annotatableEntityUuid, annotation));
//					
//				}				
//			}
				else{
				// nothing matches
				mv.addObject("status", "Controller does not know this operation");
			}
			// set xml or json view
			mv.setViewName(getLogicalView(req));
			
			// avoid memory leaks
			NamedContextHolder.clearContextKey();
			
			return mv;
		}catch(CdmObjectNonExisting e){
			sendNonExistingUuidError(resp, e);
			// avoid memory leaks
			NamedContextHolder.clearContextKey();
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
	/**
	 * Return logical spring view name to be used for rendering
	 * Read http request parameter "Accept" and decide whether to use JSON or XML for the response.
	 * Defaults to XML in case no matching header can be identified.
	 * @param request
	 * @return
	 */
	private String getLogicalView(HttpServletRequest request){
//		String DEFAULT_VIEW = "xmlView";
//		String ctype = request.getHeader("Accept");
//		String[] ctypes = ctype.split("[,;]");
//		for (String ct : ctypes){
//			if (ct.endsWith("json")){
//				return "jsonView";
//			}else if (ct.endsWith("xml")){
//				return "xmlView";
//			}
//		}
//		return DEFAULT_VIEW;
		return "jsonView";
	}


}

