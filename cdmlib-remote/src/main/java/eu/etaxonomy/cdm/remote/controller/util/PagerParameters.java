/**
* Copyright (C) 2011 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller.util;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.PagerUtils;
import eu.etaxonomy.cdm.remote.controller.HttpStatusMessage;

/**
 *
 * NOTE: As the indices for objects and pages are 0-based in {@link Pager} the
 * <code>pageNumber</code> property of this class also follows this principle.
 *
 * TODO consider merging this class with {@link PagerUtils}
 *
 * @author a.kohlbecker
 * @since 22.08.2011
 *
 */
public class PagerParameters {

    private Integer pageSize;

    private Integer pageIndex;

    public static final Integer DEFAULT_PAGESIZE = 30;

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * NOTE: As the indices for objects and pages are 0-based
     * @param pageIndex
     */
    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    /**
     * NOTE: As the indices for objects and pages are 0-based
     * @return
     */
    public Integer getPageIndex() {
        return pageIndex;
    }

    public PagerParameters(Integer pageSize, Integer pageIndex) {
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
    }

    /**
     * Normalizes this <code>PagerParameters</code> according to the following
     * rules and responds with an HTTP error in case of invalid parameters.
     * <ul>
     * <li>pageIndex defaults to 0 if set to <code>NULL</code>.</li>
     * <li>pageSize defaults to {@link #DEFAULT_PAGESIZE} if set to <code>NULL</code>.</li>
     * <li>Sends {@link HTTP_BAD_REQUEST} if <code>pageIndex</code> or <code>pageSize</code> are smaller than 0.
     * </ul>
     * @param response
     * @throws IOException
     */
    public PagerParameters normalizeAndValidate(HttpServletResponse response) throws IOException{

        if(pageIndex == null){
            pageIndex = 0;
        }
        if(pageSize == null){
            pageSize = DEFAULT_PAGESIZE;
        }
        if(pageIndex < 0){
            HttpStatusMessage.create("The query parameter 'pageIndex' must not be a negative number", 400).setStatusCode(HTTP_BAD_REQUEST).send(response);
        }
        if(pageSize < 0){
            HttpStatusMessage.create("The query parameter 'pageSize' must not be a negative number", 400).setStatusCode(HTTP_BAD_REQUEST).send(response);
        }
        return this;
    }


}
