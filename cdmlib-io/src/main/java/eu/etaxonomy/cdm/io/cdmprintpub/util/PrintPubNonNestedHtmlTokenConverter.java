/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.cdmprintpub.util;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubTextRunElement;

/**
 * Utility class converting simple HTML/CDM tokens into text runs.
 */
public class PrintPubNonNestedHtmlTokenConverter {

    public static List<PrintPubTextRunElement.Run> toRuns(
            List<PrintPubNonNestedHtmlTokenizer.PrintPubHtmlToken> tokens) {

        List<PrintPubTextRunElement.Run> runs = new ArrayList<PrintPubTextRunElement.Run>();

        for (PrintPubNonNestedHtmlTokenizer.PrintPubHtmlToken t : tokens) {
            switch (t.type) {
                case TEXT:
                    runs.add(new PrintPubTextRunElement.Run(
                            PrintPubTextRunElement.RunType.TEXT,
                            t.value
                    ));
                    break;

                case BOLD:
                    runs.add(new PrintPubTextRunElement.Run(
                            PrintPubTextRunElement.RunType.BOLD,
                            t.value,
                            t.rawMarkup,
                            t.tagName,
                            t.attributes
                    ));
                    break;

                case ITALIC:
                    runs.add(new PrintPubTextRunElement.Run(
                            PrintPubTextRunElement.RunType.ITALIC,
                            t.value,
                            t.rawMarkup,
                            t.tagName,
                            t.attributes
                    ));
                    break;

                case CDM_REFERENCE:
                    runs.add(new PrintPubTextRunElement.Run(
                            PrintPubTextRunElement.RunType.CDM_REFERENCE,
                            t.value,
                            t.rawMarkup,
                            t.tagName,
                            t.attributes
                    ));
                    break;

                case BR:
                    runs.add(new PrintPubTextRunElement.Run(
                            PrintPubTextRunElement.RunType.LINE_BREAK,
                            "",
                            t.rawMarkup,
                            t.tagName,
                            t.attributes
                    ));
                    break;
            default:
                break;
            }
        }

        return runs;
    }
}