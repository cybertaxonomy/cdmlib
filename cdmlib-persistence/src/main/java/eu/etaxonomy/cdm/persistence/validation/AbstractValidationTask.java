package eu.etaxonomy.cdm.persistence.validation;

import javax.validation.Validator;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.validation.Level3;

public abstract class AbstractValidationTask implements Runnable {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractValidationTask.class);

	private final Validator validator;
	private final Object validatable;
	private final Class<?>[] validationGroups;


	public AbstractValidationTask(Validator validator, Object validatable, Class<?> validationGroups)
	{
		this.validator = validator;
		this.validatable = validatable;
		this.validationGroups = this.validationGroups;
	}


	/**
	 * Get the object to be validated in this task
	 * 
	 * @return The object to be validated in this task
	 */
	public Object getValidatable()
	{
		return validatable;
	}


	@Override
	public void run()
	{
		validator.validate(validatable, validationGroups);
	}
}
