package eu.etaxonomy.cdm.persistence.validation;

import javax.validation.Validator;

import eu.etaxonomy.cdm.validation.Level3;

public class Level3ValidationTask extends AbstractValidationTask {

	public Level3ValidationTask(Validator validator, Object validatable)
	{
		super(validator, validatable, Level3.class);
	}

}
