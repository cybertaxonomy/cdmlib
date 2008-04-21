/**
 * 
 */
package eu.etaxonomy.cdm.model.common.init;

/**
 * @author AM
 *
 */
public class TermNotFoundException extends Exception {

	/**
	 * 
	 */
	public TermNotFoundException() {
		super();
	}

	/**
	 * @param arg0
	 */
	public TermNotFoundException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public TermNotFoundException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public TermNotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
