// $Id$
/**
* Copyright (C) 2014 EDIT
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
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

/**
 * @author a.kohlbecker
 * @date Mar 3, 2014
 *
 */

public class ClassificationIT extends Assert {

    public static final Logger logger = Logger.getLogger(ClassificationIT.class);

    int port = 9080;
    String baseUri = "http://localhost:9080/";
    RestTemplate template = new RestTemplate();

    @Before
    public void setUp() {
        if(System.getProperty("sun.java.command") != null && System.getProperty("sun.java.command").startsWith("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner")){
            port = 8080;
            logger.info(" setUp() : \n" +
                        "==================================================================\n" +
                        " Eclipse ide detected, expecting cdm remote instance at port 8080 \n" +
                        "==================================================================");
        };
        baseUri = String.format("http://localhost:%1$d/", port);
        logger.info("cdm remote instance url: " + baseUri);
    }


    @Test
    public void checkInstanceIsOnline(){
        String response = template.getForObject(baseUri + "classification.json", String.class);
        assertTrue(response.contains("My Classification"));
    }

}
