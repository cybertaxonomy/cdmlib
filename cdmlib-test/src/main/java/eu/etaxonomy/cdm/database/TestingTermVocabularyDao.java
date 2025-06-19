/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author a.mueller
 * @since 05.11.2015
 */
@Repository
public class TestingTermVocabularyDao {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private SessionFactory factory;


    protected TermVocabulary<?> findByUuid(UUID uuid) throws DataAccessException{

        CriteriaBuilder cb = getSession().getCriteriaBuilder();

        @SuppressWarnings({"rawtypes", "unchecked" })
        CriteriaQuery<TermVocabulary<?>> cq = (CriteriaQuery)cb.createQuery(TermVocabulary.class);
        @SuppressWarnings({"rawtypes" })
        Root<TermVocabulary> voc = cq.from(TermVocabulary.class);
        cq.where(cb.equal(voc.get("uuid"), uuid));
        cq.orderBy(cb.desc(voc.get("created")));

        List<TermVocabulary<?>> results = getSession().createQuery(cq).getResultList();

        if (results.isEmpty()){
            return null;
        }else{
            if(results.size() > 1){
                logger.error("findByUuid() delivers more than one result for UUID: " + uuid);
            }
            return results.get(0);
        }
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.factory = sessionFactory;
    }
    public SessionFactory getSessionFactory() {
        return factory;
    }
    protected Session getSession(){
        Session session = factory.getCurrentSession();
        return session;
    }
}