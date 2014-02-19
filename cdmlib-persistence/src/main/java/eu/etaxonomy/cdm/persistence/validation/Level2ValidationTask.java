package eu.etaxonomy.cdm.persistence.validation;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.Level2;

/**
 * A {@link Runnable} performing Level-2 validation of a JPA entity
 * 
 * @author ayco holleman
 * 
 */
public class Level2ValidationTask extends EntityValidationTask {

	public Level2ValidationTask(CdmBase entity)
	{
		super(entity, Level2.class);
	}


	public Level2ValidationTask(CdmBase entity, EntityValidationTrigger trigger)
	{
		super(entity, trigger, Level2.class);
	}

}
