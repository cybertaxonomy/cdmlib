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

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;

/**
 * @author a.kohlbecker
 * @date 23.06.2009
 *
 * @param <T>
 * @param <SERVICE>
 */
public abstract class AbstractController<T extends CdmBase, SERVICE extends IService<T>> {

    protected static final List<String> DEFAULT_INIT_STRATEGY = Arrays.asList(new String []{
            "$"
    });

    public static final Logger logger = Logger.getLogger(AbstractController.class);

    protected SERVICE service;

    public abstract void setService(SERVICE service);

    protected static final Integer DEFAULT_PAGE_SIZE = PagerParameters.DEFAULT_PAGESIZE;

    /**
     * Default thread priority for long term processes which are running in
     * separate threads. These batch processes are usually monitored with the
     * {@link ProgressMonitorController}. This value must be lower than
     * {@link Thread#NORM_PRIORITY}
     */
    public static final int DEFAULT_BATCH_THREAD_PRIORITY = 3;

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
        if(request == null) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        b.append(request.getRequestURI());
        String query = request.getQueryString();
        if(query != null) {
            b.append("?").append(query);
        }
        return b.toString();
    }

}
