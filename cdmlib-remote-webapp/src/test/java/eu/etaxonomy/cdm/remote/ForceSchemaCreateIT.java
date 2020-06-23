/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote;

import org.junit.Test;

/**
 * @author a.kohlbecker
 * @date May 18, 2020
 *
 */
public class ForceSchemaCreateIT extends WebServiceTestBase {

    @Test
    public void checkInstanceIsOnline(){
        String response = httpGetJson("metadata", null);
        logger.debug("response: " + response);
        assertTrue(response.contains("DB_SCHEMA_VERSION"));
        assertTrue(response.contains("DB_CREATE_DATE"));
        // TODO check for DB_CREATE_DATE value no older than 5 minutes
    }

}
