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
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

public abstract class DaoBase {

    private static final Logger logger = LogManager.getLogger();

    public final static boolean NO_UNPUBLISHED = false;  //constant for unpublished
    public final static boolean INCLUDE_UNPUBLISHED = true;  //constant for unpublished

    @Autowired
    // @Qualifier("defaultBeanInitializer")
    protected IBeanInitializer defaultBeanInitializer;

    public void setDefaultBeanInitializer(IBeanInitializer defaultBeanInitializer) {
        this.defaultBeanInitializer = defaultBeanInitializer;
    }

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


    /**
     * Workaround for https://dev.e-taxonomy.eu/redmine/issues/5871 and #5945
     * Terms with multiple representations return identical duplicates
     * due to eager representation loading. We expect these duplicates to appear
     * in line wo we only compare one term with its predecessor. If it already
     * exists we remove it from the result.
     * @param orginals
     * @return
     */
    protected static <S extends CdmBase> List<S> deduplicateResult(List<S> orginals) {
        List<S> result = new ArrayList<>();
        Iterator<S> it = orginals.iterator();
        S last = null;
        while (it.hasNext()){
            S a = it.next();
            if (a != last){
                //AM: why is this necessary?
                if (!result.contains(a)){
                    result.add(a);
                }
            }
            last = a;
        }
        return result;
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
        Root<T> root = q.from(type);
        q.select(root);
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

    protected <T> TypedQuery<T> addLimitAndStart(TypedQuery<T> query, Integer limit, Integer start) {
        if(limit != null) {
            if(start != null) {
                query.setFirstResult(start);
            } else {
                query.setFirstResult(0);
            }
            query.setMaxResults(limit);
        }
        return query;
    }

    protected <T> TypedQuery<T> addPageSizeAndNumber(TypedQuery<T> query, Integer pageSize, Integer pageNumber) {
        if(pageSize != null) {
            query.setMaxResults(pageSize);
            if(pageNumber != null) {
                query.setFirstResult(pageNumber * pageSize);
            } else {
                query.setFirstResult(0);
            }
        }
        return query;
    }

    protected <T> CriteriaQuery<?> order(CriteriaBuilder builder, CriteriaQuery<T> query,
            Root<?> root, List<OrderHint> orderHints){
        if (CdmUtils.isNullSafeEmpty(orderHints)) {
            return query;
        }else {
            List<Order> orders = ordersFrom(builder, root, orderHints);
            query.orderBy(orders);
            return query;
        }
    }

    protected List<Order> ordersFrom(CriteriaBuilder builder, Root<?> root, List<OrderHint> orderHints) {
        List<Order> orders = new ArrayList<>();
        if (CdmUtils.isNullSafeEmpty(orderHints)) {
            return orders;
        }else {
            orderHints.forEach(oh->{
               String propertyName = oh.getPropertyName();
               String[] props = propertyName.split("\\.");
               Path<?> currentPath = root;
//               int i = 1;
               for (String prop : props) {
                   //** if the order path includes a non-terminal attribute which
                   //   is not joined before the join (inner join) will be automatically
                   //   created. This also influences the general query result
                   //   as the inner join filters the result if null values are allowed
                   //   for the attribute. Here we try to check if the attribute is joined
                   //   already and if not, the OUTER join is added.
                   //   Problems to solve are:
                   //     1) Distinguish attributes representing an entity from those for a
//                             component or basic type
                   //     2) recursively pass the Path/From as path.get(name) returns a Path only
//                   if (i < props.length && !hasJoin(currentPath, prop)) {
//                       Join<Object, Object> propJoin = root.join(prop, JoinType.LEFT);
//                   }
//                   joins.forEach(j->{
//                       System.out.println (j.getAttribute().getName());
//                   });
                   currentPath = currentPath.get(prop);
//                   boolean isCompound = currentPath.isCompoundSelection();
//                   if (isCompound) {
//                       List<Selection<?>> sel = currentPath.getCompoundSelectionItems();
//                   }
//                   Bindable<?> model = currentPath.getModel();
//                   BindableType bt = model.getBindableType();
//                   Class<?> bjt = model.getBindableJavaType();
//                   Class<?> t = currentPath.getJavaType();
//                   System.out.println("1");
               }
               Order ord = oh.isAscending() ? builder.asc(currentPath) : builder.desc(currentPath);
               orders.add(ord);
            });
            return orders;
        }
    }

//    private boolean hasJoin(From<?,?> path, String propName) {
//        Set<Join> joins = (Set)path.getJoins();
//        return joins.stream()
//            .filter(j->j.getAttribute().getName().equals(propName))
//            .count() > 0;
//    }


    /**
     * Returns a {@link Predicate} that compares a string including null compare
     *
     * @param builder the {@link CriteriaBuilder}
     * @param path the path representing the entity
     * @param field the entitie's field
     * @param str the text to compare with
     * @return the predicate
     */
    protected <T extends CdmBase> Predicate predicateStrOrNull(CriteriaBuilder builder,
            Path<T> path, String field, String str) {

        if (str == null){
            return predicateIsNull(builder, path, field);
        }else{
            return predicateStrNotNull(builder, path, field, str);
        }
    }

    /**
     *
     * Returns a {@link Predicate} that compares a string but does not allow null
     *
     * @param builder the {@link CriteriaBuilder}
     * @param path the path representing the entity
     * @param field the entitie's field
     * @param str the text to compare with
     * @return the predicate
     */
    protected <T extends CdmBase> Predicate predicateStrNotNull(CriteriaBuilder builder,
            Path<T> path, String field, String str) {

        return builder.equal(path.get(field), str);
    }

    protected <T extends CdmBase> Predicate predicateLike(CriteriaBuilder builder,
            Path<T> path, String field, String pattern) {

        return builder.like(path.get(field), pattern);
    }

    protected <T extends CdmBase> Predicate predicateILike(CriteriaBuilder builder,
            Path<T> path, String field, String pattern) {

        return builder.like(builder.lower(path.get(field)), pattern.toLowerCase());
    }

    protected <T extends CdmBase> Predicate predicateEqual(CriteriaBuilder builder,
            Path<T> path, String field, Object obj) {

        return builder.equal(path.get(field), obj);
    }

    protected <T extends CdmBase> Predicate predicateCollectionSize(CriteriaBuilder builder,
            Path<T> path, String field, Number num) {

        return builder.equal(builder.size(path.get(field)), num);
    }

    protected <T extends CdmBase> Predicate predicateIn(Path<T> root, String fieldName, Collection<?> collection) {
        return root.get(fieldName).in(collection);
    }

    /**
     * Returns a {@link Predicate} that checks if a field is null.
     *
     * @param builder the {@link CriteriaBuilder}
     * @param path the path representing the entity
     * @param field the entitie's field
     * @return the predicate
     */
    protected <T extends CdmBase> Predicate predicateIsNull(CriteriaBuilder builder,
            Path<T> path, String field) {

        Predicate result = builder.isNull(path.get(field));
        result.alias(field+"_isNull");
        return result;
    }

    /**
    *
    * Returns a {@link Predicate} that compares a string but does not allow null
    *
    * @param builder the {@link CriteriaBuilder}
    * @param path the path representing the entity
    * @param field the entitie's field
    * @param str the text to compare with
    * @return the predicate
    */
    protected <T extends CdmBase> Predicate predicateUuid(CriteriaBuilder builder,
            Path<T> path, UUID uuid) {

        return builder.equal(path.get("uuid"), uuid);
    }

   /**
    *
    * Returns a {@link Predicate} that compares a string but does not allow null
    *
    * @param builder the {@link CriteriaBuilder}
    * @param path the path representing the entity
    * @param field the entitie's field
    * @param str the text to compare with
    * @return the predicate
    */
    protected <T extends CdmBase> Predicate predicateBoolean(CriteriaBuilder builder,
            Path<T> path, String field, Boolean bool) {

        return builder.equal(path.get(field), bool);
    }


    /**
     * Creates a predicate which is an AND-predicate for all predicates
     * in the list.
     */
    protected Predicate predicateAnd(CriteriaBuilder cb, List<Predicate> predicates) {
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    /**
     * Shortcut to get a {@link CriteriaBuilder} from the current session.
     */
    protected CriteriaBuilder getCriteriaBuilder() {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        return builder;
    }
}