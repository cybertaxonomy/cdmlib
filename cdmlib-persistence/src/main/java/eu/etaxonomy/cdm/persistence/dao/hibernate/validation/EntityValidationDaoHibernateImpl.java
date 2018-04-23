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
import eu.etaxonomy.cdm.model.validation.EntityValidation;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationDao;

/**
 *
 * @author ayco_holleman
 \* @since 15 jan. 2015
 *
 */
@Repository
@Qualifier("EntityValidationDaoHibernateImpl")
public class EntityValidationDaoHibernateImpl extends CdmEntityDaoBase<EntityValidation> implements
        IEntityValidationDao {

    private static final Logger logger = Logger.getLogger(EntityValidationDaoHibernateImpl.class);

    public EntityValidationDaoHibernateImpl() {
        super(EntityValidation.class);
    }

    @Override
    public <T extends ICdmBase> void saveEntityValidation(T validatedEntity, Set<ConstraintViolation<T>> errors,
            CRUDEventType crudEventType, Class<?>[] validationGroups) {
        EntityValidation entityValidation = getEntityValidation(validatedEntity.getClass().getName(),
                validatedEntity.getId());
        if (entityValidation == null) {
            entityValidation = EntityValidation.newInstance(validatedEntity, crudEventType);
            addNewErrors(entityValidation, validatedEntity, errors);
            getSession().save(entityValidation);
        } else {
            deleteOldErrors(entityValidation, validationGroups);
            addNewErrors(entityValidation, validatedEntity, errors);
            getSession().merge(entityValidation);
        }
    }

    @Override
    public void deleteEntityValidation(String validatedEntityClass, int validatedEntityId) {
        EntityValidation result = getEntityValidation(validatedEntityClass, validatedEntityId);
        if (result != null) {
            getSession().delete(result);
        }
    }

    @Override
    public EntityValidation getEntityValidation(String validatedEntityClass, int validatedEntityId) {
        Query query = getSession().createQuery(
                "FROM EntityValidation vr "
                        + "WHERE vr.validatedEntityClass = :cls AND vr.validatedEntityId = :id");
        query.setString("cls", validatedEntityClass);
        query.setInteger("id", validatedEntityId);
        @SuppressWarnings("unchecked")
        List<EntityValidation> result = query.list();
        if (result.size() == 0) {
            return null;
        } else {
            return result.iterator().next();
        }
    }

    @Override
    public List<EntityValidation> getEntityValidations() {
        Query query = getSession().createQuery(
                "FROM EntityValidation vr "
                        + "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        @SuppressWarnings("unchecked")       
        List<EntityValidation> result = query.list();
        return result;
    }

    @Override
    public List<EntityValidation> getEntityValidations(String validatedEntityClass) {
        Query query = getSession()
                .createQuery(
                        "FROM EntityValidation vr "
                                + "WHERE vr.validatedEntityClass = :cls ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        query.setString("cls", validatedEntityClass);
        @SuppressWarnings("unchecked")
        List<EntityValidation> result = query.list();
        return result;
    }

    @Override
    public List<EntityValidation> getEntitiesViolatingConstraint(String validatorClass) {
        Query query = getSession().createQuery(
                "FROM EntityValidation vr JOIN FETCH vr.entityConstraintViolations cv "
                        + "WHERE cv.validator = :cls ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        query.setString("cls", validatorClass);
        @SuppressWarnings("unchecked")
        List<EntityValidation> result = query.list();
        return result;
    }

    @Override
    public List<EntityValidation> getEntityValidations(String validatedEntityClass, Severity severity) {
        Query query = getSession().createQuery(
                "FROM EntityValidation vr JOIN FETCH vr.entityConstraintViolations cv "
                        + "WHERE vr.validatedEntityClass = :cls " + "AND cv.severity = :severity "
                        + "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        query.setString("cls", validatedEntityClass);
        query.setString("severity", severity.toString());
        @SuppressWarnings("unchecked")
        List<EntityValidation> result = query.list();
        return result;
    }

    @Override
    public List<EntityValidation> getEntityValidations(Severity severity) {
        Query query = getSession().createQuery(
                "FROM EntityValidation vr JOIN FETCH vr.entityConstraintViolations cv "
                        + "WHERE cv.severity = :severity " + "ORDER BY vr.validatedEntityClass, vr.validatedEntityId");
        query.setString("severity", severity.toString());
        @SuppressWarnings("unchecked")
        List<EntityValidation> result = query.list();
        return result;
    }

    private static void deleteOldErrors(EntityValidation fromResult, Class<?>[] validationGroups) {
        if (fromResult.getEntityConstraintViolations() == null || fromResult.getEntityConstraintViolations().size() == 0) {
            return;
        }
        Set<Class<?>> validationGroupSet = new HashSet<Class<?>>(Arrays.asList(validationGroups));
        Set<String> validationGroupNames = new HashSet<String>(validationGroupSet.size());
        for (Class<?> c : validationGroupSet) {
            validationGroupNames.add(c.getName());
        }
//        Iterator<EntityConstraintViolation> iterator = fromResult.getEntityConstraintViolations().iterator();
        Set<EntityConstraintViolation> constraintsToDelete = new HashSet<EntityConstraintViolation>();
        for (EntityConstraintViolation ecv : fromResult.getEntityConstraintViolations()){
            if (validationGroupNames.contains(ecv.getValidationGroup())) {
                constraintsToDelete.add(ecv);
            }
        }
        for (EntityConstraintViolation ecv : constraintsToDelete){
            fromResult.removeEntityConstraintViolation(ecv);
        }

    }

    private static <T extends ICdmBase> void addNewErrors(EntityValidation toResult, T validatedEntity,
            Set<ConstraintViolation<T>> errors) {
        for (ConstraintViolation<T> error : errors) {
            EntityConstraintViolation violation = EntityConstraintViolation.newInstance(validatedEntity, error);
            toResult.addEntityConstraintViolation(violation);
            violation.setEntityValidation(toResult);
        }
    }

    @SuppressWarnings("unused")
    private void deletedErrorRecords(int validationResultId, Class<?>[] validationGroups) {
        StringBuilder sql = new StringBuilder(127);
        sql.append("DELETE FROM EntityConstraintViolation ecv WHERE ecv.entityValidation.id = :id");
        if (validationGroups != null && validationGroups.length != 0) {
            sql.append(" AND (");
            for (int i = 0; i < validationGroups.length; ++i) {
                if (i != 0) {
                    sql.append(" OR ");
                }
                sql.append("validationgroup = :param" + i);
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
