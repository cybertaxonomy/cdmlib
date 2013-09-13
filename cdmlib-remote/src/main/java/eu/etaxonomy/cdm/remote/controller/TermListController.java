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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
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
@RequestMapping(value = {"/term"})
public class TermListController extends IdentifiableListController<DefinedTermBase, ITermService> {


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
