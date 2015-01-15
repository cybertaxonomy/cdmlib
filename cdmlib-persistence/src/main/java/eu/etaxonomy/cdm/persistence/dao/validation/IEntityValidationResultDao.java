package eu.etaxonomy.cdm.persistence.dao.validation;

import java.util.List;

import javax.validation.ConstraintValidator;

import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.validation.EntityValidationTaskBase;

/**
 * A DAO for accessing the error tables populated as a consequence of entity
 * validation errors. In general you can view errors from the perspective of the
 * constraints being violated, irrespective of the entities that violated them.
 * Or you can focus on the entities themselves, with all constraints they
 * violated. This interface provides the latter perspective while
 * {@link IEntityConstraintViolationDao} provides the former perspective.
 * Together these methods provide all persistency operations required internally
 * by the CVI (notably {@link EntityValidationTaskBase}s) and by clients. In
 * fact, strictly speaking implementors should override methods from the
 * superclass by throwing an exception. They should in any case not be exposed
 * via a service.
 *
 * @author ayco_holleman
 *
 */
public interface IEntityValidationResultDao extends IEntityValidationResultCrud, ICdmEntityDao<EntityValidationResult> {

    /**
     * Get the validation result for a particular entity.
     *
     * @param validatedEntityClass
     *            The fully qualified class name of the entity's class.
     * @param validatedEntityId
     *            The id of the entity
     * @return The {@code EntityValidationResult} or null if the entity has not
     *         been validated yet
     *
     */
    EntityValidationResult getValidationResult(String validatedEntityClass, int validatedEntityId);

    /**
     * Get all validation results for all validated entities. The results are
     * sorted according the type and id of the validated entities.
     *
     * @return The {@code EntityValidationResult}s
     */
    List<EntityValidationResult> getValidationResults();

    /**
     * Get all validation results for all validated entities of the specified
     * type. The results are sorted according to the type and id of the
     * validated entities.
     *
     * @param validatedEntityClass
     *            The fully qualified class name of the entity class
     *
     * @return The {@code EntityValidationResult}s
     */
    List<EntityValidationResult> getEntityValidationResults(String validatedEntityClass);

    /**
     * Get all entities that violated a particular constraint. The results are
     * sorted according to the type and id of the validated entities. Note that
     * the {@code validatorClass} argument is a {@code String} (like all the
     * {@code ***Class} arguments). This is because it is stored as such in the
     * database, and also because the {@code Class} object itself may not be on
     * the caller's classpath - e.g. when called from the TaxEditor.
     *
     * @param validatorClass
     *            The fully qualified class name of the
     *            {@link ConstraintValidator}.
     *
     * @return The {@code EntityValidationResult}s
     */
    List<EntityValidationResult> getEntitiesViolatingConstraint(String validatorClass);

    /**
     * Get all validation results for all entities of the specified type. Only
     * constraint violations of the specified severity are returned as part of
     * the validation result. The results are sorted according to the type and
     * id of the validated entities.
     *
     * @param validatedEntityClass
     *            The fully qualified class name of the entity class.
     * @param severity
     *            The severity of the {@link EntityConstraintViolation}s
     *            associated with the {@code EntityValidationResult}
     *
     * @return The {@code EntityValidationResult}s
     */
    List<EntityValidationResult> getValidationResults(String validatedEntityClass, Severity severity);

    /**
     * Get all validation results. Only constraint violations of the specified
     * severity are returned as part of the validation result. The results are
     * sorted according the type and id of the validated entities.
     *
     * @param severity
     *            The severity of the {@link EntityConstraintViolation}s
     *            associated with the {@code EntityValidationResult}
     *
     * @return The {@code EntityValidationResult}s
     */
    List<EntityValidationResult> getValidationResults(Severity severity);

}
