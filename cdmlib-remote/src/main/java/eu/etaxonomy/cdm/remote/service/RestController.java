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
		Map<String,String> paras = this.getParameters(req);
		ModelAndView mv = new ModelAndView();
		if(paras.get("action").equalsIgnoreCase("find")){
			// validate parameters
			UUID sec = UUID.fromString(req.getParameter("sec"));
			Set<UUID> higherTaxa = new HashSet<UUID>();
			// TODO: take higher taxa UUIDs from req.getParameter("higherTaxa")
			boolean matchAnywhere = Utils.isTrue(req.getParameter("matchAnywhere")); 
			boolean onlyAccepted = Utils.isTrue(req.getParameter("onlyAccepted"));
			// search for taxa
			mv.addObject(service.findTaxa(req.getParameter("q"), sec, higherTaxa, matchAnywhere, onlyAccepted, (int)Integer.valueOf(req.getParameter("pagesize")), (int)Integer.valueOf(req.getParameter("page")) ));
		}else{ 
			// get Object by UUID
			if(paras.get("dto").equalsIgnoreCase("name")){
				NameTO n = service.getName(UUID.fromString(paras.get("uuid")));
				mv.addObject("dto", n);
			}else if(paras.get("dto").equalsIgnoreCase("taxon")){
				NameTO n = service.getName(UUID.fromString(paras.get("uuid")));
				mv.addObject("dto", n);
			}else if(paras.get("dto").equalsIgnoreCase("whatis")){
				mv.addObject(service.findTaxa(null, null, null, true, true, 25, 1));
			}
		}
		// set xml or json view
		mv.setViewName(getLogicalView(req));
		return mv;
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
	
	private Map<String,String> getParameters(HttpServletRequest request){
		// set by org.springframework.web.servlet.handler.SimpleUrlHandlerMapping controller mapping
		return (Map) request.getAttribute("ParameterizedUrlHandlerMapping.path-parameters");
	}


}

