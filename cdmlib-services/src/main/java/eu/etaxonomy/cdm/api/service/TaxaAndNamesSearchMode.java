/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.EnumSet;

/**
 * @author a.kohlbecker
 * @since Sep 4, 2013
 *
 */
public enum TaxaAndNamesSearchMode {

    doTaxa,
    doSynonyms,
    doTaxaByCommonNames,
    doMisappliedNames,
    includeUnpublished
    ;

    public static EnumSet<TaxaAndNamesSearchMode> taxaAndSynonyms(){
        return EnumSet.of(doTaxa, doSynonyms);
    }
    public static EnumSet<TaxaAndNamesSearchMode> taxaAndSynonymsWithUnpublished(){
        return EnumSet.of(doTaxa, doSynonyms, includeUnpublished);
    }

}
