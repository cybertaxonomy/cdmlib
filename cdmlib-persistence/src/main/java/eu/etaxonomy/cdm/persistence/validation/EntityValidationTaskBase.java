/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.validation;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationCrud;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationDao;

/**
 * Abstract base class for JPA entity validation tasks. Note that in the future
 * non-entity classes might also be decorated with constraint annotations. This
 * base class, however, is specifically targeted at the validation of JPA
 * entities (more specifically instances of {@link ICdmBase}.
 *
 * @author ayco_holleman
 *
 */
public abstract class EntityValidationTaskBase implements Runnable {

    private static final Logger logger = Logger.getLogger(EntityValidationTaskBase.class);

    private final ICdmBase entity;
    private final CRUDEventType crudEventType;
    private final Class<?>[] validationGroups;

    private IEntityValidationCrud dao;
    private Validator validator;
    private WeakReference<EntityValidationThread> waitForThread;

    /**
     * Create an entity validation task for the specified entity, to be
     * validated according to the constraints in the specified validation
     * groups.
     *
     * @param entity
     *            The entity to be validated
     * @param validationGroups
     *            The validation groups to apply
     */
    public EntityValidationTaskBase(ICdmBase entity, IEntityValidationCrud dao, Class<?>... validationGroups) {
        this(entity, CRUDEventType.NONE, dao, validationGroups);
    }

    /**
     * Create an entity validation task for the specified entity, to be
     * validated according to the constraints in the specified validation
     * groups, and indicating the CRUD event that triggered the validation.
     *
     * @param entity
     *            The entity to be validated
     * @param trigger
     *            The CRUD event that triggered the validation
     * @param validationGroups
     *            The validation groups to apply
     */
    public EntityValidationTaskBase(ICdmBase entity, CRUDEventType crudEventType, IEntityValidationCrud dao,
            Class<?>... validationGroups) {
        this.entity = entity;
        this.crudEventType = crudEventType;
        this.validationGroups = validationGroups;
        this.dao = dao;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public void setDao(IEntityValidationDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        try {
            if (waitForThread != null && waitForThread.get() != null) {
                waitForThread.get().join();
            }
            Set<ConstraintViolation<ICdmBase>> errors = validateWithErrorHandling();
            if (dao == null) {
                logger.error("Cannot save validation result to database (missing DAO)");
            } else {
                dao.saveEntityValidation(entity, errors, crudEventType, validationGroups);
            }
        } catch (Throwable t) {
            logger.error("Error while validating " + entity.toString() + ": " + t.getMessage());
            t.printStackTrace();
        }
    }

    protected Set<ConstraintViolation<ICdmBase>> validateWithErrorHandling() {
        Set<ConstraintViolation<ICdmBase>> result = null;
        try {
            result = validator.validate(entity, validationGroups);
        } catch (Exception e) {
            //TODO convert it into a Constraint violation: #4695
            logger.error("Error while calling validate on validator " +
                    (validator == null ? "null" : validator.toString())+
                     " for entity: " +
                     (entity == null ? "null" : entity.toString()) +
                      "; groups: " + (validationGroups == null ? "null" : validationGroups) +
                      ":" + e.getMessage());
        }
        return result == null ?
                new HashSet<ConstraintViolation<ICdmBase>>() :
                result;
    }

    /**
     * Get the JPA entity validated in this task
     */
    protected ICdmBase getEntity() {
        return entity;
    }

    /**
     * Make this task wait for the specified thread to complete. Will be called
     * by {@link ValidationExecutor#beforeExecute(Thread, Runnable)} when it
     * detects that the specified thread is validating the same entity.
     * <p>
     * Currently this is a theoretical exercise, since we only allow one thread
     * in the thread pool. Thus concurrent validation of one and the same entity
     * can never happen (in fact, concurrent validation cannot happen
     * full-stop). However, to be future proof we already implemented a
     * mechanism to prevent the concurrent validation of one and the same
     * entity.
     * <p>
     * This method only stores a {@link WeakReference} to the thread to
     * interfere as little as possible with what's going on within the java
     * concurrency framework (i.e. the {@link ThreadPoolExecutor}).
     */
    void waitFor(EntityValidationThread thread) {
        this.waitForThread = new WeakReference<EntityValidationThread>(thread);
    }

    /**
     * Two entity validation tasks are considered equal if (1) they validate the
     * same entity and (2) they apply the same constraints, i.e. constraints
     * belonging to the same validation group(s).
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof EntityValidationTaskBase)) {
            return false;
        }
        EntityValidationTaskBase other = (EntityValidationTaskBase) obj;
        if (!Arrays.deepEquals(validationGroups, other.validationGroups)) {
            return false;
        }
        return entity.getId() == other.getEntity().getId();
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = (hash * 31) + entity.getId();
        hash = (hash * 31) + Arrays.deepHashCode(validationGroups);
        return hash;
    }

    @Override
    public String toString() {
        return EntityValidationTaskBase.class.getName() + ':' + entity.toString()
                + Arrays.deepToString(validationGroups);
    }

}
