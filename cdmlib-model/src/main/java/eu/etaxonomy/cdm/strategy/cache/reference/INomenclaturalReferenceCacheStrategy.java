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
 * A reference cache rendering strategy for all subclasses implementing INomenclaturalReference.
 * @author a.mueller
 *
 * @param <T> The concrete Reference class this strategy applies for
 */
public interface INomenclaturalReferenceCacheStrategy<T extends Reference> extends IReferenceBaseCacheStrategy<T> {
	
	
	/**
	 * returns the composed scientific taxon name string without authors nor year
	 * @param object
	 * @return
	 */
	public String getTitleCache(T nomenclaturalReference);
	
	/**
	 * returns the composed author string 
	 * @param object
	 * @return
	 */
	public String getTokenizedNomenclaturalTitel(T referenceBase);
	
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
	 * @param  nomenclaturalReference The nomenclatural reference
	 * @param  microReference	the string with the details (generally pages)
	 * 							corresponding to the nomenclatural reference supplied 
	 * 							as the first argument
	 * @return					the formatted string representing the
	 * 							nomenclatural citation
	 * @see  					INomenclaturalReference#getNomenclaturalCitation(String)
	 * @see 					name.TaxonNameBase#getNomenclaturalReference()
	 * @see 					strategy.cache.reference.INomenclaturalReferenceCacheStrategy
	 */
	public String getNomenclaturalCitation(T nomenclaturalReference, String microReference);
}
