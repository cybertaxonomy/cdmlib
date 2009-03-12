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

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import com.ibm.lsid.LSIDException;

public class CdmExceptionResolver extends SimpleMappingExceptionResolver {
	public static String LSID_ERROR_CODE_HEADER = "LSID-Error-Code";

	@Override  
	protected ModelAndView doResolveException(HttpServletRequest request,  HttpServletResponse response, Object handler, Exception exception) {
		if(exception instanceof LSIDException) {
			LSIDException lsidException = (LSIDException) exception;
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.addHeader(CdmExceptionResolver.LSID_ERROR_CODE_HEADER,Integer.toString(lsidException.getErrorCode()));
		}
		return super.doResolveException(request, response, handler, exception);  
	}
}
