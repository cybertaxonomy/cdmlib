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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityConstraintViolationDao;

/**
 * @author ayco_holleman
 * @since 15 jan. 2015
 */
@Repository
@Qualifier("entityConstraintViolationHibernateImpl")
public class EntityConstraintViolationHibernateImpl extends CdmEntityDaoBase<EntityConstraintViolation> implements
        IEntityConstraintViolationDao {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    public EntityConstraintViolationHibernateImpl() {
        super(EntityConstraintViolation.class);
    }

    @Override
    public List<EntityConstraintViolation> getConstraintViolations() {
        Query<EntityConstraintViolation> query = getSession().createQuery(
                "FROM EntityConstraintViolation cv " + "JOIN FETCH cv.entityValidation ev "
                        + "ORDER BY ev.validatedEntityClass, ev.validatedEntityId",
                        EntityConstraintViolation.class);
        List<EntityConstraintViolation> result = query.list();
        return result;
    }

    @Override
    public List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass) {
        Query<EntityConstraintViolation> query = getSession().createQuery(
                "FROM EntityConstraintViolation cv " + "JOIN FETCH cv.entityValidation ev "
                        + "WHERE ev.validatedEntityClass = :cls "
                        + "ORDER BY ev.validatedEntityClass, ev.validatedEntityId",
                        EntityConstraintViolation.class);
        query.setParameter("cls", validatedEntityClass);
        List<EntityConstraintViolation> result = query.list();
        return result;
    }

    @Override
    public List<EntityConstraintViolation> getConstraintViolations(String validatedEntityClass, Severity severity) {
        Query<EntityConstraintViolation> query = getSession().createQuery(
                "FROM EntityConstraintViolation cv " + "JOIN FETCH cv.entityValidation ev "
                        + "WHERE ev.validatedEntityClass = :cls " + "AND cv.severity = :severity "
                        + "ORDER BY ev.validatedEntityClass, ev.validatedEntityId",
                        EntityConstraintViolation.class);
        query.setParameter("cls", validatedEntityClass);
        query.setParameter("severity", severity.toString());
        List<EntityConstraintViolation> result = query.list();
        return result;
    }

    @Override
    public List<EntityConstraintViolation> getConstraintViolations(Severity severity) {
        Query<EntityConstraintViolation> query = getSession().createQuery(
                "FROM EntityConstraintViolation cv " + "JOIN FETCH cv.entityValidation ev "
                        + "WHERE cv.severity = :severity " + "ORDER BY ev.validatedEntityClass, ev.validatedEntityId",
                        EntityConstraintViolation.class);
        query.setParameter("severity", severity.toString());
        List<EntityConstraintViolation> result = query.list();
        return result;
    }
}