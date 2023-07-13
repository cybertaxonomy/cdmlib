/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author andreabee90
 * @since 30.05.2023
 */
public class NameMatchingUtilsTest {

	@Test
	public void removeExtraElements() {
		String name = "Quercus cf. robur";
		Assert.assertEquals("QUERCUS ROBUR", NameMatchingUtils.removeExtraElements(name));
		name = "Quercus (something) robur";
		Assert.assertEquals("QUERCUS ROBUR", NameMatchingUtils.removeExtraElements(name));
		name = "Quercus (something) cf. robur";
		Assert.assertEquals("QUERCUS ROBUR", NameMatchingUtils.removeExtraElements(name));
		name = "Quercus (cf.) robur";
		Assert.assertEquals("QUERCUS ROBUR", NameMatchingUtils.removeExtraElements(name));
	}
	
	
    @Test
    public void testDeleteEmptySpaces() {
        String name = "  Quercus  robur ";
        Assert.assertEquals("Quercus robur", NameMatchingUtils.deleteEmptySpaces(name));

    }

    @Test
    public void testReplaceSpecialCharacters() {
        String name = "áåâãàêèëôõøòóöìíîïüûúùñç";
        Assert.assertEquals("AAAAAEEEOOOOOOIIIIUUUUNC",NameMatchingUtils.replaceSpecialCharacters(name));
    }

    @Test
    public void testListToUpperCase() {
        List <String> testList = new ArrayList<>();
        testList.add("NAME 1");
        testList.add("nAmE 2");
        Assert.assertEquals("NAME 1", NameMatchingUtils.listToUpperCase(testList).get(0));
        Assert.assertEquals("NAME 2", NameMatchingUtils.listToUpperCase(testList).get(1));
    }

    @Test
    public void testReplaceInitialCharacter() {

        String name = "euphorbia";
        Assert.assertEquals("UPHORBIA", NameMatchingUtils.replaceInitialCharacter(name));
        name = "Cnemidia";
        Assert.assertEquals("NEMIDIA", NameMatchingUtils.replaceInitialCharacter(name));
        name = "Gnaphalium";
        Assert.assertEquals("NAPHALIUM", NameMatchingUtils.replaceInitialCharacter(name));
        name = "Philodendron";
        Assert.assertEquals("FILODENDRON", NameMatchingUtils.replaceInitialCharacter(name));
        name = "Tsuga";
        Assert.assertEquals("SUGA", NameMatchingUtils.replaceInitialCharacter(name));
        name = "Czerniaevia";
        Assert.assertEquals("CERNIAEVIA", NameMatchingUtils.replaceInitialCharacter(name));
    }
    
    @Test
    public void testSoundalike() {
        String name = "ae ia oe oi sc";
        Assert.assertEquals("E A I A S", NameMatchingUtils.soundalike(name));
    }

    @Test
    public void testRemoveDuplicate() {
        String name = "thiiss iss aa striiiing with duupliccaaaatess";
        Assert.assertEquals("this is a string with duplicates", NameMatchingUtils.removeDuplicate(name));
    }

    @Test
    public void testReplacerGenderEnding() {
    	String name="Qas";
    	Assert.assertEquals("QA", NameMatchingUtils.replaceGenderEnding(name));
//        String name="is";
//        String name="us";
//        String name="ys";
//        String name="es";
//        String name="im";
//        String name="um";
//        String name="os";
    }
    
    @Test
    public void testmodifiedDamerauLevenshteinDistance() {
       	
    	int distance = NameMatchingUtils.modifiedDamerauLevenshteinDistance("Gynoxys asterotricha", "Gynxya asrerotciha");
    	assertEquals(5,distance);
    	distance = NameMatchingUtils.modifiedDamerauLevenshteinDistance("Gynoxys asterotricha", "Gynxsa axrerotciha");
    	assertEquals(7,distance);
    	distance = NameMatchingUtils.modifiedDamerauLevenshteinDistance("Gynoxys asterotricha", "Gynxyas asrerotciha");
    	assertEquals(5,distance);
    	distance = NameMatchingUtils.modifiedDamerauLevenshteinDistance("Gynoxys asterotricha", "Gynoxya asterotricha");
    	assertEquals(1,distance);
    	distance = NameMatchingUtils.modifiedDamerauLevenshteinDistance("Gynoxys asterotricha", "Gynoxys asterotricha");
    	assertEquals(0,distance);
    }   
}