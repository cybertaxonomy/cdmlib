package eu.etaxonomy.cdm.api.service;

import java.util.List;

import javax.validation.ConstraintValidator;

import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.validation.Severity;

/**
 * A service that provides several retrieval methods for entity validation outcomes. The
 * focus is on the entities rather than on the constraint violations being validated
 * (irrespective of the entities that violated them).
 * 
 * @author ayco_holleman
 * 
 */
public interface IEntityValidationResultService extends IService<EntityValidationResult> {

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

}
