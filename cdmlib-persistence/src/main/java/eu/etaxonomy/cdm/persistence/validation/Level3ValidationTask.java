package eu.etaxonomy.cdm.persistence.validation;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.Level3;

/**
 * A {@link Runnable} performing Level-3 validation of a JPA entity
 * 
 * @author ayco holleman
 * 
 */
public class Level3ValidationTask extends EntityValidationTask {

	public Level3ValidationTask(CdmBase validatable)
	{
		super(validatable, Level3.class);
	}

}
