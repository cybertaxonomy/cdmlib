package eu.etaxonomy.cdm.io.cdmprintpub;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.IPrintPubDocumentElement;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubPageBreakElement;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubParagraphElement;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubUnorderedListElement;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubDocumentModel.PrintPubSectionHeader;

public class PrintPubMarkdownInterpreter implements IPrintPubDocumentInterpreter {

    private StringBuilder sb = new StringBuilder();

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
    }
}