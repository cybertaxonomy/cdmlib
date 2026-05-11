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

        // Defensive defaults
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
                if (state.getConfig().getFeatureTreeUuid() == null) {
                    return alphabeticalFeatureOrderStrategy;
                }
                return featureTreeOrderStrategy;
        }
    }
}