/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 *
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.IHomotypicalGroupDao;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dto.TaxonNameParts;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 *
 */
@Repository
@Qualifier("taxonNameDaoHibernateImpl")
public class TaxonNameDaoHibernateImpl extends IdentifiableDaoBase<TaxonName> implements ITaxonNameDao {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private ITaxonDao taxonDao;

    @Autowired
    private IHomotypicalGroupDao homotypicalGroupDao;

    @SuppressWarnings("unchecked")
    public TaxonNameDaoHibernateImpl() {
        super(TaxonName.class);
        indexedClasses = new Class[1];
        indexedClasses[0] = TaxonName.class;
    }

    @Override
    public int countHybridNames(INonViralName name, HybridRelationshipType type) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Query<Long> query = null;
            if(type == null) {
                query = getSession().createQuery("select count(relation) from HybridRelationship relation where relation.relatedFrom = :name", Long.class);
            } else {
                query = getSession().createQuery("select count(relation) from HybridRelationship relation where relation.relatedFrom = :name and relation.type = :type", Long.class);
                query.setParameter("type", type);
            }
            query.setParameter("name",name);
            return query.uniqueResult().intValue();
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(HybridRelationship.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId("relatedFrom").eq(name.getId()));
            query.addProjection(AuditEntity.id().count());

            if(type != null) {
                query.add(AuditEntity.relatedId("type").eq(type.getId()));
            }

            return ((Long)query.getSingleResult()).intValue();
        }
    }

    @Override
    public long countNames(String queryString) {
        checkNotInPriorView("TaxonNameDaoHibernateImpl.countNames(String queryString)");
        Criteria criteria = getCriteria(null);

        if (queryString != null) {
            criteria.add(Restrictions.ilike("nameCache", queryString));
        }
        criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));

        return (Long)criteria.uniqueResult();
    }

    @Override
    public long countNames(String queryString, MatchMode matchMode, List<Criterion> criteria) {

        Criteria crit = getCriteria(type);
        if (matchMode == MatchMode.EXACT) {
            crit.add(Restrictions.eq("nameCache", matchMode.queryStringFrom(queryString)));
        } else {
            crit.add(Restrictions.ilike("nameCache", matchMode.queryStringFrom(queryString)));
        }
        if(criteria != null) {
            for (Criterion criterion : criteria) {
                crit.add(criterion);
            }
        }

        crit.setProjection(Projections.projectionList().add(Projections.rowCount()));
        return (Long)crit.uniqueResult();
    }

    @Override
    public long countNames(String genusOrUninomial, String infraGenericEpithet,	String specificEpithet, String infraSpecificEpithet, Rank rank) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getCriteria(TaxonName.class);

            /**
             * Given HHH-2951 - "Restrictions.eq when passed null, should create a NullRestriction"
             * We need to convert nulls to NullRestrictions for now
             */
            if(genusOrUninomial != null) {
                criteria.add(Restrictions.eq("genusOrUninomial",genusOrUninomial));
            } else {
                criteria.add(Restrictions.isNull("genusOrUninomial"));
            }

            if(infraGenericEpithet != null) {
                criteria.add(Restrictions.eq("infraGenericEpithet", infraGenericEpithet));
            } else {
                criteria.add(Restrictions.isNull("infraGenericEpithet"));
            }

            if(specificEpithet != null) {
                criteria.add(Restrictions.eq("specificEpithet", specificEpithet));
            } else {
                criteria.add(Restrictions.isNull("specificEpithet"));
            }

            if(infraSpecificEpithet != null) {
                criteria.add(Restrictions.eq("infraSpecificEpithet",infraSpecificEpithet));
            } else {
                criteria.add(Restrictions.isNull("infraSpecificEpithet"));
            }

            if(rank != null) {
                criteria.add(Restrictions.eq("rank", rank));
            }

            criteria.setProjection(Projections.rowCount());
            return (Long)criteria.uniqueResult();
        } else {
            AuditQuery query = makeAuditQuery(TaxonName.class, auditEvent);

            if(genusOrUninomial != null) {
                query.add(AuditEntity.property("genusOrUninomial").eq(genusOrUninomial));
            } else {
                query.add(AuditEntity.property("genusOrUninomial").isNull());
            }

            if(infraGenericEpithet != null) {
                query.add(AuditEntity.property("infraGenericEpithet").eq(infraGenericEpithet));
            } else {
                query.add(AuditEntity.property("infraGenericEpithet").isNull());
            }

            if(specificEpithet != null) {
                query.add(AuditEntity.property("specificEpithet").eq(specificEpithet));
            } else {
                query.add(AuditEntity.property("specificEpithet").isNull());
            }

            if(infraSpecificEpithet != null) {
                query.add(AuditEntity.property("infraSpecificEpithet").eq(infraSpecificEpithet));
            } else {
                query.add(AuditEntity.property("infraSpecificEpithet").isNull());
            }

            if(rank != null) {
                query.add(AuditEntity.relatedId("rank").eq(rank.getId()));
            }

            query.addProjection(AuditEntity.id().count());
            return (Long)query.getSingleResult();
        }
    }

    @Override
    public int countNameRelationships(TaxonName name, NameRelationship.Direction direction, NameRelationshipType type) {

        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Query query = null;
            if(type == null) {
                query = getSession().createQuery("select count(relation) from NameRelationship relation where relation." + direction +" = :name");
            } else {
                query = getSession().createQuery("select count(relation) from NameRelationship relation where relation." + direction +" = :name and relation.type = :type");
                query.setParameter("type", type);
            }
            query.setParameter("name",name);
            return ((Long)query.uniqueResult()).intValue();
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(NameRelationship.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId(direction.toString()).eq(name.getId()));
            query.addProjection(AuditEntity.id().count());

            if(type != null) {
                query.add(AuditEntity.relatedId("type").eq(type.getId()));
            }

            return ((Long)query.getSingleResult()).intValue();
        }
    }


    @Override
    public long countTypeDesignations(TaxonName name, SpecimenTypeDesignationStatus status) {
        checkNotInPriorView("countTypeDesignations(TaxonName name, SpecimenTypeDesignationStatus status)");
        Query<Long> query = null;
        if(status == null) {
            query = getSession().createQuery("select count(designation) from TypeDesignationBase designation join designation.typifiedNames name where name = :name", Long.class);
        } else {
            query = getSession().createQuery("select count(designation) from TypeDesignationBase designation join designation.typifiedNames name where name = :name and designation.typeStatus = :status", Long.class);
            query.setParameter("status", status);
        }
        query.setParameter("name",name);
        return query.uniqueResult();
    }

    @Override
    public List<HybridRelationship> getHybridNames(INonViralName name, HybridRelationshipType type,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(HybridRelationship.class);
            criteria.add(Restrictions.eq("relatedFrom", name));
            if(type != null) {
                criteria.add(Restrictions.eq("type", type));
            }

            addPageSizeAndNumber(criteria, pageSize, pageNumber);
            addOrder(criteria, orderHints);

            @SuppressWarnings("unchecked")
            List<HybridRelationship> results = criteria.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(HybridRelationship.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId("relatedFrom").eq(name.getId()));

            if(type != null) {
                query.add(AuditEntity.relatedId("type").eq(type.getId()));
            }

            if(pageSize != null) {
                query.setMaxResults(pageSize);
                if(pageNumber != null) {
                    query.setFirstResult(pageNumber * pageSize);
                } else {
                    query.setFirstResult(0);
                }
            }

            @SuppressWarnings("unchecked")
            List<HybridRelationship> results =  query.getResultList();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        }
    }

    @Override
    public List<NameRelationship> getNameRelationships(TaxonName name, NameRelationship.Direction direction,
            NameRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) {

        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(NameRelationship.class);
            if (name != null || direction != null){
                criteria.add(Restrictions.eq(direction.toString(), name));
            }
            if(type != null) {
                criteria.add(Restrictions.eq("type", type));
            }

            if(pageSize != null) {
                criteria.setMaxResults(pageSize);
                if(pageNumber != null) {
                    criteria.setFirstResult(pageNumber * pageSize);
                } else {
                    criteria.setFirstResult(0);
                }
            }
            addOrder(criteria, orderHints);

            @SuppressWarnings("unchecked")
            List<NameRelationship> results = criteria.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(NameRelationship.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId(direction.toString()).eq(name.getId()));

            if(type != null) {
                query.add(AuditEntity.relatedId("type").eq(type.getId()));
            }

            if(pageSize != null) {
                query.setMaxResults(pageSize);
                if(pageNumber != null) {
                    query.setFirstResult(pageNumber * pageSize);
                } else {
                    query.setFirstResult(0);
                }
            }

            @SuppressWarnings("unchecked")
            List<NameRelationship> results = query.getResultList();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        }
    }

    @Override
    public List<TypeDesignationBase> getTypeDesignations(TaxonName name, TypeDesignationStatusBase status, Integer pageSize, Integer pageNumber,	List<String> propertyPaths){
        return getTypeDesignations(name, null, status, pageSize, pageNumber, propertyPaths);
    }

    @Override
    public List<Integer> getTypeSpecimenIdsForTaxonName(TaxonName name,
            TypeDesignationStatusBase status, Integer pageSize, Integer pageNumber){
        Query<Integer> query = getTypeDesignationQuery("designation.typeSpecimen.id", name, SpecimenTypeDesignation.class, status);

        addPageSizeAndNumber(query, pageSize, pageNumber);
        List<Integer> result = query.list();
        return result;
    }

    @Override
    public <T extends TypeDesignationBase> List<T> getTypeDesignations(TaxonName name,
                Class<T> type,
                TypeDesignationStatusBase status, Integer pageSize, Integer pageNumber,
                List<String> propertyPaths){
        checkNotInPriorView("getTypeDesignations(TaxonName name,TypeDesignationStatusBase status, Integer pageSize, Integer pageNumber,	List<String> propertyPaths)");

        Query<T> query = getTypeDesignationQuery("designation", name, type, status);

        addPageSizeAndNumber(query, pageSize, pageNumber);
        List<T> result = defaultBeanInitializer.initializeAll(query.list(), propertyPaths);
        return result;
    }

    private <T extends TypeDesignationBase> Query getTypeDesignationQuery(String select, TaxonName name,
            Class<T> type, TypeDesignationStatusBase status){

        Query<?> query = null;
        String queryString = "select "+select+" from TypeDesignationBase designation join designation.typifiedNames name where name = :name";

        if(status != null) {
            queryString +=  " and designation.typeStatus = :status";
        }
        if(type != null){
            queryString +=  " and designation.class = :type";
        }

        query = getSession().createQuery(queryString);

        if(status != null) {
            query.setParameter("status", status);
        }
        if(type != null){
            query.setParameter("type", type.getSimpleName());
        }

        query.setParameter("name",name);
        return query;
    }

    public List<TaxonName> searchNames(String queryString, MatchMode matchMode, Integer pageSize, Integer pageNumber) {
        checkNotInPriorView("TaxonNameDaoHibernateImpl.searchNames(String queryString, Integer pageSize, Integer pageNumber)");
        Criteria criteria = getSession().createCriteria(TaxonName.class);

        if (queryString != null) {
            criteria.add(Restrictions.ilike("nameCache", queryString));
        }
        if(pageSize != null) {
            criteria.setMaxResults(pageSize);
            if(pageNumber != null) {
                criteria.setFirstResult(pageNumber * pageSize);
            } else {
                criteria.setFirstResult(0);
            }
        }
        @SuppressWarnings("unchecked")
        List<TaxonName> results = criteria.list();
        return results;
    }


    @Override
    public List<TaxonName> searchNames(String queryString, Integer pageSize, Integer pageNumber) {
        return searchNames(queryString, MatchMode.BEGINNING, pageSize, pageNumber);
    }


    @Override
    public List<TaxonName> searchNames(String genusOrUninomial,String infraGenericEpithet, String specificEpithet,	String infraSpecificEpithet, Rank rank, Integer pageSize,Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(TaxonName.class);

            /**
             * Given HHH-2951 - "Restrictions.eq when passed null, should create a NullRestriction"
             * We need to convert nulls to NullRestrictions for now
             */
            if(genusOrUninomial != null) {
                criteria.add(Restrictions.eq("genusOrUninomial",genusOrUninomial));
            } else {
                criteria.add(Restrictions.isNull("genusOrUninomial"));
            }

            if(infraGenericEpithet != null) {
                criteria.add(Restrictions.eq("infraGenericEpithet", infraGenericEpithet));
            } else {
                criteria.add(Restrictions.isNull("infraGenericEpithet"));
            }

            if(specificEpithet != null) {
                criteria.add(Restrictions.eq("specificEpithet", specificEpithet));
            } else {
                criteria.add(Restrictions.isNull("specificEpithet"));
            }

            if(infraSpecificEpithet != null) {
                criteria.add(Restrictions.eq("infraSpecificEpithet",infraSpecificEpithet));
            } else {
                criteria.add(Restrictions.isNull("infraSpecificEpithet"));
            }

            if(rank != null) {
                criteria.add(Restrictions.eq("rank", rank));
            }

            if(pageSize != null) {
                criteria.setMaxResults(pageSize);
                if(pageNumber != null) {
                    criteria.setFirstResult(pageNumber * pageSize);
                } else {
                    criteria.setFirstResult(0);
                }
            }

            addOrder(criteria, orderHints);

            @SuppressWarnings("unchecked")
            List<TaxonName> results = criteria.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonName.class,auditEvent.getRevisionNumber());

            if(genusOrUninomial != null) {
                query.add(AuditEntity.property("genusOrUninomial").eq(genusOrUninomial));
            } else {
                query.add(AuditEntity.property("genusOrUninomial").isNull());
            }

            if(infraGenericEpithet != null) {
                query.add(AuditEntity.property("infraGenericEpithet").eq(infraGenericEpithet));
            } else {
                query.add(AuditEntity.property("infraGenericEpithet").isNull());
            }

            if(specificEpithet != null) {
                query.add(AuditEntity.property("specificEpithet").eq(specificEpithet));
            } else {
                query.add(AuditEntity.property("specificEpithet").isNull());
            }

            if(infraSpecificEpithet != null) {
                query.add(AuditEntity.property("infraSpecificEpithet").eq(infraSpecificEpithet));
            } else {
                query.add(AuditEntity.property("infraSpecificEpithet").isNull());
            }

            if(rank != null) {
                query.add(AuditEntity.relatedId("rank").eq(rank.getId()));
            }

            if(pageSize != null) {
                query.setMaxResults(pageSize);
                if(pageNumber != null) {
                    query.setFirstResult(pageNumber * pageSize);
                } else {
                    query.setFirstResult(0);
                }
            }

            @SuppressWarnings("unchecked")
            List<TaxonName> results = query.getResultList();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        }
    }

    @Override
    public List<TaxonName> findByName(boolean doIncludeAuthors,
            String queryString, MatchMode matchmode, Integer pageSize,
            Integer pageNumber, List<Criterion> criteria, List<String> propertyPaths) {

        Criteria crit = getSession().createCriteria(type);
        Criterion nameCacheLike;
        if (matchmode == MatchMode.EXACT) {
            nameCacheLike = Restrictions.eq("nameCache", matchmode.queryStringFrom(queryString));
        } else {
            nameCacheLike = Restrictions.ilike("nameCache", matchmode.queryStringFrom(queryString));
        }
        Criterion notNull = Restrictions.isNotNull("nameCache");
        LogicalExpression nameCacheExpression = Restrictions.and(notNull, nameCacheLike);

        Criterion titleCacheLike;
        if (matchmode == MatchMode.EXACT) {
            titleCacheLike = Restrictions.eq("titleCache", matchmode.queryStringFrom(queryString));
        } else {
            titleCacheLike =Restrictions.ilike("titleCache", matchmode.queryStringFrom(queryString));
        }
        Criterion isNull = Restrictions.isNull("nameCache");
        LogicalExpression titleCacheExpression = Restrictions.and(isNull, titleCacheLike);

        LogicalExpression orExpression = Restrictions.or(titleCacheExpression, nameCacheExpression);

        Criterion finalCriterion = doIncludeAuthors ? titleCacheLike : orExpression;

        crit.add(finalCriterion);
        if(criteria != null){
            for (Criterion criterion : criteria) {
                crit.add(criterion);
            }
        }
        crit.addOrder(Order.asc("nameCache"));

        if(pageSize != null) {
            crit.setMaxResults(pageSize);
            if(pageNumber != null) {
                crit.setFirstResult(pageNumber * pageSize);
            }
        }

        @SuppressWarnings("unchecked")
        List<TaxonName> results = crit.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);

        return results;
    }

    @Override
    public List<TaxonName> findByFullTitle(String queryString,
            MatchMode matchmode, Integer pageSize, Integer pageNumber, List<Criterion> criteria, List<String> propertyPaths) {

        Criteria crit = getSession().createCriteria(type);
        if (matchmode == null){
            matchmode = MatchMode.LIKE;
        }
        if (matchmode == MatchMode.EXACT) {
            crit.add(Restrictions.eq("fullTitleCache", matchmode.queryStringFrom(queryString)));
        } else {
            crit.add(Restrictions.ilike("fullTitleCache", matchmode.queryStringFrom(queryString)));
        }
        if(criteria != null){
            for (Criterion criterion : criteria) {
                crit.add(criterion);
            }
        }
        crit.addOrder(Order.asc("fullTitleCache"));

        if(pageSize != null) {
            crit.setMaxResults(pageSize);
            if(pageNumber != null) {
                crit.setFirstResult(pageNumber * pageSize);
            }
        }

        @SuppressWarnings("unchecked")
        List<TaxonName> results = crit.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);

        return results;
    }

    @Override
    public List<TaxonName> findByTitle(String queryString,
            MatchMode matchmode, Integer pageSize, Integer pageNumber, List<Criterion> criteria, List<String> propertyPaths) {

        Criteria crit = getSession().createCriteria(type);
        if (matchmode == MatchMode.EXACT) {
            crit.add(Restrictions.eq("titleCache", matchmode.queryStringFrom(queryString)));
        } else {
            crit.add(Restrictions.ilike("titleCache", matchmode.queryStringFrom(queryString)));
        }
        if(criteria != null){
            for (Criterion criterion : criteria) {
                crit.add(criterion);
            }
        }
        crit.addOrder(Order.asc("titleCache"));

        if(pageSize != null) {
            crit.setMaxResults(pageSize);
            if(pageNumber != null) {
                crit.setFirstResult(pageNumber * pageSize);
            }
        }

        @SuppressWarnings("unchecked")
        List<TaxonName> results = crit.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);

        return results;
    }


    @Override
    public TaxonName findByUuid(UUID uuid, List<Criterion> criteria, List<String> propertyPaths) {

        Criteria crit = getSession().createCriteria(type);

        if (uuid != null) {
            crit.add(Restrictions.eq("uuid", uuid));
        } else {
            logger.warn("UUID is NULL");
            return null;
        }
        if(criteria != null){
            for (Criterion criterion : criteria) {
                crit.add(criterion);
            }
        }
        crit.addOrder(Order.asc("uuid"));

        @SuppressWarnings("unchecked")
        List<TaxonName> results = crit.list();
        if (results.size() == 1) {
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            TaxonName taxonName = results.iterator().next();
            return taxonName;
        } else if (results.size() > 1) {
            logger.error("Multiple results for UUID: " + uuid);
        } else if (results.size() == 0) {
            logger.info("No results for UUID: " + uuid);
        }

        return null;
    }


    @Override
    public <S extends TaxonName> List<S> list(Class<S> type, List<Restriction<?>> restrictions, Integer limit,
            Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        // TODO Auto-generated method stub
        return list(type, restrictions, limit, start, orderHints, propertyPaths, INCLUDE_UNPUBLISHED);
    }

    @Override
    public <S extends TaxonName> List<S> list(Class<S> type, List<Restriction<?>> restrictions, Integer limit,
            Integer start, List<OrderHint> orderHints, List<String> propertyPaths, boolean includePublished) {
        Criteria criteria = createCriteria(type, restrictions, false);

        if(!includePublished){
            criteria.add(Restrictions.eq("taxonBases.publish", true));
        }

        addLimitAndStart(criteria, limit, start);
        addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<S> result = criteria.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    @Override
    public long count(Class<? extends TaxonName> type, List<Restriction<?>> restrictions) {
        return count(type, restrictions, INCLUDE_UNPUBLISHED);
    }


    @Override
    public long count(Class<? extends TaxonName> type, List<Restriction<?>> restrictions, boolean includePublished) {

        Criteria criteria = createCriteria(type, restrictions, false);
        if(!includePublished){
            criteria.add(Restrictions.eq("taxonBases.publish", true));
        }
        criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
        return (Long) criteria.uniqueResult();
    }

    @Override
    public Integer countByName(String queryString, MatchMode matchmode, List<Criterion> criteria) {
        //TODO improve performance
        boolean includeAuthors = false;
        List<TaxonName> results = findByName(
                includeAuthors,queryString, matchmode, null, null, criteria, null);
        return results.size();

    }

    @Override
    public List<UuidAndTitleCache> getUuidAndTitleCacheOfNames(Integer limit, String pattern) {
        String queryString = "SELECT uuid, id, fullTitleCache FROM TaxonName LIMIT " + limit;

        @SuppressWarnings("unchecked")
        List<Object[]> result = getSession().createSQLQuery(queryString).list();

        if(result.size() == 0){
            return null;
        }else{
            List<UuidAndTitleCache> list = new ArrayList<>(result.size());

            for (Object object : result){

                Object[] objectArray = (Object[]) object;

                UUID uuid = UUID.fromString((String) objectArray[0]);
                Integer id = (Integer) objectArray[1];
                String titleCache = (String) objectArray[2];

                list.add(new UuidAndTitleCache(type, uuid, id, titleCache));
            }

            return list;
        }
    }

    @Override
    public long countByName(Class<TaxonName> clazz,String queryString, MatchMode matchmode, List<Criterion> criteria) {
        return super.countByParam(clazz, "nameCache", queryString, matchmode, criteria);
    }

    @Override
    public long countByFullTitle(Class<TaxonName> clazz,String queryString, MatchMode matchmode, List<Criterion> criteria) {
        return super.countByParam(clazz, "fullTitleCache", queryString, matchmode, criteria);
    }

    @Override
    public List<TaxonName> findByName(Class<TaxonName> clazz,	String queryString, MatchMode matchmode, List<Criterion> criteria,Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,	List<String> propertyPaths) {
        return super.findByParam(clazz, "nameCache", queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
    }

    @Override
    public UUID delete (TaxonName persistentObject){
        @SuppressWarnings("rawtypes")
        Set<TaxonBase> taxonBases = persistentObject.getTaxonBases();

        getSession().saveOrUpdate(persistentObject);
        UUID homotypicalGroupUUID = persistentObject.getHomotypicalGroup().getUuid();


        for (TaxonBase<?> taxonBase: taxonBases){
            taxonDao.delete(taxonBase);
        }
        HomotypicalGroup homotypicalGroup = homotypicalGroupDao.load(homotypicalGroupUUID);
        homotypicalGroup = HibernateProxyHelper.deproxy(homotypicalGroup);

        if (homotypicalGroup != null){
            if (homotypicalGroup.getTypifiedNames().contains(persistentObject)){
                homotypicalGroup.getTypifiedNames().remove(persistentObject);
                homotypicalGroupDao.saveOrUpdate(homotypicalGroup);
            }
        }

        getSession().delete(persistentObject);
        if (homotypicalGroup.getTypifiedNames().isEmpty()){
            homotypicalGroupDao.delete(homotypicalGroup);
        }
        return persistentObject.getUuid();
    }


    @Override
    public IZoologicalName findZoologicalNameByUUID(UUID uuid){
        Criteria criteria = getSession().createCriteria(type);
        if (uuid != null) {
            criteria.add(Restrictions.eq("uuid", uuid));
        } else {
            logger.warn("UUID is NULL");
            return null;
        }

        @SuppressWarnings("unchecked")
        List<TaxonName> results = criteria.list();
        if (results.size() == 1) {
            defaultBeanInitializer.initializeAll(results, null);
            TaxonName taxonName = results.iterator().next();
            if (taxonName.isZoological()) {
                IZoologicalName zoologicalName = taxonName;
                return zoologicalName;
            } else {
                logger.warn("This UUID (" + uuid + ") does not belong to a ZoologicalName. It belongs to: " + taxonName.getUuid() + " (" + taxonName.getTitleCache() + ")");
            }
        } else if (results.size() > 1) {
            logger.error("Multiple results for UUID: " + uuid);
        } else if (results.size() == 0) {
            logger.info("No results for UUID: " + uuid);
        }
        return null;
    }

    @Override
    public List<HashMap<String,String>> getNameRecords(){
    	String sql= "SELECT"
    			+ "  (SELECT famName.namecache FROM TaxonNode famNode"
    			+ "  LEFT OUTER JOIN TaxonBase famTax ON famNode.taxon_id = famTax.id"
    			+ " LEFT OUTER JOIN TaxonName famName ON famTax.name_id = famName.id"
    			+ " WHERE famName.rank_id = 795 AND famNode.treeIndex = SUBSTRING(tn.treeIndex, 1, length(famNode.treeIndex))"
    			+ "	) as famName, "
    			+ " (SELECT famName.namecache FROM TaxonNode famNode "
    			+ " LEFT OUTER JOIN TaxonBase famTax ON famNode.taxon_id = famTax.id "
    			+ " LEFT OUTER JOIN TaxonName famName ON famTax.name_id = famName.id "
    			+ " WHERE famName.rank_id = 795 AND famNode.treeIndex = SUBSTRING(tnAcc.treeIndex, 1, length(famNode.treeIndex))"
    			+ "	) as accFamName,tb.DTYPE, tb.id as TaxonID ,tb.titleCache taxonTitle,  tnb.rank_id as RankID, tnb.id as NameID,"
    			+ " tnb.namecache as name, tnb.titleCache as nameAuthor, tnb.fullTitleCache nameAndNomRef,"
    			+ "	r.titleCache as nomRef, r.abbrevTitle nomRefAbbrevTitle, r.title nomRefTitle, r.datepublished_start nomRefPublishedStart, r.datepublished_end nomRefPublishedEnd, r.pages nomRefPages, inRef.abbrevTitle inRefAbbrevTitle,tnb.nomenclaturalmicroreference as detail,"
    			+ "	nameType.namecache nameType, nameType.titleCache nameTypeAuthor, nameType.fullTitleCache nameTypeFullTitle, nameTypeRef.titleCache nameTypeRef, "
    			+ " inRef.seriespart as inRefSeries, inRef.datepublished_start inRefPublishedStart, inRef.datepublished_end inRefPublishedEnd, inRef.volume as inRefVolume"
    			+ " FROM TaxonBase tb"
    			+ " LEFT OUTER JOIN TaxonName tnb ON tb.name_id = tnb.id"
    			+ "	LEFT OUTER JOIN OriginalSourceBase nosb ON tnb.id = nosb.sourcedName_id"
    			+ " LEFT OUTER JOIN Reference r ON nosb.citation_id = r.id"
                + "	LEFT OUTER JOIN TaxonNode tn ON tn.taxon_id = tb.id"
    			+ "	LEFT OUTER JOIN TaxonName_TypeDesignationBase typeMN ON typeMN.TaxonName_id = tnb.id"
    			+ " LEFT OUTER JOIN TypeDesignationBase tdb ON tdb.id = typeMN.typedesignations_id"
    			+ "	LEFT OUTER JOIN TaxonName nameType ON tdb.typename_id = nameType.id"
    			+ " LEFT OUTER JOIN OriginalSourceBase nameTypeOsb ON nameType.id = nameTypeOsb.sourcedName_id"
    			+ "	LEFT OUTER JOIN Reference nameTypeRef ON nameTypeOsb.citation_id = nameTypeRef.id"
                + "		LEFT OUTER JOIN Reference inRef ON inRef.id = r.inreference_id"
    			+ "	LEFT OUTER JOIN TaxonBase accT ON accT.id = tb.acceptedTaxon_id"
    			+ "		LEFT OUTER JOIN TaxonNode tnAcc ON tnAcc.taxon_id = accT.id"
    			+ "	ORDER BY DTYPE, famName, accFamName,  tnb.rank_id ,tb.titleCache";

    	String hqlQueryStringFrom = "FROM TaxonBase taxonBase "
    	        + " LEFT OUTER JOIN taxonBase.name as tnb "
    	        + " LEFT OUTER JOIN tnb.nomenclaturalSource as nomSource "
    	        + " LEFT OUTER JOIN nomSource.citation as nomRef "
                + " LEFT OUTER JOIN taxonBase.taxonNodes as node "
    	        + " LEFT OUTER JOIN tnb.typeDesignations as type "
    	        + " LEFT OUTER JOIN type.typifiedNames as nameType "
    	        + " LEFT OUTER JOIN nameType.nomenclaturalSource as nameTypeSource "
    	        + " LEFT OUTER JOIN nameTypeSource.citation as nameTypeRef "
                + " LEFT OUTER JOIN nomRef.inReference as inRef "
    	        + " LEFT OUTER JOIN taxonBase.acceptedTaxon as accTaxon "
    	        + " LEFT OUTER JOIN accTaxon.taxonNodes as accTaxonNodes";

    	Query hqlQuery = getSession().createQuery(hqlQueryStringFrom);
    	List<?> hqlResult = hqlQuery.list();

		List<HashMap<String,String>> nameRecords = new ArrayList<>();
		HashMap<String,String> nameRecord = new HashMap<>();
		TaxonNode familyNode = null;
		for(Object object : hqlResult){
			Object[] row = (Object[])object;
			nameRecord = new HashMap<>();
			TaxonBase<?> taxonBase = (TaxonBase<?>)row[0];
			Taxon accTaxon = null;
			if (taxonBase instanceof Taxon){
			    accTaxon = HibernateProxyHelper.deproxy(taxonBase, Taxon.class);
			} else{
			    nameRecord.put("famName", "");
			    Synonym syn = HibernateProxyHelper.deproxy(taxonBase, Synonym.class);
			    accTaxon = syn.getAcceptedTaxon();
			}
			Set<TaxonNode> nodes = accTaxon.getTaxonNodes();
            if (nodes.size() == 1){
                TaxonNode node = nodes.iterator().next();
                familyNode = node.getAncestorOfRank(Rank.FAMILY());
            }

            nameRecord.put("famName",familyNode.getTaxon().getName().getNameCache());
            nameRecord.put("accFamName","");

			//nameRecord.put("famName",(String)row[0]);

			nameRecord.put("accFamName",(String)row[1]);

			nameRecord.put("DTYPE",(String)row[2]);
			nameRecord.put("TaxonID",String.valueOf(row[3]));
			nameRecord.put("taxonTitle",(String)row[4]);
            nameRecord.put("RankID",String.valueOf(row[5]));
            nameRecord.put("NameID",String.valueOf(row[6]));
            nameRecord.put("name",(String)row[7]);
            nameRecord.put("nameAuthor",(String)row[8]);
            nameRecord.put("nameAndNomRef",(String)row[9]);
            nameRecord.put("nomRef",(String)row[10]);
            nameRecord.put("nomRefAbbrevTitle",(String)row[11]);
            nameRecord.put("nomRefTitle",(String)row[12]);
            nameRecord.put("nomRefPublishedStart",(String)row[13]);
            nameRecord.put("nomRefPublishedEnd",(String)row[14]);
            nameRecord.put("nomRefPages",(String)row[15]);
            nameRecord.put("inRefAbbrevTitle",(String)row[16]);
            nameRecord.put("detail",(String)row[17]);
            nameRecord.put("nameType",(String)row[18]);
            nameRecord.put("nameTypeAuthor",(String)row[19]);
            nameRecord.put("nameTypeFullTitle",(String)row[20]);
            nameRecord.put("nameTypeRef",(String)row[21]);
            nameRecord.put("inRefSeries",(String)row[22]);
            nameRecord.put("inRefPublishedStart",(String)row[23]);
            nameRecord.put("inRefPublishedEnd",(String)row[24]);
            nameRecord.put("inRefVolume",(String)row[25]);
            nameRecords.add(nameRecord);
	   }

		return nameRecords;
    }

    @Override
    public List<TaxonNameParts> findTaxonNameParts(Optional<String> genusOrUninomial,
            Optional<String> infraGenericEpithet, Optional<String> specificEpithet,
            Optional<String> infraSpecificEpithet, Rank rank, Collection<UUID> excludedNamesUuids, Integer pageSize, Integer pageIndex, List<OrderHint> orderHints) {

        StringBuilder hql = prepareFindTaxonNameParts(false, genusOrUninomial, infraGenericEpithet,
                specificEpithet, infraSpecificEpithet, rank, excludedNamesUuids);
        addOrder(hql, "n", orderHints);
        Query<TaxonNameParts> query = getSession().createQuery(hql.toString(), TaxonNameParts.class);
        if(rank != null){
            query.setParameter("rank", rank);
        }
        if(excludedNamesUuids != null && excludedNamesUuids.size() > 0){
            query.setParameterList("excludedNamesUuids", excludedNamesUuids);
        }
        addPageSizeAndNumber(query, pageSize, pageIndex);
        List<TaxonNameParts> result = query.list();
        return result;
    }

    @Override
    public long countTaxonNameParts(Optional<String> genusOrUninomial, Optional<String> infraGenericEpithet,
            Optional<String> specificEpithet, Optional<String> infraSpecificEpithet, Rank rank, Collection<UUID> excludedNamesUUIDs) {

        StringBuilder hql = prepareFindTaxonNameParts(true, genusOrUninomial, infraGenericEpithet, specificEpithet, infraSpecificEpithet, rank, excludedNamesUUIDs);
        Query<Long> query = getSession().createQuery(hql.toString(), Long.class);
        if(rank != null){
            query.setParameter("rank", rank);
        }
        if(excludedNamesUUIDs != null && excludedNamesUUIDs.size() > 0){
            query.setParameterList("excludedNamesUuids", excludedNamesUUIDs);
        }

        return query.uniqueResult();
    }

    private StringBuilder prepareFindTaxonNameParts(boolean doCount, Optional<String> genusOrUninomial,
            Optional<String> infraGenericEpithet, Optional<String> specificEpithet,
            Optional<String> infraSpecificEpithet, Rank rank, Collection<UUID> excludedNamesUuids) {

        StringBuilder hql = new StringBuilder();
        if(doCount){
            hql.append("select count(n.id) ");
        } else {
            hql.append("select new eu.etaxonomy.cdm.persistence.dto.TaxonNameParts(n.id, n.uuid, n.rank, n.genusOrUninomial, n.infraGenericEpithet, n.specificEpithet, n.infraSpecificEpithet) ");
        }
        hql.append("from TaxonName n ");
        hql.append("where 1 = 1 ");

        if(rank != null){
            hql.append("and n.rank = :rank ");
        }
        if(excludedNamesUuids != null && excludedNamesUuids.size() > 0){
            hql.append("and n.uuid not in ( :excludedNamesUuids ) ");
        }

        addFieldPredicate(hql, "n.genusOrUninomial", genusOrUninomial);
        addFieldPredicate(hql, "n.infraGenericEpithet", infraGenericEpithet);
        addFieldPredicate(hql, "n.specificEpithet", specificEpithet);
        addFieldPredicate(hql, "n.infraSpecificEpithet", infraSpecificEpithet);

        return hql;
    }

    @Override
    public long countNameRelationships(Set<NameRelationshipType> types) {
        Criteria criteria = getSession().createCriteria(NameRelationship.class);

        if (types != null) {
            if (types.isEmpty()){
                return 0l;
            }else{
                criteria.add(Restrictions.in("type", types) );
            }
        }
        //count
        criteria.setProjection(Projections.rowCount());
        long result = (Long)criteria.uniqueResult();

        return result;
    }

    @Override
    public List<NameRelationship> getNameRelationships(Set<NameRelationshipType> types,
            Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        Criteria criteria = getCriteria(NameRelationship.class);
        if (types != null) {
            if (types.isEmpty()){
                return new ArrayList<>();
            }else{
                criteria.add(Restrictions.in("type", types) );
            }
        }
        addOrder(criteria,orderHints);
        addPageSizeAndNumber(criteria, pageSize, pageNumber);

        @SuppressWarnings("unchecked")
        List<NameRelationship> results = criteria.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);

        return results;
    }

    @Override
    public long countHybridRelationships(Set<HybridRelationshipType> types) {
        Criteria criteria = getSession().createCriteria(HybridRelationship.class);

        if (types != null) {
            if (types.isEmpty()){
                return 0l;
            }else{
                criteria.add(Restrictions.in("type", types) );
            }
        }
        //count
        criteria.setProjection(Projections.rowCount());
        long result = (Long)criteria.uniqueResult();

        return result;
    }

    @Override
    public List<HybridRelationship> getHybridRelationships(Set<HybridRelationshipType> types,
            Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        Criteria criteria = getCriteria(HybridRelationship.class);
        if (types != null) {
            if (types.isEmpty()){
                return new ArrayList<>();
            }else{
                criteria.add(Restrictions.in("type", types) );
            }
        }
        addOrder(criteria,orderHints);
        addPageSizeAndNumber(criteria, pageSize, pageNumber);

        @SuppressWarnings("unchecked")
        List<HybridRelationship> results = criteria.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);

        return results;
    }
}