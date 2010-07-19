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

import java.util.List;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.remote.editor.ClassPropertyEditor;


/**
 * @author a.kohlbecker
 * @date 22.07.2009
 *
 * @param <T>
 * @param <SERVICE>
 * 
 */
public abstract class BaseListController <T extends CdmBase, SERVICE extends IService<T>> extends AbstractListController<T, SERVICE> {

	public static final Integer DEFAULT_PAGESIZE = 20;
	public static final Integer DEFAULT_PAGE_NUMBER = 0;
	
	@InitBinder
    public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Class.class, new ClassPropertyEditor());
	}
	

	/**
	 * @param pageNumber
	 *            the number of the page to be returned, the first page has the
	 *            pageNumber = 1 - <i>optional parameter</i>
	 * @param pageSize
	 *            the maximum number of entities returned per page (can be null
	 *            to return all entities in a single page) - <i>optional
	 *            parameter</i>
	 * @param type
	 *            Further restricts the type of entities to be returned. 
	 *            If null the base type <code>&lt;T&gt;</code> is being used. - <i>optional parameter</i>
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public Pager<T> doPage(
			@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "class", required = false) Class<T> type) {
		
		if(pageNumber == null){ pageNumber = DEFAULT_PAGE_NUMBER;}
		if(pageSize == null){ pageSize = DEFAULT_PAGESIZE;}
		
		return service.page(type, pageSize, pageNumber, null, DEFAULT_INIT_STRATEGY);
	}
	
	/**
	 * Parameter less method to be used as default when request without parameter are made. Otherwise
	 * the nameless methods {@link #doPage(Integer, Integer, Class)} and {@link #doList(Integer, Integer, Class)}
	 * are ambigous.
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public Pager<T> doPage(){
		return doPage(null, null, null);
	}
	
	/**
	 * @param start
	 *            The offset index from the start of the list. The first entity
	 *            has the index = 0 - <i>required parameter</i>
	 * @param limit
	 *            The maximum number of entities returned. - <i>optional parameter</i>
	 * @param type
	 *            Further restricts the type of entities to be returned. 
	 *            If null the base type <code>&lt;T&gt;</code> is being used. - <i>optional parameter</i>
	 * @return a List of entities
	 */
	@RequestMapping(method = RequestMethod.GET)
	public List<T> doList(
			@RequestParam(value = "start", required = true) Integer start,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "class", required = false) Class<T> type) {
		
		//if(start == null){ start = 0;}
		if(limit == null){ limit = DEFAULT_PAGESIZE;}
		//TODO implement initialization
		return service.list(type, limit, start, null, DEFAULT_INIT_STRATEGY);
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