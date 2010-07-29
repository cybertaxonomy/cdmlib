// $Id$
/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * TODO write controller documentation
 * 
 * @author a.kohlbecker
 * @date 24.03.2009
 */
@Controller
@RequestMapping(value = {"/name"})
public class NameListController extends BaseListController<TaxonNameBase, INameService> {

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.BaseListController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Override
	@Autowired
	public void setService(INameService service) {
		this.service = service; 
	}	
}