package eu.etaxonomy.cdm.persistence.validation;

import java.util.concurrent.ThreadFactory;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

/**
 * {@code ThreadFactory} implementation used by a {@link ValidationExecutor}.
 * 
 * @author ayco holleman
 * 
 */
class ValidationThreadFactory implements ThreadFactory {

	private static final String THREAD_GROUP_NAME = "VALIDATION";
	private static final String DEFAULT_THREAD_NAME = new String();

	private final ValidatorFactory factory;
	private final ThreadGroup threadGroup;


	public ValidationThreadFactory()
	{
		HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
		factory = config.buildValidatorFactory();
		threadGroup = new ThreadGroup(THREAD_GROUP_NAME);
	}


	@Override
	public Thread newThread(Runnable runnable)
	{
		EntityValidationTask task = (EntityValidationTask) runnable;
		return new ValidationThread(threadGroup, task, DEFAULT_THREAD_NAME, factory.getValidator());
	}

}
