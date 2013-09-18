// $Id$
/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.biocase;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import junit.framework.TestCase;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * @author pplitzner
 * @date 16.09.2013
 *
 */
public class BioCaseQueryServiceWrapperTest extends TestCase{

    @Test
    public void testQuery() {
        BioCaseQueryServiceWrapper queryService = new BioCaseQueryServiceWrapper();
        try {
            List<SpecimenOrObservationBase> response = queryService.query(new BioCaseQuery());
            System.out.println(response.get(0).getTitleCache());
        } catch (ClientProtocolException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } catch (URISyntaxException e) {
            fail(e.getMessage());
        }
    }
}
