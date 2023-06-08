/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author andreabee90
 * @since 30.05.2023
 */
public class NameServiceImplementBelenTest {

    @Test
    public void testReplaceInitialCharacter() {
        NameServiceImplementBelen test=new NameServiceImplementBelen();

        String name = "euphorbia";
        Assert.assertEquals("uphorbia", test.replaceInitialCharacter(name));
        name = "Cnemidia";
        Assert.assertEquals("nemidia", test.replaceInitialCharacter(name));
        name = "Gnaphalium";
        Assert.assertEquals("naphalium", test.replaceInitialCharacter(name));
        name = "Philodendron";
        Assert.assertEquals("filodendron", test.replaceInitialCharacter(name));
        name = "Tsuga";
        Assert.assertEquals("suga", test.replaceInitialCharacter(name));
        name = "Czerniaevia";
        Assert.assertEquals("cerniaevia", test.replaceInitialCharacter(name));
    }

    @Test
    public void testTrimCommonChar() {
        NameServiceImplementBelen test=new NameServiceImplementBelen();
        String query ="this is a query string";
        String document = "this is a database string";

        Assert.assertEquals("query", test.trimCommonChar(query, document).get(0).toString().split(" ")[0]);
        Assert.assertEquals("database", test.trimCommonChar(query, document).get(0).toString().split(" ")[1]);
    }
}
