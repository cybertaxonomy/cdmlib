/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author a.kohlbecker
 * @date May 18, 2020
 */
public class ForceSchemaCreateIT extends WebServiceTestBase {

    private static final Logger logger = LogManager.getLogger();

    @Test
    @Ignore //Problems with static sources (see https://dev.e-taxonomy.eu/redmine/issues/10751)
    public void checkInstanceIsOnline(){
        String response = httpGetJson("metadata", null);
        logger.debug("response: " + response);
        assertTrue(response.contains("DB_SCHEMA_VERSION"));
        assertTrue(response.contains("DB_CREATE_DATE"));
        // TODO check for DB_CREATE_DATE value no older than 5 minutes
    }
}