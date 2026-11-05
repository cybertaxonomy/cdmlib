/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * @author andreabee90
 * @since 30.05.2023
 */
public class NameMatchingUtilsTest {

	@Test
	public void removeExtraElements() {
		String name = "Quercus cf. robur";
		assertEquals("QUERCUS ROBUR", NameMatchingUtils.removeExtraElements(name));
		name = "Quercus (something) robur";
		assertEquals("QUERCUS ROBUR", NameMatchingUtils.removeExtraElements(name));
		name = "Quercus (something) cf. robur";
		assertEquals("QUERCUS ROBUR", NameMatchingUtils.removeExtraElements(name));
		name = "Quercus (cf.) robur";
		assertEquals("QUERCUS ROBUR", NameMatchingUtils.removeExtraElements(name));
	}


    @Test
    public void testDeleteEmptySpaces() {
        String name = "  Quercus  robur ";
        assertEquals("Quercus robur", NameMatchingUtils.deleteEmptySpaces(name));

    }

    @Test
    public void testReplaceSpecialCharacters() {
        String name = "áåâãàêèëôõøòóöìíîïüûúùñç";
        assertEquals("AAAAAEEEOOOOOOIIIIUUUUNC", NameMatchingUtils.replaceSpecialCharacters(name));
    }

    @Test
    public void testListToUpperCase() {
        List <String> testList = new ArrayList<>();
        testList.add("name oNe");
        testList.add("nAmE 2");
        assertEquals("NAME ONE", NameMatchingUtils.listToUpperCase(testList).get(0));
        assertEquals("NAME 2", NameMatchingUtils.listToUpperCase(testList).get(1));
    }

    @Test
    public void testReplaceInitialCharacter() {

        String name = "euphorbia";
        assertEquals("UPHORBIA", NameMatchingUtils.replaceInitialCharacter(name));
        name = "Cnemidia";
        assertEquals("NEMIDIA", NameMatchingUtils.replaceInitialCharacter(name));
        name = "Gnaphalium";
        assertEquals("NAPHALIUM", NameMatchingUtils.replaceInitialCharacter(name));
        name = "Philodendron";
        assertEquals("FILODENDRON", NameMatchingUtils.replaceInitialCharacter(name));
        name = "Tsuga";
        assertEquals("SUGA", NameMatchingUtils.replaceInitialCharacter(name));
        name = "Czerniaevia";
        assertEquals("CERNIAEVIA", NameMatchingUtils.replaceInitialCharacter(name));
    }

    @Test
    public void testSoundalike() {
        String name = "ae ia oe oi sc";
        assertEquals("E A I A S", NameMatchingUtils.soundalike(name));
    }

    @Test
    public void testRemoveDuplicate() {
        String name = "thiiss iss aa striiiing with duupliccaaaatess";
        assertEquals("this is a string with duplicates", NameMatchingUtils.removeDuplicate(name));
    }

    @Test
    public void testReplacerGenderEnding() {
    	String name = "Qas";
    	assertEquals("QA", NameMatchingUtils.replaceGenderEnding(name));
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
