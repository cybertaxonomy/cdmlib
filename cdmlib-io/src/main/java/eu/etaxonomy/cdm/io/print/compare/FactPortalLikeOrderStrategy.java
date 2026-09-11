/**
 * Copyright (C) 2025 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.print.compare;

import java.util.Comparator;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.print.dto.PrintPubFactDTO;

/**
 * Portal-like fact ordering:
 * - primary: fact kind (proxy for DTO class ordering in the portal)
 * - then: sortIndex
 * - then: elementId
 * - then: text
 * - then: sequence (final deterministic fallback)
 */
@Component
public class FactPortalLikeOrderStrategy implements IPrintPubFactOrderStrategy {

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

                // 1) kind (proxy for portal "class name ordering")
                String k1 = (f1.kind == null) ? null : f1.kind.name();
                String k2 = (f2.kind == null) ? null : f2.kind.name();
                int c = CdmUtils.nullSafeCompareTo(k1, k2);
                if (c != 0) {
                    return c;
                }

                // 2) sortIndex
                c = CdmUtils.nullSafeCompareTo(f1.sortIndex, f2.sortIndex);
                if (c != 0) {
                    return c;
                }

                // 3) elementId (portal falls back to id for deterministic ordering)
                c = CdmUtils.nullSafeCompareTo(f1.elementId, f2.elementId);
                if (c != 0) {
                    return c;
                }

                // 4) text (proxy for typedLabel string fallback)
                c = CdmUtils.nullSafeCompareTo(f1.text, f2.text);
                if (c != 0) {
                    return c;
                }

                // 5) sequence (final deterministic fallback)
                return Integer.compare(f1.sequence, f2.sequence);
            }
        };
    }
}