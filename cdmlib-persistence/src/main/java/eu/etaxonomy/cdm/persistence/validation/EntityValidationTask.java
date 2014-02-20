package eu.etaxonomy.cdm.persistence.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
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

	private static final Logger logger = Logger.getLogger(EntityValidationTask.class);

	private final CdmBase entity;
	private final EntityValidationTrigger trigger;
	private final Class<?>[] validationGroups;

	private Validator validator;


	/**
	 * Create an entity validation task for the specified entity, to be validated according to
	 * the constraints in the specified validation groups.
	 * 
	 * @param entity
	 *            The entity to be validated
	 * @param validationGroups
	 *            The validation groups to apply
	 */
	public EntityValidationTask(CdmBase entity, Class<?>... validationGroups)
	{
		this(entity, EntityValidationTrigger.NONE, validationGroups);
	}


	/**
	 * Create an entity validation task for the specified entity, indicating the CRUD event
	 * that triggered it and the validation groups to be applied.
	 * 
	 * @param entity
	 *            The entity to be validated
	 * @param trigger
	 *            The CRUD event that triggered the validation
	 * @param validationGroups
	 *            The validation groups to apply
	 */
	public EntityValidationTask(CdmBase entity, EntityValidationTrigger trigger, Class<?>... validationGroups)
	{
		this.entity = entity;
		this.trigger = trigger;
		this.validationGroups = validationGroups;
	}


	@Override
	public void run()
	{
		try {
			Set<ConstraintViolation<CdmBase>> violations = validate();
			// TODO: SAVE VIOLATIONS TO DATABASE
		}
		catch (Throwable t) {
			logger.error("Error while validating " + entity.toString() + ": " + t.getMessage());
		}
	}


	protected Set<ConstraintViolation<CdmBase>> validate()
	{
		assert (validator != null);
		return validator.validate(entity, validationGroups);
	}


	/**
	 * Get the JPA entity validated in this task
	 */
	CdmBase getEntity()
	{
		return entity;
	}


	void setValidator(Validator validator)
	{
		this.validator = validator;
	}

}
