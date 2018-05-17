/**
 * 
 */
package eu.etaxonomy.cdm.model.common;


/**
 * Interface for all enumerations which provide a key for persisting them as strings
 * via {@link EnumUserType}.
 * The key may also be used for other purposes.
 * The key must be unique within the enumeration.
 * 
 * The interface also provides a human readable representation with i18n support.
 * 
 * IMPORTANT: classes implementing this interface must also implement a static method
 * <code>getByKey(String)</code> which returns the according instance. This can not be 
 * guaranteed by the interface as interfaces do not allow definitions of static methods.
 * 
 * @author a.mueller
 * @since 30-Aug-2013
 *
 */
public interface IKeyTerm  {

	/**
	 * Returns the key for the enumeration.
	 * A key should be short unique identifier within the given enumeration. 
	 * If not otherwise stated it should not be longer then 3 characters. 
	 * @return the key
	 */
	public String getKey();
	
	/**
	 * Returns a human readable preferably for the default language.
	 * @return
	 */
	public String getMessage();	

	/**
	 * Returns a human readable preferably for the given language.
	 * @param language
	 * @return
	 */
	public String getMessage(Language language);

}
