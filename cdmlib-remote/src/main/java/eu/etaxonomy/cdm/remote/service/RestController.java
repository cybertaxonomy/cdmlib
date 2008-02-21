package eu.etaxonomy.cdm.remote.service;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.ResultSetPageSTO;
import eu.etaxonomy.cdm.remote.view.XmlView;
import eu.etaxonomy.cdm.remote.service.Utils;


/**
 * Controller to generate the Home Page basics to be rendered by a view.
 * It extends the convenience class AbstractController that encapsulates most
 * of the drudgery involved in handling HTTP requests.
 */
public class RestController extends AbstractController
{
	Log log = LogFactory.getLog(XmlView.class);

	@Autowired
	private CdmService service;

	/* 
	 * return page not found http error (400?) for unknown or incorrect UUIDs
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected ModelAndView handleRequestInternal(HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		ModelAndView mv = new ModelAndView();
		if(getStringPara("action",req).equalsIgnoreCase("find")){
			//
			// retrieve meaningful parameters
			UUID sec = null;
			try{
				sec = UUID.fromString(getStringPara("sec",req));
			}catch (Exception e){
				// TODO: throw HTTP Error400
			}
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
			mv.addObject(service.findTaxa(getStringPara("q",req), sec, higherTaxa, matchAnywhere, onlyAccepted, page, pagesize));
		}else{ 
			// get Object by UUID
			if(getStringPara("dto",req).equalsIgnoreCase("name")){
				NameTO n = service.getName(UUID.fromString(getStringPara("uuid",req)));
				mv.addObject("dto", n);
			}else if(getStringPara("dto",req).equalsIgnoreCase("taxon")){
				NameTO n = service.getName(UUID.fromString(getStringPara("uuid",req)));
				mv.addObject("dto", n);
			}else if(getStringPara("dto",req).equalsIgnoreCase("whatis")){
				mv.addObject(service.findTaxa(null, null, null, true, true, 25, 1));
			}
		}
		// set xml or json view
		mv.setViewName(getLogicalView(req));
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
			Map<String,String> urlParas = (Map) map;
			result = urlParas.get(parameterName);
		}
		if (result == null){
			// alternatively try querystring parameters
			result = req.getParameter(parameterName);
		}
		return result;
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

