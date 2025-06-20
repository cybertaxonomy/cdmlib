/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author muellera
 * @since 20.06.2025
 */
public abstract class CdmBaseDaoImpl
        extends DaoBase {

    private static final Logger logger = LogManager.getLogger();

    UUID saveOrUpdate_(CdmBase transientObject) throws DataAccessException {
        if (transientObject == null) {
            logger.warn("Object to save should not be null. NOP");
            return null;
        }
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("dao saveOrUpdate start...");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("transientObject(" + transientObject.getClass().getSimpleName() + ") ID:"
                        + transientObject.getId() + ", UUID: " + transientObject.getUuid());
            }
            Session session = getSession();
            if (transientObject.getId() != 0 && VersionableEntity.class.isAssignableFrom(transientObject.getClass())) {
                VersionableEntity versionableEntity = (VersionableEntity) transientObject;
                versionableEntity.setUpdated(new DateTime());
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.getPrincipal() != null
                        && authentication.getPrincipal() instanceof User) {
                    User user = (User) authentication.getPrincipal();
                    versionableEntity.setUpdatedBy(user);
                }
            }
            session.saveOrUpdate(transientObject);
            if (logger.isDebugEnabled()) {
                logger.debug("dao saveOrUpdate end");
            }
            return transientObject.getUuid();
        } catch (NonUniqueObjectException e) {
            logger.error("Error in CdmEntityDaoBase.saveOrUpdate(obj). ID=" + e.getIdentifier() + ". Class="
                    + e.getEntityName());
            logger.error(e.getMessage());

            e.printStackTrace();
            throw e;
        } catch (HibernateException e) {

            e.printStackTrace();
            throw e;
        }
    }

    <S extends CdmBase> S save_(S newInstance) throws DataAccessException {
        if (newInstance == null) {
            logger.warn("Object to save should not be null. NOP");
            return null;
        }
        getSession().save(newInstance);
        return newInstance;
    }


    UUID update_(CdmBase transientObject) throws DataAccessException {
        if (transientObject == null) {
            logger.warn("Object to update should not be null. NOP");
            return null;
        }
        getSession().update(transientObject);
        return transientObject.getUuid();
    }


    UUID delete_(CdmBase objectToDelete) throws DataAccessException {
        if (objectToDelete == null) {
            logger.info("object to delete was 'null'");
            return null;
        } else if (!objectToDelete.isPersisted()) {
            logger.info(objectToDelete.getClass().getName() + " was not persisted yet");
            return null;
        }

        // Ben Clark:
        // Merge the object in if it is detached
        //
        // I think this is preferable to catching lazy initialization errors
        // as that solution only swallows and hides the exception, but doesn't
        // actually solve it.
        CdmBase persistentObject = (CdmBase)getSession().merge(objectToDelete);
        getSession().delete(persistentObject);
        return persistentObject.getUuid();
    }

    UUID refresh_(CdmBase persistentObject) throws DataAccessException {
        getSession().refresh(persistentObject);
        return persistentObject.getUuid();
    }

    <S extends CdmBase> List<S> list_(Class<S> clazz, Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths) {

        Criteria criteria = getSession().createCriteria(clazz);

        addLimitAndStart(criteria, limit, start);

        addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<S> results = criteria.list();

        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }




//  @Override
//  public long count(Class<? extends CdmBase> type) {
//      // TODO Auto-generated method stub
//      return 0;
//  }
//
//  @Override
//  public <S extends CdmBase> List<S> list(Class<S> type, Integer limit, Integer start, List<OrderHint> orderHints,
//          List<String> propertyPaths) {
//      // TODO Auto-generated method stub
//      return null;
//  }
//
//  @Override
//  public UUID refresh(CdmBase persistentObject) throws DataAccessException {
//      // TODO Auto-generated method stub
//      return null;
//  }


}
