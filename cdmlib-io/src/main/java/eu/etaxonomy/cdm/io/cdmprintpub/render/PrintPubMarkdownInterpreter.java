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

import eu.etaxonomy.cdm.io.cdmprintpub.document.IPrintPubDocumentElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubLabeledTextElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubPageBreakElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubParagraphElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubSectionHeader;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubUnorderedListElement;

public class PrintPubMarkdownInterpreter implements IPrintPubDocumentInterpreter {

    private StringBuilder sb = new StringBuilder();

    @Override
    public void visit(IPrintPubDocumentElement element) {

        if (element instanceof PrintPubSectionHeader) {
            PrintPubSectionHeader h = (PrintPubSectionHeader) element;
            sb.append("\n");
            // Add Markdown headers (#, ##, ###)
            for(int i=0; i<h.getLevel(); i++) {
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

        }
        else if (element instanceof PrintPubLabeledTextElement) {
            PrintPubLabeledTextElement labeled = (PrintPubLabeledTextElement) element;
            sb.append("**").append(labeled.getLabel()).append("**: ")
              .append(labeled.getText()).append("\n\n");
        }
    }

    @Override
    public byte[] getResultBytes() {
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}