/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 05.01.2021
 */
public class UriTest {

    @Test
    public void test() {
        try {
            //see example in #9111
            URI uri = new URI("https://bioone.org/journals/Cactus-and-Succulent-Journal/volume-78/issue-2/0007-9367(2006)78[66:FKASLA]2.0.CO;2/Finders-Keepers-and-some-Lavranian-Adjustments-in-Mesembryanthema/10.2985/0007-9367(2006)78[66:FKASLA]2.0.CO;2.full");
            Assert.assertEquals(
                    "/journals/Cactus-and-Succulent-Journal/volume-78/issue-2/0007-9367(2006)78[66:FKASLA]2.0.CO;2/Finders-Keepers-and-some-Lavranian-Adjustments-in-Mesembryanthema/10.2985/0007-9367(2006)78[66:FKASLA]2.0.CO;2.full",
                    uri.getPath());

        } catch (URISyntaxException e) {
            Assert.fail("Parsing example URI from #9111 should not throw exception");
        }

        try {
            //see example in #9111
            URI uri = new URI("http:\\www.fail.de");
            Assert.fail("Using backslash in URI instead of slash should fail");
        } catch (URISyntaxException e) {
            //OK
        }

    }
    
    @Test
    public void testFragment() {
        try {
            //see example in #9111
            URI uri = new URI("https://max:muster@www.example.com:8080/index.html?p1=A&p2=B#ressource");
            Assert.assertEquals(
                    "ressource",
                    uri.getFragment());

        } catch (URISyntaxException e) {
            Assert.fail("Parsing example URI should find fragment");
        }

        try {
            //see example in #9111
            URI uri = new URI("http:\\www.fail.de");
            Assert.fail("Using backslash in URI instead of slash should fail");
        } catch (URISyntaxException e) {
            //OK
        }

    }

}
