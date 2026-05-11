
package eu.etaxonomy.cdm.io.cdmprintpub.compare;

import java.util.Comparator;

import eu.etaxonomy.cdm.io.cdmprintpub.PrintPubExportState;

public interface IPrintPubFeatureOrderStrategy {

    Comparator<PrintPubFeatureKey> comparator(PrintPubExportState state);
}
