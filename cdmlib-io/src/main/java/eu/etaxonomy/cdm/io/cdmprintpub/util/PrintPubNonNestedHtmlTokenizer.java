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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizer for simple, non-nested HTML markup.
 *
 * Scans text for basic tags such as <b>, <i>, and <br>
 * and produces a linear token stream suitable for formatting conversion.
 */

public class PrintPubNonNestedHtmlTokenizer {

	enum PrintPubHtmlTokenType {
		TEXT, BOLD, ITALIC, BR
	}

	static class PrintPubHtmlToken {
		final PrintPubHtmlTokenType type;
		final String value; // empty for BR

		PrintPubHtmlToken(PrintPubHtmlTokenType type, String value) {
			this.type = type;
			this.value = value;
		}

		@Override
		public String toString() {
			return value.isEmpty() ? type.name() : type.name() + "(\"" + value + "\")";
		}
	}

	private static final Pattern TOKEN_PATTERN = Pattern.compile("<b>.*?</b>|<i>.*?</i>|<br\\s*/?>|[^<>]+");

	public static List<PrintPubHtmlToken> tokenize(String input) {
		List<PrintPubHtmlToken> tokens = new ArrayList<>();
		Matcher matcher = TOKEN_PATTERN.matcher(input);

		while (matcher.find()) {
			String part = matcher.group();

			if (part.startsWith("<b>")) {
				tokens.add(new PrintPubHtmlToken(PrintPubHtmlTokenType.BOLD, part.substring(3, part.length() - 4)));
			} else if (part.startsWith("<i>")) {
				tokens.add(new PrintPubHtmlToken(PrintPubHtmlTokenType.ITALIC, part.substring(3, part.length() - 4)));
			} else if (part.startsWith("<br")) {
				tokens.add(new PrintPubHtmlToken(PrintPubHtmlTokenType.BR, ""));
			} else {
				tokens.add(new PrintPubHtmlToken(PrintPubHtmlTokenType.TEXT, part));
			}
		}

		return tokens;
	}
}