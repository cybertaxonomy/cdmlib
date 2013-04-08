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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author ben.clark
 * @author a.kohlbecker
 *
 */
public class ContentNegociationHandlerInterceptor extends HandlerInterceptorAdapter {
	
	private String defaultExtension;
	
	public static Pattern VIEW_WITH_SUFFIX_PATTERN;
	
	static {
		VIEW_WITH_SUFFIX_PATTERN = Pattern.compile("^.+\\.\\w+$");
	}
	
	private Map<ContentType,String> contentTypeToViewNameMapping;
	
	public void setMapping(Map<String,String> mapping) throws ParseException {
		this.contentTypeToViewNameMapping = new HashMap<ContentType,String>();
		
		for(String cType : mapping.keySet()) {
			ContentType key = new ContentType(cType);
			contentTypeToViewNameMapping.put(key, mapping.get(cType));
		}
	}
	
	public void setDefaultExtension(String defaultExtension)  {
		this.defaultExtension = defaultExtension;
	}
	
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		if(modelAndView != null) {
		    Matcher matcher = VIEW_WITH_SUFFIX_PATTERN.matcher(request.getServletPath());
		    if(!matcher.matches()) {
    		    ContentType bestMatch = getBestContentType(request.getHeader("Accept"), contentTypeToViewNameMapping.keySet());
		        String viewName = modelAndView.getViewName();
		        
		        if(bestMatch == null) {
	    		    modelAndView.setViewName(viewName + "." + this.defaultExtension);
		        } else {
			        modelAndView.setViewName(viewName + "." + contentTypeToViewNameMapping.get(bestMatch));
		        }
	       } else {
	    	   // remove trailing slash
	    	   modelAndView.setViewName(request.getServletPath().substring(1));
	       }
		}
	}

	/**
	 * Determine the best content type from a list of supported content types 
	 * based upon their q values. Converted from the gdata api method to work with
	 * javax.mail.internet.ContentType instead
	 * @param acceptHeader
	 * @param supportedContentTypes
	 * @return
	 */
	public ContentType getBestContentType(String acceptHeader,  Set<ContentType> supportedContentTypes) {

		    if (acceptHeader == null) {
		      return null;
		    }

		    // iterate over all of the accepted content types to find the best match
		    float bestQ = 0;
		    ContentType bestContentType = null;
		    String[] acceptedTypes = acceptHeader.split(",");
		    for (String acceptedTypeString : acceptedTypes) {

		      // create the content type object
		      ContentType acceptedContentType;
		      try {
		        acceptedContentType = new ContentType(acceptedTypeString.trim());
		      } catch (ParseException e) {
		    	  continue;
			  }

		      // parse the "q" value (default of 1)
		      float curQ = 1;
		      try {
		        String qAttr = acceptedContentType.getParameter("q");
		        if (qAttr != null) {
		          float qValue = Float.valueOf(qAttr);
		          if (qValue <= 0 || qValue > 1) {
		            continue;
		          }
		          curQ = qValue;
		        }
		      } catch (NumberFormatException ex) {
		        // ignore exception
		        continue;
		      }

		      // only check it if it's at least as good ("q") as the best one so far
		      if (curQ < bestQ) {
		        continue;
		      }

		      /* iterate over the actual content types in order to find the best match
		      to the current accepted content type */
		      for (ContentType supportedContentType : supportedContentTypes) {

		        /* if the "q" value is the same as the current best, only check for
		        better content types */
		        if (curQ == bestQ && bestContentType == supportedContentType) {
		          break;
		        }

		        /* check if the accepted content type matches the current actual
		        content type */
		        if (supportedContentType.match(acceptedContentType)) {
		          bestContentType = supportedContentType;
		          bestQ = curQ;
		          break;
		        }
		      }
		    }

		    // if found an acceptable content type, return the best one
		    if (bestQ != 0) {
		      return bestContentType;
		    }

		    // Return null if no match
		    return null;
		  }

}
