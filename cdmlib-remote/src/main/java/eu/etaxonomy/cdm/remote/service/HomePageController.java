package eu.etaxonomy.cdm.remote.service;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


/**
 * Controller to generate the Home Page basics to be rendered by a view.
 * It extends the convenience class AbstractController that encapsulates most
 * of the drudgery involved in handling HTTP requests.
 */
public class HomePageController extends AbstractController
{
	protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception
	{
		// the time at the server
		Calendar cal = Calendar.getInstance();
		java.util.Date now = cal.getTime();

		List<Integer> intList = new ArrayList<Integer>();
		Random random = new Random(now.getTime());

		// 10 random integers
		for (int i = 0; i < 10; ++i)
			intList.add(random.nextInt());

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
		mv.addObject("randList", intList);
		mv.addObject("greeting", greeting);
		mv.setViewName("home");

		return mv;
	}
}

