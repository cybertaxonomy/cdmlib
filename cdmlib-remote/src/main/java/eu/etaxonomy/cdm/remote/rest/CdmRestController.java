/**
 * 
 */
package eu.etaxonomy.cdm.remote.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.bind.annotation.*;

import eu.etaxonomy.cdm.remote.service.CdmService;

/**
 * @author markus
 *
 */
// MultiActionController
@Controller
@RequestMapping("/cdm.do") 
public class CdmRestController extends AbstractController {
	
	@Autowired
	private CdmService actions;

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	@ModelAttribute("types") 

	protected ModelAndView handleRequestInternal(HttpServletRequest arg0,
			HttpServletResponse arg1) throws Exception {
		ModelAndView mav = new ModelAndView("hello"); 
		mav.addObject("message", "Hello World!"); 
		return mav; 
	}

}
