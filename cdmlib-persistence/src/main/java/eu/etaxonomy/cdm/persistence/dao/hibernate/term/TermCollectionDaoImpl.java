/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.term;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.metadata.TermSearchField;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermGraphBase;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.term.ITermCollectionDao;
import eu.etaxonomy.cdm.persistence.dto.TermCollectionDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;
import eu.etaxonomy.cdm.persistence.dto.TermVocabularyDto;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.mueller
 * @date 12.04.2023
 */
@Repository
public class TermCollectionDaoImpl
        extends IdentifiableDaoBase<TermCollection> implements ITermCollectionDao {

    public TermCollectionDaoImpl() {
        super(TermCollection.class);
    }

    @Override
    public <TERM extends DefinedTermBase> List<TERM> listTerms(Class<TERM> type, List<TermGraphBase> graphs,
            Integer limit, String pattern, TermSearchField labelType, Language lang){
        return listTerms(type, graphs, null, limit, pattern, MatchMode.BEGINNING, labelType, lang);
    }

//    TODO add to interface once ready
    public <TERM extends DefinedTermBase> List<TERM> listTerms(Class<TERM> clazz, List<TermGraphBase> graphs,
            Integer pageNumber, Integer limit, String pattern, MatchMode matchMode, TermSearchField labelType,
            Language lang){

        clazz = clazz == null ? (Class)DefinedTermBase.class : clazz;
        matchMode = matchMode == null ? MatchMode.EXACT : matchMode;
        lang = lang == null ? Language.DEFAULT() : lang;
        labelType = labelType == null ? TermSearchField.NoAbbrev : labelType;
        boolean isLabel = labelType == TermSearchField.NoAbbrev;

        String op = matchMode.getMatchOperator();
        String hql = "SELECT DISTINCT term FROM TermCollection tc JOIN tc.termRelations rel JOIN rel.term term LEFT JOIN term.representations rep "
                + " WHERE tc.id IN :collectionIDs ";
        if (StringUtils.isNotBlank(pattern)) {
            hql += " AND (term."+labelType.getKey()+ " "+ op+" :pattern ";
            if (isLabel) {
                hql += " OR rep.label "+op+" :pattern AND rep.language = :lang " ;
            }
            hql += ")";
        }
        hql += " ORDER BY term."+ labelType.getKey();

        //TODO using clazz instead of DefinedTermBase.class for some reason does still throw hql exception
        Query<DefinedTermBase> query = getSession().createQuery(hql, DefinedTermBase.class);

        Set<Integer> collectionIDs = graphs.stream().map(g->g.getId()).collect(Collectors.toSet());
        query.setParameterList("collectionIDs", collectionIDs);

        if (StringUtils.isNotBlank(pattern)) {
            query.setParameter("pattern", matchMode.queryStringFrom(pattern));
            if (isLabel) {
                query.setParameter("lang", lang);
            }
        }

        if (limit != null) {
            query.setMaxResults(limit);
            if (pageNumber != null) {
                query.setFirstResult(pageNumber * limit);
            }
        }

        //TODO pageNumber
        List<TERM> result = new ArrayList<>();
        result.addAll(deduplicateResult((List)query.list()));
        return result;
    }

    //FIXME unify with e.g. TermTreeDao.sameMethod as it is exactly the same code except for type S
    @Override
    public <S extends TermCollection> List<UuidAndTitleCache<S>> getUuidAndTitleCacheByTermType(Class<S> clazz, TermType termType, Integer limit,
            String pattern) {
        Session session = getSession();
        Query<Object[]> query = session.createQuery(
                " SELECT uuid, id, titleCache "
                    + " FROM " + clazz.getSimpleName()
                    + (pattern!=null?" WHERE titleCache LIKE :pattern":" WHERE 1 = 1 ")
                    + (termType!=null?" AND termType = :termType ":""),
                    Object[].class
                );
        if(pattern!=null){
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            pattern = pattern + "%";
            query.setParameter("pattern", pattern);
        }
        if(termType!=null){
            query.setParameter("termType", termType);
        }
        if (limit != null){
           query.setMaxResults(limit);
        }
        return getUuidAndTitleCache(query);
    }

    @Override
    public List<TermCollectionDto> findCollectionDtoByTermTypes(Set<TermType> termTypes, String pattern, boolean includeSubtypes) {
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
        String queryString = TermVocabularyDto.getTermCollectionDtoSelect("TermCollection");

        if (!termTypeWithSubType.isEmpty()){
            queryString += " WHERE a.termType in (:termTypes) ";
            if (pattern != null){
                queryString += " AND a.titleCache LIKE :pattern";
            }
        }else{
            if (pattern != null){
                queryString += " WHERE a.titleCache LIKE :pattern";
            }
        }

        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
        if (!termTypeWithSubType.isEmpty()){
            query.setParameterList("termTypes", termTypeWithSubType);
        }
        if (pattern != null){
            pattern = pattern.replace("*", "%");
            pattern = "%"+pattern+"%";
            query.setParameter("pattern", pattern);
        }

        List<Object[]> result = query.list();
        List<TermCollectionDto> dtos = TermCollectionDto.termCollectionDtoListFrom(result);
        addTerms(dtos);
        return dtos;
    }

    @Override
    public List<TermCollectionDto> findCollectionDtoByTermTypes(Set<TermType> termTypes, boolean includeSubtypes) {
        return findCollectionDtoByTermTypes(termTypes, null, includeSubtypes);
    }

    @Override
    public List<TermCollectionDto> findCollectionDtoByUuids(List<UUID> collUuids) {

        if (collUuids == null || collUuids.isEmpty()){
            return null;
        }
        List<TermCollectionDto> list = new ArrayList<>();

        String queryString = TermCollectionDto.getTermCollectionDtoSelect()
                + " WHERE a.uuid IN :uuidList ";
//                + "order by a.titleCache";
        Query<Object[]> query =  getSession().createQuery(queryString, Object[].class);
        query.setParameterList("uuidList", collUuids);

        List<Object[]> result = query.list();
        list = TermCollectionDto.termCollectionDtoListFrom(result);

        addTerms(list);
        return list;
    }

    /**
     * @param list
     */
    protected void addTerms(List<TermCollectionDto> list) {
        Query<Object[]> query;
        List<Object[]> result;
        String queryStringTerms = TermDto.getTermDtoSelect();
        queryStringTerms += "WHERE v.uuid like :uuid" ;
        List<TermDto> termList = new ArrayList<>();
        //where vocabulary uuid like dto.uuid
        for (TermCollectionDto dto: list) {
            //get terms
            if (dto instanceof TermVocabularyDto) {
                query =  getSession().createQuery(queryStringTerms, Object[].class);
                query.setParameter("uuid", dto.getUuid());

                result = query.list();
                termList = TermDto.termDtoListFrom(result);
                dto.setTerms(termList);
            }
        }
    }


//   not yet clear if we want TermCollectionDaoImpl also as base class for other dao-s
//    protected TermCollectionDaoImpl(Class<TermCollection> type) {
//        super(type);
//    }
}
