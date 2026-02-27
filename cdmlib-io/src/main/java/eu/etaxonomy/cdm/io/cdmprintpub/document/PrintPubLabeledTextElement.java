package eu.etaxonomy.cdm.io.cdmprintpub.document;
import eu.etaxonomy.cdm.io.cdmprintpub.render.IPrintPubDocumentInterpreter;

public class PrintPubLabeledTextElement implements IPrintPubDocumentElement {
        private String label;
        private String text;

        public PrintPubLabeledTextElement(String label, String text) {
            this.label = label;
            this.text = text;
        }

        public String getLabel() { return label; }
        public String getText() { return text; }

        @Override
        public void accept(IPrintPubDocumentInterpreter interpreter) {
            interpreter.visit(this);
        }
    }