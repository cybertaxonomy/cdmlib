/**
 * 
 */
package eu.etaxonomy.cdm.remote.rest;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.bind.annotation.*;

import eu.etaxonomy.cdm.remote.dto.TaxonTO;
import eu.etaxonomy.cdm.remote.service.CdmService;

/**
 * @author markus
 *
 */
// MultiActionController
@Controller
public class CdmRestController {
	static Logger logger = Logger.getLogger(CdmRestController.class);

	@Autowired
	private CdmService cdmActions;

	@RequestMapping(value="/taxon.do", method = RequestMethod.GET) 
	public ModelAndView getTaxon(@RequestParam("uuid") String uuid) {
		ModelAndView mav = new ModelAndView("hello"); 
		JSONObject jObj = null;
		jObj = JSONObject.fromObject(cdmActions.getName(UUID.fromString(uuid)));
		mav.addObject("message", jObj.toString() ); 
		return mav; 
	}

	@RequestMapping("/hello.do") 
	public ModelAndView getHello() {
		ModelAndView mav = new ModelAndView("hello"); 
		mav.addObject("message", "Hello World!"); 
		return mav; 
	}
}
