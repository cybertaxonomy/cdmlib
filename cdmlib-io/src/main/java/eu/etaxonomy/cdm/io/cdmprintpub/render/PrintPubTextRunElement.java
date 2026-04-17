package eu.etaxonomy.cdm.io.cdmprintpub.render;

import java.util.List;

import eu.etaxonomy.cdm.io.cdmprintpub.document.IPrintPubDocumentElement;

public class PrintPubTextRunElement implements IPrintPubDocumentElement {

    public enum RunType {
        TEXT,
        BOLD,
        ITALIC,
        LINE_BREAK
    }

    public static class Run {
        public final RunType type;
        public final String text;

        public Run(RunType type, String text) {
            this.type = type;
            this.text = text;
        }
    }

    private final String label; // nullable
    private final List<Run> runs;

    public PrintPubTextRunElement(String label, List<Run> runs) {
        this.label = label;
        this.runs = runs;
    }

    public String getLabel() {
        return label;
    }

    public List<Run> getRuns() {
        return runs;
    }

    @Override
    public void accept(IPrintPubDocumentInterpreter interpreter) {
        interpreter.visit(this);
    }
}