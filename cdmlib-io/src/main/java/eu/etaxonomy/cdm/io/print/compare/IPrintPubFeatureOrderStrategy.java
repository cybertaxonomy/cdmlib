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

public interface IPrintPubFeatureOrderStrategy {

    Comparator<PrintPubFeatureKey> comparator(Map<UUID, Integer> featureOrderIndex);
}