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
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import eu.etaxonomy.cdm.remote.config.SwaggerGroupsConfig;
import eu.etaxonomy.cdm.remote.staticSwagger.StaticSwaggerApiDoc;

/**
 * The resources produced by this tests are delivered by the {@link StaticSwaggerApiDoc} controller.
 *
 * @author a.kohlbecker
 * @since Mar 3, 2014
 */
public class SwaggerStaticIT extends WebServiceTestBase {

    public static final Logger logger = Logger.getLogger(SwaggerStaticIT.class);

    private String[] swaggerResourcesPaths = new String[]{"", "/configuration/ui", "/configuration/security" };


    @Test
    public void fetchSwaggerResources() {

        String swagger2Endpoint= "/swagger-resources";

        String staticResourcesFolder = "./target/classes/"+ StaticSwaggerApiDoc.SWAGGER_STATIC + "/swagger-resources";

        logger.info("clearing old content ...");
        FileUtils.deleteQuietly(new File(staticResourcesFolder));
        FileUtils.deleteQuietly(new File(staticResourcesFolder + StaticSwaggerApiDoc.JSON));

        staticResourcesFolder = staticResourcesFolder.replace("/", File.separator);

        for(String path : swaggerResourcesPaths){
            String resourcePath = swagger2Endpoint + path;
            logger.info("fetching swagger-resources file from " + resourcePath);
            String response =  httpGetJson(resourcePath, null);
            try {
                File targetFile = new File(staticResourcesFolder + path + StaticSwaggerApiDoc.JSON);
                new File(targetFile.getParent()).mkdirs();
                FileUtils.write(targetFile, response, Charset.defaultCharset());
                logger.info(response.length() + " characters of swagger-resources written to " + targetFile.getAbsolutePath());
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    @Test
    public void fetchSwaggerGroups(){

        String swagger2Endpoint= "/v2/api-docs";
        String staticApiDocFolder = "./target/classes/"+ StaticSwaggerApiDoc.SWAGGER_STATIC + "/api-docs/";

        staticApiDocFolder = staticApiDocFolder.replace("/", File.separator);

        for(SwaggerGroupsConfig group : SwaggerGroupsConfig.values()) {
            logger.info(group.groupName());
            String response =  httpGetJson(swagger2Endpoint, "group=" + group.groupName());
            response = response.replaceAll(",\"host\":\"([^\"]*)", ",\"host\":\"" + StaticSwaggerApiDoc.HOST);
            response = response.replaceAll(",\"basePath\":\"([^\"]*)", ",\"basePath\":\"" + StaticSwaggerApiDoc.BASE_PATH);
            try {
                FileUtils.write(new File(staticApiDocFolder + group.name() + StaticSwaggerApiDoc.JSON), response, Charset.defaultCharset());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

//        File pwd = new File("pom.xml");
//        System.err.println(pwd.getAbsolutePath());
    }
}