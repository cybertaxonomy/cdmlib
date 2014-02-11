package eu.etaxonomy.cdm.persistence.validation;

import javax.validation.Validator;

import eu.etaxonomy.cdm.validation.Level2;

public class Level2ValidationTask extends AbstractValidationTask {

	public Level2ValidationTask(Validator validator, Object entity)
	{
		super(validator, entity, Level2.class);
	}

}
