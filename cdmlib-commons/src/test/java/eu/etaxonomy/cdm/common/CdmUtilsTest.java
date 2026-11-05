/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

/**
 * @author a.mueller
 * @since 22.01.2008
 */
public class CdmUtilsTest {

	private static final Logger logger = LogManager.getLogger();

/************************** TESTS ****************************************/

	@Test
	public void testFindLibrary() {
		logger.debug("{}", CdmUtils.findLibrary(CdmUtils.class));

		String library = CdmUtils.findLibrary(CdmUtils.class);
		String endOfLibrary = "target/classes/eu/etaxonomy/cdm/common/CdmUtils.class";
		String libraryContains = "/cdmlib-commons/";

		assertTrue(library.endsWith(endOfLibrary));
		assertTrue(library.contains(libraryContains));
	}

	/**
	 * This is a default test for fast running any simple test. It can be overriden and ignored whenever needed.
	 */
	@Test
	public void testAny(){
		String str = "Noms vernaculaires:";
		if (! str.matches("Nom(s)? vernaculaire(s)?\\:")){
			System.out.println("NO");
		}
	}

    @Test
    public void testEqualsIgnoreWS(){
        String str1 = null;
        String str2 = null;
        assertTrue(CdmUtils.equalsIgnoreWS(str1, str2));

        str2 = "Any ";
        assertFalse(CdmUtils.equalsIgnoreWS(str1, str2));

        str1 = "Any ";
        assertTrue(CdmUtils.equalsIgnoreWS(str1, str2));

        str1 = "An y eer";
        str2 = "A nye er";
        assertTrue(CdmUtils.equalsIgnoreWS(str1, str2));

        str1 = "An y eer";
        str2 = "A nyfffe er";
        assertFalse(CdmUtils.equalsIgnoreWS(str1, str2));
    }

    @Test
    public void testquoteRegExWithWildcard(){
        String regExBase = ".(*$[ms^";
        String regEx = CdmUtils.quoteRegExWithWildcard(regExBase);
        assertEquals("\\Q.(\\E.*\\Q$[ms^\\E", regEx);
        boolean matches = ".(*$[ms^".matches(regEx);
        assertTrue(matches);
        matches = ".(aaaaaa$[ms^".matches(regEx);
        assertTrue(matches);
        matches = "b(aaaaaa$[ms^".matches(regEx);
        assertFalse(matches);

        regEx = CdmUtils.quoteRegExWithWildcard("*abc*");
        assertEquals(".*\\Qabc\\E.*", regEx);
        assertTrue("abc".matches(regEx));
        assertTrue("a80/(--e*wabc?äe".matches(regEx));
    }

    /**
     * This test can be used for functional testing of any task but should
     * never be committed when failing.
     */
    @Test
    public void testSomething(){
        String str = ".(*$[ms^";
        String patQuote = Pattern.quote("str");
//        System.out.println(patQuote);
//        String matchQuote = Matcher.quoteReplacement(str);
//        System.out.println(matchQuote);
//        System.out.println(CdmUtils.quoteRegExWithWildcard(str));
    }

    @Test
    public void testGetReadableResourceStream() {
        String resourceFileName = "MUST-EXIST.txt";
        try {
            InputStream inputStream = CdmUtils.getReadableResourceStream(resourceFileName);
            assertNotNull(inputStream);
        } catch (IOException e) {
            fail("IOException");
        }
    }

    @Test
    public void testConcat(){
        String str1 = "Str1";
        String str2 = "Str2";
        String str3 = "Str3";
        assertEquals(CdmUtils.concat(";", str1, str2), "Str1;Str2");
        assertEquals(CdmUtils.concat(";", null, str2), "Str2");
        assertEquals(CdmUtils.concat(";", str1, null), "Str1");
        assertNull(CdmUtils.concat(";", null, null));
        assertEquals(CdmUtils.concat(";", str1, str2, str3), "Str1;Str2;Str3");
        assertEquals(CdmUtils.concat(": ", str1, str2), "Str1: Str2");
        assertEquals(CdmUtils.concat(";", "", str2, str3), "Str2;Str3");
        assertEquals(CdmUtils.concat(";", str1, "", str3), "Str1;Str3");
        assertEquals(CdmUtils.concat(";", str1, " ", str3), "Str1; ;Str3");
    }

    @Test
    public void testConcatWithDedup(){
        String str1 = "Str1";
        String str2 = "Str2";

        //same as without deduplication
        assertEquals(CdmUtils.concatWithDedup(";", str1, str2), "Str1;Str2");
        assertEquals(CdmUtils.concatWithDedup(";", null, str2), "Str2");
        assertEquals(CdmUtils.concatWithDedup(";", str1, null), "Str1");
        assertNull(CdmUtils.concatWithDedup(";", null, null));
        assertEquals(CdmUtils.concatWithDedup(";", str1, "", str2), "Str1;Str2");
        assertEquals(CdmUtils.concatWithDedup(";", str1, " ", str2), "Str1; ;Str2");

        //with deduplication
        String str3 = "Str1";
        assertEquals(CdmUtils.concatWithDedup(";", str1, str2, str3), "Str1;Str2");
        assertEquals(CdmUtils.concatWithDedup(";", "", str2, str3), "Str2;Str1");

        str2 = "Str1";
        assertEquals(CdmUtils.concatWithDedup(";", str1, str2, str3), "Str1");

        str2 = "";
        assertEquals(CdmUtils.concatWithDedup(";", str1, str2, str3), "Str1");
    }

    @Test
    public void testMergeMaps(){
        Map<Integer,String> map1 = new HashMap<>();
        Map<Integer,String> map2 = new HashMap<>();

        map1.put(1, "one");
        map1.put(2, "two");

        map2.put(2, "zwei");
        map1.put(3, "drei");

        Map<Integer, String> mergedMap = CdmUtils.mergeMaps(map1, map2);

        assertEquals("one", mergedMap.get(1));
        assertEquals("zwei", mergedMap.get(2));
        assertEquals("drei", mergedMap.get(3));
    }

    @Test
    public void testTruncate(){
        String str = "123456";
        assertEquals(str, CdmUtils.truncateWithEllipsis(str, 10));
        assertEquals(str, CdmUtils.truncateWithEllipsis(str, 6));
        assertEquals(CdmUtils.truncateWithEllipsis(str, 5), "1234"+UTF8.ELLIPSIS);
        assertEquals(CdmUtils.truncateWithEllipsis("123", 2), "1"+UTF8.ELLIPSIS);
        try {
            CdmUtils.truncateWithEllipsis("12", 1);
            fail("Minimum length must be 2");
        } catch (Exception e) {
            //expected
        }
        assertNull(CdmUtils.truncateWithEllipsis(null, 10));

    }

}
