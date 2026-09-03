/**
 * Copyright (C) 2025 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.io.print.compare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.print.PrintPubExportState;
import eu.etaxonomy.cdm.io.print.PrintPubExportConfigurator.FactSortMode;

@Component
public class PrintPubFactOrderStrategyResolver {

    @Autowired
    private FactPortalLikeOrderStrategy portalLikeFactOrderStrategy;

    @Autowired
    private FactAlphabeticalOrderStrategy alphabeticalFactOrderStrategy;

    /**
     * Resolves the fact ordering strategy based on the export configuration.
     * Falls back to PORTAL_LIKE if unset, and to alphabetical if state/config is missing.
     */
    public IPrintPubFactOrderStrategy resolve(PrintPubExportState state) {

        // Defensive default (should not normally happen)
        if (state == null || state.getConfig() == null) {
            return alphabeticalFactOrderStrategy;
        }

        FactSortMode mode = state.getConfig().getFactSortMode();
        if (mode == null) {
            mode = FactSortMode.PORTAL_LIKE;
        }

        switch (mode) {
            case ALPHABETICAL:
                return alphabeticalFactOrderStrategy;

            case PORTAL_LIKE:
            default:
                return portalLikeFactOrderStrategy;
        }
    }
}