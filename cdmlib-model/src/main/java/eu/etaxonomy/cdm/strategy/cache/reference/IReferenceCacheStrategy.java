/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.reference;

import eu.etaxonomy.cdm.format.reference.NomenclaturalSourceFormatter;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * A reference cache rendering strategy for {@link Reference references}.
 *
 * @author a.mueller
 */
public interface IReferenceCacheStrategy extends IIdentifiableEntityCacheStrategy<Reference> {

	/**
	 * Returns the full abbreviated title string which
	 * is a string representation of Reference which
	 * preferably uses the {@link Reference#getAbbrevTitle() abbrev title}
	 * of the reference instead of the title. This is not the same as
	 * the {@link NomenclaturalSourceFormatter#format(Reference, String)
	 * @return the full abbreviated title cache
	 */
	public String getFullAbbrevTitleString(Reference reference);

    /**
     * Returns the nomenclatural title cache which equals the nomenclatural citation
     * string without detail (page) information.
     * @return
     *      the nomenclatural title cache
     */
    public String getNomenclaturalTitleCache(Reference reference);

}
