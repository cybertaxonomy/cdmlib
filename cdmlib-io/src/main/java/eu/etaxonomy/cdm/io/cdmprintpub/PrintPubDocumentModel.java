package eu.etaxonomy.cdm.io.cdmprintpub;

import java.util.ArrayList;
import java.util.List;


public class PrintPubDocumentModel {

    private List<IPrintPubDocumentElement> elements = new ArrayList<>();

    public void add(IPrintPubDocumentElement element) {
        this.elements.add(element);
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public void render(IPrintPubDocumentInterpreter interpreter) {
        for (IPrintPubDocumentElement element : elements) {
            element.accept(interpreter);
        }
    }

    public interface IPrintPubDocumentElement {
        void accept(IPrintPubDocumentInterpreter interpreter);
    }

    public static class PrintPubSectionHeader implements IPrintPubDocumentElement {
        private String title;
        private int level; // 1 = H1, 2 = H2

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

    public static class PrintPubParagraphElement implements IPrintPubDocumentElement {
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

    public static class PrintPubUnorderedListElement implements IPrintPubDocumentElement {
        private List<String> items = new ArrayList<>();

        public void addItem(String item) {
            this.items.add(item);
        }

        public List<String> getItems() { return items; }

        @Override
        public void accept(IPrintPubDocumentInterpreter interpreter) {
            interpreter.visit(this);
        }
    }

    public static class PrintPubPageBreakElement implements IPrintPubDocumentElement {
        @Override
        public void accept(IPrintPubDocumentInterpreter interpreter) {
            interpreter.visit(this);
        }
    }
}