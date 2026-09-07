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
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Orders feature groups alphabetically by label, with UUID as the
 * deterministic tie-breaker.
 */
@Component
public class FeatureAlphabeticalOrderStrategy
        implements IPrintPubFeatureOrderStrategy {

    @Override
    public Comparator<PrintPubFeatureKey> comparator(
            Map<UUID, Integer> ignoredFeatureOrderIndex) {

        return Comparator
                .comparing(
                        FeatureAlphabeticalOrderStrategy::label,
                        String.CASE_INSENSITIVE_ORDER)
                .thenComparing(
                        FeatureAlphabeticalOrderStrategy::uuid);
    }

    private static String label(PrintPubFeatureKey key) {
        return key == null || key.getLabel() == null
                ? ""
                : key.getLabel();
    }

    private static String uuid(PrintPubFeatureKey key) {
        return key == null || key.getFeatureUuid() == null
                ? ""
                : key.getFeatureUuid().toString();
    }
}