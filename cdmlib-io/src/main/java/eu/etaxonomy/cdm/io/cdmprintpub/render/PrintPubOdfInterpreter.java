package eu.etaxonomy.cdm.io.cdmprintpub.render;

import java.util.List;

import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.odftoolkit.odfdom.dom.element.office.OfficeTextElement;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeAutomaticStyles;
import org.odftoolkit.odfdom.pkg.OdfElement;
import org.odftoolkit.odfdom.pkg.OdfFileDom;
import org.w3c.dom.Node;

import eu.etaxonomy.cdm.io.cdmprintpub.document.IPrintPubDocumentElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubLabeledTextElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubPageBreakElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubParagraphElement;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubSectionHeader;
import eu.etaxonomy.cdm.io.cdmprintpub.document.PrintPubUnorderedListElement;

/**
 * ODF interpreter that does not rely on external style factories.
 */
public class PrintPubOdfInterpreter implements IPrintPubDocumentInterpreter {

    private final OdfTextDocument outputDocument;
    private final OdfFileDom contentDom;
    private final OfficeTextElement officeText;
    private final OdfOfficeAutomaticStyles autoStyles;
    private final String BOLD_SPAN_STYLE_NAME = "PP_BoldSpan";

    public PrintPubOdfInterpreter() {
        try {
            outputDocument = OdfTextDocument.newTextDocument();
            contentDom = outputDocument.getContentDom();
            officeText = outputDocument.getContentRoot();
            autoStyles = contentDom.getOrCreateAutomaticStyles();

            // Clean default content
            while (officeText.getFirstChild() != null) {
                officeText.removeChild(officeText.getFirstChild());
            }

            // Create a simple bold span style in content:automatic-styles for labeled text
            //createBoldSpanStyle();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create ODF document", e);
        }
    }

    @Override
    public Object getResult() {
        return outputDocument;
    }

    @Override
    public void visit(IPrintPubDocumentElement element) {

        if (element instanceof PrintPubSectionHeader) {
            PrintPubSectionHeader h = (PrintPubSectionHeader) element;
            int level = Math.max(1, Math.min(6, h.getLevel()));
            OdfElement hElement = contentDom.createElement("text:h");
            hElement.setAttribute("text:outline-level", Integer.toString(level));
            String title = h.getTitle() != null ? h.getTitle() : "";
            hElement.setTextContent(title);
            officeText.appendChild(hElement);

        } else if (element instanceof PrintPubParagraphElement) {
            PrintPubParagraphElement p = (PrintPubParagraphElement) element;
            OdfElement pElem = contentDom.createElement("text:p");
            pElem.setTextContent(p.getText() != null ? p.getText() : "");
            officeText.appendChild(pElem);

        } else if (element instanceof PrintPubUnorderedListElement) {
            PrintPubUnorderedListElement list = (PrintPubUnorderedListElement) element;
            List<String> items = list.getItems();
            if (items != null && !items.isEmpty()) {
                OdfElement listElem = contentDom.createElement("list");
                // set a simple style name if desired (not required)
                for (String item : items) {
                    OdfElement itemElem = contentDom.createElement("list-item");
                    OdfElement itemPara = contentDom.createElement("p");
                    itemPara.setTextContent(item != null ? item : "");
                    itemElem.appendChild(itemPara);
                    listElem.appendChild(itemElem);
                }
                officeText.appendChild(listElem);
            }

        } else if (element instanceof PrintPubPageBreakElement) {
            // Insert an empty paragraph with page-break-before
            OdfElement pb = contentDom.createElement("p");
            pb.setTextContent("");
            pb.setAttribute("fo:break-before", "page");
            officeText.appendChild(pb);

        } else if (element instanceof PrintPubLabeledTextElement) {
            PrintPubLabeledTextElement labeled = (PrintPubLabeledTextElement) element;
            OdfElement pElem = contentDom.createElement("p");

            // label span (bold)
            OdfElement labelSpan = contentDom.createElement("span");
            labelSpan.setAttribute("text:style-name", BOLD_SPAN_STYLE_NAME);
            labelSpan.setTextContent(labeled.getLabel() != null ? labeled.getLabel() : "");
            pElem.appendChild(labelSpan);

            // colon and space
            pElem.appendChild(contentDom.createTextNode(": "));

            // text
            if (labeled.getText() != null) {
                pElem.appendChild(contentDom.createTextNode(labeled.getText()));
            }
            officeText.appendChild(pElem);
        }
    }

    private void createBoldSpanStyle() {
        // Create a style:style element of family text with a text-properties child setting font-weight:bold
        OdfElement spanStyle =  contentDom.createElement("style:style");
        spanStyle.setAttribute("style:name", BOLD_SPAN_STYLE_NAME);
        spanStyle.setAttribute("style:family", "text");

        OdfElement textProps = contentDom.createElement("style:text-properties");
        textProps.setAttribute("fo:font-weight", "bold");
        // also set StyleTextPropertiesElement.FontWeight attributes if needed; above uses direct attribute

        spanStyle.appendChild(textProps);
        // append into office:automatic-styles
        Node asNode = autoStyles.getOwnerDocument().importNode(spanStyle, true);
        autoStyles.appendChild(asNode);
    }
}
