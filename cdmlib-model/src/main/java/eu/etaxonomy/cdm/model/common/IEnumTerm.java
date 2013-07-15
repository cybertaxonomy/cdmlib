/**
 * 
 */
package eu.etaxonomy.cdm.model.common;

import java.util.Set;

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
	

	/**
	 * Computes if <code>this</code> term is kind of the <code>ancestor</code> term.
	 * So the <code>ancestor</code> term is direct or indirect (recursive) generalization
	 * of <code>this</code> term.
	 * @param ancestor the potential ancestor term
	 * @see #getKindOf()
	 * @see #getGeneralizationOf()
	 */
	public boolean isKindOf(T ancestor);
	
	/**
	 * Returns all defined terms this term is a generalization for.
	 * Therefore the returned terms are kind of <code>this</code> term.
	 * If parameter <code>recursive</code> is <code>false</code> only the
	 * direct descendants will be returned. If it is <code>true</code>
	 * the direct descendants and there recursive descendants (all descendants)
	 * will be returned. 
	 */
	//TODO move up to ISimpleTerm
	public Set<T> getGeneralizationOf(boolean recursive);
	
}
