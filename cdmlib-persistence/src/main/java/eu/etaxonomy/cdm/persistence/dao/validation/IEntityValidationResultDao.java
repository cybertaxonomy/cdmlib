package eu.etaxonomy.cdm.persistence.dao.validation;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.validation.EntityValidationTask;
import eu.etaxonomy.cdm.validation.CRUDEventType;
import eu.etaxonomy.cdm.validation.Severity;

/**
 * A DAO for accessing the error tables populated as a consequence of entity validation
 * errors. In general the methods allow you to view the error tables from two persectives.
 * You can focus on the constraints being violated, irrespective of the entities that
 * violated them. Or you can focus on the entities themselves, with all constraints they
 * violated.
 * 
 * @author ayco_holleman
 * 
 */
public interface IEntityValidationResultDao extends ICdmEntityDao<EntityValidationResult> {

	/**
	 * Save the result of an entity validation to the error tables. Previous validation
	 * results of the same entity will be cleared first. Note that this method should not
	 * be exposed via cdmlib-services, because this is a backend-only affair. Populating
	 * the error tables is done by the CVI (more particularly by an
	 * {@link EntityValidationTask}). External software like the TaxEditor can and should
	 * not have access to this method.
	 * 
	 * @param errors
	 *            All constraints violated by the specified entity
	 * @param entity
	 *            The validated entity
	 * @param crudEventType
	 *            The CRUD operation triggering the validation
	 */
	void saveValidationResult(Set<ConstraintViolation<CdmBase>> errors, CdmBase entity, CRUDEventType crudEventType);


	/**
	 * Delete validation result for the specified entity, presumably because it has become
	 * obsolete. This method should not be exposed via cdmlib-services.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity's class.
	 * @param validatedEntityId
	 *            The id of the entity
	 */
	void deleteValidationResult(String validatedEntityClass, int validatedEntityId);


	/**
	 * Get the validation result for a particular entity.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity's class.
	 * @param validatedEntityId
	 *            The id of the entity
	 * @return The {@code EntityValidationResult} or null if the entity has not been
	 *         validated yet
	 * 
	 */
	EntityValidationResult getValidationResult(String validatedEntityClass, int validatedEntityId);


	/**
	 * Get all validation results for all validated entities. The results are sorted
	 * according the type and id of the validated entities.
	 * 
	 * @return The {@code EntityValidationResult}s
	 */
	List<EntityValidationResult> getValidationResults();


	/**
	 * Get all constraint violations for all validated entities of the specified type. The
	 * constraint violations are sorted according to the type and id of the validated
	 * entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class
	 * 
	 * @return The {@code EntityConstraintViolation}s
	 */
	List<EntityConstraintViolation> getConstraintViolations();


	/**
	 * Get all validation results for all validated entities of the specified type. The
	 * results are sorted according to the type and id of the validated entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class
	 * 
	 * @return The {@code EntityValidationResult}s
	 */
	List<EntityValidationResult> getEntityValidationResults(String validatedEntityClass);


	/**
	 * Get all entities that violated a particular constraint. The results are sorted
	 * according to the type and id of the validated entities. Note that the
	 * {@code validatorClass} argument is a {@code String} (like all the {@code ***Class}
	 * arguments). This is because it is stored as such in the database, and also because
	 * the {@code Class} object itself may not be on the caller's classpath - e.g. when
	 * called from the TaxEditor.
	 * 
	 * @param validatorClass
	 *            The fully qualified class name of the {@link ConstraintValidator}.
	 * 
	 * @return The {@code EntityValidationResult}s
	 */
	List<EntityValidationResult> getEntitiesViolatingConstraint(String validatorClass);


	/**
	 * Get all constraint violations for all entities of the specified type. The
	 * constraint violations are sorted according to the type and id of the validated
	 * entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class
	 * 
	 * @return The {@code EntityConstraintViolation}s
	 */
	List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass);


	/**
	 * Get all validation results for all entities of the specified type. Only constraint
	 * violations of the specified severity are returned as part of the validation result.
	 * The results are sorted according to the type and id of the validated entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class.
	 * @param severity
	 *            The severity of the {@link EntityConstraintViolation}s associated with
	 *            the {@code EntityValidationResult}
	 * 
	 * @return The {@code EntityValidationResult}s
	 */
	List<EntityValidationResult> getValidationResults(String validatedEntityClass, Severity severity);


	/**
	 * Get all constraint violations of the specified severity for all entities of the
	 * specified type. The constraint violations are sorted according to the type and id
	 * of the validated entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class
	 * @param severity
	 *            The severity of the {@link EntityConstraintViolation}s associated with
	 *            the {@code EntityValidationResult}
	 * 
	 * @return The {@code EntityConstraintViolation}s
	 */
	List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass, Severity severity);


	/**
	 * Get all validation results. Only constraint violations of the specified severity
	 * are returned as part of the validation result. The results are sorted according the
	 * type and id of the validated entities.
	 * 
	 * @param severity
	 *            The severity of the {@link EntityConstraintViolation}s associated with
	 *            the {@code EntityValidationResult}
	 * 
	 * @return The {@code EntityValidationResult}s
	 */
	List<EntityValidationResult> getValidationResults(Severity severity);


	/**
	 * Get all constraint violations of the specified severity. The constraint violations
	 * are sorted according to the type and id of the validated entities.
	 * 
	 * @param severity
	 *            The severity of the {@link EntityConstraintViolation}s associated with
	 *            the {@code EntityValidationResult}
	 * 
	 * @return The {@code EntityConstraintViolation}s
	 */
	List<EntityConstraintViolation> getConstraintViolations(Severity severity);

}
