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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import eu.etaxonomy.cdm.remote.config.SwaggerGroupsConfig;

/**
 * @author a.kohlbecker
 * @date Mar 3, 2014
 *
 */

public class SwaggerGroupsIT extends WebServiceTestBase {

    public static final Logger logger = Logger.getLogger(SwaggerGroupsIT.class);

    String swagger2Endpoint= "/v2/api-docs";

    @Test
    public void fetchSwaggerGroups(){

        String staticApiDocFolder = "./target/classes/api-docs-static/";

        staticApiDocFolder.replace("/", File.separator);


        for(SwaggerGroupsConfig group : SwaggerGroupsConfig.values()) {
            logger.info(group.groupName());
            String response =  httpGetJson(swagger2Endpoint, "group=" + group.groupName());
            try {
                FileUtils.write(new File(staticApiDocFolder + group.name()), response);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        File pwd = new File("pom.xml");


        System.err.println(pwd.getAbsolutePath());
    }

}
