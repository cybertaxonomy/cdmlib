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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author a.kohlbecker
 * @date 22.07.2010
 *
 */
@Controller
@RequestMapping(value = {"/termvocabulary/{uuid}"})
public class VocabularyController extends BaseController<TermVocabulary, IVocabularyService> {

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.BaseController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(IVocabularyService service) {
		this.service = service;
	}

}
