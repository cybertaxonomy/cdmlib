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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * TODO write controller documentation
 * 
 * @author a.kohlbecker
 * @date 24.03.2009
 */

@Controller
@RequestMapping(value = {"/reference/*","/reference/*/annotation", "/reference/*/authorTeam", "/reference/*/nomenclaturalCitation"})
public class ReferenceController extends AnnotatableController<ReferenceBase, IReferenceService>
{
	
	private static final List<String> NOMENCLATURAL_CITATION_INIT_STRATEGY = Arrays.asList(new String []{
			"$",
			"inBook.authorTeam",
			"inJournal",
			"inProceedings",
	});
	
	private static final List<String> CITATION_WITH_AUTHORTEAM_INIT_STRATEGY = Arrays.asList(new String []{
			"authorTeam.$",
			"authorTeam.titleCache",
	});
	
	public ReferenceController(){
		super();
		setUuidParameterPattern("^/reference/([^/?#&\\.]+).*");
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
		value = {"/reference/*/nomenclaturalCitation"},
		method = RequestMethod.GET)
	public ModelAndView doGetNomenclaturalCitation(
			HttpServletRequest request, 
			HttpServletResponse response,
			@RequestParam(value = "microReference", required = false) String microReference)throws IOException {
		ModelAndView mv = new ModelAndView();
		UUID nomRefUuid = readValueUuid(request, null);
		ReferenceBase rb = service.load(nomRefUuid, NOMENCLATURAL_CITATION_INIT_STRATEGY);
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
			value = {"/reference/*/authorTeam"},
			method = RequestMethod.GET)
		public ModelAndView doGetAuthorTeam(
				HttpServletRequest request, 
				HttpServletResponse response) {
		ModelAndView mv = new ModelAndView();
		UUID refUuid = readValueUuid(request, null);
		ReferenceBase rb = service.load(refUuid, CITATION_WITH_AUTHORTEAM_INIT_STRATEGY);
		mv.addObject(rb.getAuthorTeam());
		return mv;
	}

}
