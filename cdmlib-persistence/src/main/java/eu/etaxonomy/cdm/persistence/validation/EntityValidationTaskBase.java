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
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultCrud;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultDao;

/**
 * Abstract base class for JPA entity validation tasks. Note that in the future
 * non-entity classes might also be decorated with constraint annotations. This
 * base class, however, is specifically targeted at the validation of JPA
 * entities (more specifically instances of {@link CdmBase}.
 *
 * @author ayco_holleman
 *
 */
public abstract class EntityValidationTaskBase implements Runnable {

    private static final Logger logger = Logger.getLogger(EntityValidationTaskBase.class);

    private final CdmBase entity;
    private final CRUDEventType crudEventType;
    private final Class<?>[] validationGroups;

    private IEntityValidationResultCrud dao;
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
    public EntityValidationTaskBase(CdmBase entity, IEntityValidationResultCrud dao, Class<?>... validationGroups) {
        this(entity, CRUDEventType.NONE, dao, validationGroups);
    }

    /**
     * Create an entity validation task for the specified entity, indicating the
     * CRUD event that triggered it and the validation groups to be applied.
     *
     * @param entity
     *            The entity to be validated
     * @param trigger
     *            The CRUD event that triggered the validation
     * @param validationGroups
     *            The validation groups to apply
     */
    public EntityValidationTaskBase(CdmBase entity, CRUDEventType crudEventType, IEntityValidationResultCrud dao,
            Class<?>... validationGroups) {
        this.entity = entity;
        this.crudEventType = crudEventType;
        this.validationGroups = validationGroups;
        this.dao = dao;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public void setDao(IEntityValidationResultDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        try {
            if (waitForThread != null && waitForThread.get() != null) {
                waitForThread.get().join();
            }
            Set<ConstraintViolation<CdmBase>> errors = validate();
            if (dao != null) {
                /*
                 * This test for null is a hack!!! It should normally be
                 * regarded as a program error (assertion error) if the dao is
                 * null. The beforeExecute() method of the ValidationExecutor
                 * guarantees that both the dao and the validator are set before
                 * an entity is validated. However, in the test phase mock
                 * records are inserted into the test database (H2), which
                 * triggers their validation (i.e. this method will be called).
                 * At that time the dao is not set yet. So where I can have the
                 * dao injected such that I can pass it on to the
                 * EntityValidationTask? When I annotate the dao field with
                 *
                 * @SpringBeanByType, it doesn't work, even though when I add
                 *
                 * @SpringBeanByType to the same dao in my test classes (e.g.
                 * eu.etaxonomy.cdm.persistence.dao.hibernate.validation.
                 * EntityValidationResultDaoHibernateImplTest) it DOES work.
                 */
                dao.deleteValidationResult(entity.getClass().getName(), entity.getId());
                dao.saveValidationResult(errors, entity, crudEventType);
            }
        } catch (Throwable t) {
            logger.error("Error while validating " + entity.toString() + ": " + t.getMessage());
        }
    }

    protected Set<ConstraintViolation<CdmBase>> validate() {
        assert (validator != null);
        return validator.validate(entity, validationGroups);
    }

    /**
     * Get the JPA entity validated in this task
     */
    CdmBase getEntity() {
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
