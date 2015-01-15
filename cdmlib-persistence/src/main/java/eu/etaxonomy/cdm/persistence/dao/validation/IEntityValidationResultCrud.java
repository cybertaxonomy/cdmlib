// $Id$
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

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.persistence.validation.EntityValidationTaskBase;

/**
 * @author a.mueller
 * @date 09.01.2015
 *
 */
public interface IEntityValidationResultCrud {

    /**
     * Save the result of an entity validation to the error tables. Previous
     * validation results of the same entity will be cleared first. Note that
     * this method should not be exposed via cdmlib-services, because this is a
     * backend-only affair. Populating the error tables is done by the CVI (more
     * particularly by an {@link EntityValidationTaskBase}). External software
     * like the TaxEditor can and should not have access to this method.
     *
     * @param errors
     *            All constraints violated by the specified entity
     * @param entity
     *            The validated entity
     * @param crudEventType
     *            The CRUD operation triggering the validation
     */
    <T extends CdmBase> void saveValidationResult(Set<ConstraintViolation<T>> errors, T entity,
            CRUDEventType crudEventType);

    /**
     * Delete validation result for the specified entity, presumably because it
     * has become obsolete. This method should not be exposed via
     * cdmlib-services.
     *
     * @param validatedEntityClass
     *            The fully qualified class name of the entity's class.
     * @param validatedEntityId
     *            The id of the entity
     */
    void deleteValidationResult(String validatedEntityClass, int validatedEntityId);

}
