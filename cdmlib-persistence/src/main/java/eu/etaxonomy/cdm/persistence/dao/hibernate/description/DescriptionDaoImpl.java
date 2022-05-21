/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.description;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.OperationNotSupportedInPriorViewException;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dto.SortableTaxonNodeQueryResult;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

@Repository
@Qualifier("descriptionDaoImpl")
public class DescriptionDaoImpl
            extends IdentifiableDaoBase<DescriptionBase>
            implements IDescriptionDao{

    private static final Logger logger = Logger.getLogger(DescriptionDaoImpl.class);

    public DescriptionDaoImpl() {
        super(DescriptionBase.class);
        indexedClasses = new Class[3];
        indexedClasses[0] = TaxonDescription.class;
        indexedClasses[1] = TaxonNameDescription.class;
        indexedClasses[2] = SpecimenDescription.class;
    }

//    @Override  //Override for testing
//    public DescriptionBase load(UUID uuid, List<String> propertyPaths){
//    	DescriptionBase bean = findByUuid(uuid);
//        if(bean == null){
//            return bean;
//        }
//        defaultBeanInitializer.initialize(bean, propertyPaths);
//
//        return bean;
//    }

    @Override
    public long countDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTerm status) {
        checkNotInPriorView("DescriptionDaoImpl.countDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase status)");
        Query<Long> query = null;

        if(status == null) {
            query = getSession().createQuery("select count(distinct description) from TaxonDescription description left join description.descriptionElements element join element.area area where area in (:namedAreas)", Long.class);
        } else {
            query = getSession().createQuery("select count(distinct description) from TaxonDescription description left join description.descriptionElements element join element.area area  join element.status status where area in (:namedAreas) and status = :status", Long.class);
            query.setParameter("status", status);
        }
        query.setParameterList("namedAreas", namedAreas);

        return query.uniqueResult();
    }

    @Override
    public <T extends DescriptionElementBase> long countDescriptionElements(DescriptionBase description, Set<Feature> features, Class<T> clazz) {
        return countDescriptionElements(description, null, features, clazz);
    }

    @Override
    public <T extends DescriptionElementBase> long countDescriptionElements(DescriptionBase description, Class<? extends DescriptionBase> descriptionType,
            Set<Feature> features, Class<T> clazz) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if (clazz == null){
            clazz = (Class<T>)DescriptionElementBase.class;
        }
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getCriteria(clazz);

            if(description != null) {
                criteria.add(Restrictions.eq("inDescription", description));
            }

            if(descriptionType != null) {
                criteria.createAlias("inDescription", "d").add(Restrictions.eq("d.class", descriptionType));
            }

            if(features != null && !features.isEmpty()) {
                criteria.add(Restrictions.in("feature", features));
            }

            criteria.setProjection(Projections.rowCount());

            return (Long)criteria.uniqueResult();
        } else {
            if(features != null && !features.isEmpty()) {
                long count = 0;
                for(Feature f : features) {
                    AuditQuery query = null;
                    query = makeAuditQuery(clazz, auditEvent);

                    if(description != null) {
                        query.add(AuditEntity.relatedId("inDescription").eq(description.getId()));
                    }

                    if(descriptionType != null) {
                        query.add(AuditEntity.property("inDescription.class").eq(descriptionType));
                    }

                    query.add(AuditEntity.relatedId("feature").eq(f.getId()));
                    query.addProjection(AuditEntity.id().count());
                    count += (Long)query.getSingleResult();
                }

                return count;
            } else {
                AuditQuery query = makeAuditQuery(clazz, auditEvent);

                if(description != null) {
                    query.add(AuditEntity.relatedId("inDescription").eq(description.getId()));
                }
                if(descriptionType != null) {
                    query.add(AuditEntity.property("inDescription.class").eq(descriptionType));
                }

                query.addProjection(AuditEntity.id().count());
                return (Long)query.getSingleResult();
            }
        }
    }

    @Override
    public long countDescriptions(Class<? extends DescriptionBase> clazz, Boolean hasImages, Boolean hasText, Set<Feature> features) {
        checkNotInPriorView("DescriptionDaoImpl.countDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText, Set<Feature> features)");

        Criteria inner = getCriteria(clazz);

        Criteria elementsCriteria = inner.createCriteria("descriptionElements");
        if(hasText != null) {
            if(hasText) {
                elementsCriteria.add(Restrictions.isNotEmpty("multilanguageText"));
            } else {
                elementsCriteria.add(Restrictions.isEmpty("multilanguageText"));
            }
        }

        if(hasImages != null) {
            if(hasImages) {
                elementsCriteria.add(Restrictions.isNotEmpty("media"));
            } else {
                elementsCriteria.add(Restrictions.isEmpty("media"));
            }
        }

        if(features != null && !features.isEmpty()) {
            elementsCriteria.add(Restrictions.in("feature", features));
        }

        inner.setProjection(Projections.countDistinct("id"));

        return (Long)inner.uniqueResult();
    }

    @Override
    public long countTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes,
            Set<NamedArea> geographicalScopes, Set<MarkerType> markerTypes, Set<DescriptionType> descriptionTypes) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getCriteria(TaxonDescription.class);

            if(taxon != null) {
                criteria.add(Restrictions.eq("taxon", taxon));
            }

            if(scopes != null && !scopes.isEmpty()) {
                Set<Integer> scopeIds = new HashSet<>();
                for(DefinedTerm s : scopes) {
                    scopeIds.add(s.getId());
                }
                criteria.createCriteria("scopes").add(Restrictions.in("id", scopeIds));
            }

            if(geographicalScopes != null && !geographicalScopes.isEmpty()) {
                Set<Integer> geoScopeIds = new HashSet<>();
                for(NamedArea n : geographicalScopes) {
                    geoScopeIds.add(n.getId());
                }
                criteria.createCriteria("geoScopes").add(Restrictions.in("id", geoScopeIds));
            }

            addMarkerTypesCriterion(markerTypes, criteria);
            addDescriptionTypesCriterion(descriptionTypes, criteria);

            criteria.setProjection(Projections.rowCount());

            return (Long)criteria.uniqueResult();
        } else {
            if((scopes == null || scopes.isEmpty())&& (geographicalScopes == null || geographicalScopes.isEmpty()) && (markerTypes == null || markerTypes.isEmpty())) {
                AuditQuery query = makeAuditQuery(TaxonDescription.class,auditEvent);
                if(taxon != null) {
                    query.add(AuditEntity.relatedId("taxon").eq(taxon.getId()));
                }

                query.addProjection(AuditEntity.id().count());

                return (Long)query.getSingleResult();
            } else {
                throw new OperationNotSupportedInPriorViewException("countTaxonDescriptions(Taxon taxon, Set<Scope> scopes,Set<NamedArea> geographicalScopes)");
            }
        }
    }

    private void addDescriptionTypesCriterion(Set<DescriptionType> descriptionTypes, Criteria criteria) {
        if(descriptionTypes != null && !descriptionTypes.isEmpty()) {
            Set<Criterion> typeCriteria = new HashSet<>();
            for (DescriptionType descriptionType : descriptionTypes) {
                typeCriteria.add(Restrictions.sqlRestriction("{alias}.types like '%"+descriptionType.getKey()+"%'"));
            }
            criteria.add(Restrictions.and(typeCriteria.toArray(new Criterion[]{})));
        }
    }

    /**
     * @param markerTypes
     * @param criteria
     *
     */
    //TODO move to AnnotatableEntityDao(?)
    private void addMarkerTypesCriterion(Set<MarkerType> markerTypes, Criteria criteria) {

        if(markerTypes != null && !markerTypes.isEmpty()) {
            Set<Integer> markerTypeIds = new HashSet<Integer>();
            for(MarkerType markerType : markerTypes) {
                markerTypeIds.add(markerType.getId());
            }
            criteria.createCriteria("markers").add(Restrictions.eq("flag", true))
                    .createAlias("markerType", "mt")
                     .add(Restrictions.in("mt.id", markerTypeIds));
        } else if (markerTypes != null && markerTypes.isEmpty()){
            //AT: added in case the projects requires an third state description, An empty Marker type set
        }
    }
    @Override
    public <T extends DescriptionElementBase> List<T> getDescriptionElements(
            DescriptionBase description, Set<Feature> features,
            Class<T> clazz, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        return getDescriptionElements(description, null, features, clazz, pageSize, pageNumber, propertyPaths);
    }

    @Override
    public <T extends DescriptionElementBase> List<T> getDescriptionElements(
            DescriptionBase description, Class<? extends DescriptionBase> descriptionType,
            Set<Feature> features,
            Class<T> clazz,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = null;
            if(clazz == null) {
                criteria = getSession().createCriteria(DescriptionElementBase.class);
            } else {
                criteria = getSession().createCriteria(clazz);
            }

            if(description != null) {
                criteria.add(Restrictions.eq("inDescription", description));
            }
            if(descriptionType != null) {
                criteria.createAlias("inDescription", "d").add(Restrictions.eq("d.class", descriptionType));
            }

            if(features != null && !features.isEmpty()) {
                criteria.add(Restrictions.in("feature", features));
            }

            if(pageSize != null) {
                criteria.setMaxResults(pageSize);
                if(pageNumber != null) {
                    criteria.setFirstResult(pageNumber * pageSize);
                }
            }

            List<T> results = criteria.list();

            defaultBeanInitializer.initializeAll(results, propertyPaths);

            return results;
        } else {
            List<T> result = new ArrayList<T>();
            if(features != null && !features.isEmpty()) {

                for(Feature f : features) {
                    AuditQuery query = null;
                    if(clazz == null) {
                        query = getAuditReader().createQuery().forEntitiesAtRevision(DescriptionElementBase.class,auditEvent.getRevisionNumber());
                    } else {
                        query = getAuditReader().createQuery().forEntitiesAtRevision(clazz,auditEvent.getRevisionNumber());
                    }

                    if(description != null) {
                        query.add(AuditEntity.relatedId("inDescription").eq(description.getId()));
                    }

                    if(descriptionType != null) {
                        query.add(AuditEntity.property("inDescription.class").eq(descriptionType));
                    }

                    query.add(AuditEntity.relatedId("feature").eq(f.getId()));
                    result.addAll(query.getResultList());
                }
            } else {
                AuditQuery query = null;
                if(clazz == null) {
                    query = getAuditReader().createQuery().forEntitiesAtRevision(DescriptionElementBase.class,auditEvent.getRevisionNumber());
                } else {
                    query = getAuditReader().createQuery().forEntitiesAtRevision(clazz,auditEvent.getRevisionNumber());
                }

                if(description != null) {
                    query.add(AuditEntity.relatedId("inDescription").eq(description.getId()));
                }

                if(descriptionType != null) {
                    query.add(AuditEntity.property("inDescription.class").eq(descriptionType));
                }

                result = query.getResultList();
            }

            defaultBeanInitializer.initializeAll(result, propertyPaths);

            return result;
        }
    }

    @Override
    public List<TaxonDescription> listTaxonDescriptions(Taxon taxon, Set<DefinedTerm> scopes, Set<NamedArea> geographicalScopes, Set<MarkerType> markerTypes, Set<DescriptionType> descriptionTypes, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(TaxonDescription.class);

            if(taxon != null) {
                criteria.add(Restrictions.eq("taxon", taxon));
            }

            if(scopes != null && !scopes.isEmpty()) {
                Set<Integer> scopeIds = new HashSet<Integer>();
                for(DefinedTerm s : scopes) {
                    scopeIds.add(s.getId());
                }
                criteria.createCriteria("scopes").add(Restrictions.in("id", scopeIds));
            }

            if(geographicalScopes != null && !geographicalScopes.isEmpty()) {
                Set<Integer> geoScopeIds = new HashSet<Integer>();
                for(NamedArea n : geographicalScopes) {
                    geoScopeIds.add(n.getId());
                }
                criteria.createCriteria("geoScopes").add(Restrictions.in("id", geoScopeIds));
            }

            addMarkerTypesCriterion(markerTypes, criteria);
            addDescriptionTypesCriterion(descriptionTypes, criteria);

            if(pageSize != null) {
                criteria.setMaxResults(pageSize);
                if(pageNumber != null) {
                    criteria.setFirstResult(pageNumber * pageSize);
                }
            }

            List<TaxonDescription> results = criteria.list();

            defaultBeanInitializer.initializeAll(results, propertyPaths);

            return results;
        } else {
            if((scopes == null || scopes.isEmpty())&& (geographicalScopes == null || geographicalScopes.isEmpty())&& (markerTypes == null || markerTypes.isEmpty())) {
                AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonDescription.class,auditEvent.getRevisionNumber());
                if(taxon != null) {
                    query.add(AuditEntity.relatedId("taxon").eq(taxon.getId()));
                }

                if(pageSize != null) {
                    query.setMaxResults(pageSize);
                    if(pageNumber != null) {
                        query.setFirstResult(pageNumber * pageSize);
                    } else {
                        query.setFirstResult(0);
                    }
                }

                List<TaxonDescription> results = query.getResultList();
                defaultBeanInitializer.initializeAll(results, propertyPaths);
                return results;
            } else {
                throw new OperationNotSupportedInPriorViewException("countTaxonDescriptions(Taxon taxon, Set<Scope> scopes,Set<NamedArea> geographicalScopes)");
            }
        }
    }

    @Override
    public List<TaxonNameDescription> getTaxonNameDescriptions(TaxonName name, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(TaxonNameDescription.class);

          if(name != null) {
              criteria.add(Restrictions.eq("taxonName", name));
          }

          if(pageSize != null) {
              criteria.setMaxResults(pageSize);
              if(pageNumber != null) {
                  criteria.setFirstResult(pageNumber * pageSize);
              }
          }

          List<TaxonNameDescription> results = criteria.list();

          defaultBeanInitializer.initializeAll(results, propertyPaths);

          return results;
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonNameDescription.class,auditEvent.getRevisionNumber());

            if(name != null) {
                query.add(AuditEntity.relatedId("taxonName").eq(name.getId()));
            }

            if(pageSize != null) {
                  query.setMaxResults(pageSize);
                  if(pageNumber != null) {
                      query.setFirstResult(pageNumber * pageSize);
                  }
            }

            List<TaxonNameDescription> results = query.getResultList();

            defaultBeanInitializer.initializeAll(results, propertyPaths);

            return results;
        }

    }

    @Override
    public long countTaxonNameDescriptions(TaxonName name) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getCriteria(TaxonNameDescription.class);

            if(name != null) {
                criteria.add(Restrictions.eq("taxonName", name));
            }

            criteria.setProjection(Projections.rowCount());

            return (Long)criteria.uniqueResult();
        } else {
            AuditQuery query = makeAuditQuery(TaxonNameDescription.class,auditEvent);

            if(name != null) {
                query.add(AuditEntity.relatedId("taxonName").eq(name.getId()));
            }

            query.addProjection(AuditEntity.id().count());
            return (Long)query.getSingleResult();
        }
    }

    /**
     * Should use a DetachedCriteria & subquery, but HHH-158 prevents this, for now.
     *
     * e.g. DetachedCriteria inner = DestachedCriteria.forClass(type);
     *
     * outer.add(Subqueries.propertyIn("id", inner));
     */
    @Override
    public List<DescriptionBase> listDescriptions(Class<? extends DescriptionBase> clazz, Boolean hasImages, Boolean hasText,	Set<Feature> features, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        checkNotInPriorView("DescriptionDaoImpl.listDescriptions(Class<TYPE> type, Boolean hasImages, Boolean hasText,	Set<Feature> features, Integer pageSize, Integer pageNumber)");
        Criteria inner = getCriteria(clazz);

        Criteria elementsCriteria = inner.createCriteria("descriptionElements");
        if(hasText != null) {
            if(hasText) {
                elementsCriteria.add(Restrictions.isNotEmpty("multilanguageText"));
            } else {
                elementsCriteria.add(Restrictions.isEmpty("multilanguageText"));
            }
        }

        if(hasImages != null) {
            if(hasImages) {
                elementsCriteria.add(Restrictions.isNotEmpty("media"));
            } else {
                elementsCriteria.add(Restrictions.isEmpty("media"));
            }
        }

        if(features != null && !features.isEmpty()) {
            elementsCriteria.add(Restrictions.in("feature", features));
        }

        inner.setProjection(Projections.distinct(Projections.id()));

        @SuppressWarnings("unchecked")
        List<Object> intermediateResult = inner.list();

        if(intermediateResult.isEmpty()) {
            return new ArrayList<>();
        }

        Integer[] resultIds = new Integer[intermediateResult.size()];
        for(int i = 0; i < resultIds.length; i++) {
                resultIds[i] = (Integer)intermediateResult.get(i);
        }

        Criteria outer = getCriteria(clazz);

        outer.add(Restrictions.in("id", resultIds));
        addOrder(outer, orderHints);

        addPageSizeAndNumber(outer, pageSize, pageNumber);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<DescriptionBase> results = outer.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public List<TaxonDescription> searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTerm status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        checkNotInPriorView("DescriptionDaoImpl.searchDescriptionByDistribution(Set<NamedArea> namedAreas, PresenceAbsenceTermBase status, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)");

        Criteria criteria = getSession().createCriteria(TaxonDescription.class);
        Criteria elements = criteria.createCriteria("descriptionElements", "descriptionElement", Criteria.LEFT_JOIN);
        elements.add(Restrictions.in("area", namedAreas.toArray()));

        if(status != null) {
            elements.add(Restrictions.eq("status", status));
        }

        ProjectionList projectionList = Projections.projectionList().add(Projections.id());

        if(orderHints != null && !orderHints.isEmpty()) {
            for(OrderHint orderHint : orderHints) {
                projectionList = projectionList.add(Projections.property(orderHint.getPropertyName()));
            }
        }

        criteria.setProjection(Projections.distinct(projectionList));

        if(pageSize != null) {
            criteria.setMaxResults(pageSize);
            if(pageNumber != null) {
                criteria.setFirstResult(pageNumber * pageSize);
            }
        }

        addOrder(criteria,orderHints);

        @SuppressWarnings("unchecked")
        List<Object> intermediateResult = criteria.list();

        if(intermediateResult.isEmpty()) {
            return new ArrayList<>();
        }

        Integer[] resultIds = new Integer[intermediateResult.size()];
        for(int i = 0; i < resultIds.length; i++) {
            if(orderHints == null || orderHints.isEmpty()) {
                resultIds[i] = (Integer)intermediateResult.get(i);
            } else {
              resultIds[i] = ((Number)((Object[])intermediateResult.get(i))[0]).intValue();
            }
        }

        criteria = getSession().createCriteria(TaxonDescription.class);
        criteria.add(Restrictions.in("id", resultIds));
        addOrder(criteria,orderHints);

        @SuppressWarnings("unchecked")
        List<TaxonDescription> results = criteria.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public List<CommonTaxonName> searchDescriptionByCommonName(String queryString, MatchMode matchMode, Integer pageSize, Integer pageNumber) {

        Criteria crit = getSession().createCriteria(CommonTaxonName.class);
        if (matchMode == MatchMode.EXACT) {
            crit.add(Restrictions.eq("name", matchMode.queryStringFrom(queryString)));
        } else {
            crit.add(Restrictions.ilike("name", matchMode.queryStringFrom(queryString)));
        }

        if(pageSize != null) {
            crit.setMaxResults(pageSize);
            if(pageNumber != null) {
                crit.setFirstResult(pageNumber * pageSize);
            }
        }
        @SuppressWarnings("unchecked")
        List<CommonTaxonName> results = crit.list();
        return results;
    }

    @Override
    public Integer countDescriptionByCommonName(String queryString, MatchMode matchMode) {
        //TODO inprove performance
        List<CommonTaxonName> results =  searchDescriptionByCommonName(queryString, matchMode, null, null);
        return results.size();
    }

    @Override
    public DescriptionBase find(LSID lsid) {
        DescriptionBase<?> descriptionBase = super.find(lsid);
        if(descriptionBase != null) {
            List<String> propertyPaths = new ArrayList<>();
            propertyPaths.add("createdBy");
            propertyPaths.add("updatedBy");
            propertyPaths.add("taxon");
            propertyPaths.add("taxonName");
            propertyPaths.add("descriptionElements");
            propertyPaths.add("descriptionElements.createdBy");
            propertyPaths.add("descriptionElements.updatedBy");
            propertyPaths.add("descriptionElements.feature");
            propertyPaths.add("descriptionElements.multilanguageText");
            propertyPaths.add("descriptionElements.multilanguageText.language");
            propertyPaths.add("descriptionElements.area");
            propertyPaths.add("descriptionElements.status");
            propertyPaths.add("descriptionElements.modifyingText");
            propertyPaths.add("descriptionElementsmodifyingText.language");
            propertyPaths.add("descriptionElements.modifiers");

            defaultBeanInitializer.initialize(descriptionBase, propertyPaths);
        }
        return descriptionBase;
    }


    @Override
    public List<Integer> getIndividualAssociationSpecimenIDs(UUID taxonUuid,
            Set<Feature> features, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths){
        Query<Integer> query = prepareGetDescriptionElementForTaxon(taxonUuid, features, IndividualsAssociation.class, pageSize, pageNumber, "de.associatedSpecimenOrObservation.id");
        List<Integer> results = query.list();
        return results;
    }

    @Override
    public List<SortableTaxonNodeQueryResult> getNodeOfIndividualAssociationForSpecimen(UUID specimenUuid, UUID classificationUuid){
        String selectString = " new " +SortableTaxonNodeQueryResult.class.getName()+"(n.uuid, n.id, n.treeIndex, t.uuid, t.titleCache, t.name.titleCache, t.name.rank, n.parent.uuid) ";
        Query<SortableTaxonNodeQueryResult> query = prepareGetIndividualAssociationForSpecimen(specimenUuid, classificationUuid, selectString);
        @SuppressWarnings("unchecked")
        List<SortableTaxonNodeQueryResult> results = query.list();
        return results;
    }

    @Override
    public <T extends DescriptionElementBase> List<T> getDescriptionElementForTaxon(
            UUID taxonUuid, Set<Feature> features,
            Class<T> type, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths) {

//      Logger.getLogger("org.hibernate.SQL").setLevel(Level.TRACE);
        Query<T> query = prepareGetDescriptionElementForTaxon(taxonUuid, features, type, pageSize, pageNumber, "de");

        if (logger.isDebugEnabled()){logger.debug(" dao: get list ...");}
        List<T> results = query.list();
        if (logger.isDebugEnabled()){logger.debug(" dao: initialize ...");}
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        if (logger.isDebugEnabled()){logger.debug(" dao: initialize - DONE");}

//        Logger.getLogger("org.hibernate.SQL").setLevel(Level.WARN);
        return results;
    }

    @Override
    public <T extends DescriptionElementBase> long countDescriptionElementForTaxon(
            UUID taxonUuid, Set<Feature> features, Class<T> type) {

        Query<Long> query = prepareGetDescriptionElementForTaxon(taxonUuid, features, type, null, null, "count(de)");

        return query.uniqueResult();
    }

    private <T extends DescriptionElementBase, R extends Object> Query<R> prepareGetDescriptionElementForTaxon(UUID taxonUuid,
            Set<Feature> features, Class<T> type, Integer pageSize, Integer pageNumber, String selectString) {

        String queryString = "SELECT " + selectString + " FROM DescriptionElementBase AS de" +
                " LEFT JOIN de.inDescription AS d" +
                " LEFT JOIN d.taxon AS t" +
                " WHERE d.class = 'TaxonDescription' AND t.uuid = :taxon_uuid ";

        if(type != null){
            queryString += " and de.class = :type";
        }
        if (features != null && features.size() > 0){
            queryString += " and de.feature in (:features) ";
        }
        Query<R> query = getSession().createQuery(queryString);

        query.setParameter("taxon_uuid", taxonUuid);
        if(type != null){
            query.setParameter("type", type.getSimpleName());
        }
        if(features != null && features.size() > 0){
            query.setParameterList("features", features) ;
        }

        if(pageSize != null) {
            query.setMaxResults(pageSize);
            if(pageNumber != null) {
                query.setFirstResult(pageNumber * pageSize);
            }
        }
        return query;
    }

    private Query<SortableTaxonNodeQueryResult> prepareGetIndividualAssociationForSpecimen(UUID specimenUuid, UUID classificationUuid, String selectString) {

        String queryString = "SELECT " + selectString + " FROM DescriptionElementBase AS de" +
                " LEFT JOIN de.inDescription AS d" +
                " LEFT JOIN d.taxon AS t" +
                " LEFT JOIN t.taxonNodes AS n" +
                " LEFT JOIN de.associatedSpecimenOrObservation AS specimen ";
        String classificationString = "";
        if (classificationUuid != null){
            classificationString = " LEFT JOIN n.classification AS c ";
        }
        String whereString = " WHERE specimen.uuid = :specimen_uuid";
        if (classificationUuid != null){
            whereString = whereString + " AND c.uuid = :classifcationUuid";
        }

        Query<SortableTaxonNodeQueryResult> query = getSession().createQuery(queryString + classificationString + whereString, SortableTaxonNodeQueryResult.class);

        query.setParameter("specimen_uuid", specimenUuid);
        if (classificationUuid != null){
            query.setParameter("classifcationUuid", classificationUuid);
        }



        return query;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao#listTaxonDescriptionMedia(java.util.UUID, java.lang.Boolean, java.util.Set, java.lang.Integer, java.lang.Integer, java.util.List)
     */
    @Override
    public List<Media> listTaxonDescriptionMedia(UUID taxonUuid,
            Boolean limitToGalleries, Set<MarkerType> markerTypes,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

               AuditEvent auditEvent = getAuditEventFromContext();
            if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
                String queryString = " SELECT media " +
                    getTaxonDescriptionMediaQueryString(
                        taxonUuid, limitToGalleries,  markerTypes);
                queryString +=
                    " GROUP BY media "
//	    						" ORDER BY index(media) "  //not functional
                    ;

                Query<Media> query = getSession().createQuery(queryString, Media.class);

                setTaxonDescriptionMediaParameters(query, taxonUuid, limitToGalleries, markerTypes);


//	            addMarkerTypesCriterion(markerTypes, hql);

                setPagingParameter(query, pageSize, pageNumber);

                @SuppressWarnings("unchecked")
                List<Media> results = query.list();

                defaultBeanInitializer.initializeAll(results, propertyPaths);

                return results;
            } else {
                throw new OperationNotSupportedInPriorViewException("countTaxonDescriptionMedia(UUID taxonUuid, boolean restrictToGalleries)");
            }
    }

    @Override
    public int countTaxonDescriptionMedia(UUID taxonUuid,
            Boolean limitToGalleries, Set<MarkerType> markerTypes) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            String queryString = " SELECT count(DISTINCT media) " +
                getTaxonDescriptionMediaQueryString(
                    taxonUuid, limitToGalleries, markerTypes);

            Query<Long> query = getSession().createQuery(queryString, Long.class);
            setTaxonDescriptionMediaParameters(query, taxonUuid, limitToGalleries, markerTypes);
            return query.uniqueResult().intValue();
        }else{
            throw new OperationNotSupportedInPriorViewException("countTaxonDescriptionMedia(UUID taxonUuid)");
        }

    }

    private void setTaxonDescriptionMediaParameters(Query query, UUID taxonUuid, Boolean limitToGalleries, Set<MarkerType> markerTypes) {
        if(taxonUuid != null){
            query.setParameter("uuid", taxonUuid);
        }

    }

    /**
     * @param taxonUuid
     * @param restrictToGalleries
     * @param markerTypes
     * @return
     */
    private String getTaxonDescriptionMediaQueryString(UUID taxonUuid,
            Boolean restrictToGalleries, Set<MarkerType> markerTypes) {
        String fromQueryString =
            " FROM DescriptionElementBase as deb INNER JOIN " +
                " deb.inDescription as td "
                + " INNER JOIN td.taxon as t "
                + " JOIN deb.media as media "
                + " LEFT JOIN td.markers marker ";

        String whereQueryString = " WHERE (1=1) ";
        if (taxonUuid != null){
            whereQueryString += " AND t.uuid = :uuid ";
        }
        if (restrictToGalleries){
            whereQueryString += " AND td.imageGallery is true ";
        }
        if (markerTypes != null && !markerTypes.isEmpty()){
            whereQueryString += " AND (1=0";
            for (MarkerType markerType : markerTypes){
                whereQueryString += " OR ( marker.markerType.id = " + markerType.getId() + " AND marker.flag is true)";

            }
            whereQueryString += ") ";
        }

        return fromQueryString + whereQueryString;

    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TermDto> listNamedAreasInUse(boolean includeAllParents, Integer pageSize, Integer pageNumber) {

//        Logger.getLogger("org.hibernate.SQL").setLevel(Level.TRACE);

        StringBuilder queryString = new StringBuilder(
                "SELECT DISTINCT a.id, a.partOf.id"
                + " FROM Distribution AS d JOIN d.area AS a");
        Query<Object[]> query = getSession().createQuery(queryString.toString(), Object[].class);

        List<Object[]> areasInUse = query.list();
        List<Object[]> parentResults = new ArrayList<>();

        if(!areasInUse.isEmpty()) {
            Set<Object> allAreaIds = new HashSet<>(areasInUse.size());

            if(includeAllParents) {
                // find all parent nodes
                String allAreasQueryStr = "select a.id, a.partOf.id from NamedArea as a";
                query = getSession().createQuery(allAreasQueryStr, Object[].class);
                List<Object[]> allAreasResult = query.list();
                Map<Object, Object> allAreasMap = ArrayUtils.toMap(allAreasResult.toArray());

                Set<Object> parents = new HashSet<>();

                for(Object[] leaf : areasInUse) {
                    allAreaIds.add(leaf[0]);
                    Object parentId = leaf[1];
                    while (parentId != null) {
                        if(parents.contains(parentId)) {
                            // break if the parent already is in the set
                            break;
                        }
                        parents.add(parentId);
                        parentId = allAreasMap.get(parentId);
                    }
                }
                allAreaIds.addAll(parents);
            } else {
                // only add the ids found so far
                for(Object[] leaf : areasInUse) {
                    allAreaIds.add(leaf[0]);
                }
            }


            // NOTE can't use "select new TermDto(distinct a.uuid, r , a.vocabulary.uuid) since we will get multiple
            // rows for a term with multiple representations
            String parentAreasQueryStr = TermDto.getTermDtoSelect("NamedArea")
                    + "where a.id in (:allAreaIds) order by a.idInVocabulary";
            query = getSession().createQuery(parentAreasQueryStr, Object[].class);
            query.setParameterList("allAreaIds", allAreaIds);
            if(pageSize != null) {
                query.setMaxResults(pageSize);
                if(pageNumber != null) {
                    query.setFirstResult(pageNumber * pageSize);
                }
            }
            parentResults = query.list();
        }
        List<TermDto> dtoList = TermDto.termDtoListFrom(parentResults);

        return dtoList;
    }





}
