/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import eu.etaxonomy.cdm.api.filter.EntityFilter;
import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.api.filter.Restriction;
import eu.etaxonomy.cdm.api.filter.Restriction.Operator;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IPublishable;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.persistence.hibernate.PostMergeEntityListener;
import eu.etaxonomy.cdm.persistence.hibernate.replace.ReferringObjectMetadata;
import eu.etaxonomy.cdm.persistence.hibernate.replace.ReferringObjectMetadataFactory;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * Hibernate implementation for {@link ICdmEntityDao}.
 */
public abstract class CdmEntityDaoBase<T extends CdmBase>
        extends CdmBaseDaoImpl
        implements ICdmEntityDao<T> {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private ICdmGenericDao genericDao;

    protected Class<T> type;

    @Autowired
    private ReferringObjectMetadataFactory referringObjectMetadataFactory;

    protected static final EnumSet<Operator> LEFTOUTER_OPS = EnumSet.of(Operator.AND_NOT, Operator.OR, Operator.OR_NOT);

    public CdmEntityDaoBase(Class<T> type) {
        this.type = type;
        assert type != null;
        logger.debug("Creating DAO of type [" + type.getSimpleName() + "]");
    }

    @Override
    public void lock(T t, LockOptions lockOptions) {
        getSession().lock(t, lockOptions.getLockMode());
    }

    @Override
    public UUID refresh(T persistentObject) throws DataAccessException {
        return super.refresh_(persistentObject);
    }

    @Override
    public void refresh(T t, LockOptions lockOptions, List<String> propertyPaths) {
        getSession().refresh(t, lockOptions.getLockMode());
        defaultBeanInitializer.initialize(t, propertyPaths);
    }

    // TODO this method should be moved to a concrete class (not typed)
    public UUID saveCdmObj(CdmBase cdmObj) throws DataAccessException {
        getSession().saveOrUpdate(cdmObj);
        return cdmObj.getUuid();
    }

    // TODO: Replace saveCdmObj() by saveCdmObject_
    private UUID saveCdmObject_(T cdmObj) {
        getSession().saveOrUpdate(cdmObj);
        return cdmObj.getUuid();
    }

    // TODO: Use everywhere CdmEntityDaoBase.saveAll() instead of
    // ServiceBase.saveCdmObjectAll()?
    // TODO: why does this use saveCdmObject_ which actually savesOrUpdateds
    // data ?
    @Override
    public Map<UUID, T> saveAll(Collection<? extends T> cdmObjCollection) {
        int types = cdmObjCollection.getClass().getTypeParameters().length;
        if (types > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("ClassType: + " + cdmObjCollection.getClass().getTypeParameters()[0]);
            }
        }

        Map<UUID, T> resultMap = new HashMap<>();
        Iterator<? extends T> iterator = cdmObjCollection.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            if (((i % 2000) == 0) && (i > 0)) {
                logger.debug("Saved " + i + " objects");
            }
            T cdmObj = iterator.next();
            UUID uuid = saveCdmObject_(cdmObj);
            if (logger.isDebugEnabled()) {
                logger.debug("Save cdmObj: " + (cdmObj == null ? null : cdmObj.toString()));
            }
            resultMap.put(uuid, cdmObj);
            i++;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Saved " + i + " objects");
        }
        return resultMap;
    }

    private UUID saveOrUpdateCdmObject(T cdmObj) {
        getSession().saveOrUpdate(cdmObj);
        return cdmObj.getUuid();
    }

    @Override
    public Map<UUID, T> saveOrUpdateAll(Collection<T> cdmObjCollection) {
        int types = cdmObjCollection.getClass().getTypeParameters().length;
        if (types > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("ClassType: + " + cdmObjCollection.getClass().getTypeParameters()[0]);
            }
        }

        Map<UUID, T> resultMap = new HashMap<>();
        Iterator<T> iterator = cdmObjCollection.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            if (((i % 2000) == 0) && (i > 0)) {
                logger.debug("Saved " + i + " objects");
            }
            T cdmObj = iterator.next();
            UUID uuid = saveOrUpdateCdmObject(cdmObj);
            if (logger.isDebugEnabled()) {
                logger.debug("Save cdmObj: " + (cdmObj == null ? null : cdmObj.toString()));
            }
            resultMap.put(uuid, cdmObj);
            i++;
            //Note AM: the following creates more problems than it solves due to unsaved transient instance exceptions, therefore I removed it for now.
//            if ((i % flushAfterNo) == 0) {
//                try {
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("flush");
//                    }
//                    flush();
//                } catch (Exception e) {
//                    logger.error("An exception occurred when trying to flush data");
//                    e.printStackTrace();
//                    throw new RuntimeException(e);
//                }
//            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Saved " + i + " objects");
        }
        return resultMap;
    }

    @Override
    public T replace(T x, T y) {
        if (x.equals(y)) {
            return y;
        }

        Class<?> commonClass = x.getClass();
        if (y != null) {
            while (!commonClass.isAssignableFrom(y.getClass())) {
                if (commonClass.equals(type)) {
                    throw new RuntimeException();
                }
                commonClass = commonClass.getSuperclass();
            }
        }

        getSession().merge(x);

        Set<ReferringObjectMetadata> referringObjectMetas = referringObjectMetadataFactory.get(x.getClass());

        for (ReferringObjectMetadata referringObjectMetadata : referringObjectMetas) {

            List<? extends CdmBase> referringObjects = referringObjectMetadata.getReferringObjects(x, getSession());

            for (CdmBase referringObject : referringObjects) {
                try {
                    referringObjectMetadata.replace(referringObject, x, y);
                    getSession().update(referringObject);

                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return y;
    }

    @Override
    public Session getSession() throws DataAccessException {
        return super.getSession();
    }

    @Override
    public void clear() throws DataAccessException {
        Session session = getSession();
        session.clear();
        if (logger.isDebugEnabled()) {
            logger.debug("dao clear end");
        }
    }

    @Override
    public MergeResult<T> merge(T transientObject, boolean returnTransientEntity) throws DataAccessException {
        Session session = getSession();
        PostMergeEntityListener.addSession(session);
        MergeResult<T> result = null;
        try {
            @SuppressWarnings("unchecked")
            T persistentObject = (T) session.merge(transientObject);
            if (logger.isDebugEnabled()) {
                logger.debug("dao merge end");
            }

            if (returnTransientEntity) {
                if (transientObject != null && persistentObject != null) {
                    transientObject.setId(persistentObject.getId());
                }
                result = new MergeResult(transientObject, PostMergeEntityListener.getNewEntities(session));
            } else {
                result = new MergeResult(persistentObject, null);
            }
//            if (transientObject instanceof DescriptionBase) {
//                logger.info("merged object: "+result.getMergedEntity().getUuid() + " - " + result.getMergedEntity().getUserFriendlyTypeName());
//                for(Object newEntity : result.getNewEntities()) {
//                    if (newEntity instanceof CdmBase) {
//                        logger.info("new object: "+((CdmBase)newEntity).getUuid() + " - " + ((CdmBase)newEntity).getUserFriendlyTypeName());
//                    }
//                }
//            }
            return result;
        } finally {
            PostMergeEntityListener.removeSession(session);
        }
    }

    @Override
    public T merge(T transientObject) throws DataAccessException {
        Session session = getSession();
        @SuppressWarnings("unchecked")
        T persistentObject = (T) session.merge(transientObject);
        if (logger.isDebugEnabled()) {
            logger.debug("dao merge end");
        }
        return persistentObject;
    }

    @Override
    public T merge(T transientEntity, Collection<CdmBase> detachedObjectsToRemove) throws DataAccessException{
        T result = merge(transientEntity);
        for (CdmBase detachedObject : detachedObjectsToRemove) {
            CdmBase persistedObject = genericDao.find(CdmBase.deproxy(detachedObject.getClass()), detachedObject.getUuid());
            if (persistedObject != null) {
                genericDao.delete(persistedObject);
            }
        }
        return result;
    }

    @Override
    public UUID saveOrUpdate(T transientObject) throws DataAccessException {
        return super.saveOrUpdate_(transientObject);
    }

    @Override
    public void save(T newInstance1, T newInstance2) throws DataAccessException {
        save(newInstance1);
        save(newInstance2);
    }


    @Override
    public <S extends T> S save(S newInstance) throws DataAccessException {
        return super.save_(newInstance);
    }

    @Override
    public UUID update(T transientObject) throws DataAccessException {
        return super.update_(transientObject);
    }

    @Override
    public UUID delete(T objectToDelete) throws DataAccessException {
        return super.delete_(objectToDelete);
    }

    @Override
    public T findById(int id) throws DataAccessException {
        return getSession().get(type, id);
    }

    @Override
    public T findByUuid(UUID uuid) throws DataAccessException {
        return this.findByUuid(uuid, INCLUDE_UNPUBLISHED);
    }

    private T findByUuid(UUID uuid, boolean includeUnpublished) throws DataAccessException {

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(type);
        Root<T> root = cq.from(type);
        Predicate where = predicateUuid(cb, root, uuid);
        if (IPublishable.class.isAssignableFrom(type) && !includeUnpublished) {
            where = cb.and(where, predicateBoolean(cb, root, "publish", Boolean.TRUE));
        }
        cq.select(root)
          .where(where)
          .orderBy(cb.desc(root.get("created")));

        List<T> results = getSession().createQuery(cq).getResultList();

        Set<T> resultSet = new HashSet<>();
        resultSet.addAll(results);
        if (resultSet.isEmpty()) {
            return null;
        } else {
            if (resultSet.size() > 1) {
                logger.error("findByUuid() delivers more than one result for UUID: " + uuid);
            }
            return results.get(0);
        }
    }

    @Override
    public T findByUuidWithoutFlush(UUID uuid) throws DataAccessException {
        return findByUuidWithoutFlush(type, uuid);
    }

    protected T findByUuidWithoutFlush(Class<T> clazz, UUID uuid) throws DataAccessException {
        Session session = getSession();
        FlushMode currentFlushMode = session.getHibernateFlushMode();
        try {
            // set flush mode to manual so that the session does not flush
            // when before performing the query
            session.setHibernateFlushMode(FlushMode.MANUAL);

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(type);
            Root<T> root = cq.from(clazz);
            cq.select(root)
              .where(predicateUuid(cb, root, uuid))
              .orderBy(cb.desc(root.get("created")));
            List<T> results = session.createQuery(cq).getResultList();

            results = deduplicateResult(results);
            if (results.isEmpty()) {
                return null;
            } else {
                if (results.size() > 1) {
                    logger.error("findByUuid() delivers more than one result for UUID: " + uuid);
                }
                return results.get(0);
            }
        } finally {
            // set back the session flush mode
            if (currentFlushMode != null) {
                session.setHibernateFlushMode(currentFlushMode);
            }
        }
    }

    @Override
    public List<T> loadList(Collection<Integer> ids, List<OrderHint> orderHints, List<String> propertyPaths) throws DataAccessException {

        if (ids.isEmpty()) {
            return new ArrayList<>(0);
        }

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(type);
        Root<T> root = cq.from(type);

        if (orderHints == null) {
            orderHints = OrderHint.defaultOrderHintsFor(type);
        }

        cq.select(root)
          .where(root.get("id").in(ids))
          .orderBy(ordersFrom(cb, root, orderHints));
        List<T> result = getSession().createQuery(cq).getResultList();

        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    @Override
    public List<T> list(Collection<UUID> uuids, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) throws DataAccessException {

        return list(null, uuids, pageSize, pageNumber, orderHints, propertyPaths);
    }

    @Override
    public <S extends T> List<S> list(Class<S> clazz, Collection<UUID> uuids, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) throws DataAccessException {

        if (uuids == null || uuids.isEmpty()) {
            return new ArrayList<>();
        }
        if (clazz == null){
            clazz = (Class)type;
        }
        if (orderHints == null) {
            orderHints = OrderHint.defaultOrderHintsFor(clazz);
        }

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<S> cq = cb.createQuery(clazz);
        Root<S> root = cq.from(clazz);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(root.get("uuid").in(uuids));

        cq.select(root)
          .where(cb.and(predicates.toArray(new Predicate[0])))
          .orderBy(ordersFrom(cb, root, orderHints));
        List<S> result = addPageSizeAndNumber(
                    getSession().createQuery(cq)
                    , pageSize, pageNumber)
                .getResultList();

        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    @Override
    public <S extends T> List<S> list(Class<S> type, List<Restriction<?>> restrictions, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        type = entityType(type);
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<S> cq = cb.createQuery(type);
        Root<S> root = cq.from(type);

        Predicate predicate = predicateFromRestrictions(cb, root, restrictions);

        cq.select(root)
          .distinct(true)
          .where(predicate)
          .orderBy(ordersFrom(cb, root, orderHints));

        List<S> results = addPageSizeAndNumber(
                getSession().createQuery(cq), pageSize, pageNumber)
               .getResultList();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return deduplicateResult(results);
    }

    private <S extends T> Predicate predicateFromRestrictions(CriteriaBuilder cb,
            Root<S> root, List<Restriction<?>> restrictions) {

        if(restrictions == null || restrictions.isEmpty()){
            return cb.conjunction();
        }

        Predicate finalPredicate = null;
        for(Restriction<?> restriction : restrictions){
            Predicate restrictionPredicate;

            String propertyPath = restriction.getPropertyName();
            Collection<? extends Object> values = restriction.getValues();

            if (CdmUtils.isNullSafeEmpty(values) ||
                    StringUtils.isBlank(propertyPath)  ){
                restrictionPredicate = cb.conjunction();  //always true
            }else {

                JoinType joinType = LEFTOUTER_OPS.contains(restriction.getOperator())
                        ? JoinType.LEFT
                        : JoinType.INNER;

                List<String> props = Arrays.asList(propertyPath.split("\\."));
                List<String> notLastProps = props.subList(0, props.size()-1);
                String lastProp = props.get(props.size() - 1);

                From<?,?> path = root;
                for (String notLastProp : notLastProps) {
                    // treat() to subclass when attribute exists only there
                    // (e.g. typeDesignations.typeSpecimen → SpecimenTypeDesignation)
                    path = joinPathSegment(cb, path, notLastProp, joinType);
                }
                path = treatForAttribute(cb, path, lastProp);

                // Legacy Hibernate Criteria: AND_NOT combines values with AND, all other operators with OR
                boolean andValues = restriction.getOperator() == Operator.AND_NOT;
                restrictionPredicate = andValues ? cb.conjunction() : cb.disjunction();
                for (Object value : values) {
                    Predicate valuePredicate = createPredicate(cb, path, lastProp, value, restriction.getMatchMode());
                    if (restriction.isNot() && props.size() > 1) {
                        // match legacy Hibernate Criteria behavior for nested properties
                        valuePredicate = cb.or(cb.not(valuePredicate), cb.isNull(path.get(lastProp)));
                    } else if (restriction.isNot()) {
                        valuePredicate = cb.not(valuePredicate);
                    }
                    restrictionPredicate = andValues
                            ? cb.and(restrictionPredicate, valuePredicate)
                            : cb.or(restrictionPredicate, valuePredicate);
                }
            }

            if (finalPredicate == null) {
                finalPredicate = restrictionPredicate;
            } else if (restriction.getOperator() == Operator.OR
                    || restriction.getOperator() == Operator.OR_NOT) {
                finalPredicate = cb.or(finalPredicate, restrictionPredicate);
            } else {
                finalPredicate = cb.and(finalPredicate, restrictionPredicate);
            }
        }
        return finalPredicate == null ? cb.conjunction() : finalPredicate;
    }

    private Predicate createPredicate(CriteriaBuilder cb, From<?, ?> path,
            String propertyName, Object value, MatchMode matchMode) {

        Predicate predicate;
        if (value == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("createPredicate() " + propertyName + " is null ");
            }
            predicate = predicateIsNull(cb, path, propertyName);
        } else if (value instanceof EnumSet<?>) {
            //in EnumSet restriction
            if (logger.isDebugEnabled()) {
                logger.debug("createRestriction() " + propertyName + " IN " + value.toString());
            }

            predicate = predicateIn(path, propertyName, (EnumSet<?>)value);
        } else if (matchMode == null || !(value instanceof String)) {
            if (logger.isDebugEnabled()) {
                logger.debug("createRestriction() " + propertyName + " = " + value.toString());
            }
            predicate = predicateEqual(cb, path, propertyName, value);
        } else {
            String queryString = (String) value;
            if (logger.isDebugEnabled()) {
                logger.debug("createRestriction() " + propertyName + " " + matchMode.getMatchOperator() + " "
                        + matchMode.queryStringFrom(queryString));
            }
            boolean ignoreCase = true;
            predicate = predicateForMatchMode(propertyName, queryString, matchMode, cb, path, ignoreCase);
        }
        return predicate;
    }

    /**
     * Joins {@code attributeName} on {@code from}, using {@link CriteriaBuilder#treat}
     * when the attribute exists only on a subclass (polymorphic associations).
     */
    private From<?, ?> joinPathSegment(CriteriaBuilder cb, From<?, ?> from, String attributeName,
            JoinType joinType) {
        From<?, ?> typed = treatForAttribute(cb, from, attributeName);
        return typed.join(attributeName, joinType);
    }

    /**
     * Returns {@code from} if it already declares {@code attributeName}, otherwise
     * {@code treat}s to a concrete subclass that declares it (legacy Hibernate Criteria
     * did this implicitly via createAlias).
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private From<?, ?> treatForAttribute(CriteriaBuilder cb, From<?, ?> from, String attributeName) {
        try {
            from.get(attributeName);
            return from;
        } catch (IllegalArgumentException e) {
            Class<?> baseType = from.getJavaType();
            if (baseType == null) {
                throw e;
            }
            for (EntityType<?> entityType : getSession().getMetamodel().getEntities()) {
                Class<?> subClass = entityType.getJavaType();
                if (!baseType.isAssignableFrom(subClass) || baseType.equals(subClass)) {
                    continue;
                }
                try {
                    entityType.getAttribute(attributeName);
                } catch (IllegalArgumentException noAttr) {
                    continue;
                }
                if (from instanceof Root) {
                    return cb.treat((Root) from, subClass);
                }
                if (from instanceof Join) {
                    return cb.treat((Join) from, subClass);
                }
                throw e;
            }
            throw e;
        }
    }


    private List<Restriction<?>> addRestriction(List<Restriction<?>> restrictions, Restriction<?> restriction, boolean atStart) {
        List<Restriction<?>> result = new ArrayList<>();
        if (restrictions == null) {
            restrictions = new ArrayList<>();
        }
        if (atStart) {
            result.add(restriction);
            result.addAll(restrictions);
        } else {
            result.addAll(restrictions);
            result.add(restriction);
        }
        return result;
    }

    protected List<Restriction<?>> addPublishOnlyRestriction(List<Restriction<?>> restrictions, boolean includeUnpublished,
            String path) {
        if(!includeUnpublished){
            final String publishField = path == null ? "publish" : path +".publish" ;
            boolean publish = true;
            Restriction<?> restriction = new Restriction<>(publishField, null, publish);
            restrictions = addRestriction(restrictions, restriction, false);
        }
        return restrictions;
    }

    protected List<Restriction<?>> addStringRestriction(List<Restriction<?>> restrictions, String param, String queryString, MatchMode matchMode) {
        Restriction<?> restriction = new Restriction<>(param, matchMode, queryString);
        restrictions = addRestriction(restrictions, restriction, true);
        return restrictions;
    }

    @Override
    public <S extends T> long count(Class<S> clazz, List<Restriction<?>> restrictions) {

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<S> root = cq.from(entityType(clazz));

        Predicate predicate = predicateFromRestrictions(cb, root, restrictions);

        cq.select(cb.countDistinct(root))
          .where(predicate);

        return getSession().createQuery(cq).getSingleResult();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <S extends T> Class<S> entityType(Class<S> clazz){
        if (clazz != null) {
            return clazz;
        } else {
            return (Class)type;
        }
    }

    @Override
    public T load(UUID uuid) {
        T bean = findByUuid(uuid);
        if (bean == null) {
            return null;
        }
        defaultBeanInitializer.load(bean);

        return bean;
    }

    @Override
    public T load(int id, List<String> propertyPaths) {
        T bean = findById(id);
        if (bean == null) {
            return bean;
        }
        defaultBeanInitializer.initialize(bean, propertyPaths);

        return bean;
    }

    @Override
    public T loadWithoutInitializing(int id){
        return this.getSession().load(type, id);
    }

    @Override
    public T load(UUID uuid, List<String> propertyPaths) {
        return this.load(uuid, INCLUDE_UNPUBLISHED, propertyPaths);
    }

    protected T load(UUID uuid, boolean includeUnpublished, List<String> propertyPaths) {
        T bean = findByUuid(uuid, includeUnpublished);
        if (bean == null) {
            return bean;
        }
        defaultBeanInitializer.initialize(bean, propertyPaths);

        return bean;
    }

    @Override
    public Boolean exists(UUID uuid) {
        if (findByUuid(uuid) == null) {
            return false;
        }
        return true;
    }

    @Override
    public long count() {
        return this.count(null);
    }

    @Override
    public long count(Class<? extends T> clazz) {
        clazz = clazz == null ? type : clazz;
        return super.count_(clazz);
    }

    /**
     * Lists all entries of the given class. Should be open to the pulic
     * only for those DAOs which are expected to not have larger numbers
     * of entries (e.g. CdmPreference , {@link Classification}, ...)
     */
    protected List<T> list(){
        return super.list(type);
        // or send to another list(...) method
    }

    @Override
    public List<T> list(Integer pageSize, Integer pageNumber) {
        return list(pageSize, pageNumber, null);
    }

    @Override
    public List<Object[]> group(Class<? extends T> clazz, Integer limit, Integer start, List<Grouping> groups,
            List<String> propertyPaths) {

        Class<? extends T> entityClass = clazz == null ? type : clazz;
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<? extends T> root = cq.from(entityClass);

        List<javax.persistence.criteria.Selection<?>> selections = new ArrayList<>();
        List<javax.persistence.criteria.Expression<?>> groupByExpressions = new ArrayList<>();
        List<javax.persistence.criteria.Order> orders = new ArrayList<>();
        Map<String, From<?, ?>> joins = new HashMap<>();

        if (groups != null) {
            for (Grouping grouping : groups) {
                grouping.addToCriteria(cb, root, joins, selections, groupByExpressions, orders);
            }
        }

        cq.multiselect(selections);
        if (!groupByExpressions.isEmpty()) {
            cq.groupBy(groupByExpressions);
        }
        if (!orders.isEmpty()) {
            cq.orderBy(orders);
        }

        List<Object[]> result = addLimitAndStart(getSession().createQuery(cq), limit, start)
                .getResultList();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    @Override
    public List<T> list(Integer pageSize, Integer pageNumber, List<OrderHint> orderHints) {
        return list(pageSize, pageNumber, orderHints, null);
    }

    @Override
    public List<T> list(Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(type);
        Root<T> root = cq.from(type);

        cq.select(root)
          .distinct(true)
          .orderBy(ordersFrom(cb, root, orderHints));

        List<T> results = addPageSizeAndNumber(
                getSession().createQuery(cq), pageSize, pageNumber)
               .getResultList();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return deduplicateResult(results);
    }

    public <S extends T> List<S> list(Class<S> type, Integer limit, Integer start, List<OrderHint> orderHints) {
        return list(type, limit, start, orderHints, null);
    }


    @Override
    public <S extends T> List<S> list(Class<S> clazz, Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths) {

        clazz = clazz == null ? (Class)type : clazz;
        return super.list_(clazz, limit, start, orderHints, propertyPaths);

    }


    @Override
    public <S extends T> List<S> list(Class<S> type, Integer limit, Integer start) {
        return list(type, limit, start, null, null);
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public long count(T example, Set<String> includeProperties) {

        Class<T> clazz = (Class)example.getClass();
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(clazz);

        Predicate predicate = getExamplePredicate(cb, root, example, includeProperties);
        cq.select(cb.countDistinct(root))
          .where(predicate);

        return getSession().createQuery(cq).getSingleResult();
    }

    private <S extends T> Predicate getExamplePredicate(CriteriaBuilder cb, Root<S> root, S example, Set<String> includeProperties) {
        if (example == null) {
            return cb.conjunction();
        }
        try {
            List<Predicate> predicates = new ArrayList<>();
            if (!CdmUtils.isNullSafeEmpty(includeProperties)) {
                for (String property : includeProperties) {
                    Object value = getPropertyValueByPath(example, property);
                    Path<?> path = root;
                    String[] segments = property.split("\\.");
                    for (String seg : segments) {
                        path = path.get(seg);
                    }
                    if (value == null) {
                        predicates.add(cb.isNull(path));
                    } else {
                        predicates.add(cb.equal(path, value));
                    }
                }
            } else {
                Class<?> clazz = example.getClass();
                while (clazz != null && !clazz.equals(Object.class)) {
                    for (Field field : clazz.getDeclaredFields()) {
                        int mods = field.getModifiers();
                        if (java.lang.reflect.Modifier.isStatic(mods) || field.isSynthetic()) {
                            continue;
                        }
                        field.setAccessible(true);
                        Object value = field.get(example);
                        if (value != null) {
                            Path<?> path = root.get(field.getName());
                            predicates.add(cb.equal(path, value));
                        }
                    }
                    clazz = clazz.getSuperclass();
                }
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            } else if (predicates.size() == 1) {
                return predicates.get(0);
            } else {
                return cb.and(predicates.toArray(new Predicate[0]));
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InvalidDataAccessApiUsageException("Tried to build predicate from example", e);
        }
    }

    private Object getPropertyValueByPath(Object root, String propertyPath) throws IllegalAccessException {
        if (root == null || propertyPath == null || propertyPath.isEmpty()) {
            return null;
        }
        String[] parts = propertyPath.split("\\.");
        Object current = root;
        for (String part : parts) {
            if (current == null) {
                return null;
            }
            Field field = org.springframework.util.ReflectionUtils.findField(current.getClass(), part);
            if (field != null) {
                field.setAccessible(true);
                current = field.get(current);
            } else {
                // try standard getter as fallback: getXxx() or isXxx()
                String capital = part.substring(0, 1).toUpperCase() + part.substring(1);
                String getter = "get" + capital;
                String isser = "is" + capital;
                try {
                    java.lang.reflect.Method method = current.getClass().getMethod(getter);
                    current = method.invoke(current);
                } catch (NoSuchMethodException e1) {
                    try {
                        java.lang.reflect.Method method = current.getClass().getMethod(isser);
                        current = method.invoke(current);
                    } catch (NoSuchMethodException e2) {
                        throw new IllegalArgumentException("Property not found: " + part + " in " + current.getClass(), e2);
                    } catch (ReflectiveOperationException roe) {
                        throw new IllegalArgumentException("Error invoking accessor for " + part + " on " + current.getClass(), roe);
                    }
                } catch (ReflectiveOperationException roe) {
                    throw new IllegalArgumentException("Error invoking accessor for " + part + " on " + current.getClass(), roe);
                }
            }
        }
        return current;
    }

    /**
     *
     * NOTE: We can't reuse
     * {@link #list(Class, String, Object, MatchMode, Integer, Integer, List, List)
     * here due to different default behavior of the <code>matchmode</code>
     * parameter.
     */
    @Override
    public <S extends T> List<S> findByParam(Class<S> clazz, String param, String queryString, MatchMode matchmode,
            List<EntityFilter<S>> entityFilters, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) {

        Set<String> stringSet = new HashSet<>();
        stringSet.add(param);
        return this.findByParam(clazz, stringSet, queryString, matchmode,
                entityFilters, pageSize, pageNumber, orderHints,
                propertyPaths);
    }

    @Override
    public <S extends T> List<S> findByParam(Class<S> clazz, Set<String> params, String queryString, MatchMode matchmode,
            List<EntityFilter<S>> filter, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) {

        clazz = clazz == null ? (Class<S>)type : clazz;
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<S> cq = cb.createQuery(clazz);
        Root<S> root = cq.from(clazz);

        Predicate predicate = null;
        if (queryString != null) {
            for (String param : params){
                Predicate paramPredicate = predicateForMatchMode(param, queryString, matchmode, cb, root, true);
                //OR
                predicate = predicate == null ? paramPredicate :  cb.or(predicate, paramPredicate);
            }
        }
        predicate = addPredicateFromFilter(predicate, filter, cb, root);

        cq.select(root)
          .where(predicate)
          .orderBy(ordersFrom(cb, root, orderHints));

        List<S> results = addPageSizeAndNumber(
               getSession().createQuery(cq), pageSize, pageNumber)
              .getResultList();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public <S extends T> long countByParam(Class<S> clazz, String param, String queryString,
            MatchMode matchmode, List<EntityFilter<S>> filter) {

        clazz = clazz == null ? (Class<S>)type : clazz;
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<S> root = cq.from(clazz);
        Predicate predicate = null;
        if (queryString != null) {
            predicate = predicateForMatchMode(param, queryString, matchmode, cb, root, true);
        }
        predicate = addPredicateFromFilter(predicate, filter, cb, root);

        cq.select(cb.countDistinct(root.get("id")))
          .where(predicate);

        return getSession().createQuery(cq).getSingleResult();
    }

    @Override
    public <S extends T> List<S> findByParamWithRestrictions(Class<S> clazz, String param, String queryString,
            MatchMode matchmode, List<Restriction<?>> restrictions, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        restrictions = addStringRestriction(restrictions, param, queryString, matchmode);
        return this.list(clazz, restrictions, pageSize, pageNumber, orderHints, propertyPaths);
    }

    @Override
    public long countByParamWithRestrictions(Class<? extends T> clazz, String param, String queryString,
            MatchMode matchmode, List<Restriction<?>> restrictions) {

        restrictions = addStringRestriction(restrictions, param, queryString, matchmode);
        return count(clazz, restrictions);
    }

    //TODO: there is a very similar implementation somewhere else
    protected <S extends T> Predicate predicateForMatchMode(String param,
            String queryString, MatchMode matchMode,
            CriteriaBuilder cb, From<?,?> root, boolean ignoreCase) {

        Predicate result;
        if (ignoreCase) {
            String lowerQueryString = queryString == null ? "" : queryString.toLowerCase();
            if (matchMode == null) {
                result = cb.like(cb.lower(root.get(param)), lowerQueryString);
            } else if (matchMode == MatchMode.EXACT) {
                result = cb.equal(cb.lower(root.get(param)), lowerQueryString);
            } else if (matchMode == MatchMode.BEGINNING || matchMode == MatchMode.END
                    || matchMode == MatchMode.ANYWHERE || matchMode == MatchMode.LIKE) {
                result = cb.like(cb.lower(root.get(param)), matchMode.queryStringFrom(lowerQueryString));
            } else {
                throw new RuntimeException("Unsupported MatchMode: " + matchMode.name());
            }
        }else {
            String lowerQueryString = queryString == null ? "" : queryString;
            if (matchMode == null) {
                result = cb.like(root.get(param), lowerQueryString);
            } else if (matchMode == MatchMode.EXACT) {
                result = cb.equal(root.get(param), lowerQueryString);
            } else if (matchMode == MatchMode.BEGINNING || matchMode == MatchMode.END
                    || matchMode == MatchMode.ANYWHERE || matchMode == MatchMode.LIKE) {
                result = cb.like(root.get(param), matchMode.queryStringFrom(lowerQueryString));
            } else {
                throw new RuntimeException("Unsupported MatchMode: " + matchMode.name());
            }
        }
        return result;
    }

    @Override
    public <S extends T> List<S> list(S example, Set<String> includeProperties, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        Class<S> clazz = example == null ? (Class<S>)type : (Class<S>) example.getClass();
        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<S> cq = cb.createQuery(clazz);
        Root<S> root = cq.from(clazz);

        Predicate predicate = getExamplePredicate(cb, root, example, includeProperties);

        cq.select(root)
          .distinct(true)
          .where(predicate)
          .orderBy(ordersFrom(cb, root, orderHints));

        List<S> results = addPageSizeAndNumber(
                getSession().createQuery(cq), limit, start)
            .getResultList();

        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return deduplicateResult(results);
    }

    protected AuditQuery makeAuditQuery(Class<? extends CdmBase> clazz, AuditEvent auditEvent) {
        AuditQuery query = null;

        if (clazz == null) {
            query = getAuditReader().createQuery().forEntitiesAtRevision(type, auditEvent.getRevisionNumber());
        } else {
            query = getAuditReader().createQuery().forEntitiesAtRevision(clazz, auditEvent.getRevisionNumber());
        }
        return query;
    }

    protected AuditReader getAuditReader() {
        return AuditReaderFactory.get(getSession());
    }

    /**
     * Returns clazz, or if clazz is <code>null</code> the base type of this DAO.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected <S extends T> Class<S> nullSafeClass(Class<S> clazz) {
        return clazz = (clazz == null) ? (Class)type : clazz;
    }

    protected <S extends T> long countByFilter(Class<S> clazz, EntityFilter<S> filter) {

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<S> root = cq.from(clazz);

        cq.select(cb.countDistinct(root))
          .where(filter.toPredicate(root, cb));

        return getSession().createQuery(cq).getSingleResult();
    }

    protected <S extends T> List<S> listByFilter(Class<S> clazz, EntityFilter<S> filter,
            Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<S> cq = cb.createQuery(clazz);
        Root<S> root = cq.from(clazz);

        filter.toPredicate(root, cb);

        cq.select(root)
          .distinct(true)
          .where(filter.toPredicate(root, cb))
          .orderBy(ordersFrom(cb, root, orderHints));

        List<S> results = addPageSizeAndNumber(
                getSession().createQuery(cq), pageSize, pageNumber)
               .getResultList();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return deduplicateResult(results);
    }

    protected <S extends T> S findByFilter(Class<S> clazz, EntityFilter<S> filter,
            List<String> propertyPaths) {
        return findByFilter(clazz, filter, null, propertyPaths);
    }

    /**
     * Returns a single result matching the given filter or <code>null</code> if no such result exists.
     * If more than one result matches the filter, only the first one is returned.
     */
    protected <S extends T> S findByFilter(Class<S> clazz, EntityFilter<S> filter,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        CriteriaBuilder cb = getCriteriaBuilder();
        CriteriaQuery<S> cq = cb.createQuery(clazz);
        Root<S> root = cq.from(clazz);

        filter.toPredicate(root, cb);

        cq.select(root)
          .distinct(true)
          .where(filter.toPredicate(root, cb))
          .orderBy(ordersFrom(cb, root, orderHints));

        S result = getSession().createQuery(cq)
               .getResultStream().findFirst().orElse(null);
       defaultBeanInitializer.initialize(result, propertyPaths);
       return result;
    }
}