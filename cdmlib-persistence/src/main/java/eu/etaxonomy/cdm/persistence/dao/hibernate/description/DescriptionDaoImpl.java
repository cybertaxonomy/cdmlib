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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.OperationNotSupportedInPriorViewException;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.term.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dto.CategoricalDataDto;
import eu.etaxonomy.cdm.persistence.dto.DescriptionBaseDto;
import eu.etaxonomy.cdm.persistence.dto.FeatureDto;
import eu.etaxonomy.cdm.persistence.dto.QuantitativeDataDto;
import eu.etaxonomy.cdm.persistence.dto.SortableTaxonNodeQueryResult;
import eu.etaxonomy.cdm.persistence.dto.TermDto;

@Repository
@Qualifier("descriptionDaoImpl")
public class DescriptionDaoImpl
            extends IdentifiableDaoBase<DescriptionBase>
            implements IDescriptionDao{

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private IDefinedTermDao termDao;


    @SuppressWarnings("unchecked")
    public DescriptionDaoImpl() {
        super(DescriptionBase.class);
        indexedClasses = new Class[3];
        indexedClasses[0] = TaxonDescription.class;
        indexedClasses[1] = TaxonNameDescription.class;
        indexedClasses[2] = SpecimenDescription.class;
    }

    @Override
    public <T extends DescriptionElementBase> long countDescriptionElements(
            DescriptionBase description, Class<? extends DescriptionBase> descriptionType,
            Set<Feature> features, Class<T> clazz, boolean includeUnpublished) {

        AuditEvent auditEvent = getAuditEventFromContext();
        if (clazz == null){
            clazz = (Class<T>)DescriptionElementBase.class;
        }
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getCriteria(clazz);
            criteria.createAlias("inDescription", "d");

            if(description != null) {
                criteria.add(Restrictions.eq("inDescription", description));
            }

            if(descriptionType != null) {
                criteria.add(Restrictions.eq("d.class", descriptionType));
            }

            if(features != null && !features.isEmpty()) {
                criteria.add(Restrictions.in("feature", features));
            }

            if (!includeUnpublished) {
                criteria.add(Restrictions.eq("d.publish", true));
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

                    if (!includeUnpublished) {
                        query.add(AuditEntity.property("inDescription.publish").eq(true));
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
            DescriptionBase description, Class<? extends DescriptionBase> descriptionType,
            Set<Feature> features,
            Class<T> clazz, boolean includeUnpublished,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            clazz = clazz == null? (Class)DescriptionElementBase.class : clazz;

            CriteriaBuilder cb = getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(clazz);
            Root<T> root = cq.from(clazz);
            List<Predicate> predicates = new ArrayList<>();
            if(description != null) {
                predicates.add(predicateEqual(cb, root, "inDescription", description));
            }
            if (!includeUnpublished) {
                Join<T, DescriptionBase> descriptionJoin = root.join("inDescription", JoinType.INNER);
                predicates.add(predicateBoolean(cb, descriptionJoin, "publish", true));
            }
            if(descriptionType != null) {
                Join<T, DescriptionBase> join = root.join("inDescription", JoinType.INNER);
                Join<T, ? extends DescriptionBase> treatJoin = cb.treat(join, descriptionType);
                predicates.add(treatJoin.isNotNull());
//                cb.equal(cb.typ .type(join), descriptionType);  //CriteriaBuilder.type() should exist in HibernateCriteriaBuilder since 5.2 but doesn't
            }
            if(!CdmUtils.isNullSafeEmpty(features)) {
                predicates.add(predicateIn(root, "feature", features));
            }

            cq.select(root)
              .where(predicateAnd(cb, predicates));

            List<T> results = addPageSizeAndNumber(
                     getSession().createQuery(cq), pageSize, pageNumber)
                    .getResultList();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;

        } else {
            List<T> result = new ArrayList<>();
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
            Set<Feature> features, boolean includeUnpublished,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths){

        Query<Integer> query = prepareGetDescriptionElementForTaxon(taxonUuid, features,
                IndividualsAssociation.class, includeUnpublished, pageSize, pageNumber,
                "de.associatedSpecimenOrObservation.id");
        List<Integer> results = query.list();
        return results;
    }

    @Override
    public List<SortableTaxonNodeQueryResult> getNodeOfIndividualAssociationForSpecimen(UUID specimenUuid, UUID classificationUuid){
        String selectString = " new " +SortableTaxonNodeQueryResult.class.getName()+"(n.uuid, n.id, n.treeIndex, t.uuid, t.titleCache, t.name.titleCache, t.name.rank, n.parent.uuid) ";
        Query<SortableTaxonNodeQueryResult> query = prepareGetIndividualAssociationForSpecimen(specimenUuid, classificationUuid, selectString);
        List<SortableTaxonNodeQueryResult> results = query.list();
        return results;
    }

    @Override
    public <T extends DescriptionElementBase> List<T> getDescriptionElementForTaxon(
            UUID taxonUuid, Set<Feature> features,
            Class<T> type, boolean includeUnpublished,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

//      LogUtils.setLevel("org.hibernate.SQL", Level.TRACE);
        Query<T> query = prepareGetDescriptionElementForTaxon(taxonUuid, features, type, includeUnpublished,
                pageSize, pageNumber, "de");

        if (logger.isDebugEnabled()){logger.debug(" dao: get list ...");}
        List<T> results = query.list();
        if (logger.isDebugEnabled()){logger.debug(" dao: initialize ...");}
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        if (logger.isDebugEnabled()){logger.debug(" dao: initialize - DONE");}

//      LogUtils.setLevel("org.hibernate.SQL", Level.WARN);
        return results;
    }

    @Override
    public <T extends DescriptionElementBase> long countDescriptionElementForTaxon(
            UUID taxonUuid, Set<Feature> features, Class<T> type, boolean includeUnpublished) {

        Query<Long> query = prepareGetDescriptionElementForTaxon(taxonUuid, features, type, includeUnpublished, null, null, "count(de)");

        return query.uniqueResult();
    }

    private <T extends DescriptionElementBase, R extends Object> Query<R> prepareGetDescriptionElementForTaxon(UUID taxonUuid,
            Set<Feature> features, Class<T> type, boolean includeUnpublished, Integer pageSize, Integer pageNumber, String selectString) {

        String queryString = "SELECT " + selectString + " FROM DescriptionElementBase AS de" +
                " LEFT JOIN de.inDescription AS d" +
                " LEFT JOIN d.taxon AS t" +
                " WHERE d.class = 'TaxonDescription' AND t.uuid = :taxon_uuid " +
                (includeUnpublished? "" : " AND d.publish = :publish ");

        if(type != null){
            queryString += " and de.class = :type";
        }
        boolean hasFeatureFilter = !CdmUtils.isNullSafeEmpty(features);
        if (hasFeatureFilter){
            queryString += " and de.feature in (:features) ";
        }
        Query<R> query = getSession().createQuery(queryString);

        query.setParameter("taxon_uuid", taxonUuid);
        if(type != null){
            query.setParameter("type", type.getSimpleName());
        }
        if (!includeUnpublished){
            query.setParameter("publish", true);
        }
        if(hasFeatureFilter){
            query.setParameterList("features", features) ;
        }

        addPageSizeAndNumber(query, pageSize, pageNumber);
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

    @Override
    public List<TermDto> listNamedAreasInUse(boolean includeAllParents, Integer pageSize, Integer pageNumber) {

//      LogUtils.setLevel("org.hibernate.SQL", Level.TRACE);

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

            addPageSizeAndNumber(query, pageSize, pageNumber);
            parentResults = query.list();
        }
        List<TermDto> dtoList = TermDto.termDtoListFrom(parentResults, null);

        return dtoList;
    }

    @Override
    public DescriptionBaseDto loadDto(UUID descriptionUuid) {
        String sqlSelect =  DescriptionBaseDto.getDescriptionBaseDtoSelect();
        Query<Object[]> query =  getSession().createQuery(sqlSelect, Object[].class);
        List<UUID> uuids = new ArrayList<>();
        uuids.add(descriptionUuid);
        query.setParameterList("uuid", uuids);

        List<Object[]> result = query.list();

        List<DescriptionBaseDto> list = DescriptionBaseDto.descriptionBaseDtoListFrom(result);

        if (list.size()== 1){
            DescriptionBaseDto dto = list.get(0);
            //get categorical data
            sqlSelect = CategoricalDataDto.getCategoricalDtoSelect();
            query =  getSession().createQuery(sqlSelect);
            query.setParameter("uuid", descriptionUuid);

            @SuppressWarnings("unchecked")
            List<Object[]>  resultCat = query.list();
            List<CategoricalDataDto> listCategorical = CategoricalDataDto.categoricalDataDtoListFrom(resultCat);

            List<UUID> featureUuids = new ArrayList<>();
            for (CategoricalDataDto catDto: listCategorical){
                featureUuids.add(catDto.getFeatureUuid());
            }
            Map<UUID, TermDto> featureDtos = termDao.findFeatureByUUIDsAsDtos(featureUuids);
            for (CategoricalDataDto catDto: listCategorical){
                FeatureDto featuredto = (FeatureDto)featureDtos.get(catDto.getFeatureUuid());
                catDto.setFeatureDto(featuredto);
            }
            dto.getElements().addAll(listCategorical);
            //get quantitative data
            sqlSelect = QuantitativeDataDto.getQuantitativeDataDtoSelect();
            query =  getSession().createQuery(sqlSelect);
            query.setParameter("uuid", descriptionUuid);
            @SuppressWarnings("unchecked")
            List<Object[]>  resultQuant = query.list();
            List<QuantitativeDataDto> listQuant = QuantitativeDataDto.quantitativeDataDtoListFrom(resultQuant);
            dto.getElements().addAll(listQuant);
            return dto;
        }else{
            return null;
        }
    }


    @Override
    public List<DescriptionBaseDto> loadDtos(Set<UUID> descriptionUuids) {
        String sqlSelect =  DescriptionBaseDto.getDescriptionBaseDtoSelect();
        Query<Object[]> query =  getSession().createQuery(sqlSelect, Object[].class);
        query.setParameterList("uuid", descriptionUuids);

        List<Object[]> result = query.list();

        List<DescriptionBaseDto> list = DescriptionBaseDto.descriptionBaseDtoListFrom(result);

        for (DescriptionBaseDto dto: list){

            //get categorical data
            sqlSelect = CategoricalDataDto.getCategoricalDtoSelect();
            query = getSession().createQuery(sqlSelect, Object[].class);
            query.setParameter("uuid", dto.getDescriptionUuid());
            List<Object[]>  resultCat = query.list();
            List<CategoricalDataDto> listCategorical = CategoricalDataDto.categoricalDataDtoListFrom(resultCat);

            List<UUID> featureUuids = new ArrayList<>();
            for (CategoricalDataDto catDto: listCategorical){
                featureUuids.add(catDto.getFeatureUuid());
            }
            Map<UUID, TermDto> featureDtos = termDao.findFeatureByUUIDsAsDtos(featureUuids);
            for (CategoricalDataDto catDto: listCategorical){
                FeatureDto featuredto = (FeatureDto)featureDtos.get(catDto.getFeatureUuid());
                catDto.setFeatureDto(featuredto);
            }
            dto.getElements().addAll(listCategorical);
            //get quantitative data
            sqlSelect = QuantitativeDataDto.getQuantitativeDataDtoSelect();
            query = getSession().createQuery(sqlSelect, Object[].class);
            query.setParameter("uuid",  dto.getDescriptionUuid());
            List<Object[]> resultQuant = query.list();
            List<QuantitativeDataDto> listQuant = QuantitativeDataDto.quantitativeDataDtoListFrom(resultQuant);
            dto.getElements().addAll(listQuant);
        }
        return list;
    }
}