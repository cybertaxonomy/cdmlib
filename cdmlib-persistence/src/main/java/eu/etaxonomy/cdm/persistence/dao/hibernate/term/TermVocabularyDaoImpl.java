/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.CdmClass;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.term.ITermVocabularyDao;
import eu.etaxonomy.cdm.persistence.dto.FeatureDto;
import eu.etaxonomy.cdm.persistence.dto.TermCollectionDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.TermVocabularyDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 */
@Repository
public class TermVocabularyDaoImpl extends IdentifiableDaoBase<TermVocabulary> implements
		ITermVocabularyDao {

	@SuppressWarnings("unchecked")
    public TermVocabularyDaoImpl() {
		super(TermVocabulary.class);
		indexedClasses = new Class[2];
		indexedClasses[0] = TermVocabulary.class;
		indexedClasses[1] = OrderedTermVocabulary.class;
	}

	@Override
    public long countTerms(TermVocabulary termVocabulary) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select count(term) from DefinedTermBase term where term.vocabulary = :vocabulary");
		    query.setParameter("vocabulary", termVocabulary);

		    return (Long)query.uniqueResult();
		} else {
			AuditQuery query = makeAuditQuery(null, auditEvent);
			query.addProjection(AuditEntity.id().count());
			query.add(AuditEntity.relatedId("vocabulary").eq(termVocabulary.getId()));
			return (Long)query.getSingleResult();
		}
	}

	@Override
    public <T extends DefinedTermBase> List<T> getTerms(TermVocabulary<T> vocabulary,Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
			Criteria criteria = getCriteria(DefinedTermBase.class);
			criteria.createCriteria("vocabulary").add(Restrictions.idEq(vocabulary.getId()));

			addPageSizeAndNumber(criteria, pageSize, pageNumber);
		    this.addOrder(criteria, orderHints);

		    @SuppressWarnings("unchecked")
            List<T> result = DefinedTermDaoImpl.deduplicateResult(criteria.list());
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
		    return result;
		} else {
			AuditQuery query = makeAuditQuery(null, auditEvent);
			query.add(AuditEntity.relatedId("vocabulary").eq(vocabulary.getId()));

			addPageSizeAndNumber(query, pageSize, pageNumber);

			@SuppressWarnings("unchecked")
            List<T> result = DefinedTermDaoImpl.deduplicateResult(query.getResultList());
		    defaultBeanInitializer.initializeAll(result, propertyPaths);
			return result;
		}
	}

    @Override
    public <T extends DefinedTermBase> TermVocabulary<T> findByUri(String termSourceUri, Class<T> clazz) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    //TODO use clazz
    		Query query = getSession().createQuery("select vocabulary from TermVocabulary vocabulary where vocabulary.termSourceUri= :termSourceUri");
	    	query.setParameter("termSourceUri", termSourceUri);

	    	@SuppressWarnings("unchecked")
	    	TermVocabulary<T> result = (TermVocabulary<T>)query.uniqueResult();
	    	return result;
		} else {
            AuditQuery query = makeAuditQuery(clazz, auditEvent);
			query.add(AuditEntity.property("termSourceUri").eq(termSourceUri));

			@SuppressWarnings("unchecked")
            TermVocabulary<T> result = (TermVocabulary<T>)query.getSingleResult();
			return result;
		}
	}

	@Override
    public <T extends DefinedTermBase> List<T> getTerms(TermVocabulary<T> termVocabulary, Integer pageSize,	Integer pageNumber) {
		return getTerms(termVocabulary, pageSize, pageNumber, null, null);
	}

    @Override
    public <T extends DefinedTermBase> List<TermVocabulary<T>> findByTermType(TermType termType, List<String> propertyPaths) {

        Criteria criteria = getSession().createCriteria(type);
        criteria.add(Restrictions.eq("termType", termType));
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        //this.addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<TermVocabulary<T>> result = DefinedTermDaoImpl.deduplicateResult(criteria.list());
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

	@Override
    public List<TermVocabulary> listByTermType(TermType termType, boolean includeSubTypes, Integer limit, Integer start,List<OrderHint> orderHints, List<String> propertyPaths) {
        checkNotInPriorView("TermVocabularyDao.listByTermType(TermType termType, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths)");

        Set<TermType> allTermTypes = new HashSet<TermType>();
        allTermTypes.add(termType);
        if (includeSubTypes){
            allTermTypes.addAll(termType.getGeneralizationOf(true));
        }

        Criteria criteria = getSession().createCriteria(type);
        criteria.add(Restrictions.in("termType", allTermTypes));

        if(limit != null) {
            criteria.setMaxResults(limit);
            if(start != null) {
                criteria.setFirstResult(start);
            }
        }

        this.addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<TermVocabulary> result = DefinedTermDaoImpl.deduplicateResult(criteria.list());
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

	@Override
	public void missingTermUuids(
			Map<UUID, Set<UUID>> uuidsRequested,
			Map<UUID, Set<UUID>> uuidMissingTermsRepsonse,
			Map<UUID, TermVocabulary<?>> vocabularyResponse){

		Set<UUID> missingTermCandidateUuids = new HashSet<>();

		for (Set<UUID> uuidsPerVocSet : uuidsRequested.values()){
			missingTermCandidateUuids.addAll(uuidsPerVocSet);
		}

 		//search persisted subset of required (usually all)
		String hql = " SELECT terms.uuid " +
				" FROM TermVocabulary voc join voc.terms terms  " +
				" WHERE terms.uuid IN (:uuids) " +
				" ORDER BY voc.uuid ";
		Query query = getSession().createQuery(hql);

		int splitSize = 2000;
		List<Collection<UUID>> missingTermCandidates = splitCollection(missingTermCandidateUuids, splitSize);
		List<UUID> persistedUuids = new ArrayList<>();

		for (Collection<UUID> uuids : missingTermCandidates){
		    query.setParameterList("uuids", uuids);
		    @SuppressWarnings("unchecked")
            List<UUID> list = query.list();
		    persistedUuids.addAll(list);
		}


 		//fully load and initialize vocabularies if required
		if (vocabularyResponse != null){
			String hql2 = " SELECT DISTINCT voc " +
					" FROM TermVocabulary voc " +
						" LEFT JOIN FETCH voc.terms terms " +
						" LEFT JOIN FETCH terms.representations representations " +
						" LEFT JOIN FETCH voc.representations vocReps " +
					" WHERE terms.uuid IN (:termUuids) OR  (  voc.uuid IN (:vocUuids)  ) " +  //was: AND voc.terms is empty, but did not load originally empty vocabularies with user defined terms added
//					" WHERE  voc.uuid IN (:vocUuids) AND voc.terms is empty  " +
					" ORDER BY voc.uuid ";
			query = getSession().createQuery(hql2);
			query.setParameterList("termUuids", missingTermCandidateUuids);
			query.setParameterList("vocUuids", uuidsRequested.keySet());

			for (Collection<UUID> uuids : missingTermCandidates){
			    query.setParameterList("termUuids", uuids);
			    @SuppressWarnings("unchecked")
	            List<TermVocabulary<?>> o = query.list();
	            for (TermVocabulary<?> voc : o){
	                vocabularyResponse.put(voc.getUuid(), voc);
	            }
	        }
		}

		//compute missing terms
		if (missingTermCandidateUuids.size() == persistedUuids.size()){
			missingTermCandidateUuids.clear();
		}else{
			missingTermCandidateUuids.removeAll(persistedUuids);
			//add missing terms to response
			for (UUID vocUUID : uuidsRequested.keySet()){
				for (UUID termUuid : uuidsRequested.get(vocUUID)){
					if (missingTermCandidateUuids.contains(termUuid)){
						Set<UUID> r = uuidMissingTermsRepsonse.get(vocUUID);
						if (r == null){
							r = new HashSet<>();
							uuidMissingTermsRepsonse.put(vocUUID, r);
						}
						r.add(termUuid);
					}
				}
			}
		}

		return;
	}

	@Override
	public Collection<TermDto> getTerms(List<UUID> vocabularyUuids) {
	    String queryString = TermDto.getTermDtoSelect()
	            + "where v.uuid in :vocabularyUuids "
	            + "order by a.titleCache";
	    Query query =  getSession().createQuery(queryString);
	    query.setParameterList("vocabularyUuids", vocabularyUuids);

	    @SuppressWarnings("unchecked")
	    List<Object[]> result = query.list();

	    List<TermDto> list = TermDto.termDtoListFrom(result);
	    return list;
	}

	@Override
	public Collection<TermDto> getTerms(UUID vocabularyUuid) {
	    String queryString = TermDto.getTermDtoSelect()
	            + "where v.uuid = :vocabularyUuid "
	            + "order by a.titleCache";
	    Query query =  getSession().createQuery(queryString);
	    query.setParameter("vocabularyUuid", vocabularyUuid);

	    @SuppressWarnings("unchecked")
	    List<Object[]> result = query.list();

	    List<TermDto> list = TermDto.termDtoListFrom(result);
	    return list;
	}

	@Override
    public Collection<TermDto> getNamedAreaTerms(List<UUID> vocabularyUuids) {
        String queryString = TermDto.getTermDtoSelectNamedArea()
                + "where v.uuid in :vocabularyUuids "
                + "order by a.titleCache";
        Query query =  getSession().createQuery(queryString);
        query.setParameterList("vocabularyUuids", vocabularyUuids);

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

        List<TermDto> list = TermDto.termDtoListFrom(result);
        return list;
    }

    @Override
    public Collection<TermDto> getTopLevelTerms(UUID vocabularyUuid) {
        String queryString = TermDto.getTermDtoSelect()
                + "where v.uuid = :vocabularyUuid "
                + "and a.partOf is null "
                + "and a.kindOf is null";
        Query query =  getSession().createQuery(queryString);
        query.setParameter("vocabularyUuid", vocabularyUuid);

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

        List<TermDto> list = TermDto.termDtoListFrom(result);
        return list;
    }

    @Override
    public Collection<TermDto> getTopLevelTerms(UUID vocabularyUuid, TermType type) {
        String queryString;
        if (type.equals(TermType.NamedArea)){
            queryString = TermDto.getTermDtoSelectNamedArea();
        }else if (type.equals(TermType.Feature) || type.isKindOf(TermType.Feature)){
            queryString = FeatureDto.getTermDtoSelect();
        }else{
            queryString = TermDto.getTermDtoSelect();
        }
        queryString = queryString
                + "where v.uuid = :vocabularyUuid "
                + "and a.partOf is null "
                + "and a.kindOf is null";
        Query query =  getSession().createQuery(queryString);
        query.setParameter("vocabularyUuid", vocabularyUuid);

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();
        List<TermDto> list = null;
        if (type.equals(TermType.Feature)|| type.isKindOf(TermType.Feature)){
            list = FeatureDto.termDtoListFrom(result);
        }else{
            list = TermDto.termDtoListFrom(result);
        }
        return list;
    }

    @Override
    public List<TermVocabularyDto> findVocabularyDtoByTermTypes(Set<TermType> termTypes) {
        return findVocabularyDtoByTermTypes(termTypes, true);
    }

    @Override
    public List<TermVocabularyDto> findVocabularyDtoByTermTypes(Set<TermType> termTypes, boolean includeSubtypes) {
        return findVocabularyDtoByTermTypes(termTypes, null, includeSubtypes);
    }

    @Override
    public List<TermVocabularyDto> findVocabularyDtoByAvailableFor(Set<CdmClass> availableForSet) {

        String queryVocWithFittingTerms = "SELECT DISTINCT(v.uuid) FROM DefinedTermBase term JOIN term.vocabulary as v WHERE " ;
        for (CdmClass availableFor: availableForSet){
            queryVocWithFittingTerms += " term.availableFor like '%"+availableFor.getKey()+"%' AND term.termType = :feature";
        }

        String queryString = TermCollectionDto.getTermCollectionDtoSelect()
                + " WHERE a.uuid in "
                + " (" + queryVocWithFittingTerms + ")";

        Query query =  getSession().createQuery(queryString);
        query.setParameter("feature", TermType.Feature);

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

        List<TermVocabularyDto>  dtos = TermVocabularyDto.termVocabularyDtoListFrom(result);

        return dtos;
    }


    @Override
    public List<TermVocabularyDto> findVocabularyDtoByTermTypes(Set<TermType> termTypes, String pattern, boolean includeSubtypes) {
        Set<TermType> termTypeWithSubType = new HashSet<>();
        if (! (termTypes.isEmpty() || (termTypes.size() == 1 && termTypes.iterator().next() == null))){
            termTypeWithSubType = new HashSet<>(termTypes);
        }

        if(includeSubtypes){
            if (!termTypes.isEmpty()){
                for (TermType termType : termTypes) {
                    if (termType != null){
                        termTypeWithSubType.addAll(termType.getGeneralizationOf(true));
                    }
                }
            }
        }
        String queryString = TermVocabularyDto.getTermCollectionDtoSelect();

        if (!termTypeWithSubType.isEmpty()){
            queryString += " where a.termType in (:termTypes) ";
            if (pattern != null){
                queryString += " AND a.titleCache like :pattern";
            }
        }else{
            if (pattern != null){
                queryString += " WHERE a.titleCache like :pattern";
            }
        }

        Query query =  getSession().createQuery(queryString);
        if (!termTypeWithSubType.isEmpty()){
            query.setParameterList("termTypes", termTypeWithSubType);
        }
        if (pattern != null){
            pattern = pattern.replace("*", "%");
            pattern = "%"+pattern+"%";
            query.setParameter("pattern", pattern);
        }
        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();
        List<TermVocabularyDto> dtos = TermVocabularyDto.termVocabularyDtoListFrom(result);
        return dtos;

//        Map<UUID, TermVocabularyDto> dtoMap = new HashMap<>(result.size());
//        for (Object[] elements : result) {
//            UUID uuid = (UUID)elements[0];
//            TermType termType = (TermType)elements[1];
//            if(dtoMap.containsKey(uuid)){
//                dtoMap.get(uuid).addRepresentation((Representation)elements[2]);
//            } else {
//                Set<Representation> representations = new HashSet<>();
//                if(elements[2] instanceof Representation) {
//                    representations = new HashSet<Representation>();
//                    representations.add((Representation)elements[2]);
//                } else {
//                    representations = (Set<Representation>)elements[2];
//                }
//                dtoMap.put(uuid, new TermVocabularyDto(uuid, representations, termType, (String)elements[3]));
//            }
//        }
//        return new ArrayList<>(dtoMap.values());
    }

    @Override
    public List<TermVocabularyDto> findVocabularyDtoByTermType(TermType termType) {
        return findVocabularyDtoByTermTypes(Collections.singleton(termType));
    }

    @Override
    public <S extends TermVocabulary> List<UuidAndTitleCache<S>> getUuidAndTitleCache(Class<S> clazz, TermType termType,
            Integer limit, String pattern) {
        if(termType==null){
            return getUuidAndTitleCache(clazz, limit, pattern);
        }
        Session session = getSession();
        Query query = null;
        if (pattern != null){
            query = session.createQuery(
                      " SELECT uuid, id, titleCache "
                    + " FROM " + clazz.getSimpleName()
                    + " WHERE titleCache LIKE :pattern "
                    + " AND termType = :termType");
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            pattern = pattern + "%";
            query.setParameter("pattern", pattern);
        } else {
            query = session.createQuery(
                      " SELECT uuid, id, titleCache "
                    + " FROM  " + clazz.getSimpleName()
                    + " WHERE termType = :termType");
        }
        query.setParameter("termType", termType);
        if (limit != null){
           query.setMaxResults(limit);
        }
        return getUuidAndTitleCache(query);
    }

    @Override
    public TermVocabularyDto findVocabularyDtoByUuid(UUID vocUuid) {
        if (vocUuid == null ){
            return null;
        }

        String queryString = TermCollectionDto.getTermCollectionDtoSelect()
                + " where a.uuid like :uuid ";
//                + "order by a.titleCache";
        Query query =  getSession().createQuery(queryString);
        query.setParameter("uuid", vocUuid);

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();
        if (result.size() == 1){
            return TermVocabularyDto.termVocabularyDtoListFrom(result).get(0);
        }
        return null;
    }

    @Override
    public List<TermVocabularyDto> findVocabularyDtoByUuids(List<UUID> vocUuids) {

        if (vocUuids == null || vocUuids.isEmpty()){
            return null;
        }
        List<TermVocabularyDto> list = new ArrayList<>();

        String queryString = TermCollectionDto.getTermCollectionDtoSelect()
                + "where a.uuid in :uuidList ";
//                + "order by a.titleCache";
        Query query =  getSession().createQuery(queryString);
        query.setParameterList("uuidList", vocUuids);

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

        list = TermVocabularyDto.termVocabularyDtoListFrom(result);
        return list;

    }
}