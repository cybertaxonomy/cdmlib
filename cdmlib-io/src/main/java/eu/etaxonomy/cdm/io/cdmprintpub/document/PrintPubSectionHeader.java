package eu.etaxonomy.cdm.io.cdmprintpub.document;

import eu.etaxonomy.cdm.io.cdmprintpub.render.IPrintPubDocumentInterpreter;

public class PrintPubSectionHeader implements IPrintPubDocumentElement {
        private String title;
        private int level;

        public PrintPubSectionHeader(String title, int level) {
            this.title = title;
            this.level = level;
        }

        public String getTitle() { return title; }
        public int getLevel() { return level; }

        @Override
        public void accept(IPrintPubDocumentInterpreter interpreter) {
            interpreter.visit(this);
        }
    }