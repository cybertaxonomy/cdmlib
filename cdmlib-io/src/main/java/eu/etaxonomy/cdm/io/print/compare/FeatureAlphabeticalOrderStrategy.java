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

import eu.etaxonomy.cdm.io.print.PrintPubExportState;

/**
 * Orders feature groups alphabetically by their label, with UUID as deterministic tie-breaker.
 */
@Component
public class FeatureAlphabeticalOrderStrategy implements IPrintPubFeatureOrderStrategy {

    @Override
    public Comparator<PrintPubFeatureKey> comparator(PrintPubExportState state) {
        return new Comparator<PrintPubFeatureKey>() {
            @Override
            public int compare(PrintPubFeatureKey a, PrintPubFeatureKey b) {

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
}