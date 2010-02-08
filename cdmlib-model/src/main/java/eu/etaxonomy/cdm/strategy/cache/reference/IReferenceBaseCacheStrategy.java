/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.strategy.cache.reference;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * A reference cache rendering strategy for all subclasses of ReferenceBase.
 * @author a.mueller
 *
 * @param <T> The concrete ReferenceBase class this strategy applies for
 */
public interface IReferenceBaseCacheStrategy<T extends ReferenceBase> extends IIdentifiableEntityCacheStrategy<T> {
	
	/**
	 * Returns a short version of the reference, suitable for citation (e.g. ${authorname}, ${year})
	 * @return
	 */
	public String getCitation(T referenceBase);

}
