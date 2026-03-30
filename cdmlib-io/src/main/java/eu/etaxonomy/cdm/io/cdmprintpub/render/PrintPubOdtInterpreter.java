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
import org.odftoolkit.odfdom.dom.OdfContentDom;
import org.odftoolkit.odfdom.dom.OdfDocumentNamespace;
import org.odftoolkit.odfdom.dom.element.text.TextHElement;
import org.odftoolkit.odfdom.dom.element.text.TextPElement;
import org.odftoolkit.odfdom.dom.element.text.TextSpanElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.dom.style.props.OdfParagraphProperties;
import org.odftoolkit.odfdom.dom.style.props.OdfTextProperties;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.pkg.OdfElement;

import eu.etaxonomy.cdm.io.cdmprintpub.document.IPrintPubDocumentElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubLabeledTextElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubPageBreakElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubParagraphElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubSectionHeader;

public class PrintPubOdtInterpreter implements IPrintPubDocumentInterpreter {

    private OdfTextDocument document;
    private OdfElement textRoot;
    private OdfContentDom contentDom;

    public PrintPubOdtInterpreter() throws Exception {
        document = OdfTextDocument.newTextDocument();
        contentDom = document.getContentDom();
        textRoot = (OdfElement) contentDom.getElementsByTagName("office:text").item(0);

        ensureStyles();
    }

    /**
     * Creates required styles: PageBreak and Bold. Compatible with ODFDOM 0.9.0
     * (incubator API).
     */
    private void ensureStyles() {
        OdfOfficeStyles styles = document.getOrCreateDocumentStyles();

        // --- PageBreak style ---
        OdfStyle pageBreakStyle = styles.getStyle("PageBreak", OdfStyleFamily.Paragraph);

        if (pageBreakStyle == null) {
            pageBreakStyle = styles.newStyle("PageBreak", OdfStyleFamily.Paragraph);
            pageBreakStyle.setAttributeNS(OdfDocumentNamespace.STYLE.getUri(), "style:parent-style-name", "Standard");
            pageBreakStyle.setProperty(OdfParagraphProperties.BreakBefore, "page"); // fo:break-before="page"
        }

        // --- Bold character style ---
        OdfStyle boldStyle = styles.getStyle("Bold", OdfStyleFamily.Text);
        if (boldStyle == null) {
            boldStyle = styles.newStyle("Bold", OdfStyleFamily.Text);
            boldStyle.setProperty(OdfTextProperties.FontWeight, "bold");
        }
    }

    @Override
    public void visit(IPrintPubDocumentElement element) {
        if (element instanceof PrintPubSectionHeader) {
            PrintPubSectionHeader header = (PrintPubSectionHeader) element;
            TextHElement h = contentDom.newOdfElement(TextHElement.class);
            h.setTextContent(header.getTitle());
            h.setAttribute("text:outline-level", Integer.toString(header.getLevel()));
            textRoot.appendChild(h);
        } else if (element instanceof PrintPubParagraphElement) {
            PrintPubParagraphElement para = (PrintPubParagraphElement) element;
            TextPElement p = contentDom.newOdfElement(TextPElement.class);
            p.setTextContent(para.getText());
            textRoot.appendChild(p);
        } else if (element instanceof PrintPubLabeledTextElement) {
            PrintPubLabeledTextElement labeled = (PrintPubLabeledTextElement) element;

            TextPElement p = contentDom.newOdfElement(TextPElement.class);

            TextSpanElement labelSpan = contentDom.newOdfElement(TextSpanElement.class);

            labelSpan.setAttributeNS(OdfDocumentNamespace.TEXT.getUri(), "text:style-name", "Bold");

            labelSpan.setTextContent(labeled.getLabel() + ": ");

            TextSpanElement valueSpan = contentDom.newOdfElement(TextSpanElement.class);
            valueSpan.setTextContent(labeled.getText());

            p.appendChild(labelSpan);
            p.appendChild(valueSpan);
            textRoot.appendChild(p);
        } else if (element instanceof PrintPubPageBreakElement) {
            TextPElement p = contentDom.newOdfElement(TextPElement.class);

            p.setAttributeNS(OdfDocumentNamespace.TEXT.getUri(), "text:style-name", "PageBreak");

            textRoot.appendChild(p);
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

    @Override
    public String getTimestampedFileName() {
        return "printpub_" + System.currentTimeMillis() + ".odt";
    }
}
