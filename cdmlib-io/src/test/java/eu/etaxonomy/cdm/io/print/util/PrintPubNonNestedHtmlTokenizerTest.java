/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.io.print.util.PrintPubNonNestedHtmlTokenizer.PrintPubHtmlToken;
import eu.etaxonomy.cdm.io.print.util.PrintPubNonNestedHtmlTokenizer.PrintPubHtmlTokenType;

/**
 * @author muellera
 * @since 28.09.2026
 */
public class PrintPubNonNestedHtmlTokenizerTest {

    @Test
    public void testTokenize() {

        List<PrintPubHtmlToken> tokens = PrintPubNonNestedHtmlTokenizer
                .tokenize("But this line ends <Br/>\n And this is the next line");
        assertEquals(3, tokens.size());

        PrintPubHtmlToken first = tokens.get(0);
        assertEquals("But this line ends ", first.value);
        assertEquals(null, first.tagName);
        assertEquals(null, first.rawMarkup);
        assertEquals(PrintPubHtmlTokenType.TEXT, first.type);

        assertEquals("", tokens.get(1).value);
        assertEquals(PrintPubHtmlTokenType.BR, tokens.get(1).type);

        assertEquals("And this is the next line", tokens.get(2).value);
        assertEquals(PrintPubHtmlTokenType.TEXT, tokens.get(2).type);

        //italics and bold
        tokens = PrintPubNonNestedHtmlTokenizer
                .tokenize("The tree <I>Abies alba</I> is <B>really</b> nice.");
        assertEquals(5, tokens.size());

    }

}
