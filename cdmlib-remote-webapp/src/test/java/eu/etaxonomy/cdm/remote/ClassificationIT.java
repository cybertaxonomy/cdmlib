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
import org.junit.Test;

/**
 * @author a.kohlbecker
 * @since Mar 3, 2014
 *
 */

public class ClassificationIT extends WebServiceTestBase  {

    public static final Logger logger = Logger.getLogger(ClassificationIT.class);

    @Test
    public void checkInstanceIsOnline(){
        String response = httpGetJson("classification.json", null);
        assertTrue(response.contains("My Classification"));
    }

}
