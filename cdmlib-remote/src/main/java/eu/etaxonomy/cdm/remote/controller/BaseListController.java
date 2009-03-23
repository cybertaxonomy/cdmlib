/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.CdmBase;


public abstract class BaseListController <T extends CdmBase, SERVICE extends IService<T>> {

	protected SERVICE service;
	
	public static final Integer DEFAULT_PAGESIZE = 20;
	public static final Integer DEFAULT_PAGE = 1;
	
	public abstract void setService(SERVICE service);

	@RequestMapping(method = RequestMethod.GET)
	public Pager<T> doGet(
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "pageSize", required = false) Integer pageSize) {
		
		if(page == null){ page = DEFAULT_PAGE;}
		if(pageSize == null){ pageSize = DEFAULT_PAGESIZE;}
		
		return (Pager<T>) service.list(pageSize, page);
	}

  /* TODO 
   @RequestMapping(method = RequestMethod.POST)
  public T doPost(@ModelAttribute("object") T object, BindingResult result) {
        validator.validate(object, result);
        if (result.hasErrors()) {
                // set http status code depending upon what happened, possibly return
            // the put object and errors so that they can be rendered into a suitable error response
        } else {
          // should set the status to 201 created  and "Location" header to "/resource/uuid"
          service.save(object);
        }
  }
  */
}