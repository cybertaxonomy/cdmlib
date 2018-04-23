/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.web.client.RestTemplate;


/**
 * @author a.kohlbecker
 * @since Feb 22, 2016
 *
 */
public class WebServiceTestBase extends Assert {

    public static final Logger logger = Logger.getLogger(WebServiceTestBase.class);


    private int port = 9180;
    private String baseUri = "";

    RestTemplate template = new RestTemplate();

    @Before
    public void setUp() {
        if(System.getProperty("sun.java.command") != null && System.getProperty("sun.java.command").startsWith("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner")){
            port = 8080;
            logger.info(" setUp() : \n" +
                        "==================================================================\n" +
                        " Eclipse ide detected, expecting cdm remote instance at port 8080 \n" +
                        "==================================================================");
        }
        baseUri = String.format("http://localhost:%1$d", port);
        logger.info("cdm remote instance url: " + baseUri);
    }

    public String getBaseUri() {
        return baseUri;
    }

    public String httpGetJson(String endPoint, String query) {
        StringBuilder uri = new StringBuilder(baseUri);
        if(endPoint != null) {
            uri.append(endPoint);
        }
        if(query != null) {
            uri.append("?").append(query);
        }
        logger.debug("httpGetJson: " + uri.toString());
        return template.getForObject(uri.toString(), String.class);
    }

}
