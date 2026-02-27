package eu.etaxonomy.cdm.io.cdmprintpub.document;

import eu.etaxonomy.cdm.io.cdmprintpub.render.IPrintPubDocumentInterpreter;

public  class PrintPubParagraphElement implements IPrintPubDocumentElement {
        private String text;

        public PrintPubParagraphElement(String text) {
            this.text = text;
        }

        public String getText() { return text; }

        @Override
        public void accept(IPrintPubDocumentInterpreter interpreter) {
            interpreter.visit(this);
        }
    }
