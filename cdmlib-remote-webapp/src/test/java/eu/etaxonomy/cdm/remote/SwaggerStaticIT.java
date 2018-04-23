/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import eu.etaxonomy.cdm.remote.config.SwaggerGroupsConfig;
import eu.etaxonomy.cdm.remote.staticSwagger.StaticSwaggerApiDoc;

/**
 * @author a.kohlbecker
 \* @since Mar 3, 2014
 *
 */

public class SwaggerStaticIT extends WebServiceTestBase {

    public static final Logger logger = Logger.getLogger(SwaggerStaticIT.class);


    @Test
    public void fetchSwaggerResources(){

        String swagger2Endpoint= "/swagger-resources";

        String staticResourcesFolder = "./target/classes/"+ StaticSwaggerApiDoc.SWAGGER_STATIC + "/swagger-resources";

        staticResourcesFolder.replace("/", File.separator);

        logger.info("fetching swagger-resources");
        String response =  httpGetJson(swagger2Endpoint, null);
        try {
            FileUtils.write(new File(staticResourcesFolder), response);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        File pwd = new File("pom.xml");
//        System.err.println(pwd.getAbsolutePath());
    }


    @Test
    public void fetchSwaggerGroups(){

        String swagger2Endpoint= "/v2/api-docs";


        String staticApiDocFolder = "./target/classes/"+ StaticSwaggerApiDoc.SWAGGER_STATIC + "/api-docs/";

        staticApiDocFolder.replace("/", File.separator);


        for(SwaggerGroupsConfig group : SwaggerGroupsConfig.values()) {
            logger.info(group.groupName());
            String response =  httpGetJson(swagger2Endpoint, "group=" + group.groupName());
            response = response.replaceAll(",\"host\":\"([^\"]*)", ",\"host\":\"" + StaticSwaggerApiDoc.HOST);
            response = response.replaceAll(",\"basePath\":\"([^\"]*)", ",\"basePath\":\"" + StaticSwaggerApiDoc.BASE_PATH);
            try {
                FileUtils.write(new File(staticApiDocFolder + group.name()), response);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

//        File pwd = new File("pom.xml");
//        System.err.println(pwd.getAbsolutePath());
    }

}
