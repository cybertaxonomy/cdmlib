// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.ibm.lsid.LSIDException;

public class CdmExceptionResolver extends SimpleMappingExceptionResolver {
	
	public static final Logger logger = Logger.getLogger(CdmExceptionResolver.class);
	
	
	
	public static String LSID_ERROR_CODE_HEADER = "LSID-Error-Code";

	@Override  
	protected ModelAndView doResolveException(HttpServletRequest request,  HttpServletResponse response, Object handler, Exception exception) {
			
		ModelAndView mv = new ModelAndView("error");
		
//			if(exception instanceof IllegalArgumentException){
//				try {
//					if(exception.getMessage().equals(BaseController.MSG_INVALID_UUID)){
//						response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
//					} else if(exception.getMessage().equals(BaseController.MSG_UUID_MISSING)){
//						response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
//					} else if(exception.getMessage().equals(BaseController.MSG_UUID_NOT_FOUND)){
//						response.sendError(HttpServletResponse.SC_NOT_FOUND, exception.getMessage());
//					}
//				} catch (IOException e) {
//					logger.error(e.getMessage(), e);
//				}
//			}
		
		if(exception instanceof LSIDException) {
			LSIDException lsidException = (LSIDException) exception;
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.addHeader(CdmExceptionResolver.LSID_ERROR_CODE_HEADER,Integer.toString(lsidException.getErrorCode()));
			//return mv;
		}
		
		return super.doResolveException(request, response, handler, exception); 
	}
}
