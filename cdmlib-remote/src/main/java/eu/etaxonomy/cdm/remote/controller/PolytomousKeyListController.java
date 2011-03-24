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

import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.model.description.PolytomousKey;

/**
 * @author a.kohlbecker
 * @date 24.03.2011
 *
 */
@Controller
@RequestMapping(value = {"/polytomousKey"})
public class PolytomousKeyListController extends BaseListController<PolytomousKey, IPolytomousKeyService> {

	@Autowired
	public void setService(IPolytomousKeyService service) {
		this.service = service;
	}
	
}
	
