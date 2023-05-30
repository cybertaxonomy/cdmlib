/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author andreabee90
 * @since 30.05.2023
 */
public class CdmUtilsBelenTest {

    @Test
    public void testDeleteEmptySpaces() {
        String name= "  Quercus  robur ";
        Assert.assertEquals("Quercus robur", CdmUtilsBelen.deleteEmptySpaces(name));

    }

    @Test
    public void testReplaceSpecialCharacters() {
        String name= "áåâãàêèëôõøòóöìíîïüûúùñç";
        Assert.assertEquals("aaaaaeeeooooooiiiiuuuunc",CdmUtilsBelen.replaceSpecialCharacters(name));
    }

    @Test
    public void testListToLowerCase() {
        List <String> testList= new ArrayList<>();
        testList.add("NAME 1");
        testList.add("nAmE 2");
        Assert.assertEquals("name 1", CdmUtilsBelen.listToLowerCase(testList).get(0));
        Assert.assertEquals("name 2", CdmUtilsBelen.listToLowerCase(testList).get(1));
    }

    @Test
    public void testSoundalike() {
        String name = "ae ia oe oi sc";
        Assert.assertEquals("e a i a s", CdmUtilsBelen.soundalike(name));
    }

    @Test
    public void testRemoveDuplicate() {
        String name = "thiiss iss aa striiiing with duupliccaaaatess";
        Assert.assertEquals("this is a string with duplicates", CdmUtilsBelen.removeDuplicate(name));
    }

    @Test
    public void testReplacerGenderEnding() {
//        String name="is";
//        String name="us";
//        String name="ys";
//        String name="es";
//        String name="im";
        String name="as";
//        String name="um";
//        String name="os";
        Assert.assertEquals("a", CdmUtilsBelen.replacerGenderEnding(name));
    }
}
