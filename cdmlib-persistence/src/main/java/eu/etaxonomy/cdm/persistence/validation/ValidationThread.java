package eu.etaxonomy.cdm.persistence.validation;

import javax.validation.Validator;

public final class ValidationThread extends Thread {

	public ValidationThread(AbstractValidationTask task, Validator validator)
	{
		super(task);
		setPriority(MIN_PRIORITY);
	}

}
