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
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Example.PropertySelector;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.sql.JoinType;
import org.hibernate.type.Type;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.ReflectionUtils;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IPublishable;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction.Operator;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.persistence.hibernate.PostMergeEntityListener;
import eu.etaxonomy.cdm.persistence.hibernate.replace.ReferringObjectMetadata;
import eu.etaxonomy.cdm.persistence.hibernate.replace.ReferringObjectMetadataFactory;
import eu.etaxonomy.cdm.persistence.query.Grouping;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller FIXME CdmEntityDaoBase is abstract, can it be annotated
 *         with @Repository?
 */
@Repository
public abstract class CdmEntityDaoBase<T extends CdmBase> extends DaoBase implements ICdmEntityDao<T> {

    private static final Logger logger = Logger.getLogger(CdmEntityDaoBase.class);

    protected int flushAfterNo = 1000; // large numbers may cause
                                       // synchronisation errors when commiting
                                       // the session !!

    protected Class<T> type;

    @Autowired
    // @Qualifier("defaultBeanInitializer")
    protected IBeanInitializer defaultBeanInitializer;

    public void setDefaultBeanInitializer(IBeanInitializer defaultBeanInitializer) {
        this.defaultBeanInitializer = defaultBeanInitializer;
    }

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
        getSession().buildLockRequest(lockOptions).lock(t);
    }

    @Override
    public void refresh(T t, LockOptions lockOptions, List<String> propertyPaths) {
        getSession().refresh(t, lockOptions);
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
    public Map<UUID, T> saveAll(Collection<T> cdmObjCollection) {
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
            UUID uuid = saveCdmObject_(cdmObj);
            if (logger.isDebugEnabled()) {
                logger.debug("Save cdmObj: " + (cdmObj == null ? null : cdmObj.toString()));
            }
            resultMap.put(uuid, cdmObj);
            i++;
            if ((i % flushAfterNo) == 0) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("flush");
                    }
                    flush();
                } catch (Exception e) {
                    logger.error("An exception occurred when trying to flush data");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
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
            if ((i % flushAfterNo) == 0) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("flush");
                    }
                    flush();
                } catch (Exception e) {
                    logger.error("An exception occurred when trying to flush data");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
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

            List<CdmBase> referringObjects = referringObjectMetadata.getReferringObjects(x, getSession());

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
    public UUID saveOrUpdate(T transientObject) throws DataAccessException {
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

    @Override
    public T save(T newInstance) throws DataAccessException {
        if (newInstance == null) {
            logger.warn("Object to save should not be null. NOP");
            return null;
        }
        getSession().save(newInstance);
        return newInstance;
    }

    @Override
    public UUID update(T transientObject) throws DataAccessException {
        if (transientObject == null) {
            logger.warn("Object to update should not be null. NOP");
            return null;
        }
        getSession().update(transientObject);
        return transientObject.getUuid();
    }

    @Override
    public UUID refresh(T persistentObject) throws DataAccessException {
        getSession().refresh(persistentObject);
        return persistentObject.getUuid();
    }

    @Override
    public UUID delete(T persistentObject) throws DataAccessException {
        if (persistentObject == null) {
            logger.warn(type.getName() + " was 'null'");
            return null;
        }

        // Merge the object in if it is detached
        //
        // I think this is preferable to catching lazy initialization errors
        // as that solution only swallows and hides the exception, but doesn't
        // actually solve it.
        persistentObject = (T) getSession().merge(persistentObject);
        getSession().delete(persistentObject);
        return persistentObject.getUuid();
    }

    @Override
    public T findById(int id) throws DataAccessException {
        return getSession().get(type, id);
    }

    @Override
    public T findByUuid(UUID uuid) throws DataAccessException {
        return this.findByUuid(uuid, INCLUDE_UNPUBLISHED);
    }

    protected T findByUuid(UUID uuid, boolean includeUnpublished) throws DataAccessException {
        Session session = getSession();
        Criteria crit = session.createCriteria(type);
        crit.add(Restrictions.eq("uuid", uuid));
        crit.addOrder(Order.desc("created"));
        if (IPublishable.class.isAssignableFrom(type) && !includeUnpublished) {
            crit.add(Restrictions.eq("publish", Boolean.TRUE));
        }

        @SuppressWarnings("unchecked")
        List<T> results = crit.list();
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
        Session session = getSession();
        FlushMode currentFlushMode = session.getFlushMode();
        try {
            // set flush mode to manual so that the session does not flush
            // when before performing the query
            session.setFlushMode(FlushMode.MANUAL);
            Criteria crit = session.createCriteria(type);
            crit.add(Restrictions.eq("uuid", uuid));
            crit.addOrder(Order.desc("created"));
            @SuppressWarnings("unchecked")
            List<T> results = crit.list();
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
                session.setFlushMode(currentFlushMode);
            }
        }
    }

    @Override
    public List<T> loadList(Collection<Integer> ids, List<OrderHint> orderHints, List<String> propertyPaths) throws DataAccessException {

        if (ids.isEmpty()) {
            return new ArrayList<>(0);
        }

        Criteria criteria = prepareList(null, ids, null, null, orderHints, "id");

        if (logger.isDebugEnabled()) {
            logger.debug(criteria.toString());
        }

        @SuppressWarnings("unchecked")
        List<T> result = criteria.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    @Override
    public List<T> list(Collection<UUID> uuids, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) throws DataAccessException {

        if (uuids == null || uuids.isEmpty()) {
            return new ArrayList<>();
        }

        Criteria criteria = prepareList(null, uuids, pageSize, pageNumber, orderHints, "uuid");
        @SuppressWarnings("unchecked")
        List<T> result = criteria.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    @Override
    public <S extends T> List<S> list(Class<S> clazz, Collection<UUID> uuids, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) throws DataAccessException {

        if (uuids == null || uuids.isEmpty()) {
            return new ArrayList<>();
        }

        Criteria criteria = prepareList(clazz, uuids, pageSize, pageNumber, orderHints, "uuid");
        @SuppressWarnings("unchecked")
        List<S> result = criteria.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends T> List<S> list(Class<S> type, List<Restriction<?>> restrictions, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        Criteria criteria = createCriteria(type, restrictions, false);

        addLimitAndStart(criteria, limit, start);
        addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<S> result = criteria.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    /**
     * @param restrictions
     * @param criteria
     */
    private void addRestrictions(List<Restriction<?>> restrictions, DetachedCriteria criteria) {

        if(restrictions == null || restrictions.isEmpty()){
            return ;
        }

        List<CriterionWithOperator> perProperty = new ArrayList<>(restrictions.size());
        Map<String, String> aliases = new HashMap<>();



        for(Restriction<?> restriction : restrictions){
            Collection<? extends Object> values = restriction.getValues();
            JoinType jointype = LEFTOUTER_OPS.contains(restriction.getOperator()) ? JoinType.LEFT_OUTER_JOIN : JoinType.INNER_JOIN;
            if(values != null && !values.isEmpty()){
                // ---
                String propertyPath = restriction.getPropertyName();
                String[] props =  propertyPath.split("\\.");
                String propertyName;
                if(props.length == 1){
                    // direct property of the base type of the criteria
                    propertyName = propertyPath;
                } else {
                    // create aliases if the propertyName is a dot separated property path
                    String aĺiasKey = jointype.name() + "_";
                    String aliasedProperty = null;
                    String alias = "";
                    for(int p = 0; p < props.length -1; p++){
                        aĺiasKey = aĺiasKey + (aĺiasKey.isEmpty() ? "" : ".") + props[p];
                        aliasedProperty = alias + (alias.isEmpty() ? "" : ".") + props[p];
                        if(!aliases.containsKey(aliasedProperty)){
                            alias = alias + (alias.isEmpty() ? "" : "_" ) + props[p];
                            aliases.put(aĺiasKey, alias);
                            criteria.createAlias(aliasedProperty, alias, jointype);
                            if(logger.isDebugEnabled()){
                                logger.debug("addRestrictions() alias created with aliasKey " + aĺiasKey + " => " + aliasedProperty + " as " + alias);
                            }
                        }
                    }
                    propertyName = alias + "." + props[props.length -1];
                }
                // ---
                Criterion[] predicates = new Criterion[values.size()];
                int i = 0;
                for(Object v : values){
                    Criterion criterion = createRestriction(propertyName, v, restriction.getMatchMode());
                    if(restriction.isNot()){
                        if(props.length > 1){
                            criterion = Restrictions.or(Restrictions.not(criterion), Restrictions.isNull(propertyName));
                        } else {
                            criterion = Restrictions.not(criterion);
                        }
                    }
                    predicates[i++] = criterion;
                    if(logger.isDebugEnabled()){
                        logger.debug("addRestrictions() predicate with " + propertyName + " " + (restriction.getMatchMode() == null ? "=" : restriction.getMatchMode().name()) + " " + v.toString());
                    }
                }
                if(restriction.getOperator() == Operator.AND_NOT){
                    perProperty.add(new CriterionWithOperator(restriction.getOperator(), Restrictions.and(predicates)));
                } else {
                    perProperty.add(new CriterionWithOperator(restriction.getOperator(), Restrictions.or(predicates)));
                }
            } // check has values
        } // loop over restrictions

        Restriction.Operator firstOperator = null;
        if(!perProperty.isEmpty()){
            LogicalExpression logicalExpression = null;
            for(CriterionWithOperator cwo : perProperty){
                if(logicalExpression == null){
                    firstOperator = cwo.operator;
                    logicalExpression = Restrictions.and(Restrictions.sqlRestriction("1=1"), cwo.criterion);
                } else {
                    switch(cwo.operator){
                        case AND:
                        case AND_NOT:
                            logicalExpression = Restrictions.and(logicalExpression, cwo.criterion);
                            break;
                        case OR:
                        case OR_NOT:
                            logicalExpression = Restrictions.or(logicalExpression, cwo.criterion);
                            break;
                        default:
                            throw new RuntimeException("Unsupported Operator");
                    }
                }

            }


            criteria.add(logicalExpression);
//            if(firstOperator == Operator.OR){
//                // OR
//            } else {
//                // AND
//                criteria.add(Restrictions.and(queryStringCriterion, logicalExpression));
//            }
        }
        if(logger.isDebugEnabled()){
            logger.debug("addRestrictions() final criteria: " + criteria.toString());
        }
    }

    /**
     * @param propertyName
     * @param value
     * @param matchMode
     *            is only applied if the <code>value</code> is a
     *            <code>String</code> object
     * @param criteria
     * @return
     */
    private Criterion createRestriction(String propertyName, Object value, MatchMode matchMode) {

        Criterion restriction;
        if (value == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("createRestriction() " + propertyName + " is null ");
            }
            restriction = Restrictions.isNull(propertyName);
        } else if (matchMode == null || !(value instanceof String)) {
            if (logger.isDebugEnabled()) {
                logger.debug("createRestriction() " + propertyName + " = " + value.toString());
            }
            restriction = Restrictions.eq(propertyName, value);
        } else {
            String queryString = (String) value;
            if (logger.isDebugEnabled()) {
                logger.debug("createRestriction() " + propertyName + " " + matchMode.getMatchOperator() + " "
                        + matchMode.queryStringFrom(queryString));
            }
            switch(matchMode){
            case BEGINNING:
                restriction = Restrictions.ilike(propertyName, queryString, org.hibernate.criterion.MatchMode.START);
                break;
            case END:
                restriction = Restrictions.ilike(propertyName, queryString, org.hibernate.criterion.MatchMode.END);
                break;
            case LIKE:
                restriction = Restrictions.ilike(propertyName, matchMode.queryStringFrom(queryString), org.hibernate.criterion.MatchMode.ANYWHERE);
                break;
            case EXACT:
                restriction = Restrictions.ilike(propertyName, queryString, org.hibernate.criterion.MatchMode.EXACT);
                break;
            case ANYWHERE:
                restriction = Restrictions.ilike(propertyName, queryString, org.hibernate.criterion.MatchMode.ANYWHERE);
                break;
            default:
                throw new RuntimeException("Unknown MatchMode: " + matchMode.name());
            }
        }
        return restriction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count(Class<? extends T> type, List<Restriction<?>> restrictions) {

        Criteria criteria = createCriteria(type, restrictions, false);

        criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));

        return (Long) criteria.uniqueResult();

    }

    /**
     * @param uuids
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyName
     * @return
     */
    private Criteria prepareList(Class<? extends T> clazz, Collection<?> uuids, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            String propertyName) {
        if (clazz == null){
            clazz = type;
        }
        Criteria criteria = getSession().createCriteria(clazz);
        criteria.add(Restrictions.in(propertyName, uuids));

        if (pageSize != null) {
            criteria.setMaxResults(pageSize);
            if (pageNumber != null) {
                criteria.setFirstResult(pageNumber * pageSize);
            } else {
                criteria.setFirstResult(0);
            }
        }

        if (orderHints == null) {
            orderHints = OrderHint.defaultOrderHintsFor(type);
        }
        addOrder(criteria, orderHints);
        return criteria;
    }

    /**
     * @param clazz
     * @return
     */
    private Criteria criterionForType(Class<? extends T> clazz) {
        return  getSession().createCriteria(entityType(clazz));
    }

    protected Class<? extends T> entityType(Class<? extends T> clazz){
        if (clazz != null) {
            return clazz;
        } else {
            return type;
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
        return count(type);
    }

    @Override
    public long count(Class<? extends T> clazz) {
        Session session = getSession();
        Criteria criteria = null;
        if (clazz == null) {
            criteria = session.createCriteria(type);
        } else {
            criteria = session.createCriteria(clazz);
        }
        criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));

        // since hibernate 4 (or so) uniqueResult returns Long, not Integer,
        // therefore needs
        // to be casted. Think about returning long rather then int!
        return (long) criteria.uniqueResult();
    }

    @Override
    public List<T> list(Integer limit, Integer start) {
        return list(limit, start, null);
    }

    @Override
    public List<Object[]> group(Class<? extends T> clazz, Integer limit, Integer start, List<Grouping> groups,
            List<String> propertyPaths) {

        Criteria criteria = null;
        criteria = criterionForType(clazz);

        addGroups(criteria, groups);

        if (limit != null) {
            criteria.setFirstResult(start);
            criteria.setMaxResults(limit);
        }

        @SuppressWarnings("unchecked")
        List<Object[]> result = criteria.list();

        if (propertyPaths != null && !propertyPaths.isEmpty()) {
            for (Object[] objects : result) {
                defaultBeanInitializer.initialize(objects[0], propertyPaths);
            }
        }

        return result;
    }

    protected void countGroups(DetachedCriteria criteria, List<Grouping> groups) {
        if (groups != null) {

            Map<String, String> aliases = new HashMap<String, String>();

            for (Grouping grouping : groups) {
                if (grouping.getAssociatedObj() != null) {
                    String alias = null;
                    if ((alias = aliases.get(grouping.getAssociatedObj())) == null) {
                        alias = grouping.getAssociatedObjectAlias();
                        aliases.put(grouping.getAssociatedObj(), alias);
                        criteria.createAlias(grouping.getAssociatedObj(), alias);
                    }
                }
            }

            ProjectionList projectionList = Projections.projectionList();

            for (Grouping grouping : groups) {
                grouping.addProjection(projectionList);
            }
            criteria.setProjection(projectionList);
        }
    }

    protected void addGroups(Criteria criteria, List<Grouping> groups) {
        if (groups != null) {

            Map<String, String> aliases = new HashMap<String, String>();

            for (Grouping grouping : groups) {
                if (grouping.getAssociatedObj() != null) {
                    String alias = null;
                    if ((alias = aliases.get(grouping.getAssociatedObj())) == null) {
                        alias = grouping.getAssociatedObjectAlias();
                        aliases.put(grouping.getAssociatedObj(), alias);
                        criteria.createAlias(grouping.getAssociatedObj(), alias);
                    }
                }
            }

            ProjectionList projectionList = Projections.projectionList();

            for (Grouping grouping : groups) {
                grouping.addProjection(projectionList);
            }
            criteria.setProjection(projectionList);

            for (Grouping grouping : groups) {
                grouping.addOrder(criteria);

            }
        }
    }

    @Override
    public List<T> list(Integer limit, Integer start, List<OrderHint> orderHints) {
        return list(limit, start, orderHints, null);
    }

    @Override
    public List<T> list(Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        Criteria criteria = getSession().createCriteria(type);
        if (limit != null) {
            criteria.setFirstResult(start);
            criteria.setMaxResults(limit);
        }

        addOrder(criteria, orderHints);
        @SuppressWarnings("unchecked")
        List<T> results = criteria.list();

        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public <S extends T> List<S> list(Class<S> clazz, Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths) {
        Criteria criteria = null;
        if (clazz == null) {
            criteria = getSession().createCriteria(type);
        } else {
            criteria = getSession().createCriteria(clazz);
        }

        addLimitAndStart(criteria, limit, start);

        addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<S> results = criteria.list();

        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    public <S extends T> List<S> list(Class<S> type, Integer limit, Integer start, List<OrderHint> orderHints) {
        return list(type, limit, start, orderHints, null);
    }

    @Override
    public <S extends T> List<S> list(Class<S> type, Integer limit, Integer start) {
        return list(type, limit, start, null, null);
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    protected void setPagingParameter(Query query, Integer pageSize, Integer pageIndex) {
        if (pageSize != null) {
            query.setMaxResults(pageSize);
            if (pageIndex != null) {
                query.setFirstResult(pageIndex * pageSize);
            } else {
                query.setFirstResult(0);
            }
        }
    }

    protected void setPagingParameter(AuditQuery query, Integer pageSize, Integer pageIndex) {
        if (pageSize != null) {
            query.setMaxResults(pageSize);
            if (pageIndex != null) {
                query.setFirstResult(pageIndex * pageSize);
            } else {
                query.setFirstResult(0);
            }
        }
    }

    @Override
    public long count(T example, Set<String> includeProperties) {
        Criteria criteria = getSession().createCriteria(example.getClass());
        addExample(criteria, example, includeProperties);

        criteria.setProjection(Projections.rowCount());
        return (Long) criteria.uniqueResult();
    }

    protected void addExample(Criteria criteria, T example, Set<String> includeProperties) {
        if (includeProperties != null && !includeProperties.isEmpty()) {
            criteria.add(Example.create(example).setPropertySelector(new PropertySelectorImpl(includeProperties)));
            ClassMetadata classMetadata = getSession().getSessionFactory().getClassMetadata(example.getClass());
            for (String property : includeProperties) {
                Type type = classMetadata.getPropertyType(property);
                if (type.isEntityType()) {
                    try {
                        Field field = ReflectionUtils.findField(example.getClass(), property);
                        field.setAccessible(true);
                        Object value = field.get(example);
                        if (value != null) {
                            criteria.add(Restrictions.eq(property, value));
                        } else {
                            criteria.add(Restrictions.isNull(property));
                        }
                    } catch (SecurityException se) {
                        throw new InvalidDataAccessApiUsageException("Tried to add criteria for property " + property,
                                se);
                    } catch (HibernateException he) {
                        throw new InvalidDataAccessApiUsageException("Tried to add criteria for property " + property,
                                he);
                    } catch (IllegalArgumentException iae) {
                        throw new InvalidDataAccessApiUsageException("Tried to add criteria for property " + property,
                                iae);
                    } catch (IllegalAccessException ie) {
                        throw new InvalidDataAccessApiUsageException("Tried to add criteria for property " + property,
                                ie);
                    }

                }
            }
        } else {
            criteria.add(Example.create(example));
        }
    }

    /**
     *
     * NOTE: We can't reuse
     * {@link #list(Class, String, Object, MatchMode, Integer, Integer, List, List)
     * here due to different default behavior of the <code>matchmode</code>
     * parameter.
     *
     * @param clazz
     * @param param
     * @param queryString
     * @param matchmode
     * @param criterion
     * @param pageSize
     * @param pageNumber
     * @param orderHints
     * @param propertyPaths
     * @return
     */
    @Override
    public <S extends T> List<S> findByParam(Class<S> clazz, String param, String queryString, MatchMode matchmode,
            List<Criterion> criterion, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) {
        Set<String> stringSet = new HashSet<>();
        stringSet.add(param);
        return this.findByParam(clazz, stringSet, queryString, matchmode,
                criterion, pageSize, pageNumber, orderHints,
                propertyPaths);
    }

    @Override
    public <S extends T> List<S> findByParam(Class<S> clazz, Set<String> params, String queryString, MatchMode matchmode,
            List<Criterion> criterion, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) {

        Criteria criteria = criterionForType(clazz);

        if (queryString != null) {
            Set<Criterion> criterions = new HashSet<>();
            for (String param: params){
                Criterion crit;
                if (matchmode == null) {
                     crit = Restrictions.ilike(param, queryString);
                } else if (matchmode == MatchMode.BEGINNING) {
                     crit = Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.START);
                } else if (matchmode == MatchMode.END) {
                    crit = Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.END);
                } else if (matchmode == MatchMode.EXACT) {
                    crit = Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.EXACT);
                } else {
                    crit = Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.ANYWHERE);
                }
                criterions.add(crit);
            }
            if (criterions.size()>1){
                Iterator<Criterion> critIterator = criterions.iterator();
                Disjunction disjunction = Restrictions.disjunction();
                while (critIterator.hasNext()){
                    disjunction.add(critIterator.next());
                }

                criteria.add(disjunction);

            }else{
                if (!criterions.isEmpty()){
                    criteria.add(criterions.iterator().next());
                }
            }

        }

        addCriteria(criteria, criterion);

        if (pageSize != null) {
            criteria.setMaxResults(pageSize);
            if (pageNumber != null) {
                criteria.setFirstResult(pageNumber * pageSize);
            } else {
                criteria.setFirstResult(0);
            }
        }

        addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<S> result = criteria.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    /**
     *
     * @param clazz
     * @param param
     * @param queryString
     * @param matchmode
     * @param criterion
     * @return
     */
    @Override
    public long countByParam(Class<? extends T> clazz, String param, String queryString, MatchMode matchmode,
            List<Criterion> criterion) {

        Criteria criteria = null;

        criteria = criterionForType(clazz);

        if (queryString != null) {
            if (matchmode == null) {
                criteria.add(Restrictions.ilike(param, queryString));
            } else if (matchmode == MatchMode.BEGINNING) {
                criteria.add(Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.START));
            } else if (matchmode == MatchMode.END) {
                criteria.add(Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.END));
            } else if (matchmode == MatchMode.EXACT) {
                criteria.add(Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.EXACT));
            } else {
                criteria.add(Restrictions.ilike(param, queryString, org.hibernate.criterion.MatchMode.ANYWHERE));
            }
        }

        addCriteria(criteria, criterion);

        criteria.setProjection(Projections.rowCount());

        return (Long) criteria.uniqueResult();
    }

    /**
     * Creates a criteria query for the CDM <code>type</code> either for counting or listing matching entities.
     * <p>
     * The set of matching entities can be restricted by passing a list  of {@link Restriction} objects.
     * Restrictions can logically combined:
     <pre>
       Arrays.asList(
           new Restriction<String>("titleCache", MatchMode.ANYWHERE, "foo"),
           new Restriction<String>("institute.name", Operator.OR, MatchMode.BEGINNING, "Bar")
       );
     </pre>
     * The first Restriction in the example above by default has the <code>Operator.AND</code> which will be
     * ignored since this is the first restriction. The <code>Operator</code> of further restrictions in the
     * list are used to combine with the previous restriction.
     *
     * @param type
     * @param restrictions
     * @param doCount
     * @return
     */
    protected Criteria createCriteria(Class<? extends T> type, List<Restriction<?>> restrictions, boolean doCount) {

        DetachedCriteria idsOnlyCriteria = DetachedCriteria.forClass(entityType(type));
        idsOnlyCriteria.setProjection(Projections.distinct(Projections.id()));

        addRestrictions(restrictions, idsOnlyCriteria);

        Criteria criteria = criterionForType(type);
        criteria.add(Subqueries.propertyIn("id", idsOnlyCriteria));

        if(doCount){
            criteria.setProjection(Projections.rowCount());
        } else {
            idsOnlyCriteria.setProjection(Projections.distinct(Projections.property("id")));
        }

        return criteria;
    }


    @Override
    public <S extends T> List<S> findByParamWithRestrictions(Class<S> clazz, String param, String queryString,
            MatchMode matchmode, List<Restriction<?>> restrictions, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        List<Restriction<?>> allRestrictions = new ArrayList<>();
        allRestrictions.add(new Restriction<String>(param, matchmode, queryString));
        if(restrictions != null){
            allRestrictions.addAll(restrictions);
        }
        Criteria criteria = createCriteria(clazz, allRestrictions, false);

        if (pageSize != null) {
            criteria.setMaxResults(pageSize);
            if (pageNumber != null) {
                criteria.setFirstResult(pageNumber * pageSize);
            } else {
                criteria.setFirstResult(0);
            }
        }

        addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<S> result = criteria.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;

    }

    @Override
    public long countByParamWithRestrictions(Class<? extends T> clazz, String param, String queryString,
            MatchMode matchmode, List<Restriction<?>> restrictions) {

        List<Restriction<?>> allRestrictions = new ArrayList<>();
        allRestrictions.add(new Restriction<String>(param, matchmode, queryString));
        if(restrictions != null){
            allRestrictions.addAll(restrictions);
        }
        Criteria criteria = createCriteria(clazz, allRestrictions, true);

        return (Long) criteria.uniqueResult();
    }

    @Override
    public <S extends T> List<S> list(S example, Set<String> includeProperties, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPaths) {
        Criteria criteria = getSession().createCriteria(example.getClass());
        addExample(criteria, example, includeProperties);

        addLimitAndStart(criteria, limit, start);

        addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<S> results = criteria.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    private class PropertySelectorImpl implements PropertySelector {

        private final Set<String> includeProperties;
        /**
         *
         */
        private static final long serialVersionUID = -3175311800911570546L;

        public PropertySelectorImpl(Set<String> includeProperties) {
            this.includeProperties = includeProperties;
        }

        @Override
        public boolean include(Object propertyValue, String propertyName, Type type) {
            if (includeProperties.contains(propertyName)) {
                return true;
            } else {
                return false;
            }
        }

    }

    private class CriterionWithOperator {

        Restriction.Operator operator;
        Criterion criterion;


        public CriterionWithOperator(Operator operator, Criterion criterion) {
            super();
            this.operator = operator;
            this.criterion = criterion;
        }


    }

    /**
     * Returns a Criteria for the given {@link Class class} or, if
     * <code>null</code>, for the base {@link Class class} of this DAO.
     *
     * @param clazz
     * @return the Criteria
     */
    protected Criteria getCriteria(Class<? extends CdmBase> clazz) {
        Criteria criteria = null;
        if (clazz == null) {
            criteria = getSession().createCriteria(type);
        } else {
            criteria = getSession().createCriteria(clazz);
        }
        return criteria;
    }

    /**
     * @param clazz
     * @param auditEvent
     * @return
     */
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
}
