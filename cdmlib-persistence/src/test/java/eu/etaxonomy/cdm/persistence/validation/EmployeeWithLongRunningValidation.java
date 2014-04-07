package eu.etaxonomy.cdm.persistence.validation;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * Mock class that we know will take long to validate.
 */
@SuppressWarnings("serial")
public class EmployeeWithLongRunningValidation extends CdmBase {

	@LongRunningCheckCase(value = CaseMode.UPPER, groups = { Level2.class })
	private String firstName;


	public String getFirstName()
	{
		return firstName;
	}


	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}


	/**
	 * Will always return false. This is because we use this class to stress-test the
	 * ValidationExecutor and we want to be sure each submitted task will be treated as a new
	 * task, otherwise the task would not enter the queue in the first place. The easiest way
	 * to accomplish this is to just let the equals() method simply return false. See
	 * {@link EntityValidationTask#equals(Object)}.
	 */
	public boolean equals(Object obj)
	{
		return false;
	}

}
