// $Id: TaxonController.java 5473 2009-03-25 13:42:07Z a.kohlbecker $
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

import eu.etaxonomy.cdm.remote.controller.HttpStatusMessage;

/**
 * @author a.kohlbecker
 * @date 22.08.2011
 *
 */
public class PagerParameters {

    private Integer pageSize;

    private Integer pageNumber;

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public PagerParameters(Integer pageSize, Integer pageNumber) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
    }

    public void normalizeAndValidate(HttpServletResponse response) throws IOException{

        if(pageNumber == null){
            pageNumber = 0;
        }
        if(pageSize == null){
            pageSize = 0;
        }
        if(pageNumber < 0){
            HttpStatusMessage.fromString("The query parameter 'pageNumber' must not be a negative number").setStatusCode(HTTP_BAD_REQUEST).send(response);
        }
        if(pageSize < 0){
            HttpStatusMessage.fromString("The query parameter 'pageSize' must not be a negative number").setStatusCode(HTTP_BAD_REQUEST).send(response);
        }
    }


}
