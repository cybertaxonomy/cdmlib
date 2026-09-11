package eu.etaxonomy.cdm.io.print;

import java.util.HashMap;
import java.util.Map;

public final class PrintPubCitationRegistry {

    private final Map<String, Integer> shortCitationCounters =
            new HashMap<>();

    /**
     * Returns "", "a", "b", and so on for repeated occurrences of the same
     * short citation.
     */
    public String incrementShortCitation(String shortCitation) {

        if (shortCitation == null || shortCitation.isBlank()) {
            return "";
        }

        int counter =
                shortCitationCounters.getOrDefault(
                        shortCitation,
                        0);

        shortCitationCounters.put(
                shortCitation,
                counter + 1);

        return counterToString(counter);
    }

    public void clear() {
        shortCitationCounters.clear();
    }

    private String counterToString(int counter) {

        if (counter == 0) {
            return "";
        }

        int character = 'a' + counter - 1;

        // Preserve existing behavior by skipping "j".
        if (character >= 'j') {
            character++;
        }

        return Character.toString((char) character);
    }
}