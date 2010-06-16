// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller.interceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import eu.etaxonomy.cdm.database.NamedContextHolder;

/**
 * @author a.kohlbecker
 *
 */
@Deprecated
public class DatasourceContextHandlerInterceptor extends HandlerInterceptorAdapter {
	private static final Logger logger = Logger.getLogger(DatasourceContextHandlerInterceptor.class);
	
	private final static Pattern basepathPattern = Pattern.compile("^/([^/]+)/.*");
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		Matcher matcher = basepathPattern.matcher(request.getServletPath());
        if (matcher.matches())
        {
        	String basepath = matcher.group(1);
        	NamedContextHolder.setContextKey(basepath);
        	logger.info("datasource context set to: " + basepath);
        }
        return true;
	}



	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		NamedContextHolder.clearContextKey();
		logger.info("datasource context cleared");
		
	}
}
