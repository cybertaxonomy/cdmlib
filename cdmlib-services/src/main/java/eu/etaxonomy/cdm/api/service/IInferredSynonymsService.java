/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Service that handles tasks related to the creation of inferred synonyms (#...).
 * This was copied from {@link ITaxonService}.
 *
 * @author muellera
 * @since 11.09.2025
 */
public interface IInferredSynonymsService {

    public static final String POTENTIAL_COMBINATION_NAMESPACE = "Potential combination";

    public static final String INFERRED_EPITHET_NAMESPACE = "Inferred epithet";

    public static final String INFERRED_GENUS_NAMESPACE = "Inferred genus";

    public List<Synonym> createAllInferredSynonyms(Taxon taxon, Classification classification,
            boolean doWithMisappliedNames, boolean includeUnpublished);

    public List<Synonym> createInferredSynonyms(Taxon taxon, Classification classification,
            SynonymType inferredSynonymType, boolean doWithMisappliedNames,
            boolean includeUnpublished);

}
