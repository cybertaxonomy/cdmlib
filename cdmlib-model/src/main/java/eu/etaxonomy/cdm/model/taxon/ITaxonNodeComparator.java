/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.taxon;

/**
 * @author a.kohlbecker
 * @date 24.06.2009
 *
 */
public interface ITaxonNodeComparator<T> {

    /**
     * Whether to ignore the hybrid sign ("\u00D7") during comparison.
     * Implementations of this method should default to true.
     *
     * @return
     */
    public boolean isIgnoreHybridSign();

    public void setIgnoreHybridSign(boolean ignore);
}

