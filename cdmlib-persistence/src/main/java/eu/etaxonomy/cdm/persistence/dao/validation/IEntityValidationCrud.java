/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;

/**
 * @author a.mueller
 * @since 09.01.2015
 *
 */
public interface IEntityValidationCrud {

    /**
     * Save the result of an entity validation to the error tables. Errors from
     * previous validations of the same entity must first be cleared by classes
     * implementing this interface, because those errors pertain to an obsolete
     * state of the entity. However, errors corresponding to constraints that
     * have not been checked must not be thrown away. In other words, error
     * records that don't belong to any of the specified validation groups must
     * be retained.
     * <p>
     * Note that validation groups that have been applied could also have been
     * extracted from the specified set of {@code ConstraintViolation}s, using
     * the metadata APIs of the javax.validation framework. However the set of
     * {@code ConstraintViolation}s is most likely to result from a call to one
     * of the {@code validate} methods in {@link Validator}. These methods may
     * return an empty set, meaning "nothing wrong". If so, all previously
     * created error records for the entity must now be deleted, but, again,
     * only in so far as they resulted from applying the same validation groups.
     * Therefore the applied validation groups are provided separately.
     *
     * @param validatedEntity
     *            The validated entity
     * @param errors
     *            All constraints violated by the specified entity
     * @param crudEventType
     *            The CRUD operation triggering the validation
     * @param validationGroups
     */
    <T extends ICdmBase> void saveEntityValidation(T validatedEntity, Set<ConstraintViolation<T>> errors,
            CRUDEventType crudEventType, Class<?>[] validationGroups);

    /**
     * Delete validation result for the specified entity, presumably because it
     * has become obsolete.
     *
     * @param validatedEntityClass
     *            The fully qualified class name of the entity's class.
     * @param validatedEntityId
     *            The id of the entity
     */
    void deleteEntityValidation(String validatedEntityClass, int validatedEntityId);

}
