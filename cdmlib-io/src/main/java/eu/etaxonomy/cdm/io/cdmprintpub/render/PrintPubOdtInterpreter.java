/**
 * Copyright (C) 2025 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.cdmprintpub.render;

import java.io.ByteArrayOutputStream;

import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.dom.element.text.TextHElement;
import org.odftoolkit.odfdom.dom.element.text.TextPElement;
import org.odftoolkit.odfdom.pkg.OdfFileDom;

import eu.etaxonomy.cdm.io.cdmprintpub.document.IPrintPubDocumentElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubLabeledTextElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubPageBreakElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubParagraphElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubSectionHeader;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubUnorderedListElement;

public class PrintPubOdtInterpreter implements IPrintPubDocumentInterpreter {

    private OdfTextDocument document;
    private OdfFileDom contentDom;

    public PrintPubOdtInterpreter() throws Exception {
        document = OdfTextDocument.newTextDocument();
        contentDom = document.getContentDom();
    }

    @Override
    public void visit(IPrintPubDocumentElement element) {

        if (element instanceof PrintPubSectionHeader) {
            PrintPubSectionHeader headerElement = (PrintPubSectionHeader) element;
            TextHElement headingDom = contentDom.newOdfElement(TextHElement.class);
            headingDom.setTextContent(headerElement.getTitle());
            headingDom.setAttribute("text:outline-level", Integer.toString(headerElement.getLevel()));
            contentDom.getRootElement().appendChild(headingDom);
        } else if (element instanceof PrintPubParagraphElement) {
            PrintPubParagraphElement headerElement = (PrintPubParagraphElement) element;
            TextPElement headingDom = contentDom.newOdfElement(TextPElement.class);
            headingDom.setTextContent(headerElement.getText());
            contentDom.getRootElement().appendChild(headingDom);
        } else if (element instanceof PrintPubUnorderedListElement) {
            // throw NotImplementedException();
        } else if (element instanceof PrintPubPageBreakElement) {
            // throw NotImplementedException();
        } else if (element instanceof PrintPubLabeledTextElement) {
            // throw NotImplementedException();
        }
    }

    @Override
    public byte[] getResultBytes() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ODT document", e);
        }
    }

}