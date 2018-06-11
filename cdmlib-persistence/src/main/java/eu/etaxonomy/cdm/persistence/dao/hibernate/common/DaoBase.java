/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.persistence.query.OrderHint;

public abstract class DaoBase {

    final static Logger logger = Logger.getLogger(DaoBase.class);

    public final static boolean NO_UNPUBLISHED = false;  //constant for unpublished
    public final static boolean INCLUDE_UNPUBLISHED = true;  //constant for unpublished

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
            logger.error("Opening new session in turn of a HibernateException", e);
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
            if (o1.equals(o2)){
                return 0;
            }
            int result = o1.getPropertyName().compareTo(o2.getPropertyName());
            if (result == 0){
                result = o1.toString().compareTo(o2.toString());
            }
            return result;
        }

    }

    protected void addOrder(Criteria criteria, List<OrderHint> orderHints) {

        if(orderHints != null){
            Collections.sort(orderHints, new OrderHintComparator());

            Map<String,Criteria> criteriaMap = new HashMap<>();
            for(OrderHint orderHint : orderHints){
                orderHint.add(criteria, criteriaMap);
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


    /**
     * Splits a set of e.g. query parameters into a list of sets with size <code>splitSize</code>.
     * Only the last set may be smaller if the collection's size is not an exact multiple of split size.
     * @param collection the collection to split
     * @param splitSize the split size
     * @return a list of collections
     */
    protected <T extends Object> List<Collection<T>> splitCollection(Set<T> collection, int splitSize) {
        if (splitSize < 1){
            throw new IllegalArgumentException("Split size must not be positive integer");
        }
        List<Collection<T>> result = new ArrayList<>();
        Iterator<T> it = collection.iterator();
        int i = 0;
        Set<T> nextCollection = new HashSet<>();
        while (it.hasNext()){
            nextCollection.add(it.next());
            i++;
            if (i == splitSize){
                result.add(nextCollection);
                nextCollection = new HashSet<>();
                i = 0;
            }
        }
        if (!nextCollection.isEmpty()){
            result.add(nextCollection);
        }
        return result;
    }

}
