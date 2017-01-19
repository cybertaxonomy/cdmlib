/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.view;

import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class BaseView {

    protected Object getResponseData(Map model){
        // Retrieve data from model
        Object data = null;
        if (model!=null && model.values().size()>0){
            data = model.values().toArray()[0];
        }
        return data;
    }

    /**
     * Basic render method that may be used to render content with the cdmlib-remote api but without beeing in
     * a webapplication context.
     * @param entity
     * @param writer
     * @param request TODO
     * @param response TODO
     * @throws Exception
     */
    public abstract void render (Object entity, PrintWriter writer, String jsonpCallback, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
