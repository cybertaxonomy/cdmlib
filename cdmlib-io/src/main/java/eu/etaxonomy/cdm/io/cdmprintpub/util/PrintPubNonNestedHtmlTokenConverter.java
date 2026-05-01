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
 * Utility class converting simple HTML tokens into text runs.
 *
 * Bridges legacy non-nested HTML markup with the structured document model used
 * for print publication rendering.
 */

public class PrintPubNonNestedHtmlTokenConverter {

	public static List<PrintPubTextRunElement.Run> toRuns(
			List<PrintPubNonNestedHtmlTokenizer.PrintPubHtmlToken> tokens) {

		List<PrintPubTextRunElement.Run> runs = new ArrayList<>();

		for (PrintPubNonNestedHtmlTokenizer.PrintPubHtmlToken t : tokens) {
			switch (t.type) {
			case TEXT:
				runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.TEXT, t.value));
				break;
			case BOLD:
				runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.BOLD, t.value));
				break;
			case ITALIC:
				runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.ITALIC, t.value));
				break;
			case BR:
				runs.add(new PrintPubTextRunElement.Run(PrintPubTextRunElement.RunType.LINE_BREAK, ""));
				break;
			}
		}
		return runs;
	}
}