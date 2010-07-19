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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.mapping.Map;
import org.springframework.util.Assert;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;

//$Id$
/**
 * based on org.cateproject.controller.common
 * @author b.clark
 * @author a.kohlbecker
 *
 * @param <T>
 * @param <SERVICE>
 */

public abstract class BaseController<T extends CdmBase, SERVICE extends IService<T>> extends AbstractController {

	protected SERVICE service;
	
	public abstract void setService(SERVICE service);

	@InitBinder
    public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET)
	public T doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("doGet() " + request.getServletPath());
		T obj = (T) getCdmBase(request, response, initializationStrategy, CdmBase.class);
		return obj;
	}

	/**
	 * @param request
	 * @param response
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	protected <CDM_BASE> CDM_BASE  getCdmBase(HttpServletRequest request, HttpServletResponse response, 
			List<String> initStrategy, Class<CDM_BASE> clazz) throws IOException {
		T obj = null;
		try {
			UUID uuid = readValueUuid(request, null);
			Assert.notNull(uuid, HttpStatusMessage.UUID_MISSING.toString());
			
			if(initStrategy == null){
				// may be null is set to null via the setter
				obj = service.find(uuid);
			} else {				
				obj = service.load(uuid, initStrategy);
			}
			Assert.notNull(obj, HttpStatusMessage.UUID_NOT_FOUND.toString());
			
		} catch (IllegalArgumentException iae) {
			HttpStatusMessage.fromString(iae.getMessage()).send(response);
		}
		CDM_BASE t;
		try {
			t = (CDM_BASE)obj;
			return t;
		} catch (Exception e) {
			HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
			return null;
		}
	}
	
	protected T getCdmBaseInstance(UUID uuid, 
			HttpServletResponse response, 
			List<String> pathProperties) throws IOException{
		CdmBase cdmBaseObject = service.load(uuid, pathProperties);
		if(cdmBaseObject == null){
			HttpStatusMessage.UUID_NOT_FOUND.send(response);
		}
		return (T) cdmBaseObject;
	}
	
	protected T getCdmBaseInstance(UUID uuid, HttpServletResponse response, String pathProperty) throws IOException{
		return getCdmBaseInstance(uuid, response, Arrays.asList(new String[]{pathProperty}));
	}
	
	
	@RequestMapping(value = "{uuid}/*", method = RequestMethod.GET)
	public ModelAndView doGetMethod(@PathVariable("uuid") UUID uuid,
			HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		logger.info("doGetMethod() " + request.getServletPath());
		
		ModelAndView modelAndView = new ModelAndView();
		
		String servletPath = request.getServletPath();
		String baseName = FilenameUtils.getBaseName(servletPath);
		
		T instance = getCdmBaseInstance(uuid, response, Arrays.asList(new String[]{baseName + ".titleCache"}));
		
		try {
			String methodName = "get" + StringUtils.capitalize(baseName);
			Method method = instance.getClass().getMethod(methodName, null);
			
			Class<?> returnType = method.getReturnType();
			
			if(CdmBase.class.isAssignableFrom(returnType)){
				CdmBase resultInstance = (CdmBase) method.invoke(instance, null);
				modelAndView.addObject(resultInstance);
			}
			else if(Collection.class.isAssignableFrom(returnType) || Map.class.isAssignableFrom(returnType)){
				// TODO
				logger.warn("Collections or Maps not implemented yet.");
			}else{
				HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
			}
		} catch (SecurityException e) {
			logger.error("SecurityException: ", e);
			HttpStatusMessage.INTERNAL_ERROR.send(response);
		} catch (NoSuchMethodException e) {
			HttpStatusMessage.PROPERTY_NOT_FOUND.send(response);
		} catch (IllegalArgumentException e) {
			HttpStatusMessage.PROPERTY_NOT_FOUND.send(response);
		} catch (IllegalAccessException e) {
			HttpStatusMessage.PROPERTY_NOT_FOUND.send(response);
		} catch (InvocationTargetException e) {
			HttpStatusMessage.PROPERTY_NOT_FOUND.send(response);
		}
		
		return modelAndView;
	}
	
	
	  /* TODO implement
	   
	  private Validator validator;
	  
	  private javax.validation.Validator javaxValidator;
	
	  @RequestMapping(method = RequestMethod.PUT, headers="content-type=multipart/form-data")
	  public T doPutForm(@PathVariable(value = "uuid") UUID uuid, @ModelAttribute("object") T object, BindingResult result) {
		  object.setUuid(uuid);
	      validator.validate(object, result);
	      if (result.hasErrors()) {
	      	throw new Error();
	            // set http status code depending upon what happened, possibly return
	            // the put object and errors so that they can be rendered into a suitable error response
	      } else {
	         // requires merging detached object ?gilead?
	         service.save(object);
	      }
	        
	        return object;
	  }
	  
	  @RequestMapping(method = RequestMethod.PUT, headers="content-type=text/json")
	  public T doPutJSON(@PathVariable(value = "uuid") UUID uuid, @RequestBody String jsonMessage) {
		  JSONObject jsonObject = JSONObject.fromObject(jsonMessage);
		  T object = (T)JSONObject.toBean(jsonObject, this.getClass());
		  

		  Set<ConstraintViolation<T>> constraintViolations = javaxValidator.validate(object);
	        if (!constraintViolations.isEmpty()) {
	        	throw new Error();
	                // set http status code depending upon what happened, possibly return
	            // the put object and errors so that they can be rendered into a suitable error response
	        } else {
	          // requires merging detached object ?gilead?
	          service.save(object);
	        }
	        
	        return object;
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
