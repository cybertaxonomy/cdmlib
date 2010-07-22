// $Id$
/**
* Copyright (C) 2009 EDIT
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;

/**
 * TODO write controller documentation
 * 
 * @author a.kohlbecker
 * @date 23.06.2009
 *
 */
@Controller
@RequestMapping(value = {"/term/", "/term/{uuid}"}) //FIXME refactor type mappings
public class TermListController extends BaseListController<DefinedTermBase, ITermService> {
	
	private static final List<String> VOCABULARY_LIST_INIT_STRATEGY = Arrays.asList(new String []{
			"representations"
	});
	
	private static final List<String> TERM_COMPARE_INIT_STRATEGY = Arrays.asList(new String []{
			"vocabulary"
	});
	
	private static final List<String> VOCABULARY_INIT_STRATEGY = Arrays.asList(new String []{
			"$",
			"representations",
			"terms.representations"
	});
	
	private IVocabularyService vocabularyService;
	
	@Autowired
	public void setVocabularyService(IVocabularyService vocabularyService) {
		this.vocabularyService = vocabularyService;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.AbstractListController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(ITermService service) {
		this.service = service;
	}
	
	/**
	 * TODO write controller method documentation
	 * 
	 * @param page
	 * @param pageSize
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET,
		value = "/term/")
	public Pager<TermVocabulary> doGetVocabularies(
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) {
		
		if(page == null){ page = DEFAULT_PAGE_NUMBER;}
		if(pageSize == null){ pageSize = DEFAULT_PAGESIZE;}
		
		return (Pager<TermVocabulary>) vocabularyService.page(null,pageSize, page, null, VOCABULARY_LIST_INIT_STRATEGY);
	}
	
	/**
	 * TODO write controller method documentation
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET,
		value = "/term/{uuid}")
	public TermVocabulary<DefinedTermBase> doGetTerms(
			@PathVariable("uuid") UUID uuid,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		TermVocabulary<DefinedTermBase> vocab = vocabularyService.load(uuid, VOCABULARY_INIT_STRATEGY);
		return vocab;
	}
	
	/**
	 * TODO write controller method documentation
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET,
		value = "/term/{uuid}/compareTo/{uuidThat}")
	public ModelAndView doCompare(@PathVariable("uuid") UUID uuidThis,
			@PathVariable("uuidThat") UUID uuidThat,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		ModelAndView mv = new ModelAndView();
		DefinedTermBase thisTerm = service.load(uuidThis, TERM_COMPARE_INIT_STRATEGY);
		DefinedTermBase thatTerm = service.load(uuidThat, TERM_COMPARE_INIT_STRATEGY);
		if(thisTerm.getVocabulary().equals(thatTerm.getVocabulary())){
			if(OrderedTermBase.class.isAssignableFrom(thisTerm.getClass())){
				Integer result = ((OrderedTermBase)thisTerm).compareTo((OrderedTermBase)thatTerm);
				mv.addObject(result);
				return mv;
			}
			response.sendError(400, "Only ordered term types can be compared");
			return mv;
		}
		response.sendError(400, "Terms of different vocabuaries can not be compared");
		return mv;
	}
	
	/**
	 * TODO write controller method documentation
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET,
		value = "/term/tdwg/*")
	public List<NamedArea> doGetTdwgLevel(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String path = request.getServletPath();
		String[] pathTokens = path.split("/");
		String levelStr = pathTokens[3];
		if(levelStr.indexOf('.') > -1){
			levelStr = levelStr.substring(0, levelStr.indexOf('.'));
		}
		Integer levelId = Integer.valueOf(levelStr);
		NamedAreaLevel level = null;
		switch(levelId){
			case 1: level = NamedAreaLevel.TDWG_LEVEL1(); break;
			case 2: level = NamedAreaLevel.TDWG_LEVEL2(); break;
			case 3: level = NamedAreaLevel.TDWG_LEVEL3(); break;
			case 4: level = NamedAreaLevel.TDWG_LEVEL4(); break;
		}
		Pager<NamedArea> p = service.list(level, (NamedAreaType)null, null, null, null, null);
		return p.getRecords();
	}

	
	

}
