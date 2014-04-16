package eu.etaxonomy.cdm.persistence.validation;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.log4j.Logger;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultDao;
import eu.etaxonomy.cdm.validation.CRUDEventType;

/**
 * Abstract base class for JPA entity validation tasks. Note that in the future other
 * types of classes might be decorated with annotations from the JSR-303 validation
 * framework. This base class, hoewever, is specifically targeted at the validation of JPA
 * entities.
 * 
 * @author ayco holleman
 * 
 */
public abstract class EntityValidationTask implements Runnable {

	private static final Logger logger = Logger.getLogger(EntityValidationTask.class);

	private final CdmBase entity;
	private final CRUDEventType crudEventType;
	private final Class<?>[] validationGroups;

	private Validator validator;
	private WeakReference<EntityValidationThread> waitForThread;

	@SpringBeanByType
	private IEntityValidationResultDao dao;


	/**
	 * Create an entity validation task for the specified entity, to be validated
	 * according to the constraints in the specified validation groups.
	 * 
	 * @param entity
	 *            The entity to be validated
	 * @param validationGroups
	 *            The validation groups to apply
	 */
	public EntityValidationTask(CdmBase entity, Class<?>... validationGroups)
	{
		this(entity, CRUDEventType.NONE, validationGroups);
	}


	/**
	 * Create an entity validation task for the specified entity, indicating the CRUD
	 * event that triggered it and the validation groups to be applied.
	 * 
	 * @param entity
	 *            The entity to be validated
	 * @param trigger
	 *            The CRUD event that triggered the validation
	 * @param validationGroups
	 *            The validation groups to apply
	 */
	public EntityValidationTask(CdmBase entity, CRUDEventType crudEventType, Class<?>... validationGroups)
	{
		this.entity = entity;
		this.crudEventType = crudEventType;
		this.validationGroups = validationGroups;
	}


	@Override
	public void run()
	{
		try {
			if (waitForThread != null && waitForThread.get() != null) {
				waitForThread.get().join();
			}
			Set<ConstraintViolation<CdmBase>> errors = validate();
			for (ConstraintViolation<CdmBase> error : errors) {
			}
		}
		catch (Throwable t) {
			logger.error("Error while validating " + entity.toString() + ": " + t.getMessage());
		}
	}


	/**
	 * Two entity validation tasks are considered equal if (1) they validate the same
	 * entity and (2) they apply the same constraints, i.e. constraints belonging to the
	 * same validation group(s).
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
	 * specified thread is validating the same entity. This is currently a completely
	 * theoretical exercise, since we only allow one thread in the thread pool. Thus
	 * concurrent validation of one and the same entity can never happen (in fact,
	 * concurrent validation cannot happen at all). However, to be future-proof we already
	 * implemented a mechanism to prevent the concurrent validation of one and the same
	 * entity. This method only stores a {@link WeakReference} to the thread to interfere
	 * as little as possible with what's going on within the java concurrency framework
	 * (i.e. the {@link ThreadPoolExecutor}).
	 */
	void waitFor(EntityValidationThread thread)
	{
		this.waitForThread = new WeakReference<EntityValidationThread>(thread);
	}

}
