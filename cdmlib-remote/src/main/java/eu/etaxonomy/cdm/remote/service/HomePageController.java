package eu.etaxonomy.cdm.remote.service;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


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
		// the time at the server
		Calendar cal = Calendar.getInstance();
		java.util.Date now = cal.getTime();

		// time-of-day dependent greeting
		String greeting = "Morning";
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (hour == 12)
			greeting = "Day";
		else if (hour > 18)
			greeting = "Evening";
		else if (hour > 12)
			greeting = "Afternoon";

		ModelAndView mv = new ModelAndView();
		mv.addObject("time", now);
		mv.addObject("greeting", greeting);
		mv.addObject("pmap", request.getParameterMap());
		mv.addObject("path", request.getPathInfo());
		mv.addObject("ctype", request.getContentType());
		mv.setViewName("home");

		return mv;
	}
}

