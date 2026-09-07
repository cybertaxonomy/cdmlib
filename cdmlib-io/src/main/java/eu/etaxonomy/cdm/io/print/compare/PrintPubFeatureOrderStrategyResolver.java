package eu.etaxonomy.cdm.io.print.compare;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.print.PrintPubExportConfigurator.FeatureSortMode;

@Component
public class PrintPubFeatureOrderStrategyResolver {

    @Autowired
    private FeatureTreeOrderStrategy featureTreeOrderStrategy;

    @Autowired
    private FeatureAlphabeticalOrderStrategy alphabeticalFeatureOrderStrategy;

    public IPrintPubFeatureOrderStrategy resolve(
            FeatureSortMode mode,
            Map<UUID, Integer> featureOrderIndex) {

        if (mode == FeatureSortMode.FEATURE_TREE
                && featureOrderIndex != null
                && !featureOrderIndex.isEmpty()) {

            return featureTreeOrderStrategy;
        }

        return alphabeticalFeatureOrderStrategy;
    }
}