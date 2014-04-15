package eu.etaxonomy.cdm.persistence.dao.validation;

import java.util.List;

import javax.validation.ConstraintValidator;

import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.validation.Severity;

/**
 * A DAO for accessing the error tables populated as a consequence of entity validation errors.
 * In general the methods allow you to view the error tables from two persectives. You can
 * focus on the constraints being violated, irrespective of the entities that violated them. Or
 * you can focus on the entities themselves, with all constraints they violated.
 * 
 * @author ayco_holleman
 * 
 */
public interface IEntityValidationResultDao extends ICdmEntityDao<EntityValidationResult> {

	/**
	 * Get the validation result for a particular entity.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity's class.
	 * @param validatedEntityId
	 *            The id of th entity
	 * @return The {@code EntityValidationResult} or null if the entity has not been validated
	 *         yet
	 * 
	 */
	EntityValidationResult getValidationResult(String validatedEntityClass, int validatedEntityId);


	/**
	 * Get all validation results for all validated entities. The results are sorted according
	 * the type and id of the validated entities.
	 * 
	 * @return The {@code EntityValidationResult}s
	 */
	List<EntityValidationResult> getValidationResults();


	/**
	 * Get all constraint violations for all validated entities of the specified type. The
	 * constraint violations are sorted according to the type and id of the validated entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class
	 * 
	 * @return The {@code EntityConstraintViolation}s
	 */
	List<EntityConstraintViolation> getConstraintViolations();


	/**
	 * Get all validation results for all validated entities of the specified type. The results
	 * are sorted according to the type and id of the validated entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class
	 * 
	 * @return The {@code EntityValidationResult}s
	 */
	List<EntityValidationResult> getEntityValidationResults(String validatedEntityClass);


	/**
	 * Get all entities that violated a particular constraint. The results are sorted according
	 * to the type and id of the validated entities.
	 * 
	 * @param validatorClass
	 *            The fully qualified class name of the {@link ConstraintValidator}.
	 * 
	 * @return The {@code EntityValidationResult}s
	 */
	List<EntityValidationResult> getEntitiesViolatingConstraint(String validatorClass);


	/**
	 * Get all constraint violations for all entities of the specified type. The constraint
	 * violations are sorted according to the type and id of the validated entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class
	 * 
	 * @return The {@code EntityConstraintViolation}s
	 */
	List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass);


	/**
	 * Get all validation results for all entities of the specified type. Only constraint
	 * violations of the specified severity are returned as part of the validation result. The
	 * results are sorted according to the type and id of the validated entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class.
	 * @param severity
	 *            The severity of the {@link EntityConstraintViolation}s associated with the
	 *            {@code EntityValidationResult}
	 * 
	 * @return The {@code EntityValidationResult}s
	 */
	List<EntityValidationResult> getValidationResults(String validatedEntityClass, Severity severity);


	/**
	 * Get all constraint violations of the specified severity for all entities of the
	 * specified type. The constraint violations are sorted according to the type and id of the
	 * validated entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class
	 * @param severity
	 *            The severity of the {@link EntityConstraintViolation}s associated with the
	 *            {@code EntityValidationResult}
	 * 
	 * @return The {@code EntityConstraintViolation}s
	 */
	List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass, Severity severity);


	/**
	 * Get all validation results. Only constraint violations of the specified severity are
	 * returned as part of the validation result. The results are sorted according the type and
	 * id of the validated entities.
	 * 
	 * @param severity
	 *            The severity of the {@link EntityConstraintViolation}s associated with the
	 *            {@code EntityValidationResult}
	 * 
	 * @return The {@code EntityValidationResult}s
	 */
	List<EntityValidationResult> getValidationResults(Severity severity);


	/**
	 * Get all constraint violations of the specified severity. The constraint violations are
	 * sorted according to the type and id of the validated entities.
	 * 
	 * @param severity
	 *            The severity of the {@link EntityConstraintViolation}s associated with the
	 *            {@code EntityValidationResult}
	 * 
	 * @return The {@code EntityConstraintViolation}s
	 */
	List<EntityConstraintViolation> getConstraintViolations(Severity severity);

}
