package eu.etaxonomy.cdm.persistence.validation;

import java.util.concurrent.ThreadFactory;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

public class ValidationThreadFactory implements ThreadFactory {

	private final ValidatorFactory factory;


	public ValidationThreadFactory()
	{
		HibernateValidatorConfiguration config = Validation.byProvider(HibernateValidator.class).configure();
		factory = config.buildValidatorFactory();
	}


	@Override
	public Thread newThread(Runnable runnable)
	{
		if (runnable instanceof AbstractValidationTask) {
			AbstractValidationTask task = (AbstractValidationTask) runnable;
			return new ValidationThread(task, factory.getValidator());
		}
		throw new IllegalArgumentException("This ThreadFactory can only create threads for validation tasks");
	}

}
