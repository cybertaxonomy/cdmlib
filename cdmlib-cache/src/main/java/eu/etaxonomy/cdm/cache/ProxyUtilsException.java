package eu.etaxonomy.cdm.cache;

/**
 * originally this was CdmRemotingException
 *
 * @author a.kohlbecker
 * @since Jan 19, 2018
 *
 */
public class ProxyUtilsException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -560332689478356360L;

	public ProxyUtilsException(String message) {
		super(message);
	}

	public ProxyUtilsException(Exception exception) {
	    super(exception);
	}

}
