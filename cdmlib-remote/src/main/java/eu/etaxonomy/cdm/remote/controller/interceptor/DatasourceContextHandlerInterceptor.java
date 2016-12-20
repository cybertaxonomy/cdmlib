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
 * @deprecated<b>NOTICE:</b> 
 * <em>This class is related to the switchable database infrastructure which allows to serve 
 * multiple databases with only a single instance of the cdm-remote-webapp. 
 * This concept however is deprecated due to several problems of which the most severe is the term loading issue.
 * This class should however not deleted since we once might wish to switch back to this concept when we are 
 * able to deal with the implicated issues. 
 * 
 * See http://dev.e-taxonomy.eu/trac/wiki/CdmServerSwitchableDataSources for more information.</em>
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
