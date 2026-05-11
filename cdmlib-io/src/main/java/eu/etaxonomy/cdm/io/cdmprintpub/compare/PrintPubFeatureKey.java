package eu.etaxonomy.cdm.io.cdmprintpub.compare;

import java.util.Objects;
import java.util.UUID;

/**
 * Key representing a feature group of facts (featureUuid + printable label).
 */
public final class PrintPubFeatureKey {

    private final UUID featureUuid;
    private final String label;

    public PrintPubFeatureKey(UUID featureUuid, String label) {
        this.featureUuid = featureUuid;
        this.label = label;
    }

    public UUID getFeatureUuid() {
        return featureUuid;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PrintPubFeatureKey)) {
            return false;
        }
        PrintPubFeatureKey other = (PrintPubFeatureKey) o;
        return Objects.equals(featureUuid, other.featureUuid)
                && Objects.equals(label, other.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(featureUuid, label);
    }

    @Override
    public String toString() {
        return "PrintPubFeatureKey{featureUuid=" + featureUuid + ", label='" + label + "'}";
    }
}