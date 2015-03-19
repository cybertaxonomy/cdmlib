/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
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


	public String getFirstName(){
		return firstName;
	}


	public void setFirstName(String firstName){
		this.firstName = firstName;
	}


	/**
	 * Will always return false. This is because we use this class to stress-test the
	 * ValidationExecutor and we want to be sure each submitted task will be treated as a new
	 * task, otherwise the task would not enter the queue in the first place. The easiest way
	 * to accomplish this is to just let the equals() method simply return false. See
	 * {@link EntityValidationTaskBase#equals(Object)}.
	 */
	@Override
	public boolean equals(Object obj){
		return false;
	}

}
