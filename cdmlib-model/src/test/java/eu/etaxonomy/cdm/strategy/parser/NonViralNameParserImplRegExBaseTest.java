/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author a.mueller
 * @date 18.01.2022
 */
public class NonViralNameParserImplRegExBaseTest {

    private static String fWs = NonViralNameParserImplRegExBase.fWs;

    @Test
    public void testBracketVolume() {
        String nr4 = NonViralNameParserImplRegExBase.nr4;
        String bracketVolume;
        bracketVolume = "(" + nr4 + "[A-Za-z]?" + "([-\u2013,]\\s*" + nr4 + ")?|" +
          "((\\d{1,2},\\s*)?(Suppl|Beibl|App|Beil|Misc|Vorabdr|Erg|Bih|(Sess\\.\\s*)?Extr|Reimpr|Bibl|Polypet|Litt|Phys|Orchid)\\.(\\s*\\d{1,4})?|Heft\\s*\\d{1,4}|Extra)){1,2}";
        bracketVolume = NonViralNameParserImplRegExBase.bracketVolume;
        Assert.assertTrue("2".matches(bracketVolume));
        Assert.assertTrue("2-4".matches(bracketVolume));
        Assert.assertTrue("2a-4".matches(bracketVolume));
        Assert.assertTrue("2,Suppl.".matches(bracketVolume));
        Assert.assertTrue("Heft 3", "Heft 3".matches(bracketVolume));
        Assert.assertTrue("2,3", "2,3".matches(bracketVolume));
        Assert.assertTrue("2, 3", "2, 3".matches(bracketVolume));

        Assert.assertFalse("xxx", "xxx".matches(bracketVolume));
    }

    @Test
    public void testVolume() {
        String volume;
        volume = NonViralNameParserImplRegExBase.volume;
        Assert.assertTrue("2".matches(volume));
        Assert.assertTrue("2(3)".matches(volume));
        Assert.assertTrue("2(3,4)".matches(volume));
        Assert.assertTrue("2(Suppl.)".matches(volume));
    }

}
