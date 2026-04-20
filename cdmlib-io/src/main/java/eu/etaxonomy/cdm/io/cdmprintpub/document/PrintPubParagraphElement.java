package eu.etaxonomy.cdm.io.cdmprintpub.document;

import eu.etaxonomy.cdm.io.cdmprintpub.render.IPrintPubDocumentInterpreter;

public class PrintPubParagraphElement implements IPrintPubDocumentElement {

    private final String text;
    private final boolean indented;

    public PrintPubParagraphElement(String text) {
        this(text, false);
    }

    public PrintPubParagraphElement(String text, boolean indented) {
        this.text = text;
        this.indented = indented;
    }

    public String getText() {
        return text;
    }

    public boolean isIndented() {
        return indented;
    }

    @Override
    public void accept(IPrintPubDocumentInterpreter interpreter) {
        interpreter.visit(this);
    }
}