// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @author a.kohlbecker
 * @date 23.06.2009
 *
 */
public abstract class AbstractController {

    protected static final List<String> DEFAULT_INIT_STRATEGY = Arrays.asList(new String []{
            "$"
    });

    public static final Logger logger = Logger.getLogger(AbstractController.class);

    protected static final Integer DEFAULT_PAGE_SIZE = 30;

    protected List<String> initializationStrategy = DEFAULT_INIT_STRATEGY;

    /**
     * Set the default initialization strategy for this controller.
     *
     * @param initializationStrategy
     */
    public void setInitializationStrategy(List<String> initializationStrategy) {
        this.initializationStrategy = initializationStrategy;
    }

    /**
     * Returns the HTTP request path and query parameters as string
     *
     * @param request
     * @return request path and query parameters as string.
     */
    protected String requestPathAndQuery(HttpServletRequest request) {
        StringBuilder b = new StringBuilder();
        b.append(request.getServletPath());
        String query = request.getQueryString();
        if(query != null) {
            b.append("?").append(query);
        }
        return b.toString();
    }

}
