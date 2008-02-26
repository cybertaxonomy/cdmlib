/**
 * 
 */
package eu.etaxonomy.cdm.strategy.exceptions;

/**
 * @author a.mueller
 *
 */
public class UnknownRankException extends Exception {

	/**
	 * 
	 */
	public UnknownRankException() {
		super();
	}

	/**
	 * @param message
	 */
	public UnknownRankException(String message) {
		super(message);
	}
	
	/**
	 * @param cause
	 */
	public UnknownRankException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnknownRankException(String message, Throwable cause) {
		super(message, cause);
	}

}
