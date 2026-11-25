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
import org.springframework.dao.DataAccessException;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

/**
 * @author muellera
 * @since 20.06.2025
 */
public abstract class CdmBaseDaoImpl
        extends DaoBase {

    private static final Logger logger = LogManager.getLogger();

    <S extends CdmBase> S save_(S newInstance) throws DataAccessException {
        if (newInstance == null) {
            logger.warn("Object to save should not be null. NOP");
            return null;
        }
        getSession().persist(newInstance);
        return newInstance;
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
        getSession().remove(persistentObject);
        return persistentObject.getUuid();
    }

    UUID refresh_(CdmBase persistentObject) throws DataAccessException {
        getSession().refresh(persistentObject);
        return persistentObject.getUuid();
    }

    <S extends CdmBase> List<S> list_(Class<S> clazz, Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths) {

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<S> cq = cb.createQuery(clazz);
        Root<S> root = cq.from(clazz);
        cq.select(root)
          .orderBy(ordersFrom(cb, root, orderHints));

        List<S> results = addLimitAndStart(getSession().createQuery(cq), limit, start)
                .getResultList();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }
}