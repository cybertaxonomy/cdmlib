
package eu.etaxonomy.cdm.database;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

public class EvaluationFailedException extends HibernateException {
	private static final Logger logger = Logger
			.getLogger(EvaluationFailedException.class);
	
	
	
	/**
	 * @param message
	 */
	public EvaluationFailedException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public EvaluationFailedException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EvaluationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
