/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache.reference;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * A reference cache rendering strategy for all subclasses implementing INomenclaturalReference.
 * @author a.mueller
 *
 * @param <T> The concrete ReferenceBase class this strategy applies for
 */
public interface INomenclaturalReferenceCacheStrategy<T extends ReferenceBase> extends IReferenceBaseCacheStrategy<T> {
	
	
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

}
