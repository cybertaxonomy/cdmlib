/**
 * Copyright (C) 2025 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.cdmprintpub.render;

import java.nio.charset.StandardCharsets;

import eu.etaxonomy.cdm.io.cdmprintpub.element.IPrintPubDocumentElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubLabeledTextElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubPageBreakElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubParagraphElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubSectionHeaderElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubTextRunElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubUnorderedListElement;

/**
 * Document interpreter rendering Print Publication output as Markdown.
 *
 * Converts document elements into Markdown syntax suitable for lightweight
 * publication or debugging.
 */

public class PrintPubMarkdownInterpreter implements IPrintPubDocumentInterpreter {

    private StringBuilder sb = new StringBuilder();

    @Override
    public void visit(IPrintPubDocumentElement element) {

        if (element instanceof PrintPubSectionHeaderElement) {
            PrintPubSectionHeaderElement h = (PrintPubSectionHeaderElement) element;
            sb.append("\n");
            // Add Markdown headers (#, ##, ###)
            for (int i = 0; i < h.getLevel(); i++) {
                sb.append("#");
            }
            sb.append(" ").append(h.getTitle()).append("\n\n");

        } else if (element instanceof PrintPubParagraphElement) {
            PrintPubParagraphElement p = (PrintPubParagraphElement) element;
            if (p.getText() != null) {
                sb.append(p.getText()).append("\n\n");
            }

        } else if (element instanceof PrintPubUnorderedListElement) {
            PrintPubUnorderedListElement list = (PrintPubUnorderedListElement) element;
            for (String item : list.getItems()) {
                sb.append("* ").append(item).append("\n");
            }
            sb.append("\n");

        } else if (element instanceof PrintPubPageBreakElement) {
            sb.append("\n---\n\n"); // Markdown Horizontal Rule acting as page break

        } else if (element instanceof PrintPubLabeledTextElement) {
            PrintPubLabeledTextElement labeled = (PrintPubLabeledTextElement) element;
            sb.append("**").append(labeled.getLabel()).append("**: ").append(labeled.getText()).append("\n\n");
        } else if (element instanceof PrintPubTextRunElement) {
            PrintPubTextRunElement e = (PrintPubTextRunElement) element;

            if (e.getLabel() != null) {
                sb.append("**").append(e.getLabel()).append("**: ");
            }

            for (PrintPubTextRunElement.Run run : e.getRuns()) {
                switch (run.type) {
                case TEXT:
                    sb.append(run.text);
                    break;
                case BOLD:
                    sb.append("**").append(run.text).append("**");
                    break;
                case ITALIC:
                    sb.append("*").append(run.text).append("*");
                    break;
                case CDM_REFERENCE:
                    sb.append("*").append(run.text).append("*");
                    break;
                case LINE_BREAK:
                    sb.append("\n");
                    break;
                default:
                    sb.append(run.text);
                    break;
                }
            }
            sb.append("\n\n");
        }

    }

    @Override
    public byte[] getResultBytes() {
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String getTimestampedFileName() {
        return "printpub_" + System.currentTimeMillis() + ".md";
    }
}