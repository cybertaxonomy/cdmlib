// $Id$
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

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * @author a.mueller
 * @date 05.11.2015
 *
 */
@Repository
public class TestingTermVocabularyDao {
    private static final Logger logger = Logger.getLogger(TestingTermVocabularyDao.class);


    @Autowired
    private SessionFactory factory;

    protected TermVocabulary<?> findByUuid(UUID uuid) throws DataAccessException{
        Session session = getSession();
        Criteria crit = session.createCriteria(TermVocabulary.class);
        crit.add(Restrictions.eq("uuid", uuid));
        crit.addOrder(Order.desc("created"));
        @SuppressWarnings("unchecked")
        List<TermVocabulary<?>> results = crit.list();
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
