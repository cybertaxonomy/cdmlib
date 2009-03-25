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

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.model.common.CdmBase;

//$Id$
/**
 * based on org.cateproject.controller.common
 * @author b.clark
 * @author a.kohlbecker
 *
 * @param <T>
 * @param <SERVICE>
 */

public abstract class BaseController<T extends CdmBase, SERVICE extends IService<T>> {
	
	public static final Logger logger = Logger.getLogger(BaseController.class);
	
	protected static final Integer DEFAULT_PAGE_SIZE = 30;
	
	protected SERVICE service;
	
	protected Pattern uuidParameterPattern = null;
	
	protected void setUuidParameterPattern(String pattern){
		uuidParameterPattern = Pattern.compile(pattern);
	}
	
	public abstract void setService(SERVICE service);
	
	/**@InitBinder
    public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
		//TODO do we need this one?: binder.registerCustomEditor(Class.class, new ClassPropertyEditor());
	}
	*/
	
	protected UUID readValueUuid(HttpServletRequest request) {
		String path = request.getServletPath();
		if(path != null) {
			Matcher uuidMatcher = uuidParameterPattern.matcher(path);
			if(uuidMatcher.matches() && uuidMatcher.groupCount() > 0){
				try {
					UUID uuid = UUID.fromString(uuidMatcher.group(1));
					return uuid;
				} catch (Exception e) {
					logger.warn(uuidMatcher.group(1) + "is not a uuid");
				}
			}
		}
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public T doGet(HttpServletRequest request) {
		
		UUID uuid = readValueUuid(request);
		Assert.notNull(uuid, "no valid uuid");
		if(uuid == null){
			return null;
		}
		return service.findByUuid(uuid);
	}

	  /* TODO implement
	   * 
	  @RequestMapping(method = RequestMethod.POST)
	  public T doPost(@PathVariable(value = "uuid") UUID uuid, @ModelAttribute("object") T object, BindingResult result) {
	        validator.validate(object, result);
	        if (result.hasErrors()) {
	                // set http status code depending upon what happened, possibly return
	            // the put object and errors so that they can be rendered into a suitable error response
	        } else {
	          // requires merging detached object ?gilead?
	          service.update(object);
	        }
	  }

	  @RequestMapping(method = RequestMethod.PUT) // the cdm-server may not allow clients to specify the uuid for resources
	  public T doPut(@PathVariable(value = "uuid") UUID uuid, @ModelAttribute("object") T object, BindingResult result) {
	        validator.validate(object, result);
	        if (result.hasErrors()) {
	                // set http status code depending upon what happened, possibly return
	            // the put object and errors so that they can be rendered into a suitable error response
	        } else {
	          service.save(object);
	        }
	  }

	   @RequestMapping(method = RequestMethod.DELETE)
	   public void doDelete(@PathVariable(value = "uuid") UUID uuid) {
	       T object = service.find(uuid);
	       // provided the object exists
	       service.delete(uuid);
	       // might return 204 or 200
	   }
	}
*/
	

}
