package eu.etaxonomy.cdm.remote.service;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import eu.etaxonomy.cdm.remote.dto.NameTO;


/**
 * Controller to generate the Home Page basics to be rendered by a view.
 * It extends the convenience class AbstractController that encapsulates most
 * of the drudgery involved in handling HTTP requests.
 */
public class HomePageController extends AbstractController
{
	@Autowired
	private CdmService service;
	
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		// time-of-day dependent greeting
		NameTO n = service.getName(UUID.fromString("f81d4fae-7dec-11d0-a765-00a0c91e6bf6"));

		ModelAndView mv = new ModelAndView();
		mv.addObject(n);
		mv.setViewName("json1");

		return mv;
	}
}

