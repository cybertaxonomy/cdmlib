// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.geo;

/**
 * @author a.kohlbecker
 * @date Jun 24, 2015
 *
 */
public enum CondensedDistributionRecipe {

    /**
     * The recipe for creation of the condensed distribution strings
     * as used in Euro+Med.
     *
     * For reference see:
     * <ul>
     *   <li>{@link http://ww2.bgbm.org/EuroPlusMed/explanations.asp}</li>
     *   <li>{@link http://dev.e-taxonomy.eu/trac/ticket/3907}</li>
     * </ul>
     */
    EuroPlusMed(EuroPlusMedCondensedDistributionComposer.class),
    FloraCuba(FloraCubaCondensedDistributionComposer.class);

    Class<? extends ICondensedDistributionComposer> implementation;

    CondensedDistributionRecipe(Class<? extends ICondensedDistributionComposer> implementation) {
        this.implementation = implementation;
    }

    public ICondensedDistributionComposer newCondensedDistributionComposerInstance() throws InstantiationException, IllegalAccessException {
        return implementation.newInstance();
    }
}
