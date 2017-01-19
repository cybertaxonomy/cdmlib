/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.persistence.query.OrderHint;

public abstract class DaoBase {

    @Autowired
    private SessionFactory factory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.factory = sessionFactory;
    }
    public SessionFactory getSessionFactory() {
        return factory;
    }
    protected Session getSession(){
        Session session ;
        try {
            session = factory.getCurrentSession();
        } catch (HibernateException e) {
            session = factory.openSession();
        }
        return session;
    }

    public void flush(){
        getSession().flush();
    }

    private class OrderHintComparator implements Comparator<OrderHint> {

        @Override
        public int compare(OrderHint o1, OrderHint o2) {
            return o1.getPropertyName().compareTo(o2.getPropertyName());
        }

    }

    protected void addOrder(Criteria criteria, List<OrderHint> orderHints) {

        if(orderHints != null){
            Collections.sort(orderHints, new OrderHintComparator());

            Map<String,Criteria> criteriaMap = new HashMap<String,Criteria>();
            for(OrderHint orderHint : orderHints){
                orderHint.add(criteria,criteriaMap);
            }
        }
    }

    /**
     * Null save method which compiles a order by clause from the given list of OrderHints
     *
     * @param orderHints can be NULL
     * @return a StringBuffer holding the hql orderby clause
     */
    protected StringBuffer orderByClause(List<OrderHint> orderHints, String aliasName) {

        StringBuffer orderString = new StringBuffer();

        StringBuffer aliasPrefix = new StringBuffer();
        aliasPrefix.append(" ");
        if(aliasName != null && !aliasName.isEmpty()){
            aliasPrefix.append(aliasName).append(".");
        }

        if(orderHints != null && !orderHints.isEmpty()) {
            orderString.append(" order by");
            for(OrderHint orderHint : orderHints) {
                orderString.append(aliasPrefix).append(orderHint.toHql());
            }
        }
        return orderString;
    }

}
