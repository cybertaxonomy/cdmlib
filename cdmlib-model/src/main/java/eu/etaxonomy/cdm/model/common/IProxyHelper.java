package eu.etaxonomy.cdm.model.common;

public interface IProxyHelper {

	// ************************** Hibernate proxies *******************/
	/**
	 * These methods are present due to HHH-1517 - that in a one-to-many
	 * relationship with a superclass at the "one" end, the proxy created
	 * by hibernate is the superclass, and not the subclass, resulting in
	 * a classcastexception when you try to cast it.
	 *
	 * Hopefully this will be resolved through improvements with the creation of
	 * proxy objects by hibernate and the following methods will become redundant,
	 * but for the time being . . .
	 * @param <T>
	 * @param object
	 * @param clazz
	 * @return
	 * @throws ClassCastException
	 */
	public <T> T deproxy(Object object, Class<T> clazz)
			throws ClassCastException;

	public boolean isInstanceOf(Object object, Class clazz)
			throws ClassCastException;

}