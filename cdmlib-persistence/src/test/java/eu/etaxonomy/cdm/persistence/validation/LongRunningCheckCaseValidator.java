package eu.etaxonomy.cdm.persistence.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * A ConstraintValidator that deliberately takes some time to test task queue overruns.
 * It calls Thread.sleep() to force a long execution time.
 * 
 * @author ayco_holleman
 *
 */
public class LongRunningCheckCaseValidator implements ConstraintValidator<LongRunningCheckCase, String> {
	
	private CaseMode caseMode;

	public void initialize(LongRunningCheckCase constraintAnnotation)
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
		if (object == null) {
			return true;
		}
		if (caseMode == CaseMode.UPPER) {
			return object.equals(object.toUpperCase());
		}
		else {
			return object.equals(object.toLowerCase());
		}
	}
}
