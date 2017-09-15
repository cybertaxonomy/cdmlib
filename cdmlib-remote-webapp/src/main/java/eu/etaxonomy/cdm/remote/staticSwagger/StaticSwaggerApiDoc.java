/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.staticSwagger;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.remote.config.SwaggerGroupsConfig;
import eu.etaxonomy.cdm.remote.controller.HttpStatusMessage;

/**
 * This controller replaces the dynamic swagger api doc endpoint which is
 * otherwise provided by springfox-swagger2. In contrast to the original controller
 * this implementation serves static json files which contain the api documentation.
 * <p>
 * The static api doc files are created by the integration test class <code>SwaggerStaticIT</code>.
 * <p>
 * For more details see {@link http://dev.e-taxonomy.eu/trac/wiki/cdmlib-remote-webappConfigurationAndBootstrapping}
 *
 * @author a.kohlbecker
 * @date Feb 22, 2016
 *
 */

@Controller
public class StaticSwaggerApiDoc {

    public static final String SWAGGER_STATIC = "swagger-static";
    public static final String HOST = "{HOST}";
    public static final String HOST_REGEX = "\\{HOST\\}";
    public static final String BASE_PATH = "{BASE_PATH}";
    public static final String BASE_PATH_REGEX = "\\{BASE_PATH\\}";


    @RequestMapping(value = "configuration/ui", method = RequestMethod.GET)
    public void configurationUi(
            HttpServletResponse response) throws IOException {

            response.addHeader("Content-Type", "application/json;charset=utf-8");
            response.getOutputStream().write("{\"validatorUrl\":null}".getBytes());
    }

    @RequestMapping(value = "swagger-resources", method = RequestMethod.GET)
    public void swaggerResources(
             HttpServletRequest request,
            HttpServletResponse response) throws IOException {


        InputStream staticDocStream = getClass().getClassLoader().getResourceAsStream(SWAGGER_STATIC + "/swagger-resources");
        if(staticDocStream == null) {
            HttpStatusMessage.create("Static swagger-resources not found.", 500).send(response);
        } else {
            response.addHeader("Content-Type", "application/json;charset=utf-8");
            IOUtils.copy(staticDocStream,  response.getOutputStream());
            staticDocStream.close();
        }
    }

    @RequestMapping(value = "/v2/api-docs", method = RequestMethod.GET)
    public void apiDocs(
            @RequestParam(value = "group", required = true) String group,
             HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String hostValue = request.getServerName() + ":" + request.getServerPort();
        String basePathValue = request.getContextPath();

        SwaggerGroupsConfig groupConfig = SwaggerGroupsConfig.byGroupName(group);
        if(groupConfig == null) {
            HttpStatusMessage.create("Unknown swagger group name.", 400).send(response);
        }
        InputStream staticDocStream = getClass().getClassLoader().getResourceAsStream(SWAGGER_STATIC + "/api-docs/" + groupConfig.name());
        if(staticDocStream == null) {
            HttpStatusMessage.create("Static swagger api doc file for group '" + group + "' not found.", 500).send(response);
        } else {
            response.addHeader("Content-Type", "application/json;charset=utf-8");
            String staticDocText = IOUtils.toString(staticDocStream);
            staticDocText = staticDocText.replaceFirst(HOST_REGEX, hostValue);
            staticDocText = staticDocText.replaceFirst(BASE_PATH_REGEX, basePathValue);

            IOUtils.write(staticDocText, response.getOutputStream());
            staticDocStream.close();
        }
    }

}
