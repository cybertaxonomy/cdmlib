package eu.etaxonomy.cdm.model.common;

/**
 * An interface especially meant to be implemented by entities that can provide a
 * meaningful description of themselves. If an entity implements this interface, and it
 * turns out to violate some validation constraint, the CVI will use will use the methods
 * of this interface when saving data to the error tables, rather than defaults that might
 * not be very intelligible to the end user. See the invidual methods for what those
 * defaults are for an entity. In fact, {@link CdmBase} implements this interface, but just
 * returns the defaults for each method.
 *
 * @author ayco_holleman
 *
 */
public interface ISelfDescriptive {

	/**
	 * A user-friendly description of the type of the object. Default: the simple name of
	 * the class of the object.
	 *
	 */
	String getUserFriendlyTypeName();


	/**
	 * A user-friendly description of the object itself. Default:
	 * {@link Object#toString()}.
	 */
	String getUserFriendlyDescription();


	/**
	 * A user-friendly name for the specified field (presumably a field that was found to
	 * be invalid by the CVI). Default: the specified field name as-is.
	 *
	 * @param fieldName
	 */
	String getUserFriendlyFieldName(String field);

}
