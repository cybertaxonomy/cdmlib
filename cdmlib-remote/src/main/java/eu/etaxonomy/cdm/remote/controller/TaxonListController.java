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

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * TODO write controller documentation
 * 
 * @author a.kohlbecker
 * @date 20.03.2009
 */
@Controller
@RequestMapping(value = {"/taxon"})
public class TaxonListController extends BaseListController<TaxonBase, ITaxonService> {

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.BaseListController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Override
	@Autowired
	public void setService(ITaxonService service) {
		this.service = service; 
	}	
}