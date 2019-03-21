/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.common;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author a.mueller
 * @since 22.01.2008
 *
 */
public class CdmUtilsTest {
	private static final Logger logger = Logger.getLogger(CdmUtilsTest.class);


	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

/************************** TESTS ****************************************/

	@Test
	public void testFindLibrary() {
		if (logger.isEnabledFor(Level.DEBUG)) {logger.debug(CdmUtils.findLibrary(CdmUtils.class));}

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
        String resourceFileName = CdmUtils.MUST_EXIST_FILE;
        try {
            InputStream inputStream = CdmUtils.getReadableResourceStream(resourceFileName);
            assertNotNull(inputStream);
        } catch (IOException e) {
            Assert.fail("IOException");
        }
    }





}
