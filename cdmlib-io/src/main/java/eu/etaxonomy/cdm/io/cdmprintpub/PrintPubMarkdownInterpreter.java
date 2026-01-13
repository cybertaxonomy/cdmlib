package eu.etaxonomy.cdm.io.cdmprintpub;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.IPrintPubDocumentElement;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubLabeledTextElement;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubPageBreakElement;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubParagraphElement;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubSectionHeader;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubUnorderedListElement;

public class PrintPubMarkdownInterpreter implements IPrintPubDocumentInterpreter {

    private StringBuilder sb = new StringBuilder();

    @Override
    public Object getResult() {
        return sb.toString();
    }

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
            // The Interpreter decides that labels are bolded with asterisks
            sb.append("**").append(labeled.getLabel()).append("**: ")
              .append(labeled.getText()).append("\n\n");
        }
    }
}