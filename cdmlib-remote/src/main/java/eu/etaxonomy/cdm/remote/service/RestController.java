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
import eu.etaxonomy.cdm.remote.view.XmlView;


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
	
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		ModelAndView mv = new ModelAndView();
		NameTO n = service.getName(UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6"));
		mv.addObject("dto", n);
		mv.setViewName(getLogicalView(request));
		return mv;
	}
	
	private String getLogicalView(HttpServletRequest request){
		String ctype = request.getHeader("Accept");
		Enumeration headers = request.getHeaderNames();
		return "xmlView";
		//return "jsonView";
	}
	
	private Map getParameters(HttpServletRequest request){
		// set by org.springframework.web.servlet.handler.SimpleUrlHandlerMapping controller mapping
		return (Map) request.getAttribute("ParameterizedUrlHandlerMapping.path-parameters");
	}
}

