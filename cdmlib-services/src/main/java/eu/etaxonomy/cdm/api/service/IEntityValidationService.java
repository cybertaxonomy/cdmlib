/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidation;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationCrud;

/**
 * A service that provides several retrieval methods for entity validation outcomes. The
 * focus is on the entities rather than on the constraint violations being validated
 * (irrespective of the entities that violated them).
 *
 * @author ayco_holleman
 *
 */
public interface IEntityValidationService extends IService<EntityValidation>,
        IEntityValidationCrud {

    /**
     * Save the result of an entity validation to the error tables. Previous validation
     * results of the same entity will be cleared first.
     * @param validatedEntity
     *            The validated entity
     * @param errors
     *            All constraints violated by the specified entity
     * @param crudEventType
     *            The CRUD operation triggering the validation
     */
@Override
//    Note that this method should not
//    * be exposed via cdmlib-services, because this is a backend-only affair. Populating
//    * the error tables is done by the CVI (more particularly by an
//    * {@link EntityValidationTaskBase}). External software like the TaxEditor can and should
//    * not have access to this method.
    <T extends ICdmBase> void  saveEntityValidation(T validatedEntity, Set<ConstraintViolation<T>> errors, CRUDEventType crudEventType, Class<?>[] validationGroups);


    /**
     * Delete validation result for the specified entity, presumably because it has become
     * obsolete.
     *
     * @param validatedEntityClass
     *            The fully qualified class name of the entity's class.
     * @param validatedEntityId
     *            The id of the entity
     */
@Override
//    This method should not be exposed via cdmlib-services.
    void deleteEntityValidation(String validatedEntityClass, int validatedEntityId);


	/**
	 * Get the validation result for a particular entity.
	 *
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity's class.
	 * @param validatedEntityId
	 *            The id of the entity
	 * @return The {@code EntityValidation} or null if the entity has not been
	 *         validated yet
	 *
	 */
	EntityValidation getValidationResult(String validatedEntityClass, int validatedEntityId);


	/**
	 * Get all validation results for all validated entities. The results are sorted
	 * according the type and id of the validated entities.
	 *
	 * @return The {@code EntityValidation}s
	 */
	List<EntityValidation> getValidationResults();


	/**
	 * Get all validation results for all validated entities of the specified type. The
	 * results are sorted according to the type and id of the validated entities.
	 *
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class
	 *
	 * @return The {@code EntityValidation}s
	 */
	List<EntityValidation> getEntityValidations(String validatedEntityClass);


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
	 * @return The {@code EntityValidation}s
	 */
	List<EntityValidation> getEntitiesViolatingConstraint(String validatorClass);


	/**
	 * Get all validation results for all entities of the specified type. Only constraint
	 * violations of the specified severity are returned as part of the validation result.
	 * The results are sorted according to the type and id of the validated entities.
	 *
	 * @param validatedEntityClass
	 *            The fully qualified class name of the entity class.
	 * @param severity
	 *            The severity of the {@link EntityConstraintViolation}s associated with
	 *            the {@code EntityValidation}
	 *
	 * @return The {@code EntityValidation}s
	 */
	List<EntityValidation> getValidationResults(String validatedEntityClass, Severity severity);


	/**
	 * Get all validation results. Only constraint violations of the specified severity
	 * are returned as part of the validation result. The results are sorted according the
	 * type and id of the validated entities.
	 *
	 * @param severity
	 *            The severity of the {@link EntityConstraintViolation}s associated with
	 *            the {@code EntityValidation}
	 *
	 * @return The {@code EntityValidation}s
	 */
	List<EntityValidation> getValidationResults(Severity severity);

}
