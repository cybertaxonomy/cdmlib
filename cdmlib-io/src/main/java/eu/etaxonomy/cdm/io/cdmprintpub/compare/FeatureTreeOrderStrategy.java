/**
 * Copyright (C) 2025 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.cdmprintpub.compare;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;

/**
 * Orders feature groups using the precomputed feature order index
 * stored in {@link PrintPubExportState}.
 *
 * Unknown features are placed after indexed features and then
 * deterministically ordered by label and UUID.
 */
@Component
public class FeatureTreeOrderStrategy implements IPrintPubFeatureOrderStrategy {

    @Override
    public Comparator<PrintPubFeatureKey> comparator(PrintPubExportState state) {
        return new Comparator<PrintPubFeatureKey>() {
            @Override
            public int compare(PrintPubFeatureKey a, PrintPubFeatureKey b) {

                int oa = orderIndex(a != null ? a.getFeatureUuid() : null, state);
                int ob = orderIndex(b != null ? b.getFeatureUuid() : null, state);

                if (oa != ob) {
                    return Integer.compare(oa, ob);
                }

                String la = (a == null || a.getLabel() == null) ? "" : a.getLabel();
                String lb = (b == null || b.getLabel() == null) ? "" : b.getLabel();
                int c = la.compareToIgnoreCase(lb);
                if (c != 0) {
                    return c;
                }

                String ua = (a == null || a.getFeatureUuid() == null) ? "" : a.getFeatureUuid().toString();
                String ub = (b == null || b.getFeatureUuid() == null) ? "" : b.getFeatureUuid().toString();
                return ua.compareTo(ub);
            }
        };
    }

    private int orderIndex(UUID featureUuid, PrintPubExportState state) {
        if (featureUuid == null || state == null) {
            return Integer.MAX_VALUE / 4;
        }

        Map<UUID, Integer> idx = state.getFeatureOrderIndex();
        if (idx == null || idx.isEmpty()) {
            return Integer.MAX_VALUE / 4;
        }

        Integer value = idx.get(featureUuid);
        return value == null ? Integer.MAX_VALUE / 4 : value.intValue();
    }
}