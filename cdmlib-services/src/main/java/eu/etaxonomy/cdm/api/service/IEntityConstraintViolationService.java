package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.Severity;

/**
 * A service that provides several retrieval methods for entity validation outcomes. The
 * focus is on the constraints being violated rather than on the entities that violated
 * them.
 * 
 * @author ayco_holleman
 * 
 */
public interface IEntityConstraintViolationService extends IService<EntityConstraintViolation> {

	/**
	 * Get all constraint violations for all validated entities. The constraint violations
	 * are sorted according to the type and id of the validated entities.
	 * 
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class
	 * 
	 * @return The {@code EntityConstraintViolation}s
	 */
	List<EntityConstraintViolation> getConstraintViolations();


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
