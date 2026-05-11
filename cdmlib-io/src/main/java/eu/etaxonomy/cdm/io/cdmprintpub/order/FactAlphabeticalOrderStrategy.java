package eu.etaxonomy.cdm.io.cdmprintpub.order;

import java.util.Comparator;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.cdmprintpub.dto.PrintPubFactDTO;

/**
 * Alphabetical fact ordering:
 * - primary: label
 * - then: text
 * - then: elementId (deterministic)
 * - then: sequence (final deterministic fallback)
 */
@Component
public class FactAlphabeticalOrderStrategy implements IPrintPubFactOrderStrategy {

    @Override
    public Comparator<PrintPubFactDTO> comparator() {
        return new Comparator<PrintPubFactDTO>() {
            @Override
            public int compare(PrintPubFactDTO f1, PrintPubFactDTO f2) {

                if (f1 == f2) {
                    return 0;
                }
                if (f1 == null) {
                    return 1;
                }
                if (f2 == null) {
                    return -1;
                }

                // 1) label
                int c = CdmUtils.nullSafeCompareTo(f1.label, f2.label);
                if (c != 0) {
                    return c;
                }

                // 2) text
                c = CdmUtils.nullSafeCompareTo(f1.text, f2.text);
                if (c != 0) {
                    return c;
                }

                // 3) elementId
                c = CdmUtils.nullSafeCompareTo(f1.elementId, f2.elementId);
                if (c != 0) {
                    return c;
                }

                // 4) sequence
                return Integer.compare(f1.sequence, f2.sequence);
            }
        };
    }
}