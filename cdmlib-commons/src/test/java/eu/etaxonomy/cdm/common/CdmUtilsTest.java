/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

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

		Assert.assertTrue(library.endsWith(endOfLibrary));
		Assert.assertTrue(library.contains(libraryContains));
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
        Assert.assertTrue(CdmUtils.equalsIgnoreWS(str1, str2));

        str2 = "Any ";
        Assert.assertFalse(CdmUtils.equalsIgnoreWS(str1, str2));

        str1 = "Any ";
        Assert.assertTrue(CdmUtils.equalsIgnoreWS(str1, str2));

        str1 = "An y eer";
        str2 = "A nye er";
        Assert.assertTrue(CdmUtils.equalsIgnoreWS(str1, str2));

        str1 = "An y eer";
        str2 = "A nyfffe er";
        Assert.assertFalse(CdmUtils.equalsIgnoreWS(str1, str2));
    }

    @Test
    public void testquoteRegExWithWildcard(){
        String regExBase = ".(*$[ms^";
        String regEx = CdmUtils.quoteRegExWithWildcard(regExBase);
        Assert.assertEquals("\\Q.(\\E.*\\Q$[ms^\\E", regEx);
        boolean matches = ".(*$[ms^".matches(regEx);
        Assert.assertTrue(matches);
        matches = ".(aaaaaa$[ms^".matches(regEx);
        Assert.assertTrue(matches);
        matches = "b(aaaaaa$[ms^".matches(regEx);
        Assert.assertFalse(matches);

        regEx = CdmUtils.quoteRegExWithWildcard("*abc*");
        Assert.assertEquals(".*\\Qabc\\E.*", regEx);
        Assert.assertTrue("abc".matches(regEx));
        Assert.assertTrue("a80/(--e*wabc?äe".matches(regEx));
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
            Assert.fail("IOException");
        }
    }

    @Test
    public void testConcat(){
        String str1 = "Str1";
        String str2 = "Str2";
        String str3 = "Str3";
        Assert.assertEquals("Str1;Str2", CdmUtils.concat(";", str1, str2));
        Assert.assertEquals("Str2", CdmUtils.concat(";", null, str2));
        Assert.assertEquals("Str1", CdmUtils.concat(";", str1, null));
        Assert.assertNull(CdmUtils.concat(";", null, null));
        Assert.assertEquals("Str1;Str2;Str3", CdmUtils.concat(";", str1, str2, str3));
        Assert.assertEquals("Str1: Str2", CdmUtils.concat(": ", str1, str2));
        Assert.assertEquals("Str2;Str3", CdmUtils.concat(";", "", str2, str3));
        Assert.assertEquals("Str1;Str3", CdmUtils.concat(";", str1, "", str3));
        Assert.assertEquals("Str1; ;Str3", CdmUtils.concat(";", str1, " ", str3));
    }
    
    @Test
    public void testmodifiedDamerauLevenshteinDistance() {
       	
    	int distance = CdmUtils.modifiedDamerauLevenshteinDistance("Gynoxys asterotricha", "Gynxya asrerotciha");
    	assertEquals(5,distance);
    	distance = CdmUtils.modifiedDamerauLevenshteinDistance("Gynoxys asterotricha", "Gynxsa axrerotciha");
    	assertEquals(7,distance);
    	distance = CdmUtils.modifiedDamerauLevenshteinDistance("Gynoxys asterotricha", "Gynxyas asrerotciha");
    	assertEquals(5,distance);
    	distance = CdmUtils.modifiedDamerauLevenshteinDistance("Gynoxys asterotricha", "Gynoxya asterotricha");
    	assertEquals(1,distance);
    	distance = CdmUtils.modifiedDamerauLevenshteinDistance("Gynoxys asterotricha", "Gynoxys asterotricha");
    	assertEquals(0,distance);
    }
}