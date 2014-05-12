/**
 * 
 */
package eu.etaxonomy.cdm.database.update;

/**
 * @author a.mueller
 *
 */
public class DatabaseTypeNotSupportedException extends Exception {
	private static final long serialVersionUID = -6065181245886098240L;

	/**
	 * 
	 */
	public DatabaseTypeNotSupportedException() {

	}

	/**
	 * @param arg0
	 */
	public DatabaseTypeNotSupportedException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public DatabaseTypeNotSupportedException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public DatabaseTypeNotSupportedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
