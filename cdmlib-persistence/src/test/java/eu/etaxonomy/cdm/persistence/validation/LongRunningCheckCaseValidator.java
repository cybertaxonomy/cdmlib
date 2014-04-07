package eu.etaxonomy.cdm.persistence.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LongRunningCheckCaseValidator implements ConstraintValidator<CheckCase, String> {
	private CaseMode caseMode;


	public void initialize(CheckCase constraintAnnotation)
	{
		this.caseMode = constraintAnnotation.value();
	}


	public boolean isValid(String object, ConstraintValidatorContext constraintContext)
	{
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("stopped waiting");
		if (object == null)
			return true;
		if (caseMode == CaseMode.UPPER)
			return object.equals(object.toUpperCase());
		else
			return object.equals(object.toLowerCase());
	}
}
