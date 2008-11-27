/**
 * 
 */
package eu.etaxonomy.cdm.model.common.init;

import org.apache.log4j.Logger;

/**
 * @author AM
 *
 */
public class TermNotFoundException extends Exception {
	private static final long serialVersionUID = 4288479011948189304L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermNotFoundException.class);
	
	
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
