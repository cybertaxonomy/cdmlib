/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

/**
 * @author a.kohlbecker
 *
 */
public class LocaleContextHandlerInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private final LocaleContext localeContext = null;

	@SuppressWarnings("unchecked")
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
	    localeContext.setLocales(request.getLocales());
        return true;
	}

}
