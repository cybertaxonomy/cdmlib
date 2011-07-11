// $Id$
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author a.kohlbecker
 * @date 20.07.2010
 *
 */
public abstract class AbstractWebApplicationConfigurer {

    private static final String ATTRIBUTE_ERROR_MESSAGES = "cdm.errorMessages";

	public static final Logger logger = Logger.getLogger(AbstractWebApplicationConfigurer.class);
	
	WebApplicationContext webApplicationContext;

	
	@Autowired
	public void setApplicationContext(ApplicationContext applicationContext){

		if(WebApplicationContext.class.isAssignableFrom(applicationContext.getClass())) {
			this.webApplicationContext = (WebApplicationContext)applicationContext;
		} else {
			logger.error("The " + this.getClass().getSimpleName() + " only can be used within a WebApplicationContext");
		}
	}

	/**
	 * Find a property in the ServletContext if not found search in a second
	 * step in the environment variables of the OS
	 * 
	 * @param property
	 * @param required
	 * @return
	 */
	protected String findProperty(String property, boolean required) {
		// 1. look for the property in the ServletContext
		Object obj = webApplicationContext.getServletContext().getAttribute(property);
		String value = (String)obj;
		// 2. look for the property in environment variables of the OS
		if(value == null){
			value = System.getProperty(property);
		}
		if(value == null && required){
			logger.error("property {" + property + "} not found.");
			logger.error("--> This property can be set in two ways:");
			logger.error("--> 		1. as attribute to the ServletContext");
			logger.error("--> 		2. as system property e.g. -D" + property);
			logger.error("Stopping application ...");
			System.exit(-1);
		}
		return value;
	}

	protected void addErrorMessageToServletContextAttributes(String errorMessage) {
		Object o = webApplicationContext.getServletContext().getAttribute(ATTRIBUTE_ERROR_MESSAGES);
		List<String> messages;
		if(o != null  && o instanceof List<?>){
			messages = (List<String>) o;
		} else {
			messages = new ArrayList<String>();
		}
		messages.add(errorMessage);
		webApplicationContext.getServletContext().setAttribute(ATTRIBUTE_ERROR_MESSAGES, messages);
	}
	
   

}