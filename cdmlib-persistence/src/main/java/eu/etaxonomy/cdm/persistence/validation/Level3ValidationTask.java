package eu.etaxonomy.cdm.persistence.validation;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.CRUDEventType;
import eu.etaxonomy.cdm.validation.Level3;

/**
 * A {@link Runnable} performing Level-3 validation of a JPA entity
 * 
 * @author ayco holleman
 * 
 */
public class Level3ValidationTask extends EntityValidationTask {

	public Level3ValidationTask(CdmBase entity)
	{
		super(entity, Level3.class);
	}


	public Level3ValidationTask(CdmBase entity, CRUDEventType crudEventType)
	{
		super(entity, crudEventType, Level3.class);
	}

}
