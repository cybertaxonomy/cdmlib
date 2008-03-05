package eu.etaxonomy.cdm.remote.service;

import java.io.IOException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import eu.etaxonomy.cdm.datagenerator.TaxonGenerator;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
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
			String sec = getStringPara("sec",req);
			
			log.info(String.format("Request received: act=%s op=%s dto=%s uuid=%s sec=%s", action, op, dto, uuid, sec));
			
			if(action==""){
				// get Object by UUID
				if(dto.equalsIgnoreCase("name")){
					NameTO n = service.getName( getUuid(uuid));
					mv.addObject(n);
				}else if(dto.equalsIgnoreCase("taxon")){
					TaxonTO t = service.getTaxon(getUuid(uuid));
					mv.addObject(t);
				}else if(dto.equalsIgnoreCase("whatis")){
					//TODO: somehow the whatis url path is not delegated to this controller ?!#!??
					Object cl = service.whatis(getUuid(uuid));
					mv.addObject(cl);
				}
			}else if(action.equalsIgnoreCase("simple")){
				if(dto.equalsIgnoreCase("name")){
					NameSTO n = service.getSimpleName( getUuid(uuid));
					mv.addObject(n);
				}else if(dto.equalsIgnoreCase("taxon")){
					TaxonSTO t = service.getSimpleTaxon(getUuid(uuid));
					mv.addObject(t);
				}
			}else if(action.equalsIgnoreCase("find")){
				//
				// retrieve meaningful parameters
				String q = getStringPara("q",req);
				if (q==null){
					q="";
				};
				Set<UUID> higherTaxa = new HashSet<UUID>();
				// TODO: take higher taxa UUIDs from "higherTaxa"
				Boolean matchAnywhere = getBoolPara("matchAnywhere",req);
				if (matchAnywhere==null){
					matchAnywhere=false;
				};
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
				Object obj = service.findTaxa(q, getUuid(sec), higherTaxa, matchAnywhere, onlyAccepted, page, pagesize);
				mv.addObject(obj);
			}else if(action.equalsIgnoreCase("taxonomy")){
				Object obj = null; 
				if(op!=null && op.equalsIgnoreCase("children")){
					obj = service.getChildrenTaxa(getUuid(uuid));
				}
				else if(op!=null && op.equalsIgnoreCase("parent")){
					obj = service.getParentTaxa(getUuid(uuid));
				}
				mv.addObject(obj);
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
			Map<String,String> urlParas = (Map) map;
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
	 * Read http request parameter "Accept" and decide whether to use JSON or XML for the response.
	 * Defaults to XML in case no matching header can be identified.
	 * @param request
	 * @return
	 */
	private String getLogicalView(HttpServletRequest request){
		String ctype = request.getHeader("Accept");
		String[] ctypes = ctype.split("[,;]");
		for (String ct : ctypes){
			if (ct.endsWith("json")){
				return "jsonView";
			}else if (ct.endsWith("xml")){
				return "xmlView";
			}
		}
		// default to XML
		return "xmlView";
	}


}

