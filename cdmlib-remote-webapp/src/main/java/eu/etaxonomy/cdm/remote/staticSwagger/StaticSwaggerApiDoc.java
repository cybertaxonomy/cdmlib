// $Id$
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

import eu.etaxonomy.cdm.remote.SwaggerGroupsIT;
import eu.etaxonomy.cdm.remote.config.SwaggerGroupsConfig;
import eu.etaxonomy.cdm.remote.controller.HttpStatusMessage;

/**
 * This controller replaces the dynamic swagger api doc endpoint which is
 * otherwise provided by springfox-swagger2. In contrast to the original controller
 * this implementation serves static json files which contain the api documentation.
 * <p>
 * The static api doc files are created by the integration test class {@link SwaggerGroupsIT}.
 * <p>
 * For more details see {@link http://dev.e-taxonomy.eu/trac/wiki/cdmlib-remote-webappConfigurationAndBootstrapping}
 *
 * @see {@link SwaggerGroupsIT}
 * @author a.kohlbecker
 * @date Feb 22, 2016
 *
 */

@Controller
@RequestMapping(value = {"/v2/api-docs"})
public class StaticSwaggerApiDoc {

    @RequestMapping(method = RequestMethod.GET)
    public void group(
            @RequestParam(value = "group", required = true) String group,
             HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        SwaggerGroupsConfig groupConfig = SwaggerGroupsConfig.byGroupName(group);
        if(groupConfig == null) {
            HttpStatusMessage.create("Unknown swagger group name.", 400).send(response);
        }
        InputStream staticDocStream = getClass().getClassLoader().getResourceAsStream("api-docs-static/" + groupConfig.name());
        if(staticDocStream == null) {
            HttpStatusMessage.create("Static swagger api doc file for group '" + group + "' not found.", 500).send(response);
        } else {
            response.addHeader("Content-Type", "application/json;charset=utf-8");
            IOUtils.copy(staticDocStream,  response.getOutputStream());
            staticDocStream.close();
        }
    }

}
