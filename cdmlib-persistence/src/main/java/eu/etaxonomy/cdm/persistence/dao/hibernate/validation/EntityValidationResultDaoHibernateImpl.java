/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
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
    public <T extends ICdmBase> void saveValidationResult(T validatedEntity, Set<ConstraintViolation<T>> errors,
            CRUDEventType crudEventType, Class<?>[] validationGroups) {
        EntityValidationResult result = getValidationResult(validatedEntity.getClass().getName(),
                validatedEntity.getId());
        if (result == null) {
            result = EntityValidationResult.newInstance(validatedEntity, crudEventType);
            addNewErrors(result, validatedEntity, errors);
            getSession().save(result);
        } else {
            deleteOldErrors(result, validationGroups);
            addNewErrors(result, validatedEntity, errors);
            getSession().merge(result);
        }
    }

    @Override
    public void deleteValidationResult(String validatedEntityClass, int validatedEntityId) {
        EntityValidationResult result = getValidationResult(validatedEntityClass, validatedEntityId);
        if (result != null) {
            getSession().delete(result);
        }
    }

    @Override
    public EntityValidationResult getValidationResult(String validatedEntityClass, int validatedEntityId) {
        Query query = getSession().createQuery(
                "FROM EntityValidationResult vr "
                        + "WHERE vr.validatedEntityClass = :cls AND vr.validatedEntityId = :id");
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
        Query query = getSession().createQuery(
                "FROM EntityValidationResult vr JOIN FETCH vr.entityConstraintViolations cv "
                        + "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        @SuppressWarnings("unchecked")
        List<EntityValidationResult> result = query.list();
        return result;
    }

    @Override
    public List<EntityValidationResult> getEntityValidationResults(String validatedEntityClass) {
        Query query = getSession()
                .createQuery(
                        "FROM EntityValidationResult vr "
                                + "WHERE vr.validatedEntityClass = :cls ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        query.setString("cls", validatedEntityClass);
        @SuppressWarnings("unchecked")
        List<EntityValidationResult> result = query.list();
        return result;
    }

    @Override
    public List<EntityValidationResult> getEntitiesViolatingConstraint(String validatorClass) {
        Query query = getSession().createQuery(
                "FROM EntityValidationResult vr JOIN FETCH vr.entityConstraintViolations cv "
                        + "WHERE cv.validator = :cls ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        query.setString("cls", validatorClass);
        @SuppressWarnings("unchecked")
        List<EntityValidationResult> result = query.list();
        return result;
    }

    @Override
    public List<EntityValidationResult> getValidationResults(String validatedEntityClass, Severity severity) {
        Query query = getSession().createQuery(
                "FROM EntityValidationResult vr JOIN FETCH vr.entityConstraintViolations cv "
                        + "WHERE vr.validatedEntityClass = :cls " + "AND cv.severity = :severity "
                        + "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        query.setString("cls", validatedEntityClass);
        query.setString("severity", severity.toString());
        @SuppressWarnings("unchecked")
        List<EntityValidationResult> result = query.list();
        return result;
    }

    @Override
    public List<EntityValidationResult> getValidationResults(Severity severity) {
        Query query = getSession().createQuery(
                "FROM EntityValidationResult vr JOIN FETCH vr.entityConstraintViolations cv "
                        + "WHERE cv.severity = :severity " + "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        query.setString("severity", severity.toString());
        @SuppressWarnings("unchecked")
        List<EntityValidationResult> result = query.list();
        return result;
    }

    private static void deleteOldErrors(EntityValidationResult fromResult, Class<?>[] validationGroups) {
        if (fromResult.getEntityConstraintViolations() == null || fromResult.getEntityConstraintViolations().size() == 0) {
            return;
        }
        Set<Class<?>> validationGroupSet = new HashSet<Class<?>>(Arrays.asList(validationGroups));
        Set<String> validationGroupNames = new HashSet<String>(validationGroupSet.size());
        for (Class<?> c : validationGroupSet) {
            validationGroupNames.add(c.getName());
        }
        Iterator<EntityConstraintViolation> iterator = fromResult.getEntityConstraintViolations().iterator();
        while (iterator.hasNext()) {
            EntityConstraintViolation ecv = iterator.next();
            if (validationGroupNames.contains(ecv.getValidationGroup0())) {
                iterator.remove();
            }
        }
    }

    private static <T extends ICdmBase> void addNewErrors(EntityValidationResult toResult, T validatedEntity,
            Set<ConstraintViolation<T>> errors) {
        for (ConstraintViolation<T> error : errors) {
            EntityConstraintViolation violation = EntityConstraintViolation.newInstance(validatedEntity, error);
            toResult.addEntityConstraintViolation(violation);
            violation.setEntityValidationResult(toResult);
        }
    }

    @SuppressWarnings("unused")
    private void deletedErrorRecords(int validationResultId, Class<?>[] validationGroups) {
        StringBuilder sql = new StringBuilder(127);
        sql.append("DELETE FROM EntityConstraintViolation ecv WHERE ecv.entityValidationResult.id = :id");
        if (validationGroups != null && validationGroups.length != 0) {
            sql.append(" AND (");
            for (int i = 0; i < validationGroups.length; ++i) {
                if (i != 0) {
                    sql.append(" OR ");
                }
                sql.append("validationgroup0 = :param" + i);
            }
            sql.append(")");
        }
        Query query = getSession().createQuery(sql.toString());
        query.setInteger("id", validationResultId);
        if (validationGroups != null && validationGroups.length != 0) {
            for (int i = 0; i < validationGroups.length; ++i) {
                query.setString("param" + i, validationGroups[i].getName());
            }
        }
        int n = query.executeUpdate();
        if (logger.isDebugEnabled()) {
            logger.debug("Deleted " + n + " error records");
        }
    }

}
