/**
 * 
 */
package eu.etaxonomy.cdm.model.common;

/**
 * @author a.mueller
 * @created 15-Jul-2013
 *
 */
public interface IEnumTerm<T extends ISimpleTerm> extends ISimpleTerm<T> {

	String getKey();

	String getReadableString();

	String getMessage();	

	String getMessage(Language language);

}
