// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;

/**
 * TODO write controller documentation
 * 
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@RequestMapping(value = {"/reference/{uuid}"})
public class ReferenceController extends AnnotatableController<Reference, IReferenceService>
{
	
	private static final List<String> NOMENCLATURAL_CITATION_INIT_STRATEGY = Arrays.asList(new String []{
			"$",
			"authorTeam", // TODO obsolete??
			"inReference.authorTeam"
	});
	
	private static final List<String> CITATION_WITH_AUTHORTEAM_INIT_STRATEGY = Arrays.asList(new String []{
			"authorTeam.$"  // TODO obsolete??
	});
	
	public ReferenceController(){
		setInitializationStrategy(Arrays.asList(new String[]{
				"$",
				"authorTeam.$" // TODO obsolete??
				}));
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(IReferenceService service) {
		this.service = service;
	}
	
	/**
	 * TODO write controller documentation
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(
		value = {"nomenclaturalCitation"},
		method = RequestMethod.GET)
	public ModelAndView doGetNomenclaturalCitation(
			@PathVariable("uuid") UUID uuid,
			HttpServletRequest request, 
			HttpServletResponse response,
			@RequestParam(value = "microReference", required = false) String microReference)throws IOException {
		ModelAndView mv = new ModelAndView();
		Reference rb = service.load(uuid, NOMENCLATURAL_CITATION_INIT_STRATEGY);
		if(INomenclaturalReference.class.isAssignableFrom(rb.getClass())){
			String nomRefCit = ((INomenclaturalReference)rb).getNomenclaturalCitation(microReference);
			mv.addObject(nomRefCit);
			return mv;
		} else {
			response.sendError(400, "The supplied reference-uuid must specify a INomenclaturalReference.");
		}
		return mv;
	}
	
	@RequestMapping(
			value = {"authorTeam"},
			method = RequestMethod.GET)
		public ModelAndView doGetAuthorTeam(
				@PathVariable("uuid") UUID uuid,
				HttpServletRequest request, 
				HttpServletResponse response) {
		ModelAndView mv = new ModelAndView();
		Reference rb = service.load(uuid, CITATION_WITH_AUTHORTEAM_INIT_STRATEGY);
		if(rb.getAuthorTeam() != null){
			mv.addObject(rb.getAuthorTeam());
		}
		return mv;
	}

}
