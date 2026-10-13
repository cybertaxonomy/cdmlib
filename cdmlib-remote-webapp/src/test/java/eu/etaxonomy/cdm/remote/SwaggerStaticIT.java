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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.remote.config.SwaggerGroupsConfig;
import eu.etaxonomy.cdm.remote.staticSwagger.StaticSwaggerApiDoc;

/**
 * Fetches live OpenAPI docs (springdoc) into {@code swagger-static/} for packaging
 * into the WAR and serving via {@link StaticSwaggerApiDoc} when the {@code swagger}
 * profile is not active.
 * <p>
 * Requires Jetty with {@code spring.profiles.active=remoting,swagger}.
 * Still ignored until static serving is adapted from Swagger 2 ({@code /v2/api-docs})
 * to OpenAPI 3 ({@code /v3/api-docs/{group}}).
 *
 * @author a.kohlbecker
 * @since Mar 3, 2014
 */
@Ignore("Re-enable after StaticSwaggerApiDoc is updated for OpenAPI 3 / springdoc group URLs")
public class SwaggerStaticIT extends WebServiceTestBase {

    private static final Logger logger = LogManager.getLogger();

    @Ignore
/*
    @Test
    public void fetchOpenApiGroups() {

        String openApiEndpoint = "/v3/api-docs";
        String staticApiDocFolder = "./target/classes/" + StaticSwaggerApiDoc.SWAGGER_STATIC + "/api-docs/";

        staticApiDocFolder = staticApiDocFolder.replace("/", File.separator);
        new File(staticApiDocFolder).mkdirs();

        for (SwaggerGroupsConfig group : SwaggerGroupsConfig.values()) {
            String groupPath = URLEncoder.encode(group.groupName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            String resourcePath = openApiEndpoint + "/" + groupPath;
            logger.info("fetching OpenAPI group: " + group.groupName() + " from " + resourcePath);
            String response = httpGetJson(resourcePath, null);
            try {
                FileUtils.write(new File(staticApiDocFolder + group.name() + StaticSwaggerApiDoc.JSON),
                        response, Charset.defaultCharset());
                logger.info(response.length() + " characters written for group " + group.name());
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }
    */
}
