/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @author a.kohlbecker
 * @since Jan 26, 2017
 *
 */
public class JsonpUtil {

    public static final Logger logger = Logger.getLogger(JsonpUtil.class);

    /**
     * @param request
     * @return
     */
    static public String readJsonpCallback(HttpServletRequest request) {
        String jsonpCallback= null;
        String queryString = request.getQueryString();
        if(queryString != null){
            String[] tokens = request.getQueryString().split("&", 0);
            String jsonpParamName = "callback";
            for (int i = 0; i < tokens.length; i++) {
                if(tokens[i].startsWith(jsonpParamName)){
                    jsonpCallback = tokens[i].substring(jsonpParamName.length() + 1);
                    break;
                }
            }
        }
        return jsonpCallback;
    }

}
