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
import java.io.InputStream;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.apache.http.client.ClientProtocolException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

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
            InputStream response = queryService.query();
            SAXBuilder builder = new SAXBuilder();
            Document xmlResponse = builder.build(response);
            System.out.println(new XMLOutputter().outputString(xmlResponse));
        } catch (ClientProtocolException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        } catch (URISyntaxException e) {
            fail(e.getMessage());
        } catch (JDOMException e) {
            fail(e.getMessage());
        }
    }
}
