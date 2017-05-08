/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.name;

import eu.etaxonomy.cdm.model.name.IViralName;

/**
 * A name cache rendering strategy for all TaxonName subclasses.
 * Different TaxonName subclasses could have different strategies.
 * @author a.mueller
 *
 * @param <T> The concrete TaxonName class this strategy applies for
 */
public interface IViralNameCacheStrategy<T extends IViralName> extends INameCacheStrategy<T> {


}
