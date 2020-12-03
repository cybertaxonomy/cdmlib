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
 * A reference cache rendering strategy for {@link Reference references}.
 *
 * @author a.mueller
 */
public interface IReferenceCacheStrategy extends IIdentifiableEntityCacheStrategy<Reference> {

	/**
	 * Returns a short version of the reference, suitable for citation (e.g. ${authorname}, ${year})
	 * @deprecated consider using {@link #createShortCitation(Reference, String, Boolean)}. These methods
	 * will probably be merged in future
	 */
    @Deprecated
	public String getCitation(Reference reference, String microReference);

	/**
	 * Returns the full abbreviated title string which
	 * is a string representation of Reference which
	 * preferably uses the {@link Reference#getAbbrevTitle() abbrev title}
	 * of the reference instead of the title. This is not the same as
	 * the {@link INomenclaturalReferenceCacheStrategy#nomenclaturalTitleCache
	 * @return
	 */
	public String getFullAbbrevTitleString(Reference reference);


    //TODO this method seems to be used only for type designations and/or cdmlight, it should be unified with getCitation()
    /**
     * Creates a citation in form <i>author year: detail</i> or <i>author (year: detail)</i>.
     * <BR>
     * If reference has protected titlecache only the titlecache is returned (may change in future).
     * <BR>
     * The author team is abbreviated with <code>et al.</code> if more than 2 authors exist in the team.
     *
     * @param reference the reference to format
     * @param citationDetail the microreference (page, figure, etc.), if <code>null</code> also the colon separator is not used
     * @param withYearBrackets if <code>false</code> the result comes without brackets (default is <code>false</code>)
     * @return
     */
    public String createShortCitation(Reference reference, String citationDetail, Boolean withYearBrackets);



}
