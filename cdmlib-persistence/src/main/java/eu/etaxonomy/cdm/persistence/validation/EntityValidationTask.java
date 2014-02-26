package eu.etaxonomy.cdm.persistence.validation;

import java.lang.ref.WeakReference;
import java.util.Arrays;
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
	private WeakReference<EntityValidationThread> waitForThread;


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
			if (waitForThread != null && waitForThread.get() != null) {
				waitForThread.get().join();
			}
			Set<ConstraintViolation<CdmBase>> violations = validate();
			// TODO: SAVE VIOLATIONS TO DATABASE
		}
		catch (Throwable t) {
			System.out.println("e");
			logger.error("Error while validating " + entity.toString() + ": " + t.getMessage());
		}
	}


	/**
	 * Two entity validation tasks are considered equal if (1) they validate the same entity
	 * and (2) they apply the same constraints - i.e. constraints belonging to the same
	 * validation group(s).
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof EntityValidationTask)) {
			return false;
		}
		EntityValidationTask other = (EntityValidationTask) obj;
		if (!Arrays.deepEquals(validationGroups, other.validationGroups)) {
			return false;
		}
		return entity.equals(other.entity);
	}


	@Override
	public int hashCode()
	{
		int hash = 17;
		hash = (hash * 31) + entity.hashCode();
		hash = (hash * 31) + Arrays.deepHashCode(validationGroups);
		return hash;
	}


	@Override
	public String toString()
	{
		return EntityValidationTask.class.getName() + ':' + entity.toString() + Arrays.deepToString(validationGroups);
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


	/**
	 * Make this task wait for the specified thread to complete. Will be called by
	 * {@link ValidationExecutor#beforeExecute(Thread, Runnable)} when it detects that the
	 * specified thread is validating the same entity as the one validated by this task.
	 */
	void waitFor(EntityValidationThread thread)
	{
		this.waitForThread = new WeakReference<EntityValidationThread>(thread);
	}

}
