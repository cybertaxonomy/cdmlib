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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizer for simple, non-nested HTML/CDM markup.
 *
 * Scans text for basic tags such as <b>, <i>, <br>, and
 * <cdm:reference ...>...</cdm:reference>.
 */
public class PrintPubNonNestedHtmlTokenizer {

    enum PrintPubHtmlTokenType {
        TEXT,
        BOLD,
        ITALIC,
        BR,
        CDM_REFERENCE
    }

    static class PrintPubHtmlToken {

        final PrintPubHtmlTokenType type;
        final String value;

        // Future-proof metadata
        final String rawMarkup;
        final String tagName;
        final Map<String, String> attributes;

        PrintPubHtmlToken(PrintPubHtmlTokenType type, String value) {
            this(type, value, null, null, new LinkedHashMap<String, String>());
        }

        PrintPubHtmlToken(
                PrintPubHtmlTokenType type,
                String value,
                String rawMarkup,
                String tagName,
                Map<String, String> attributes) {

            this.type = type;
            this.value = value == null ? "" : value;
            this.rawMarkup = rawMarkup;
            this.tagName = tagName;
            this.attributes = attributes == null
                    ? new LinkedHashMap<String, String>()
                    : attributes;
        }

        @Override
        public String toString() {
            return value.isEmpty() ? type.name() : type.name() + "(\"" + value + "\")";
        }
    }

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "<cdm:reference\\b[^>]*>.*?</cdm:reference>"
            + "|<b>.*?</b>"
            + "|<i>.*?</i>"
            + "|<br\\s*/?>"
            + "|[^<>]+",
            Pattern.DOTALL
    );

    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
            "([A-Za-z_:][A-Za-z0-9_:\\.-]*)\\s*=\\s*(['\"])(.*?)\\2"
    );

    public static List<PrintPubHtmlToken> tokenize(String input) {
        List<PrintPubHtmlToken> tokens = new ArrayList<PrintPubHtmlToken>();

        if (input == null || input.isEmpty()) {
            return tokens;
        }

        Matcher matcher = TOKEN_PATTERN.matcher(input);

        while (matcher.find()) {
            String part = matcher.group();

            if (part.startsWith("<cdm:reference")) {
                tokens.add(new PrintPubHtmlToken(
                        PrintPubHtmlTokenType.CDM_REFERENCE,
                        extractTagBody(part),
                        part,
                        "cdm:reference",
                        extractAttributes(part)
                ));

            } else if (part.startsWith("<b>")) {
                tokens.add(new PrintPubHtmlToken(
                        PrintPubHtmlTokenType.BOLD,
                        part.substring(3, part.length() - 4),
                        part,
                        "b",
                        null
                ));

            } else if (part.startsWith("<i>")) {
                tokens.add(new PrintPubHtmlToken(
                        PrintPubHtmlTokenType.ITALIC,
                        part.substring(3, part.length() - 4),
                        part,
                        "i",
                        null
                ));

            } else if (part.startsWith("<br")) {
                tokens.add(new PrintPubHtmlToken(
                        PrintPubHtmlTokenType.BR,
                        "",
                        part,
                        "br",
                        null
                ));

            } else {
                tokens.add(new PrintPubHtmlToken(PrintPubHtmlTokenType.TEXT, part));
            }
        }

        return tokens;
    }

    private static String extractTagBody(String taggedText) {
        int start = taggedText.indexOf('>');
        int end = taggedText.lastIndexOf("</");

        if (start < 0 || end < 0 || end <= start) {
            return "";
        }

        return taggedText.substring(start + 1, end);
    }

    private static Map<String, String> extractAttributes(String taggedText) {
        Map<String, String> attributes = new LinkedHashMap<String, String>();

        int start = taggedText.indexOf(' ');
        int end = taggedText.indexOf('>');

        if (start < 0 || end < 0 || end <= start) {
            return attributes;
        }

        String attributeText = taggedText.substring(start + 1, end);
        Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributeText);

        while (matcher.find()) {
            attributes.put(matcher.group(1), matcher.group(3));
        }

        return attributes;
    }
}
