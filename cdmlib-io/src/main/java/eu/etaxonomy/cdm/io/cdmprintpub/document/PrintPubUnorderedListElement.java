package eu.etaxonomy.cdm.io.cdmprintpub.document;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.io.cdmprintpub.render.IPrintPubDocumentInterpreter;

public class PrintPubUnorderedListElement implements IPrintPubDocumentElement {
    private List<String> items = new ArrayList<>();

    public void addItem(String item) {
        this.items.add(item);
    }

    public List<String> getItems() {
        return items;
    }

    @Override
    public void accept(IPrintPubDocumentInterpreter interpreter) {
        interpreter.visit(this);
    }
}