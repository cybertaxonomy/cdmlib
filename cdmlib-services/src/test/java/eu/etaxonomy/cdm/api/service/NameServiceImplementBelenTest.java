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


        String name = "euphorbia";
        Assert.assertEquals("uphorbia", NameServiceImplementBelen.replaceInitialCharacter(name));
        name = "Cnemidia";
        Assert.assertEquals("nemidia", NameServiceImplementBelen.replaceInitialCharacter(name));
        name = "Gnaphalium";
        Assert.assertEquals("naphalium", NameServiceImplementBelen.replaceInitialCharacter(name));
        name = "Philodendron";
        Assert.assertEquals("filodendron", NameServiceImplementBelen.replaceInitialCharacter(name));
        name = "Tsuga";
        Assert.assertEquals("suga", NameServiceImplementBelen.replaceInitialCharacter(name));
        name = "Czerniaevia";
        Assert.assertEquals("cerniaevia", NameServiceImplementBelen.replaceInitialCharacter(name));
    }

    @Test
    public void testTrimCommonChar() {

        String query ="Nectandra";
        String document = "Nectalisma";

        Assert.assertEquals("ndr", NameServiceImplementBelen.trimCommonChar(query, document).split(" ")[0]);
        Assert.assertEquals("lism", NameServiceImplementBelen.trimCommonChar(query, document).split(" ")[1]);

        Assert.assertEquals("Equal input should return empty result",
                "", NameServiceImplementBelen.trimCommonChar(query, query) );
    }
}