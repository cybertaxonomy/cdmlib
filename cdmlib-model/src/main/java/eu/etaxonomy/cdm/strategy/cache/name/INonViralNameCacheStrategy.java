/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.name;

import eu.etaxonomy.cdm.model.name.INonViralName;

/**
 * A name cache rendering strategy for all TaxonName subclasses.
 * Different TaxonName subclasses could have different strategies.
 * @author a.mueller
 *
 * @param <T> The concrete TaxonName class this strategy applies for
 */
public interface INonViralNameCacheStrategy<T extends INonViralName> extends INameCacheStrategy<T> {

	/**
	 * Returns the last epithet of the name (i.e. uninomial if generic or above, infrageneric epithet if infrageneric,
	 * specific epithet if species and infraspecific epithet if infraspecific, possibly with other information (e.g. ranks
	 * in infraspecific names, indications of hybrid status etc)).
	 *
	 * This is suitable for rendering the name in context of the name which taxonomically includes it i.e. taxonomic hierarchies or
	 * checklists organised into a hierarchy where the initial epithets are removed for brevity
	 *
	 * @param object
	 * @return
	 */
	public String getLastEpithet(T taxonName);



}
