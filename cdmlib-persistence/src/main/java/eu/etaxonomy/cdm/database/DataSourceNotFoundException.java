/**
 * 
 */
package eu.etaxonomy.cdm.database;

/**
 * @author a.mueller
 *
 */
public class DataSourceNotFoundException extends Exception {


	/**
	 * @param message
	 */
	public DataSourceNotFoundException() {
		super();
	}
	
	/**
	 * @param message
	 */
	public DataSourceNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DataSourceNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DataSourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
