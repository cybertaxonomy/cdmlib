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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextQuery;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public abstract class DaoBase {

    private static final Logger logger = LogManager.getLogger();

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
            String stackTrace = "";
            for(StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
                stackTrace = stackTrace + System.lineSeparator() + stackTraceElement.toString();
            }
            logger.warn("[#7106] Opening new session in turn of a HibernateException: " + e.getMessage() + System.lineSeparator() + stackTrace);
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

    // -------------- hql, query and criteria helper methods -------------- //

    protected void addFieldPredicate(StringBuilder hql, String field, Optional<String> value) {
        if(value != null){
            hql.append("AND " + field);
            if(value.isPresent()){
                if(value.get().contains("*")){
                    hql.append(" LIKE '" + value.get().replace('*', '%') + "' ");
                } else {
                    hql.append(" = '" + value.get() + "' ");
                }
            } else {
                hql.append(" IS NULL ");
            }
        }
    }

    protected void addPageSizeAndNumber(AuditQuery query, Integer pageSize, Integer pageNumber) {
        if(pageSize != null) {
            query.setMaxResults(pageSize);
            if(pageNumber != null) {
                query.setFirstResult(pageNumber * pageSize);
            } else {
                query.setFirstResult(0);
            }
        }
    }

    protected void addPageSizeAndNumber(Criteria criteria, Integer pageSize, Integer pageNumber) {
        if(pageSize != null) {
            criteria.setMaxResults(pageSize);
            if(pageNumber != null) {
                criteria.setFirstResult(pageNumber * pageSize);
            } else {
                criteria.setFirstResult(0);
            }
        }
    }

    protected void addLimitAndStart(Criteria criteria, Integer limit, Integer start) {
        if(limit != null) {
            if(start != null) {
                criteria.setFirstResult(start);
            } else {
                criteria.setFirstResult(0);
            }
            criteria.setMaxResults(limit);
        }
    }

    protected void addLimitAndStart(AuditQuery query, Integer limit, Integer start) {
        if(limit != null) {
            if(start != null) {
                query.setFirstResult(start);
            } else {
                query.setFirstResult(0);
            }
            query.setMaxResults(limit);
        }
    }

    protected void addCriteria(Criteria criteria, List<Criterion> criterion) {
        if(criterion != null) {
            for(Criterion c : criterion) {
                criteria.add(c);
            }
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

    protected void addOrder(FullTextQuery fullTextQuery, List<OrderHint> orderHints) {
        //FIXME preliminary hardcoded type:
        SortField.Type type = SortField.Type.STRING;

        if(orderHints != null && !orderHints.isEmpty()) {
            org.apache.lucene.search.Sort sort = new Sort();
            SortField[] sortFields = new SortField[orderHints.size()];
            for(int i = 0; i < orderHints.size(); i++) {
                OrderHint orderHint = orderHints.get(i);
                switch(orderHint.getSortOrder()) {
                case ASCENDING:
                    sortFields[i] = new SortField(orderHint.getPropertyName(), type, true);
                    break;
                case DESCENDING:
                default:
                    sortFields[i] = new SortField(orderHint.getPropertyName(), type, false);
                }
            }
            sort.setSort(sortFields);
            fullTextQuery.setSort(sort);
        }
    }

    /**
     * Null save method which compiles an order by clause from the given list of OrderHints
     *
     * @param orderHints can be NULL
     * @return a StringBuffer holding the hql orderby clause
     */
    protected StringBuilder orderByClause(String aliasName, List<OrderHint> orderHints) {

        StringBuilder orderString = new StringBuilder();

        if(orderHints != null && !orderHints.isEmpty()) {
            StringBuffer aliasPrefix = new StringBuffer(" ");
            if(aliasName != null && !aliasName.isEmpty()){
                aliasPrefix.append(aliasName).append(".");
            }

            for(OrderHint orderHint : orderHints) {
                orderString.append((orderString.length() < 2) ? " ORDER BY " : "," );
                orderString.append(aliasPrefix).append(orderHint.toHql());
            }
        }
        return orderString;
    }

    protected void addOrder(StringBuilder hql, String alias, List<OrderHint> orderHints) {
        hql.append(orderByClause(alias, orderHints));
    }

    //*************************** JPA **********************************************/

    /**
     * Lists all entries of the given class. Should be open to the public
     * only for those DAOs which are expected to not have larger numbers
     * of entries (e.g. CdmPreference , {@link Classification}, ...).
     * Callers must ensure that class of <code>type</code> is a valid
     * entity class managed by the entity manager.
     */
    protected <T> List<T> list(Class<T> type){
        CriteriaQuery<T> q = getCriteriaBuilder().createQuery(type);
        return getSession().createQuery(q).getResultList();
    }

    /**
     * Returns the count of all entities for the given entity class.
     * Callers must ensure that class of <code>type</code> is a valid
     * entity class managed by the entity manager. Should be called only
     * by {@link CdmEntityDaoBase} except for cases where entities
     * have no <code>id</code> (are not of type {@link CdmBase},
     * e.g. CdmMetaData).
     *
     * @param type the entity type
     * @return the count result
     */
    protected long count_(Class<?> type) {

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(long.class);

        cq.select(cb.count(cq.from(type)));
        Long result = getSession().createQuery(cq).uniqueResult();

        return result;
    }

    protected void addLimitAndStart(TypedQuery<?> query, Integer limit, Integer start) {
        if(limit != null) {
            if(start != null) {
                query.setFirstResult(start);
            } else {
                query.setFirstResult(0);
            }
            query.setMaxResults(limit);
        }
    }

    protected void addPageSizeAndNumber(TypedQuery<?> query, Integer pageSize, Integer pageNumber) {
        if(pageSize != null) {
            query.setMaxResults(pageSize);
            if(pageNumber != null) {
                query.setFirstResult(pageNumber * pageSize);
            } else {
                query.setFirstResult(0);
            }
        }
    }
}