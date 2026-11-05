/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.xml;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.Reader;

import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

/**
 * JUnit-Jupiter-friendly XML comparison (replaces xmlunit 1.x {@code XMLAssert},
 * which subclasses {@code junit.framework.Assert}).
 */
public final class XmlAssert {

    private XmlAssert() {
    }

    public static void assertXmlEqual(String message, Reader expected, Reader actual) {
        // xmlunit 1.x XMLAssert.assertXMLEqual used Diff.similar(), not identical()
        Diff diff = DiffBuilder.compare(expected)
                .withTest(actual)
                .ignoreWhitespace()
                .normalizeWhitespace()
                .ignoreComments()
                // stylesheet PIs / other PIs are presentation, not payload
                .withNodeFilter(node -> node.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE)
                .checkForSimilar()
                .build();
        assertFalse(diff.hasDifferences(), message + ": " + diff);
    }

    public static void assertXmlEqual(Reader expected, Reader actual) {
        assertXmlEqual("XML differs", expected, actual);
    }
}
