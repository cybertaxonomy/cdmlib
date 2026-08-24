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
import org.odftoolkit.odfdom.dom.element.text.TextLineBreakElement;
import org.odftoolkit.odfdom.dom.element.text.TextPElement;
import org.odftoolkit.odfdom.dom.element.text.TextSpanElement;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.dom.style.props.OdfParagraphProperties;
import org.odftoolkit.odfdom.dom.style.props.OdfTextProperties;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.pkg.OdfElement;

import eu.etaxonomy.cdm.io.cdmprintpub.element.IPrintPubDocumentElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubLabeledTextElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubPageBreakElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubParagraphElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubSectionHeaderElement;
import eu.etaxonomy.cdm.io.cdmprintpub.element.PrintPubTextRunElement;

/**
 * Document interpreter rendering Print/Publication output as ODT.
 *
 * Uses ODFDOM APIs to generate a styled OpenDocument Text file suitable for
 * word processor-based publication.
 */
public class PrintPubOdtInterpreter implements IPrintPubDocumentInterpreter {

    private final OdfTextDocument document;
    private final OdfContentDom contentDom;
    private final OdfElement textRoot;

    public PrintPubOdtInterpreter() throws Exception {
        this.document = OdfTextDocument.newTextDocument();
        this.contentDom = document.getContentDom();
        this.textRoot = (OdfElement) contentDom.getElementsByTagName("office:text").item(0);

        ensureStyles();
    }

    private void ensureStyles() {

        // ---------- TEXT STYLES ----------
        OdfStyle bold = contentDom.getOrCreateAutomaticStyles().newStyle(OdfStyleFamily.Text);
        bold.setStyleNameAttribute("PrintPubBold");
        bold.setProperty(OdfTextProperties.FontWeight, "bold");

        OdfStyle italic = contentDom.getOrCreateAutomaticStyles().newStyle(OdfStyleFamily.Text);
        italic.setStyleNameAttribute("PrintPubItalic");
        italic.setProperty(OdfTextProperties.FontStyle, "italic");

        OdfStyle boldItalic = contentDom.getOrCreateAutomaticStyles().newStyle(OdfStyleFamily.Text);

        boldItalic.setStyleNameAttribute("PrintPubBoldItalic");
        boldItalic.setProperty(OdfTextProperties.FontWeight, "bold");
        boldItalic.setProperty(OdfTextProperties.FontStyle, "italic");

        // ---------- PARAGRAPH STYLES ----------
        OdfStyle body = contentDom.getOrCreateAutomaticStyles().newStyle(OdfStyleFamily.Paragraph);
        body.setStyleNameAttribute("PrintPubBody");
        body.setProperty(OdfParagraphProperties.MarginLeft, "0cm");

        OdfStyle indent = contentDom.getOrCreateAutomaticStyles().newStyle(OdfStyleFamily.Paragraph);
        indent.setStyleNameAttribute("PrintPubIndent");
        indent.setProperty(OdfParagraphProperties.MarginLeft, "0.8cm");

        OdfStyle pageBreak = contentDom.getOrCreateAutomaticStyles().newStyle(OdfStyleFamily.Paragraph);
        pageBreak.setStyleNameAttribute("PrintPubPageBreak");
        pageBreak.setProperty(OdfParagraphProperties.BreakBefore, "page");

        // ---------- HEADINGS ----------
        int[] headingSizes = new int[] { 20, 16, 14, 12, 11, 10 };

        for (int level = 1; level <= 6; level++) {

            OdfStyle heading = contentDom.getOrCreateAutomaticStyles().newStyle(OdfStyleFamily.Paragraph);
            heading.setStyleNameAttribute("PrintPubHeading" + level);
            heading.setProperty(OdfTextProperties.FontWeight, "bold");
            heading.setProperty(OdfTextProperties.FontSize, headingSizes[level - 1] + "pt");
            heading.setProperty(OdfParagraphProperties.MarginTop, "0.4cm");
            heading.setProperty(OdfParagraphProperties.MarginBottom, "0.2cm");
        }

        // ---------- TAXON NAME STYLE ----------
        OdfStyle taxonName = contentDom.getOrCreateAutomaticStyles().newStyle(OdfStyleFamily.Paragraph);

        taxonName.setStyleNameAttribute("PrintPubTaxonName");

        // slightly larger than body
        taxonName.setProperty(OdfTextProperties.FontSize, "16pt");

        // optional visual tuning
        taxonName.setProperty(OdfParagraphProperties.MarginTop, "0.3cm");
        taxonName.setProperty(OdfParagraphProperties.MarginBottom, "0.1cm");
    }

    @Override
    public void visit(IPrintPubDocumentElement element) {

        if (element instanceof PrintPubSectionHeaderElement) {
            renderSectionHeader((PrintPubSectionHeaderElement) element);

        } else if (element instanceof PrintPubParagraphElement) {
            renderParagraph((PrintPubParagraphElement) element);

        } else if (element instanceof PrintPubLabeledTextElement) {
            renderLabeledText((PrintPubLabeledTextElement) element);

        } else if (element instanceof PrintPubPageBreakElement) {
            renderPageBreak();

        } else if (element instanceof PrintPubTextRunElement) {
            renderTextRun((PrintPubTextRunElement) element);
        }
    }

    // =============================
    // Rendering helpers
    // =============================

    private void renderSectionHeader(PrintPubSectionHeaderElement header) {

        int level = Math.max(1, Math.min(6, header.getLevel()));

        TextHElement h = contentDom.newOdfElement(TextHElement.class);
        h.setAttributeNS(OdfDocumentNamespace.TEXT.getUri(), "text:style-name", "PrintPubHeading" + level);
        h.setAttribute("text:outline-level", Integer.toString(level));
        h.setTextContent(header.getTitle());

        textRoot.appendChild(h);
    }

    private void renderParagraph(PrintPubParagraphElement para) {

        TextPElement p = contentDom.newOdfElement(TextPElement.class);
        p.setAttributeNS(OdfDocumentNamespace.TEXT.getUri(), "text:style-name",
                para.isIndented() ? "PrintPubIndent" : "PrintPubBody");
        p.setTextContent(para.getText());

        textRoot.appendChild(p);
    }

    private void renderLabeledText(PrintPubLabeledTextElement labeled) {

        TextPElement p = contentDom.newOdfElement(TextPElement.class);
        p.setAttributeNS(OdfDocumentNamespace.TEXT.getUri(), "text:style-name", "PrintPubBody");

        TextSpanElement label = contentDom.newOdfElement(TextSpanElement.class);
        label.setAttributeNS(OdfDocumentNamespace.TEXT.getUri(), "text:style-name", "PrintPubBold");
        label.setTextContent(labeled.getLabel() + ": ");

        TextSpanElement value = contentDom.newOdfElement(TextSpanElement.class);
        value.setTextContent(labeled.getText());

        p.appendChild(label);
        p.appendChild(value);

        textRoot.appendChild(p);
    }

    private void renderPageBreak() {

        TextPElement p = contentDom.newOdfElement(TextPElement.class);
        p.setAttributeNS(OdfDocumentNamespace.TEXT.getUri(), "text:style-name", "PrintPubPageBreak");
        textRoot.appendChild(p);
    }

    private void renderTextRun(PrintPubTextRunElement element) {

        TextPElement p = contentDom.newOdfElement(TextPElement.class);

        String styleName;
        switch (element.getRole()) {
        case TAXON_NAME:
            styleName = "PrintPubTaxonName";
            break;
        case FACT_GROUP:
            styleName = "PrintPubBody"; // or future variant
            break;
        case BODY:
        default:
            styleName = "PrintPubBody";
        }

        p.setAttributeNS(OdfDocumentNamespace.TEXT.getUri(), "text:style-name", styleName);

        if (element.getLabel() != null) {
            TextSpanElement label = contentDom.newOdfElement(TextSpanElement.class);
            label.setAttributeNS(OdfDocumentNamespace.TEXT.getUri(), "text:style-name", "PrintPubBold");
            label.setTextContent(element.getLabel() + ": ");
            p.appendChild(label);
        }

        for (PrintPubTextRunElement.Run run : element.getRuns()) {

            if (run.type == PrintPubTextRunElement.RunType.LINE_BREAK) {
                p.appendChild(contentDom.newOdfElement(TextLineBreakElement.class));
                continue;
            }

            TextSpanElement span = contentDom.newOdfElement(TextSpanElement.class);

            if (run.type == PrintPubTextRunElement.RunType.BOLD) {
                span.setAttributeNS(OdfDocumentNamespace.TEXT.getUri(), "text:style-name", "PrintPubBold");
            } else if (run.type == PrintPubTextRunElement.RunType.ITALIC
                    || run.type == PrintPubTextRunElement.RunType.CDM_REFERENCE) {
                span.setAttributeNS(OdfDocumentNamespace.TEXT.getUri(), "text:style-name", "PrintPubItalic");
            } else if (run.type == PrintPubTextRunElement.RunType.BOLD_ITALIC) {
                span.setAttributeNS(OdfDocumentNamespace.TEXT.getUri(), "text:style-name", "PrintPubBoldItalic");
            }

            span.setTextContent(run.text);
            p.appendChild(span);
        }

        textRoot.appendChild(p);
    }

    // =============================
    // Output
    // =============================

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