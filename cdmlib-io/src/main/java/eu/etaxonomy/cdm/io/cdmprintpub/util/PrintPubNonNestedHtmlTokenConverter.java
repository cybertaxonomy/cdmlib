package eu.etaxonomy.cdm.io.cdmprintpub.util;

import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.io.cdmprintpub.render.PrintPubTextRunElement;

public class PrintPubNonNestedHtmlTokenConverter {

    public static List<PrintPubTextRunElement.Run> toRuns(
            List<PrintPubNonNestedHtmlTokenizer.PrintPubHtmlToken> tokens) {

        List<PrintPubTextRunElement.Run> runs = new ArrayList<>();

        for (PrintPubNonNestedHtmlTokenizer.PrintPubHtmlToken t : tokens) {
            switch (t.type) {
                case TEXT:
                    runs.add(new PrintPubTextRunElement.Run(
                            PrintPubTextRunElement.RunType.TEXT,
                            t.value));
                    break;
                case BOLD:
                    runs.add(new PrintPubTextRunElement.Run(
                            PrintPubTextRunElement.RunType.BOLD,
                            t.value));
                    break;
                case ITALIC:
                    runs.add(new PrintPubTextRunElement.Run(
                            PrintPubTextRunElement.RunType.ITALIC,
                            t.value));
                    break;
                case BR:
                    runs.add(new PrintPubTextRunElement.Run(
                            PrintPubTextRunElement.RunType.LINE_BREAK,
                            ""));
                    break;
            }
        }
        return runs;
    }
}