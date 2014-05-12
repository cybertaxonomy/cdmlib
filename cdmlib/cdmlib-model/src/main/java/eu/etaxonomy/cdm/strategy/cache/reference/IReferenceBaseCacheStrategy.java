/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.strategy.cache.reference;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * A reference cache rendering strategy for all subclasses of Reference.
 * @author a.mueller
 *
 * @param <T> The concrete Reference class this strategy applies for
 */
public interface IReferenceBaseCacheStrategy extends IIdentifiableEntityCacheStrategy<Reference> {
	
	/**
	 * Returns a short version of the reference, suitable for citation (e.g. ${authorname}, ${year})
	 * @return
	 */
	public String getCitation(Reference reference);
	
	/**
	 * Returns the abbreviated title cache which is a title cache which uses the {@link Reference#getAbbrevTitle() abbrev title}
	 * @return
	 */
	public String getAbbrevTitleCache(Reference reference);
	

}
