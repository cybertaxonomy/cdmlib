/**
 * Copyright (C) 2025 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.cdmprintpub.compare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportConfigurator.FeatureSortMode;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;

@Component
public class PrintPubFeatureOrderStrategyResolver {

    @Autowired
    private FeatureTreeOrderStrategy featureTreeOrderStrategy;

    @Autowired
    private FeatureAlphabeticalOrderStrategy alphabeticalFeatureOrderStrategy;

    public IPrintPubFeatureOrderStrategy resolve(PrintPubExportState state) {

        if (state == null || state.getConfig() == null) {
            return alphabeticalFeatureOrderStrategy;
        }

        FeatureSortMode mode = state.getConfig().getFeatureSortMode();
        if (mode == null) {
            mode = FeatureSortMode.FEATURE_TREE;
        }

        switch (mode) {
            case ALPHABETICAL:
                return alphabeticalFeatureOrderStrategy;

            case FEATURE_TREE:
            default:
                if (state.getFeatureOrderIndex() == null || state.getFeatureOrderIndex().isEmpty()) {
                    return alphabeticalFeatureOrderStrategy;
                }
                return featureTreeOrderStrategy;
        }
    }
}