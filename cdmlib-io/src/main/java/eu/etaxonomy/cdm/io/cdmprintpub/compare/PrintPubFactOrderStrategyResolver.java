package eu.etaxonomy.cdm.io.cdmprintpub.compare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportConfigurator.FactSortMode;
import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;

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