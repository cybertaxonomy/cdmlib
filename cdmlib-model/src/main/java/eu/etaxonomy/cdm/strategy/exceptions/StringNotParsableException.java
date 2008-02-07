/**
 * 
 */
package eu.etaxonomy.cdm.strategy.exceptions;

/**
 * @author a.mueller
 *
 */
public class StringNotParsableException extends Exception {

	/**
	 * 
	 */
	public StringNotParsableException() {
		super();
	}

	/**
	 * @param message
	 */
	public StringNotParsableException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public StringNotParsableException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public StringNotParsableException(String message, Throwable cause) {
		super(message, cause);
	}

}
