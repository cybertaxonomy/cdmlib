/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.validation;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultDao;

/**
 *
 * @author ayco_holleman
 * @date 15 jan. 2015
 *
 */
@Repository
@Qualifier("EntityValidationResultDaoHibernateImpl")
public class EntityValidationResultDaoHibernateImpl extends CdmEntityDaoBase<EntityValidationResult> implements
        IEntityValidationResultDao {

    private static final Logger logger = Logger.getLogger(EntityValidationResultDaoHibernateImpl.class);

    public EntityValidationResultDaoHibernateImpl() {
        super(EntityValidationResult.class);
    }

    @Override
    public <T extends ICdmBase> void saveValidationResult(Set<ConstraintViolation<T>> errors, T entity,
            CRUDEventType crudEventType) {
        EntityValidationResult old = getValidationResult(entity.getClass().getName(), entity.getId());
        if (old != null) {
            getSession().delete(old);
        }
        EntityValidationResult result = EntityValidationResult.newInstance(entity, crudEventType);
        for (ConstraintViolation<T> error : errors) {
            EntityConstraintViolation violation = EntityConstraintViolation.newInstance(entity, error);
            result.addEntityConstraintViolation(violation);
            violation.setEntityValidationResult(result);
        }
        getSession().merge(result);
    }

    @Override
    public void deleteValidationResult(String validatedEntityClass, int validatedEntityId) {
        // @formatter:off
        Query query = getSession().createQuery(
                "DELETE FROM EntityValidationResult vr " + "WHERE vr.validatedEntityClass = :cls "
                        + "AND vr.validatedEntityId = :id");
        // @formatter:on
        query.setString("cls", validatedEntityClass);
        query.setInteger("id", validatedEntityId);
        int n = query.executeUpdate();
        if (logger.isDebugEnabled()) {
            logger.debug("Deleted " + n + " EntityValidationResults");
        }
    }

    @Override
    public EntityValidationResult getValidationResult(String validatedEntityClass, int validatedEntityId) {
        // @formatter:off
        Query query = getSession().createQuery(
                "FROM EntityValidationResult vr " + "WHERE vr.validatedEntityClass = :cls "
                        + "AND vr.validatedEntityId = :id");
        // @formatter:on
        query.setString("cls", validatedEntityClass);
        query.setInteger("id", validatedEntityId);
        @SuppressWarnings("unchecked")
        List<EntityValidationResult> result = query.list();
        if (result.size() == 0) {
            return null;
        } else {
            return result.iterator().next();
        }
    }

    @Override
    public List<EntityValidationResult> getValidationResults() {
        // @formatter:off
        Query query = getSession().createQuery(
                "FROM EntityValidationResult vr " + "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        // @formatter:on
        @SuppressWarnings("unchecked")
        List<EntityValidationResult> result = query.list();
        return result;
    }

    @Override
    public List<EntityValidationResult> getEntityValidationResults(String validatedEntityClass) {
        // @formatter:off
        Query query = getSession().createQuery(
                "FROM EntityValidationResult vr " + "WHERE vr.validatedEntityClass = :cls "
                        + "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        // @formatter:on
        query.setString("cls", validatedEntityClass);
        @SuppressWarnings("unchecked")
        List<EntityValidationResult> result = query.list();
        return result;
    }

    @Override
    public List<EntityValidationResult> getEntitiesViolatingConstraint(String validatorClass) {
        // @formatter:off
        Query query = getSession().createQuery(
                "FROM EntityValidationResult vr " + "JOIN FETCH vr.entityConstraintViolations cv "
                        + "WHERE cv.validator = :cls " + "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        // @formatter:on
        query.setString("cls", validatorClass);
        @SuppressWarnings("unchecked")
        List<EntityValidationResult> result = query.list();
        return result;
    }

    @Override
    public List<EntityValidationResult> getValidationResults(String validatedEntityClass, Severity severity) {
        // @formatter:off
        Query query = getSession().createQuery(
                "FROM EntityValidationResult vr " + "JOIN FETCH vr.entityConstraintViolations cv "
                        + "WHERE vr.validatedEntityClass = :cls " + "AND cv.severity = :severity "
                        + "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        // @formatter:on
        query.setString("cls", validatedEntityClass);
        query.setString("severity", severity.toString());
        @SuppressWarnings("unchecked")
        List<EntityValidationResult> result = query.list();
        return result;
    }

    @Override
    public List<EntityValidationResult> getValidationResults(Severity severity) {
        // @formatter:off
        Query query = getSession().createQuery(
                "FROM EntityValidationResult vr " + "JOIN FETCH vr.entityConstraintViolations cv "
                        + "WHERE cv.severity = :severity " + "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        // @formatter:on
        query.setString("severity", severity.toString());
        @SuppressWarnings("unchecked")
        List<EntityValidationResult> result = query.list();
        return result;
    }
}
