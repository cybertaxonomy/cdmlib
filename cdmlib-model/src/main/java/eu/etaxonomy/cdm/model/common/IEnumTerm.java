/**
 * 
 */
package eu.etaxonomy.cdm.model.common;


/**
 * @author a.mueller
 * @created 15-Jul-2013
 *
 */
public interface IEnumTerm<T extends IEnumTerm<T>> extends ISimpleTerm<T> {

	/**
	 * @return
	 */
	String getKey();

	/**
	 * Returns a human readable preferably for the default language.
	 * @return
	 */
	String getMessage();	

	/**
	 * Returns a human readable preferably for the given language.
	 * @param language
	 * @return
	 */
	String getMessage(Language language);
	

	
}
