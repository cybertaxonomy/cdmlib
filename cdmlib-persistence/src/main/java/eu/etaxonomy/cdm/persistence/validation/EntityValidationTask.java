package eu.etaxonomy.cdm.persistence.validation;

import javax.validation.Validator;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Abstract base class for JPA entity validation tasks. Note that in the future other types of
 * classes might be decorated with annotations from the JSR-303 validation framework. This base
 * class, hoewever, is specifically targeted at the validation of JPA entities.
 * 
 * @author ayco holleman
 * 
 */
public abstract class EntityValidationTask implements Runnable {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EntityValidationTask.class);

	private final CdmBase entity;
	private final Class<?>[] validationGroups;

	private Validator validator;


	public EntityValidationTask(CdmBase entity, Class<?>... validationGroups)
	{
		this.entity = entity;
		this.validationGroups = validationGroups;
	}


	/**
	 * Get the entity to be validated in this task
	 * 
	 * @return The object to be validated in this task
	 */
	public CdmBase getEntity()
	{
		return entity;
	}


	@Override
	public void run()
	{
		assert (validator != null);
		validator.validate(entity, validationGroups);
	}


	void setValidator(Validator validator)
	{
		this.validator = validator;
	}
}
