/**
 * 
 */
package eu.etaxonomy.cdm.strategy.exceptions;

/**
 * @author a.mueller
 *
 */
public class UnknownCdmTypeException extends Exception {

	/**
	 * 
	 */
	public UnknownCdmTypeException() {
		super();
	}

	/**
	 * @param message
	 */
	public UnknownCdmTypeException(String message) {
		super(message);
	}
	
	/**
	 * @param cause
	 */
	public UnknownCdmTypeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnknownCdmTypeException(String message, Throwable cause) {
		super(message, cause);
	}

}
