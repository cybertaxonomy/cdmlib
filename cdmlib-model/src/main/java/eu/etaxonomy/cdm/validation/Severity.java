package eu.etaxonomy.cdm.validation;

import javax.validation.ConstraintViolation;
import javax.validation.Payload;

/**
 * A class conveying the severity of a {@link ConstraintViolation}. It looks a bit odd
 * because this type of information is in fact extraneous to javax.validation and can only
 * be conveyed using {@link Payload}s. Strangely however, payloads <i>must</i> be
 * {@code Class} objects rather than ordinary objects or enums (an obvious choice for
 * severity levels). The Severity class enables you to program using true Severity
 * instances (one for each level), while behind the scenes only the class of those
 * instances is inspected.
 * 
 * @author ayco_holleman
 * 
 */
public abstract class Severity {

	public static final Notice NOTICE = new Notice();
	public static final Warning WARNING = new Warning();
	public static final Error ERROR = new Error();

	public static final class Notice extends Severity implements Payload {
	};

	public static final class Warning extends Severity implements Payload {
	};

	public static final class Error extends Severity implements Payload {
	};


	private Severity()
	{
	}


	@Override
	public String toString()
	{
		return getClass().getSimpleName().toUpperCase();
	}
}
