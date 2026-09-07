package eu.etaxonomy.cdm.io.print.compare;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class FeatureTreeOrderStrategy implements IPrintPubFeatureOrderStrategy {

    @Override
    public Comparator<PrintPubFeatureKey> comparator(Map<UUID, Integer> featureOrderIndex) {

        Map<UUID, Integer> index = featureOrderIndex == null ? Map.of() : featureOrderIndex;

        return Comparator.<PrintPubFeatureKey> comparingInt(key -> orderIndex(key, index))
                .thenComparing(FeatureTreeOrderStrategy::label, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(FeatureTreeOrderStrategy::uuid);
    }

    private static int orderIndex(PrintPubFeatureKey key, Map<UUID, Integer> index) {

        if (key == null || key.getFeatureUuid() == null) {
            return Integer.MAX_VALUE;
        }

        return index.getOrDefault(key.getFeatureUuid(), Integer.MAX_VALUE);
    }

    private static String label(PrintPubFeatureKey key) {

        return key == null || key.getLabel() == null ? "" : key.getLabel();
    }

    private static String uuid(PrintPubFeatureKey key) {

        return key == null || key.getFeatureUuid() == null ? "" : key.getFeatureUuid().toString();
    }
}