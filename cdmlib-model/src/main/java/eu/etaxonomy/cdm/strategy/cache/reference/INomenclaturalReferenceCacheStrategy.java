/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.reference;

import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * A reference formatting strategy for all references implementing INomenclaturalReference.
 * As we handle all references in one class now, this could also be merged with {@link IReferenceCacheStrategy}.
 *
 * @author a.mueller
 *
 */
public interface INomenclaturalReferenceCacheStrategy extends IReferenceCacheStrategy {

	/**
	 * Returns the character sequence before the micro reference (e.g. ": ")
	 * @return
	 */
	public String getBeforeMicroReference();

	/**
	 * Returns a formatted string containing the entire citation used for
	 * nomenclatural purposes based on the {@link Reference reference} supplied - including
	 * (abbreviated) title  but not authors - and on the given details.<BR>
	 * The returned string is build according to the corresponding
	 * {@link INomenclaturalReferenceCacheStrategy cache strategy}.
	 *
	 * @param  nomenclaturalReference the nomenclatural reference
	 * @param  microReference	the string with the details (generally pages)
	 * 							corresponding to the nomenclatural reference supplied
	 * 							as the first argument
	 * @return					the formatted string representing the
	 * 							nomenclatural citation
	 * @see  					INomenclaturalReference#getNomenclaturalCitation(String)
	 * @see 					name.TaxonNameBase#getNomenclaturalReference()
	 */
	public String getNomenclaturalCitation(Reference nomenclaturalReference, String microReference);


	/**
	 * Computes the nomenclatural cache. This is the
	 * {@link #getNomenclaturalCitation(Reference, String) nomenclaturalCitation} without microReference.<BR>
	 * It is meant for searching and therefore cached in the Reference class.
	 * It is not meant for use on its own as a valid representation of a Reference.<BR>
	 * Note: often this string is not unique and can not be used as an identifier as e.g.
	 * multiple articles in the same volume of a journal may have the same nomenclatural cache.

     * @param  reference the reference
	 *
	 * @return the formatted string representing the nomenclatural cache
	 */
	public String getNomenclaturalCache(Reference reference);
}
