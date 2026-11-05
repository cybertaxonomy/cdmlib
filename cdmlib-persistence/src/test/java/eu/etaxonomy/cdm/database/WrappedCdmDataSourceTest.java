/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.test.integration.spring.CdmSpringIntegrationTest;

/**
 * @author a.mueller
 * @since 05.05.2015
 */
public class WrappedCdmDataSourceTest extends CdmSpringIntegrationTest {

    @Test
    @Disabled
    public void testByDatabaseMetaData() {
//        still needs to be implemented
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}