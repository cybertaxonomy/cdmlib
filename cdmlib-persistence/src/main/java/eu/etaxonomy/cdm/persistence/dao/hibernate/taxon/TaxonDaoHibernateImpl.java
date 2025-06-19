/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.taxon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.internal.NotNullAuditExpression;
import org.hibernate.envers.query.internal.property.EntityPropertyName;
import org.hibernate.query.Query;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.IdentifierType;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dto.SortableTaxonNodeQueryResult;
import eu.etaxonomy.cdm.persistence.dto.SortableTaxonNodeQueryResultComparator;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.NameSearchOrder;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.TaxonTitleType;

/**
 * @author a.mueller
 * @since 24.11.2008
 */
@Repository
@Qualifier("taxonDaoHibernateImpl")
public class TaxonDaoHibernateImpl
              extends IdentifiableDaoBase<TaxonBase>
              implements ITaxonDao {

//    private AlternativeSpellingSuggestionParser<TaxonBase> alternativeSpellingSuggestionParser;
    private static final Logger logger = LogManager.getLogger();

    public TaxonDaoHibernateImpl() {
        super(TaxonBase.class);
        indexedClasses = new Class[2];
        indexedClasses[0] = Taxon.class;
        indexedClasses[1] = Synonym.class;
        super.defaultField = "name.titleCache_tokenized";
    }

    @Autowired
    private ITaxonNameDao taxonNameDao;

////    spelling support currently disabled in appcontext, see spelling.xml ... "
////    @Autowired(required = false)   //TODO switched of because it caused problems when starting CdmApplicationController
//    public void setAlternativeSpellingSuggestionParser(AlternativeSpellingSuggestionParser<TaxonBase> alternativeSpellingSuggestionParser) {
//        this.alternativeSpellingSuggestionParser = alternativeSpellingSuggestionParser;
//    }

    @Override
    public TaxonBase load(UUID uuid, List<String> propertyPaths){
        return load(uuid, INCLUDE_UNPUBLISHED, propertyPaths);
    }

    @Override
    public TaxonBase load(UUID uuid, boolean includeUnpublished, List<String> propertyPaths){
        TaxonBase<?> result = super.load(uuid, includeUnpublished, propertyPaths);
        return result; //(result == null || (!result.isPublish() && !includeUnpublished))? null : result;
    }

    @Override
    public <S extends TaxonBase> List<S> list(Class<S> type, List<Restriction<?>> restrictions, Integer limit,
            Integer start, List<OrderHint> orderHints, List<String> propertyPaths) {
        return list(type, restrictions, limit, start, orderHints, propertyPaths, INCLUDE_UNPUBLISHED);
    }


    @Override
    public <S extends TaxonBase> List<S> list(Class<S> type, List<Restriction<?>> restrictions, Integer limit, Integer start,
            List<OrderHint> orderHints, List<String> propertyPaths, boolean includePublished) {

        Criteria criteria = createCriteria(type, restrictions, false);

        if(!includePublished){
            criteria.add(Restrictions.eq("publish", true));
        }

        addLimitAndStart(criteria, limit, start);
        addOrder(criteria, orderHints);

        @SuppressWarnings("unchecked")
        List<S> result = criteria.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    @Override
    public long count(Class<? extends TaxonBase> type, List<Restriction<?>> restrictions) {
        return count(type, restrictions, INCLUDE_UNPUBLISHED);
    }

    @Override
    public long count(Class<? extends TaxonBase> type, List<Restriction<?>> restrictions, boolean includePublished) {

        Criteria criteria = createCriteria(type, restrictions, false);
        if(!includePublished){
            criteria.add(Restrictions.eq("publish", true));
        }
        criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));
        return (Long) criteria.uniqueResult();
    }

    @Override
    public List<TaxonBase> getTaxaByName(String queryString, boolean includeUnpublished, Reference sec) {

        return getTaxaByName(queryString, true, includeUnpublished, sec);
    }

    @Override
    public List<TaxonBase> getTaxaByName(String queryString, Boolean accepted, boolean includeUnpublished, Reference sec) {
        checkNotInPriorView("TaxonDaoHibernateImpl.getTaxaByName(String name, Reference sec)");

        Criteria criteria = null;
        Class<? extends TaxonBase<?>> clazz = accepted ? Taxon.class : Synonym.class;
        criteria = getSession().createCriteria(clazz);

        criteria.setFetchMode( "name", FetchMode.JOIN );
        criteria.createAlias("name", "name");

        if (!includeUnpublished){
            criteria.add(Restrictions.eq("publish", Boolean.TRUE ));
        }

        if (sec != null && sec.getId() != 0) {
            criteria.createCriteria("secSource").add(Restrictions.eq("citation", sec ) );
        }

        if (queryString != null) {
            criteria.add(Restrictions.ilike("name.nameCache", queryString));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<TaxonBase> result = criteria.list();
        return result;
    }

    @Override
    public List<TaxonBase> getTaxaByName(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames, boolean doCommonNames,
            boolean includeAuthors,
            String queryString, Classification classification, TaxonNode subtree,
            MatchMode matchMode, Set<NamedArea> namedAreas, boolean includeUnpublished, NameSearchOrder order,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

        boolean doCount = false;

        String searchField = includeAuthors ? "titleCache" : "nameCache";
        Query<TaxonBase> query = prepareTaxaByName(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, includeUnpublished,
                searchField, queryString, classification, subtree, matchMode, namedAreas, order, pageSize, pageNumber, doCount,
                TaxonBase.class);

        if (query != null){
            @SuppressWarnings("rawtypes")
            List<TaxonBase> results = query.list();

            defaultBeanInitializer.initializeAll(results, propertyPaths);

            //Collections.sort(results, comp);
            return results;
        }else{
            return new ArrayList<>();
        }
    }

    //new search for the editor, for performance issues the return values are only uuid and titleCache, to avoid the initialisation of all objects
    @Override
    public List<UuidAndTitleCache<? extends IdentifiableEntity>> getTaxaByNameForEditor(boolean doTaxa, boolean doSynonyms, boolean doNamesWithoutTaxa,
            boolean doMisappliedNames, boolean doCommonNames, boolean includeUnpublished, boolean includeAuthors, String queryString, Classification classification, TaxonNode subtree,
            MatchMode matchMode, Set<NamedArea> namedAreas, NameSearchOrder order) {

        if (order == null){
            order = NameSearchOrder.ALPHA;  //TODO add to signature
        }

        boolean doCount = false;

        @SuppressWarnings("rawtypes")
        List<UuidAndTitleCache<? extends IdentifiableEntity>> resultObjects = new ArrayList<>();
        if (doNamesWithoutTaxa){
        	List<TaxonName> nameResult = taxonNameDao.findByName(
        	        includeAuthors, queryString, matchMode, null, null, null, null);

        	for (TaxonName name: nameResult){
        		if (name.getTaxonBases().size() == 0){
        			resultObjects.add(new UuidAndTitleCache<>(TaxonName.class, name.getUuid(),
        			        name.getId(), name.getTitleCache()));
        		}
        	}
        	if (!doSynonyms && !doTaxa && !doCommonNames){
        		return resultObjects;
        	}
        }
        String searchField = includeAuthors ? "titleCache" : "nameCache";
        Query<Object[]> query = prepareTaxaByNameForEditor(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, includeUnpublished,
                searchField, queryString, classification, subtree, matchMode, namedAreas, doCount, order, Object[].class);

        if (query != null){
            List<Object[]> results = query.list();

            Object[] result;
            for(int i = 0; i<results.size();i++){
                result = results.get(i);

                //differentiate taxa and synonyms
                // new Boolean(result[3].toString()) is due to the fact that result[3] could be a Boolean ora String
                // see FIXME in 'prepareQuery' for more details
                if (doTaxa || doSynonyms || doCommonNames){
                    if (result[3].equals("synonym")) {
                        resultObjects.add( new UuidAndTitleCache<>(Synonym.class, (UUID) result[0], (Integer) result[1], (String)result[2], new Boolean(result[4].toString()), null));
                    }
                    else {
                        resultObjects.add( new UuidAndTitleCache<>(Taxon.class, (UUID) result[0], (Integer) result[1], (String)result[2], new Boolean(result[4].toString()), null));
                    }

                }else if (doSynonyms){
                    resultObjects.add( new UuidAndTitleCache<>(Synonym.class, (UUID) result[0], (Integer) result[1], (String)result[2], new Boolean(result[4].toString()), null));
                }
            }
        }
        return resultObjects;

    }

    @Override
    public List<Taxon> getTaxaByCommonName(String queryString, Classification classification,
               MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize,
               Integer pageNumber, List<String> propertyPaths) {
        boolean doCount = false;
        Query<Taxon> query = prepareTaxaByCommonName(queryString, classification, matchMode, namedAreas, pageSize, pageNumber, doCount, false, Taxon.class);
        if (query != null){
            List<Taxon> results = query.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        }else{
            return new ArrayList<>();
        }

    }

    /**
     * @param clazz
     * @param searchField the field in TaxonName to be searched through usually either <code>nameCache</code> or <code>titleCache</code>
     * @param queryString
     * @param classification TODO
     * @param matchMode
     * @param namedAreas
     * @param pageSize
     * @param pageNumber
     * @param doCount
     * @return
     */
    private <R extends Object> Query<R> prepareTaxaByNameForEditor(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames, boolean doCommonNames,
            boolean includeUnpublished, String searchField, String queryString, Classification classification, TaxonNode subtree,
            MatchMode matchMode, Set<NamedArea> namedAreas, boolean doCount, NameSearchOrder order, Class<R> returnedClass) {
        return prepareByNameQuery(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, includeUnpublished,
                searchField, queryString,
                classification, subtree, matchMode, namedAreas, order, doCount, true, returnedClass);
    }

    /**
     * @param doTaxa
     * @param doSynonyms
     * @param doIncludeMisappliedNames
     * @param doCommonNames
     * @param includeUnpublished
     * @param searchField
     * @param queryString
     * @param classification
     * @param matchMode
     * @param namedAreas
     * @param order
     * @param doCount
     * @param returnIdAndTitle
     *            If set true the seach method will not return synonym and taxon
     *            entities but an array containing the uuid, titleCache, and the
     *            DTYPE in lowercase letters.
     * @return
     */
    private <R extends Object> Query<R> prepareByNameQuery(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames,
                boolean doCommonNames, boolean includeUnpublished, String searchField, String queryString,
                Classification classification, TaxonNode subtree, MatchMode matchMode, Set<NamedArea> namedAreas,
                NameSearchOrder order, boolean doCount, boolean returnIdAndTitle, Class<R> returnedClass){

            boolean doProParteSynonyms = doSynonyms;  //we may distinguish in future
            boolean doConceptRelations = doMisappliedNames || doProParteSynonyms;

            if (order == null){
                order = NameSearchOrder.DEFAULT();
            }
            String hqlQueryString = matchMode.queryStringFrom(queryString);
            String selectWhat;
            if (returnIdAndTitle){
                selectWhat = "t.uuid, t.id, t.titleCache ";
            }else {
                selectWhat = (doCount ? "count(t)": "t");
            }

            //area filter
            //TODO share code with taxon node filter
            String hql = "";
            Set<NamedArea> areasExpanded = new HashSet<>();
            if(namedAreas != null && namedAreas.size() > 0){
                // expand areas and restrict by distribution area
                Query<NamedArea> areaQuery = getSession().createQuery("SELECT childArea "
                        + " FROM NamedArea AS childArea LEFT JOIN childArea.partOf as parentArea "
                        + " WHERE parentArea = :area", NamedArea.class);
                expandNamedAreas(namedAreas, areasExpanded, areaQuery);
            }
            boolean doAreaRestriction = areasExpanded.size() > 0;

            Set<UUID> namedAreasUuids = new HashSet<>();
            for (NamedArea area:areasExpanded){
                namedAreasUuids.add(area.getUuid());
            }

            Subselects subSelects = createByNameHQLString(doConceptRelations,
                    includeUnpublished, classification, subtree, areasExpanded, matchMode, searchField);
            String taxonSubselect = subSelects.taxonSubselect;
            String synonymSubselect = subSelects.synonymSubselect;
            String conceptSelect = subSelects.conceptSelect;
            String commonNameSubSelect = subSelects.commonNameSubselect;

            if (logger.isDebugEnabled()) {
                logger.debug("taxonSubselect: " + (taxonSubselect != null ? taxonSubselect: "NULL"));
                logger.debug("synonymSubselect: " + (synonymSubselect != null ? synonymSubselect: "NULL"));
            }

            List<Integer> taxonIDs = new ArrayList<>();
            List<Integer> synonymIDs = new ArrayList<>();

            if(doTaxa){
                // find Taxa
                Query<Integer> subTaxon = getSearchQueryString(hqlQueryString, taxonSubselect, true);

                addRestrictions(doAreaRestriction, classification, subtree, includeUnpublished,
                        namedAreasUuids, subTaxon);
                taxonIDs = subTaxon.list();
            }

            if(doSynonyms){
                // find synonyms
                Query<Integer> subSynonym = getSearchQueryString(hqlQueryString, synonymSubselect, true);
                addRestrictions(doAreaRestriction, classification, subtree, includeUnpublished, namedAreasUuids,subSynonym);
                synonymIDs = subSynonym.list();
            }
            if (doConceptRelations ){
                Query<Integer> subMisappliedNames = getSearchQueryString(hqlQueryString, conceptSelect, true);
                Set<TaxonRelationshipType> relTypeSet = new HashSet<>();
                if (doMisappliedNames){
                    relTypeSet.addAll(TaxonRelationshipType.allMisappliedNameTypes());
                }
                if (doProParteSynonyms){
                    relTypeSet.addAll(TaxonRelationshipType.allSynonymTypes());
                }
                subMisappliedNames.setParameterList("rTypeSet", relTypeSet);
                addRestrictions(doAreaRestriction, classification, subtree, includeUnpublished, namedAreasUuids, subMisappliedNames);
                taxonIDs.addAll(subMisappliedNames.list());
            }

            if(doCommonNames){
                // find Taxa
                Query<Integer> subCommonNames = getSearchQueryString(hqlQueryString, commonNameSubSelect, false);
                addRestrictions(doAreaRestriction, classification, subtree, includeUnpublished, namedAreasUuids, subCommonNames);
                taxonIDs.addAll(subCommonNames.list());
            }


            if(synonymIDs.size()>0 && taxonIDs.size()>0){
                hql = "SELECT " + selectWhat;
                // in doNotReturnFullEntities mode it is necessary to also return the type of the matching entities:
                // also return the computed isOrphaned flag
                if (returnIdAndTitle &&  !doCount ){
                    hql += ", CASE WHEN t.id in (:taxa) THEN 'taxon' ELSE 'synonym' END, " +
                            " CASE WHEN t.id in (:taxa) "
                                    + " AND t.taxonNodes IS EMPTY "
                                    + " AND t.relationsFromThisTaxon IS EMPTY "
                                    + " AND t.relationsToThisTaxon IS EMPTY "
                                 + " THEN true ELSE false END ";
                }
                hql +=  " FROM %s t " +
                        " WHERE (t.id in (:taxa) OR t.id IN (:synonyms)) ";
            }else if (synonymIDs.size()>0 ){
                hql = "SELECT " + selectWhat;
                // in doNotReturnFullEntities mode it is necessary to also return the type of the matching entities:
                // also return the computed isOrphaned flag
                if (returnIdAndTitle &&  !doCount ){
                    hql += ", 'synonym', 'false' ";

                }
                hql +=  " FROM %s t " +
                        " WHERE t.id in (:synonyms) ";

            } else if (taxonIDs.size()>0 ){
                hql = "SELECT " + selectWhat;
                // in doNotReturnFullEntities mode it is necessary to also return the type of the matching entities:
                // also return the computed isOrphaned flag
                if (returnIdAndTitle &&  !doCount ){
                    hql += ", 'taxon', " +
                            " CASE WHEN t.taxonNodes is empty "
                            + "  AND t.relationsFromThisTaxon is empty "
                            + "  AND t.relationsToThisTaxon is empty "
                            + "THEN true ELSE false END ";
                }
                hql +=  " FROM %s t " +
                        " WHERE t.id in (:taxa) ";
            } else if (StringUtils.isBlank(queryString)){
                hql = "SELECT " + selectWhat + " FROM %s t";
            } else{
                return null;
            }

            String classString;
            if ((doTaxa || doCommonNames || doConceptRelations) && doSynonyms){
                classString = "TaxonBase";
            } else if (doTaxa || doCommonNames){
                classString = "Taxon";
            } else if (doSynonyms && !(doCommonNames|| doTaxa || doConceptRelations)){
                classString = "Synonym";  // as long as doProParteSynonyms = doSynonyms this case should not happen
            } else{//only misappliedNames
                classString = "Taxon";
            }

            hql = String.format(hql, classString);

            if (hql.isEmpty()) {
                return null;
            }
            if(!doCount){
                String orderBy = " ORDER BY ";
                String alphabeticBase = " t.name.genusOrUninomial, case when t.name.specificEpithet like '\"%\"' then 1 else 0 end, t.name.specificEpithet, t.name.rank desc, t.name.nameCache, t.name.authorshipCache, t.name.uuid, t.uuid ";  //the later parameters are for having deterministic behavior only

                if (order == NameSearchOrder.LENGTH_ALPHA_NAME){
                    orderBy += " length(t.name.nameCache), " + alphabeticBase;
                }else if (order == NameSearchOrder.LENGTH_ALPHA_TITLE){
                    orderBy += " length(t.name.titleCache), " + alphabeticBase;
                }else {
                    orderBy += alphabeticBase;
                }

                hql += orderBy;
            }

            if(logger.isDebugEnabled()){ logger.debug("hql: " + hql);}
            Query<R> query = getSession().createQuery(hql, returnedClass);

            // find taxa and synonyms
            if (taxonIDs.size()>0){
                query.setParameterList("taxa", taxonIDs);
            }
            if (synonymIDs.size()>0){
                query.setParameterList("synonyms",synonymIDs);
            }
            if (taxonIDs.size()== 0 && synonymIDs.size() == 0){
                return null;
            }

            return query;
    }

    protected Query<Integer> getSearchQueryString(String hqlQueryString, String subselect, boolean includeProtectedTitle) {
        Query<Integer> result = getSession().createQuery(subselect, Integer.class);
        result.setParameter("queryString", hqlQueryString);
        if (includeProtectedTitle){
            result.setParameter("protectedTitleQueryString", hqlQueryString + "%");
        }
        return result;
    }

    protected void addRestrictions(boolean doAreaRestriction, Classification classification, TaxonNode subtree, boolean includeUnpublished,
            Set<UUID> namedAreasUuids, Query<Integer> query) {
        if(doAreaRestriction){
            query.setParameterList("namedAreasUuids", namedAreasUuids);
        }
        if(classification != null){
            query.setParameter("classification", classification);
        }
        if(subtree != null){
            query.setParameter("treeIndexLike", subtree.treeIndex() + "%");
        }
        if(!includeUnpublished){
            query.setParameter("publish", true);
        }
    }


    /**
     * @param searchField the field in TaxonName to be searched through usually either <code>nameCache</code> or <code>titleCache</code>
     * @param queryString
     * @param classification TODO
     * @param matchMode
     * @param namedAreas
     * @param pageSize
     * @param pageNumber
     * @param doCount
     * @param clazz
     * @return
     *
     * FIXME implement classification restriction & implement test: see {@link TaxonDaoHibernateImplTest#testCountTaxaByName()}
     */
    private <R extends Object> Query<R> prepareTaxaByName(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames,
            boolean doCommonNames, boolean includeUnpublished, String searchField, String queryString,
            Classification classification, TaxonNode subtree, MatchMode matchMode, Set<NamedArea> namedAreas, NameSearchOrder order,
            Integer pageSize, Integer pageNumber, boolean doCount, Class<R> returnClass) {

        Query<R> query = prepareByNameQuery(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, includeUnpublished,
                searchField, queryString, classification, subtree, matchMode, namedAreas, order, doCount, false, returnClass);

        if(!doCount && query != null) {
            this.addPageSizeAndNumber(query, pageSize, pageNumber);
        }

        return query;
    }

    private <R extends Object> Query<R> prepareTaxaByCommonName(String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize, Integer pageNumber,
            boolean doCount, boolean returnIdAndTitle, Class<R> returnClass){

        String what = "SELECT DISTINCT";
        if (returnIdAndTitle){
        	what += " t.uuid, t.id, t.titleCache, \'taxon\', CASE WHEN t.taxonNodes IS EMPTY AND t.relationsFromThisTaxon IS EMPTY AND t.relationsToThisTaxon IS EMPTY THEN true ELSE false END ";
        }else {
        	what += (doCount ? " count(t)": " t");
        }
        String hql= what + " from Taxon t " +
            "join t.descriptions d "+
            "join d.descriptionElements e " +
//            "join e.feature f " +
            "where e.class = 'CommonTaxonName' and e.name "+matchMode.getMatchOperator()+" :queryString";//and ls.text like 'common%'";

        Query<R> query = getSession().createQuery(hql, returnClass);

        query.setParameter("queryString", matchMode.queryStringFrom(queryString));
        if(!doCount) {
            this.addPageSizeAndNumber(query, pageSize, pageNumber);
        }
        return query;
    }

    @Override
    public long countTaxaByName(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames, boolean doCommonNames,
            boolean doIncludeAuthors, String queryString, Classification classification, TaxonNode subtree,
        MatchMode matchMode, Set<NamedArea> namedAreas, boolean includeUnpublished) {

        boolean doCount = true;
        String searchField = doIncludeAuthors ? "titleCache": "nameCache";

        Query<Long> query = prepareTaxaByName(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, includeUnpublished,
                searchField, queryString, classification, subtree, matchMode, namedAreas, null, null, null, doCount, Long.class);
        if (query != null) {
            return query.uniqueResult();
        }else{
            return 0;
        }
    }

    private void expandNamedAreas(Collection<NamedArea> namedAreas, Set<NamedArea> areasExpanded, Query<NamedArea> areaQuery) {
        for(NamedArea a : namedAreas){
            areasExpanded.add(a);
            areaQuery.setParameter("area", a);
            List<NamedArea> childAreas = areaQuery.list();
            if(childAreas.size() > 0){
                areasExpanded.addAll(childAreas);
                expandNamedAreas(childAreas, areasExpanded, areaQuery);
            }
        }
    }

    @Override
    public UUID delete(TaxonBase taxonBase) throws DataAccessException{
        if (taxonBase == null){
            logger.warn("TaxonBase was 'null'");
            return null;
        }

        // Merge the object in if it is detached
        //
        // I think this is preferable to catching lazy initialization errors
        // as that solution only swallows and hides the exception, but doesn't
        // actually solve it.
        taxonBase = (TaxonBase)getSession().merge(taxonBase);

        taxonBase.removeSources();

        if (taxonBase instanceof Taxon){ // is Taxon
            Taxon taxon = ((Taxon)taxonBase);
            Set<Synonym> syns = new HashSet<>(taxon.getSynonyms());
            for (Synonym syn: syns){
                taxon.removeSynonym(syn);
            }
        }

        return super.delete(taxonBase);
    }

    @Override
    public List<TaxonBase> findByNameTitleCache(boolean doTaxa, boolean doSynonyms, boolean includeUnpublished, String queryString, Classification classification, TaxonNode subtree, MatchMode matchMode, Set<NamedArea> namedAreas, NameSearchOrder order, Integer pageNumber, Integer pageSize, List<String> propertyPaths) {

        boolean doCount = false;
        Query<TaxonBase> query = prepareTaxaByName(doTaxa, doSynonyms, false, false, includeUnpublished, "titleCache", queryString, classification, subtree, matchMode, namedAreas, order, pageSize, pageNumber, doCount, TaxonBase.class);
        if (query != null){
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<TaxonBase> results = query.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        }
        return new ArrayList<>();

    }

    @Override
    public TaxonBase findByUuid(UUID uuid, List<Criterion> criteria, List<String> propertyPaths) {

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

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<? extends TaxonBase> results = crit.list();
        if (results.size() == 1) {
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            TaxonBase<?> taxon = results.iterator().next();
            return taxon;
        } else if (results.size() > 1) {
            logger.error("Multiple results for UUID: " + uuid);
        } else if (results.size() == 0) {
            logger.info("No results for UUID: " + uuid);
        }

        return null;
    }

    @Override
    public List<? extends TaxonBase> findByUuids(List<UUID> uuids, List<Criterion> criteria, List<String> propertyPaths) {

        Criteria crit = getSession().createCriteria(type);

        if (uuids != null) {
            crit.add(Restrictions.in("uuid", uuids));
        } else {
            logger.warn("List<UUID> uuids is NULL");
            return null;
        }
        if(criteria != null){
            for (Criterion criterion : criteria) {
                crit.add(criterion);
            }
        }
        crit.addOrder(Order.asc("uuid"));

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<? extends TaxonBase> results = crit.list();

        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public long countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted) {
        checkNotInPriorView("TaxonDaoHibernateImpl.countMatchesByName(String queryString, ITitledDao.MATCH_MODE matchMode, boolean onlyAcccepted)");

        Criteria crit = getCriteria(type);
        crit.add(Restrictions.ilike("titleCache", matchMode.queryStringFrom(queryString)));
        crit.setProjection(Projections.rowCount());
        return (Long)crit.uniqueResult();
    }


    @Override
    public long countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted, List<Criterion> criteria) {
        checkNotInPriorView("TaxonDaoHibernateImpl.countMatchesByName(String queryString, ITitledDao.MATCH_MODE matchMode, boolean onlyAcccepted, List<Criterion> criteria)");

        Criteria crit = getCriteria(type);
        crit.add(Restrictions.ilike("titleCache", matchMode.queryStringFrom(queryString)));
        if(criteria != null){
            for (Criterion criterion : criteria) {
                crit.add(criterion);
            }
        }
        crit.setProjection(Projections.rowCount());
        return (Long)crit.uniqueResult();
    }


    @Override
    public long countSynonyms(boolean onlyAttachedToTaxon) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            String queryStr =
                    " SELECT count(syn) "
                  + " FROM Synonym syn";
            if (onlyAttachedToTaxon){
                queryStr += " WHERE syn.acceptedTaxon IS NOT NULL";
            }
            Query<Long> query = getSession().createQuery(queryStr, Long.class);

            return query.uniqueResult();
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Synonym.class,auditEvent.getRevisionNumber());
            if (onlyAttachedToTaxon){
                query.add(new NotNullAuditExpression(null, new EntityPropertyName("acceptedTaxon")));
            }
            query.addProjection(AuditEntity.id().count());

            return (Long)query.getSingleResult();
        }
    }

    @Override
    public long countSynonyms(Taxon taxon, SynonymType type) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getCriteria(Synonym.class);

            criteria.add(Restrictions.eq("acceptedTaxon", taxon));
            if(type != null) {
                criteria.add(Restrictions.eq("type", type));
            }
            criteria.setProjection(Projections.rowCount());
            return (Long)criteria.uniqueResult();
        } else {
            AuditQuery query = makeAuditQuery(Synonym.class, auditEvent);
            query.add(AuditEntity.relatedId("acceptedTaxon").eq(taxon.getId()));
            query.addProjection(AuditEntity.id().count());

            if(type != null) {
                query.add(AuditEntity.property("type").eq(type));
            }

            return (Long)query.getSingleResult();
        }
    }

    @Override
    public long countSynonyms(Synonym synonym, SynonymType type) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getCriteria(Synonym.class);

            criteria.add(Restrictions.isNotNull("acceptedTaxon"));
            if(type != null) {
                criteria.add(Restrictions.eq("type", type));
            }

            criteria.setProjection(Projections.rowCount());
            return (Long)criteria.uniqueResult();
        } else {
            AuditQuery query = makeAuditQuery(Synonym.class,auditEvent);
            query.add(new NotNullAuditExpression(null, new EntityPropertyName("acceptedTaxon")));
            query.addProjection(AuditEntity.id().count());

            if(type != null) {
                query.add(AuditEntity.property("type").eq(type));
            }

            return (Long)query.getSingleResult();
        }
    }

    @Override
    public long countTaxaByName(Class<? extends TaxonBase> clazz, String genusOrUninomial, String infraGenericEpithet, String specificEpithet,
            String infraSpecificEpithet, String authorshipCache, Rank rank) {
        checkNotInPriorView("TaxonDaoHibernateImpl.countTaxaByName(Boolean accepted, String genusOrUninomial,	String infraGenericEpithet, String specificEpithet,	String infraSpecificEpithet, String authorshipCache, Rank rank)");
        Criteria criteria = null;

        criteria = getCriteria(clazz);

        criteria.setFetchMode( "name", FetchMode.JOIN );
        criteria.createAlias("name", "name");

        if(genusOrUninomial == null) {
            criteria.add(Restrictions.isNull("name.genusOrUninomial"));
        } else if(!genusOrUninomial.equals("*")) {
            criteria.add(Restrictions.eq("name.genusOrUninomial", genusOrUninomial));
        }

        if(infraGenericEpithet == null) {
            criteria.add(Restrictions.isNull("name.infraGenericEpithet"));
        } else if(!infraGenericEpithet.equals("*")) {
            criteria.add(Restrictions.eq("name.infraGenericEpithet", infraGenericEpithet));
        }

        if(specificEpithet == null) {
            criteria.add(Restrictions.isNull("name.specificEpithet"));
        } else if(!specificEpithet.equals("*")) {
            criteria.add(Restrictions.eq("name.specificEpithet", specificEpithet));

        }

        if(infraSpecificEpithet == null) {
            criteria.add(Restrictions.isNull("name.infraSpecificEpithet"));
        } else if(!infraSpecificEpithet.equals("*")) {
            criteria.add(Restrictions.eq("name.infraSpecificEpithet", infraSpecificEpithet));
        }

        if(authorshipCache == null) {
            criteria.add(Restrictions.eq("name.authorshipCache", ""));
        } else if(!authorshipCache.equals("*")) {
            criteria.add(Restrictions.eq("name.authorshipCache", authorshipCache));
        }

        if(rank != null) {
            criteria.add(Restrictions.eq("name.rank", rank));
        }

        criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));

        return (Long)criteria.uniqueResult();
    }

    @Override
    public <T extends TaxonBase> List<T> findTaxaByName(Class<T> clazz, String genusOrUninomial, String infraGenericEpithet, String specificEpithet,
            String infraSpecificEpithet, String authorship, Rank rank, Integer pageSize,Integer pageNumber, List<String> propertyPaths) {
        checkNotInPriorView("TaxonDaoHibernateImpl.findTaxaByName(Boolean accepted, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, String authorship, Rank rank, Integer pageSize,Integer pageNumber, List<String> propertyPaths)");
        Criteria criteria = getCriteria(clazz);

        criteria.setFetchMode( "name", FetchMode.JOIN );
        criteria.createAlias("name", "name");

        if(genusOrUninomial == null) {
            criteria.add(Restrictions.isNull("name.genusOrUninomial"));
        } else if(!genusOrUninomial.equals("*")) {
            criteria.add(Restrictions.eq("name.genusOrUninomial", genusOrUninomial));
        }

        if(infraGenericEpithet == null) {
            criteria.add(Restrictions.isNull("name.infraGenericEpithet"));
        } else if(!infraGenericEpithet.equals("*")) {
            criteria.add(Restrictions.eq("name.infraGenericEpithet", infraGenericEpithet));
        }

        if(specificEpithet == null) {
            criteria.add(Restrictions.isNull("name.specificEpithet"));
        } else if(!specificEpithet.equals("*")) {
            criteria.add(Restrictions.eq("name.specificEpithet", specificEpithet));
        }

        if(infraSpecificEpithet == null) {
            criteria.add(Restrictions.isNull("name.infraSpecificEpithet"));
        } else if(!infraSpecificEpithet.equals("*")) {
            criteria.add(Restrictions.eq("name.infraSpecificEpithet", infraSpecificEpithet));
        }

        if(authorship == null) {
            criteria.add(Restrictions.eq("name.authorshipCache", ""));
        } else if(!authorship.equals("*")) {
            criteria.add(Restrictions.eq("name.authorshipCache", authorship));
        }

        if(rank != null) {
            criteria.add(Restrictions.eq("name.rank", rank));
        }

        if(pageSize != null) {
            criteria.setMaxResults(pageSize);
            if(pageNumber != null) {
                criteria.setFirstResult(pageNumber * pageSize);
            } else {
                criteria.setFirstResult(0);
            }
        }

        @SuppressWarnings({ "unchecked"})
        List<T> result = criteria.list();

        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;
    }

    @Override
    public long countTaxonRelationships(Taxon taxon, TaxonRelationshipType type,
            boolean includeUnpublished, Direction direction) {
        Set<TaxonRelationshipType> types = null;
        if (type != null){
            types = new HashSet<>();
            types.add(type);
        }
        return countTaxonRelationships(taxon, types, includeUnpublished, direction);
    }

    @Override
    public long countTaxonRelationships(Taxon taxon, Set<TaxonRelationshipType> types,
            boolean includeUnpublished, Direction direction) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {

            String queryString = prepareTaxonRelationshipQuery(types, includeUnpublished, direction, true);
            Query<Long> query = getSession().createQuery(queryString, Long.class);
            query.setParameter("relatedTaxon", taxon);
            if(types != null) {
                query.setParameterList("types",types);
            }
            if(! includeUnpublished) {
                query.setParameter("publish",Boolean.TRUE);
            }
            return query.uniqueResult();
        } else {
          //TODO unpublished

            AuditQuery query = makeAuditQuery(TaxonRelationship.class, auditEvent);
            query.add(AuditEntity.relatedId(direction.toString()).eq(taxon.getId()));
            query.addProjection(AuditEntity.id().count());

            if(types != null) {
                //TODO adapt to new Set semantic, was single type before
//                query.add(AuditEntity.relatedId("type").eq(type.getId()));
            }

            return (Long)query.getSingleResult();
        }
    }


    /**
     * @param type
     * @param includeUnpublished
     * @param direction
     * @param b
     * @return
     */
    private String prepareTaxonRelationshipQuery(Set<TaxonRelationshipType> types, boolean includeUnpublished,
            Direction direction, boolean isCount) {
        String selectStr = isCount? " count(rel) as n ":" rel ";
        String result = "SELECT " + selectStr + " FROM TaxonRelationship rel ";
        if(direction != null){
            result += " WHERE rel."+direction+" = :relatedTaxon";
        } else {
            result += " WHERE (rel.relatedFrom = :relatedTaxon OR rel.relatedTo = :relatedTaxon )";
        }
        if (types != null){
            result += " AND rel.type IN (:types) ";
        }
        if(! includeUnpublished) {
            result += " AND rel."+direction.invers()+".publish = :publish";
        }
        return result;
    }

    @Override
    public List<TaxonRelationship> getTaxonRelationships(Taxon taxon, TaxonRelationshipType type,
            boolean includeUnpublished, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths, Direction direction) {
        Set<TaxonRelationshipType> types = null;
        if (type != null){
            types = new HashSet<>();
            types.add(type);
        }
        return getTaxonRelationships(taxon, types, includeUnpublished, pageSize, pageNumber, orderHints, propertyPaths, direction);
    }

    @Override
    public List<TaxonRelationship> getTaxonRelationships(Taxon taxon, Set<TaxonRelationshipType> types,
            boolean includeUnpublished, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths, Direction direction) {

        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {

            String queryString = prepareTaxonRelationshipQuery(types, includeUnpublished, direction, false);

            queryString += orderByClause("rel", orderHints);

            Query<TaxonRelationship> query = getSession().createQuery(queryString, TaxonRelationship.class);
            query.setParameter("relatedTaxon", taxon);
            if(types != null) {
                query.setParameterList("types",types);
            }
            if(! includeUnpublished) {
                query.setParameter("publish",Boolean.TRUE);
            }
            addPageSizeAndNumber(query, pageSize, pageNumber);

            List<TaxonRelationship> result = query.list();
            defaultBeanInitializer.initializeAll(result, propertyPaths);

            return result;
        } else {
            //TODO unpublished
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonRelationship.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId("relatedTo").eq(taxon.getId()));

            if(types != null) {
                //FIXME adapt to Set (was single type before)
//                query.add(AuditEntity.relatedId("type").eq(types.getId()));
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
            List<TaxonRelationship> result = query.getResultList();
            defaultBeanInitializer.initializeAll(result, propertyPaths);

            // Ugly, but for now, there is no way to sort on a related entity property in Envers,
            // and we can't live without this functionality in CATE as it screws up the whole
            // taxon tree thing
            if(orderHints != null && !orderHints.isEmpty()) {
                SortedSet<TaxonRelationship> sortedList = new TreeSet<>(new TaxonRelationshipFromTaxonComparator());
                sortedList.addAll(result);
                return new ArrayList<>(sortedList);
            }

            return result;
        }
    }

    class TaxonRelationshipFromTaxonComparator implements Comparator<TaxonRelationship> {

        @Override
        public int compare(TaxonRelationship o1, TaxonRelationship o2) {
            if (o1.equals(o2)){
                return 0;
            }
            int result = o1.getFromTaxon().getTitleCache().compareTo(o2.getFromTaxon().getTitleCache());
            if (result == 0 ){
                result = o1.getUuid().compareTo(o2.getUuid());
            }
            return result;
        }

    }

    @Override
    public List<Synonym> getSynonyms(Taxon taxon, SynonymType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(Synonym.class);

            criteria.add(Restrictions.eq("acceptedTaxon", taxon));
            if(type != null) {
                criteria.add(Restrictions.eq("type", type));
            }

            addOrder(criteria,orderHints);

            if(pageSize != null) {
                criteria.setMaxResults(pageSize);
                if(pageNumber != null) {
                    criteria.setFirstResult(pageNumber * pageSize);
                } else {
                    criteria.setFirstResult(0);
                }
            }

            @SuppressWarnings("unchecked")
            List<Synonym> result = criteria.list();
            defaultBeanInitializer.initializeAll(result, propertyPaths);

            return result;
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Synonym.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId("acceptedTaxon").eq(taxon.getId()));

            if(type != null) {
                query.add(AuditEntity.property("type").eq(type));
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
            List<Synonym> result = query.getResultList();
            defaultBeanInitializer.initializeAll(result, propertyPaths);

            return result;
        }
    }

    @Override
    public void rebuildIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());

        for(TaxonBase<?> taxonBase : list(null,null)) { // re-index all taxon base
            Hibernate.initialize(taxonBase.getName());
            fullTextSession.index(taxonBase);
        }
        fullTextSession.flushToIndexes();
    }

    @Override
    public String suggestQuery(String queryString) {
        throw new RuntimeException("Query suggestion currently not implemented in TaxonDaoHibernateImpl");
//        checkNotInPriorView("TaxonDaoHibernateImpl.suggestQuery(String queryString)");
//        String alternativeQueryString = null;
//        if (alternativeSpellingSuggestionParser != null) {
//            try {
//
//                alternativeSpellingSuggestionParser.parse(queryString);
//                org.apache.lucene.search.Query alternativeQuery = alternativeSpellingSuggestionParser.suggest(queryString);
//                if (alternativeQuery != null) {
//                    alternativeQueryString = alternativeQuery
//                            .toString("name.titleCache");
//                }
//
//            } catch (ParseException e) {
//                throw new QueryParseException(e, queryString);
//            }
//        }
//        return alternativeQueryString;
    }

    @Override
    public Taxon acceptedTaxonFor(Synonym synonym, Classification classificationFilter, List<String> propertyPaths){

        String hql = prepareListAcceptedTaxaFor(classificationFilter, false);

        Query<Taxon> query = getSession().createQuery(hql, Taxon.class);
        query.setParameter("synonym", synonym);
        if(classificationFilter != null){
            query.setParameter("classificationFilter", classificationFilter);
        }

        List<Taxon> result = query.list();
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result.isEmpty()? null: result.get(0);
    }

    @Override
    public long countAcceptedTaxonFor(Synonym synonym, Classification classificationFilter){

        String hql = prepareListAcceptedTaxaFor(classificationFilter, true);

        Query<Long> query = getSession().createQuery(hql, Long.class);
        query.setParameter("synonym", synonym);
        if(classificationFilter != null){
            query.setParameter("classificationFilter", classificationFilter);
        }

        Long count = query.uniqueResult();
        return count;
    }

    private String prepareListAcceptedTaxaFor(Classification classificationFilter, boolean doCount) {

        String hql;
        String hqlSelect = "SELECT " + (doCount? "COUNT(taxon)" : "taxon") +
                 " FROM Synonym as syn "
                 + "   JOIN syn.acceptedTaxon as taxon ";
        String hqlWhere = " WHERE syn = :synonym";

        if(classificationFilter != null){
            hqlSelect += " JOIN taxon.taxonNodes AS taxonNode";
            hqlWhere  += " AND taxonNode.classification = :classificationFilter";
        }
        hql = hqlSelect + hqlWhere;
        return hql;
    }

    @Override
    public TaxonBase find(LSID lsid) {
        TaxonBase<?> taxonBase = super.find(lsid);
        if(taxonBase != null) {
            List<String> propertyPaths = new ArrayList<>();
            propertyPaths.add("createdBy");
            propertyPaths.add("updatedBy");
            propertyPaths.add("name");
            propertyPaths.add("secSource.citation");
            propertyPaths.add("relationsToThisTaxon");
            propertyPaths.add("relationsToThisTaxon.fromTaxon");
            propertyPaths.add("relationsToThisTaxon.toTaxon");
            propertyPaths.add("relationsFromThisTaxon");
            propertyPaths.add("relationsFromThisTaxon.toTaxon");
            propertyPaths.add("relationsToThisTaxon.type");
            propertyPaths.add("synonyms");
            propertyPaths.add("synonyms.type");
            propertyPaths.add("descriptions");

            defaultBeanInitializer.initialize(taxonBase, propertyPaths);
        }
        return taxonBase;
    }

    @Override
    public List<String> taxaByNameNotInDB(List<String> taxonNames){
        //get all taxa, already in db
        Query<TaxonName> query = getSession().createQuery(
                 " FROM TaxonName t "
                +" WHERE t.nameCache IN (:taxonList)",
                TaxonName.class);
        query.setParameterList("taxonList", taxonNames);
        List<TaxonName> taxaInDB = query.list();
        //compare the original list with the result of the query
        for (TaxonName taxonName: taxaInDB){
            String nameCache = taxonName.getNameCache();
            if (taxonNames.contains(nameCache)){
                taxonNames.remove(nameCache);
            }
        }

        return taxonNames;
    }

    @Override
    public Map<String, Map<UUID,Set<TaxonName>>> findIdenticalNames(List<UUID> sourceRefUuids, List<String> propertyPaths){
        Set<String> nameCacheCandidates = new HashSet<>();
        try {
            for (int i = 0; i<sourceRefUuids.size()-1;i++){
                UUID sourceUuid1 = sourceRefUuids.get(i);
                for (int j = i+1; j<sourceRefUuids.size();j++){
                    UUID sourceUuid2 = sourceRefUuids.get(j);
                    if (sourceUuid1.equals(sourceUuid2)){
                        continue;  //just in case we have duplicates in the list
                    }

                    String  hql = " SELECT DISTINCT n1.nameCache "
                            + " FROM TaxonBase t1 JOIN t1.name n1 JOIN t1.sources s1 JOIN s1.citation ref1 "
                            +         " , TaxonBase t2 JOIN t2.name n2 JOIN t2.sources s2 JOIN s2.citation ref2 "
                            + " WHERE  ref1.uuid = (:sourceUuid1) "
                            + "       AND n1.id <> n2.id "
                            + "       AND ref2.uuid IN (:sourceUuid2)"
                            + "       AND ref1.uuid <> ref2.uuid "
                            + "       AND n1.nameCache = n2.nameCache "
                            + "       AND t1.publish = 1 AND t2.publish = 1 "
                            + " ORDER BY n1.nameCache ";
                    Query<String> query = getSession().createQuery(hql, String.class);
                    query.setParameter("sourceUuid1", sourceUuid1);
                    query.setParameter("sourceUuid2", sourceUuid2);

                    List<String> queryNameCacheCandidates = query.list();
                    nameCacheCandidates.addAll(queryNameCacheCandidates);
                }
            }

            Map<UUID, List<TaxonName>> duplicates = new HashMap<>();
            for (UUID sourceUuid : sourceRefUuids){
                Query<TaxonName> query=getSession().createQuery("SELECT n "
                        + " FROM TaxonBase t JOIN t.name n JOIN t.sources s JOIN s.citation ref "
                        + " WHERE ref.uuid = :sourceUuid AND n.nameCache IN (:nameCacheCandidates) AND t.publish = 1 "
                        + " ORDER BY n.nameCache",
                        TaxonName.class);
                query.setParameter("sourceUuid", sourceUuid);
                query.setParameterList("nameCacheCandidates", nameCacheCandidates);
                List<TaxonName> sourceDuplicates = query.list();
                defaultBeanInitializer.initializeAll(sourceDuplicates, propertyPaths);

                duplicates.put(sourceUuid, sourceDuplicates);
            }

            List<String> nameCacheCandidateList = new ArrayList<>(nameCacheCandidates);
            Map<String, Map<UUID,Set<TaxonName>>> result = new HashMap<>();
            for (String nameCache: nameCacheCandidateList) {
                Map<UUID,Set<TaxonName>> uuidNameMap = new HashMap<>();
                result.put(nameCache, uuidNameMap);
                for(UUID sourceUuid: duplicates.keySet()){
                    Set<TaxonName> names = duplicates.get(sourceUuid).stream()
                            .filter(name->name.getNameCache().equals(nameCache))
                            .collect(Collectors.toSet());
                    uuidNameMap.put(sourceUuid, names);
                }
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public long countTaxaByCommonName(String searchString,
            Classification classification, MatchMode matchMode,
            Set<NamedArea> namedAreas) {
        boolean doCount = true;
        Query<Long> query = prepareTaxaByCommonName(searchString, classification, matchMode, namedAreas, null, null, doCount, false, Long.class);
        if (query != null && !query.list().isEmpty()) {
            Long o = query.uniqueResult();
            if(o != null) {
                return o;
            }
        }
        return 0;
    }

    private Subselects createByNameHQLString(boolean doConceptRelations,
                boolean includeUnpublished, Classification classification, TaxonNode subtree,
                Set<NamedArea> areasExpanded, MatchMode matchMode, String searchField){


        boolean doAreaRestriction = areasExpanded.size() > 0;
        boolean hasTaxonNodeFilter = classification != null || subtree != null;

        String doAreaRestrictionSubSelect =
                     " SELECT %s.id "
                   + " FROM Distribution e "
                   + "    JOIN e.inDescription d "
                   + "    JOIN d.taxon t " +
                (hasTaxonNodeFilter ? " JOIN t.taxonNodes AS tn " : " ");

        String doAreaRestrictionConceptRelationSubSelect =
                   "SELECT %s.id "
                   + " FROM Distribution e "
                   + "   JOIN e.inDescription d"
                   + "   JOIN d.taxon t";

        String doTaxonSubSelect =
                     " SELECT %s.id "
                   + " FROM Taxon t " + (hasTaxonNodeFilter ? " "
                           + " JOIN t.taxonNodes AS tn " : " ");

        String doTaxonMisappliedNameSubSelect =
                     " SELECT %s.id "
                   + " FROM Taxon t ";

        String doTaxonNameJoin = " JOIN t.name n ";

        String doSynonymSubSelect =
                  " FROM Synonym s "
                + " JOIN s.name sn "
                + " LEFT JOIN s.acceptedTaxon ";

        String doSynonymNameJoin =
                   " JOIN t.synonyms s "
                 + " JOIN s.name sn";

        String doConceptRelationJoin =
                   " LEFT JOIN t.relationsFromThisTaxon AS rft " +
                   " LEFT JOIN rft.relatedTo AS rt " +
                      (hasTaxonNodeFilter ? " LEFT JOIN rt.taxonNodes AS tn2 " : " ") +
                   " LEFT JOIN rt.name AS n2" +
                   " LEFT JOIN rft.type as rtype";

        String doCommonNamesJoin =
                   " JOIN t.descriptions AS description "+
                   " LEFT JOIN description.descriptionElements AS com " +
                   " LEFT JOIN com.feature f ";


        String doTreeWhere = classification == null ? "" : " AND tn.classification = :classification";
        String doTreeForConceptRelationsWhere = classification == null ? "": " AND tn2.classification = :classification";

        String doSubtreeWhere = subtree == null? "":" AND tn.treeIndex like :treeIndexLike";
        String doSubtreeForConceptRelationsWhere = subtree == null? "":" AND tn2.treeIndex like :treeIndexLike";

        String doAreaRestrictionWhere =  " e.area.uuid in (:namedAreasUuids)";
        String doCommonNamesRestrictionWhere = " (com.class = 'CommonTaxonName' and com.name "+matchMode.getMatchOperator()+" :queryString )";

        String doSearchFieldWhere = " (%s." + searchField + " " + matchMode.getMatchOperator() + " :queryString OR "
                + " %s.protectedTitleCache = TRUE AND %s.titleCache LIKE :protectedTitleQueryString) ";

        String doRelationshipTypeComparison = " rtype in (:rTypeSet) ";

        String taxonSubselect = null;
        String synonymSubselect = null;
        String conceptSelect = null;
        String commonNameSubselect = null;

        if(hasTaxonNodeFilter){
            if (!doConceptRelations){
                if(doAreaRestriction){
                    taxonSubselect = String.format(doAreaRestrictionSubSelect, "t") + doTaxonNameJoin +
                            " WHERE (1=1) AND " + doAreaRestrictionWhere +
                                doTreeWhere + doSubtreeWhere +
                            "  AND " + String.format(doSearchFieldWhere, "n", "n", "n");
                    synonymSubselect = String.format(doAreaRestrictionSubSelect, "s") + doSynonymNameJoin +
                            " WHERE (1=1) AND " + doAreaRestrictionWhere +
                                doTreeWhere + doSubtreeWhere +
                            "  AND " + String.format(doSearchFieldWhere, "sn", "sn", "sn");
                    commonNameSubselect =  String.format(doAreaRestrictionSubSelect, "t") + doCommonNamesJoin +
                            " WHERE (1=1) AND " +  doAreaRestrictionWhere +
                                 doTreeWhere + doSubtreeWhere +
                            "  AND " + String.format(doSearchFieldWhere, "n", "n", "n") +
                            "  AND " + doCommonNamesRestrictionWhere;
                } else {//no area restriction
                    taxonSubselect = String.format(doTaxonSubSelect, "t" )+ doTaxonNameJoin +
                            " WHERE (1=1) " + doTreeWhere + doSubtreeWhere +
                            "  AND " + String.format(doSearchFieldWhere, "n", "n", "n");
                    synonymSubselect = String.format(doTaxonSubSelect, "s" ).replace("FROM Taxon ", doSynonymSubSelect) +  //we could also use default synonym handling here as a taxon node filter requires an accepted taxon (#9047)
                            " WHERE (1=1) " + doTreeWhere + doSubtreeWhere +
                            "  AND " + String.format(doSearchFieldWhere, "sn", "sn", "sn");
                    commonNameSubselect =String.format(doTaxonSubSelect, "t" )+ doCommonNamesJoin +
                            " WHERE (1=1) " + doTreeWhere + doSubtreeWhere +
                            "  AND " + doCommonNamesRestrictionWhere;
                }
            }else{ //concept relations included
                if(doAreaRestriction){
                    conceptSelect = String.format(doAreaRestrictionConceptRelationSubSelect, "t") + doTaxonNameJoin + doConceptRelationJoin  +
                            " WHERE " + doAreaRestrictionWhere +
                            "  AND " + String.format(doSearchFieldWhere, "n", "n", "n") +
                                 doTreeForConceptRelationsWhere + doSubtreeForConceptRelationsWhere +
                            "  AND " + doRelationshipTypeComparison;
                    taxonSubselect = String.format(doAreaRestrictionSubSelect, "t") + doTaxonNameJoin +
                            " WHERE " + doAreaRestrictionWhere +
                            "  AND " + String.format(doSearchFieldWhere, "n", "n", "n") +
                                doTreeWhere + doSubtreeWhere;
                    synonymSubselect = String.format(doAreaRestrictionSubSelect, "s") + doSynonymNameJoin +
                            " WHERE " + doAreaRestrictionWhere +
                                doTreeWhere + doSubtreeWhere +
                            "  AND " + String.format(doSearchFieldWhere, "sn", "sn", "sn");
                    commonNameSubselect= String.format(doAreaRestrictionSubSelect, "t")+ doCommonNamesJoin +
                            " WHERE " + doAreaRestrictionWhere +
                                doTreeWhere + doSubtreeWhere +
                            "  AND " + doCommonNamesRestrictionWhere;
                } else {//no area restriction
                    conceptSelect = String.format(doTaxonMisappliedNameSubSelect, "t" ) + doTaxonNameJoin + doConceptRelationJoin +
                            " WHERE " + String.format(doSearchFieldWhere, "n", "n", "n") +
                                  doTreeForConceptRelationsWhere + doSubtreeForConceptRelationsWhere +
                            "  AND " + doRelationshipTypeComparison;
                    taxonSubselect = String.format(doTaxonSubSelect, "t" ) + doTaxonNameJoin +
                            " WHERE " +  String.format(doSearchFieldWhere, "n", "n", "n") +
                                 doTreeWhere + doSubtreeWhere;
                    synonymSubselect = String.format(doTaxonSubSelect, "s" ).replace("FROM Taxon ", doSynonymSubSelect) + //we could also use default synonym handling here as a taxon node filter requires an accepted taxon (#9047)
                            " WHERE (1=1) " + doTreeWhere + doSubtreeWhere +
                            "  AND " +  String.format(doSearchFieldWhere, "sn", "sn", "sn");
                    commonNameSubselect= String.format(doTaxonSubSelect, "t")+ doCommonNamesJoin +
                            " WHERE (1=1) " + doTreeWhere + doSubtreeWhere +
                            "  AND " + doCommonNamesRestrictionWhere;
                }
            }
        } else { //classification = null && subtree = null
            if(doAreaRestriction){
                conceptSelect = String.format(doAreaRestrictionConceptRelationSubSelect, "t") + doTaxonNameJoin + doConceptRelationJoin +
                        " WHERE " + doAreaRestrictionWhere +
                        "  AND " + String.format(doSearchFieldWhere, "n", "n", "n")+
                        "  AND " + doRelationshipTypeComparison;
                taxonSubselect = String.format(doAreaRestrictionSubSelect, "t") + doTaxonNameJoin +
                        " WHERE " + doAreaRestrictionWhere +
                        "  AND " + String.format(doSearchFieldWhere, "n", "n", "n");
                synonymSubselect = String.format(doAreaRestrictionSubSelect, "s") + doSynonymNameJoin +
                        " WHERE " + doAreaRestrictionWhere +
                        "  AND " + String.format(doSearchFieldWhere, "sn", "sn", "sn");
                commonNameSubselect = String.format(doAreaRestrictionSubSelect, "t")+ doCommonNamesJoin +
                        " WHERE " + doAreaRestrictionWhere +
                        "  AND " + doCommonNamesRestrictionWhere;
            } else { //no area restriction
                conceptSelect = String.format(doTaxonMisappliedNameSubSelect, "t" ) + doTaxonNameJoin + doConceptRelationJoin +
                        " WHERE " +  String.format(doSearchFieldWhere, "n", "n", "n") +
                        " AND " + doRelationshipTypeComparison;
                taxonSubselect = String.format(doTaxonSubSelect, "t" ) + doTaxonNameJoin +
                        " WHERE " +  String.format(doSearchFieldWhere, "n", "n", "n");
                synonymSubselect = String.format(doTaxonSubSelect, "s" ).replace("FROM Taxon ", doSynonymSubSelect) +
                        " WHERE " +  String.format(doSearchFieldWhere, "sn", "sn", "sn");
                commonNameSubselect = String.format(doTaxonSubSelect, "t" ) +doCommonNamesJoin +
                        " WHERE "+  doCommonNamesRestrictionWhere;
            }
        }

        if (!includeUnpublished){
            taxonSubselect   += " AND t.publish = :publish ";
            synonymSubselect += " AND s.publish = :publish AND t.publish = :publish ";
            commonNameSubselect += " AND t.publish = :publish ";
            conceptSelect += " AND t.publish = :publish AND rt.publish = :publish ";
        }

        Subselects result = new Subselects(taxonSubselect, synonymSubselect, conceptSelect, commonNameSubselect);
        return result;
    }

    private class Subselects{
        String taxonSubselect;
        String synonymSubselect;
        String conceptSelect;
        String commonNameSubselect;
        private Subselects(String taxonSubselect, String synonymSubselect, String conceptSelect,
                String commonNameSubselect) {
            this.taxonSubselect = taxonSubselect;
            this.synonymSubselect = synonymSubselect;
            this.conceptSelect = conceptSelect;
            this.commonNameSubselect = commonNameSubselect;
        }
    }

	@Override
	public List<UuidAndTitleCache<Taxon>> getTaxaByCommonNameForEditor(
			String titleSearchStringSqlized, Classification classification,
			MatchMode matchMode, Set<NamedArea> namedAreas) {

		Query<Object[]> query = prepareTaxaByCommonName(titleSearchStringSqlized, classification, matchMode, namedAreas, null, null, false, true, Object[].class);
        if (query != null){
            List<Object[]> resultArray = query.list();
            List<UuidAndTitleCache<Taxon>> returnResult = new ArrayList<>() ;
            Object[] result;
            for(int i = 0; i<resultArray.size();i++){
            	result = resultArray.get(i);
            	returnResult.add(new UuidAndTitleCache<>(Taxon.class, (UUID) result[0],(Integer)result[1], (String)result[2], new Boolean(result[4].toString()), null));
            }
            return returnResult;
        }else{
            return new ArrayList<>();
        }
	}

	@Override
	public <S extends TaxonBase> long countByIdentifier(Class<S> clazz,
			String identifier, IdentifierType identifierType, TaxonNode subtreeFilter, MatchMode matchmode) {
		if (subtreeFilter == null){
			return countByIdentifier(clazz, identifier, identifierType, matchmode);
		}

		Class<?> clazzParam = (clazz == null) ? type : clazz;
		checkNotInPriorView("TaxonDaoHibernateImpl.countByIdentifier(T clazz, String identifier, DefinedTerm identifierType, TaxonNode subMatchMode matchmode)");

		boolean isTaxon = clazzParam == Taxon.class || clazzParam == TaxonBase.class;
		boolean isSynonym = clazzParam == Synonym.class || clazzParam == TaxonBase.class;

		getSession().update(subtreeFilter);  //to avoid LIE when retrieving treeindex
		String filterStr = "'" + subtreeFilter.treeIndex() + "%%'";
		String accTreeJoin = isTaxon? " LEFT JOIN c.taxonNodes tn  " : "";
		String synTreeJoin = isSynonym ? " LEFT JOIN c.acceptedTaxon as acc LEFT JOIN acc.taxonNodes synTn  " : "";
		String accWhere = isTaxon ?  "tn.treeIndex like " + filterStr : "(1=0)";
		String synWhere = isSynonym  ?  "synTn.treeIndex like " + filterStr : "(1=0)";

		String queryString = "SELECT count(*)  FROM %s as c " +
                " INNER JOIN c.identifiers as ids " +
                accTreeJoin +
                synTreeJoin +
                " WHERE (1=1) " +
                	"  AND ( " + accWhere + " OR " + synWhere + ")";
		queryString = String.format(queryString, clazzParam.getSimpleName());

		if (identifier != null){
			if (matchmode == null || matchmode == MatchMode.EXACT){
				queryString += " AND ids.identifier = '"  + identifier + "'";
			}else {
				queryString += " AND ids.identifier LIKE '" + matchmode.queryStringFrom(identifier)  + "'";
			}
		}
		if (identifierType != null){
        	queryString += " AND ids.type = :type";
        }

		Query<Long> query = getSession().createQuery(queryString, Long.class);
        if (identifierType != null){
        	query.setParameter("type", identifierType);
        }

		return query.uniqueResult();
	}

	@Override
	public <S extends TaxonBase> List<Object[]> findByIdentifier(
			Class<S> clazz, String identifier, IdentifierType identifierType, TaxonNode subtreeFilter,
			MatchMode matchmode, boolean includeEntity,
			Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

		checkNotInPriorView("TaxonDaoHibernateImpl.findByIdentifier(T clazz, String identifier, DefinedTerm identifierType, MatchMode matchmode, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)");
		Class<?> clazzParam = clazz == null ? type : clazz;

		boolean isTaxon = clazzParam == Taxon.class || clazzParam == TaxonBase.class;
		boolean isSynonym = clazzParam == Synonym.class || clazzParam == TaxonBase.class;
		getSession().update(subtreeFilter);  //to avoid LIE when retrieving treeindex
		String filterStr = "'" + subtreeFilter.treeIndex() + "%%'";
		String accTreeJoin = isTaxon? " LEFT JOIN c.taxonNodes tn  " : "";
		String synTreeJoin = isSynonym ? " LEFT JOIN c.acceptedTaxon as acc LEFT JOIN acc.taxonNodes synTn  " : "";
		String accWhere = isTaxon ?  "tn.treeIndex like " + filterStr : "(1=0)";
		String synWhere = isSynonym  ?  "synTn.treeIndex like " + filterStr : "(1=0)";

		String queryString = " SELECT ids.type, ids.identifier, %s " +
				" FROM %s as c " +
                " INNER JOIN c.identifiers as ids " +
                accTreeJoin +
				synTreeJoin +
                " WHERE (1=1) " +
                	" AND ( " + accWhere + " OR " + synWhere + ")";
		queryString = String.format(queryString, (includeEntity ? "c":"c.uuid, c.titleCache") , clazzParam.getSimpleName());

		//Matchmode and identifier
		if (identifier != null){
			if (matchmode == null || matchmode == MatchMode.EXACT){
				queryString += " AND ids.identifier = '"  + identifier + "'";
			}else {
				queryString += " AND ids.identifier LIKE '" + matchmode.queryStringFrom(identifier)  + "'";
			}
		}
        if (identifierType != null){
        	queryString += " AND ids.type = :type";
        }
        //order
        queryString +=" ORDER BY ids.type.uuid, ids.identifier, c.uuid ";

		Query<Object[]> query = getSession().createQuery(queryString, Object[].class);

		//parameters
		if (identifierType != null){
        	query.setParameter("type", identifierType);
        }

        //paging
		addPageSizeAndNumber(query, pageSize, pageNumber);

        List<Object[]> results = query.list();
        //initialize
        if (includeEntity){
        	List<S> entities = new ArrayList<>();
        	for (Object[] result : results){
        		entities.add((S)result[2]);
        	}
        	defaultBeanInitializer.initializeAll(entities, propertyPaths);
        }
        return results;
	}

    /**
     * {@inheritDoc}
     *
     * @see #countByIdentifier(Class, String, DefinedTerm, TaxonNode, MatchMode)
     */
    @Override
    public <S extends TaxonBase> long countByMarker(Class<S> clazz, MarkerType markerType,
            Boolean markerValue, TaxonNode subtreeFilter) {
        if (markerType == null){
            return 0;
        }

        if (subtreeFilter == null){
            return countByMarker(clazz, markerType, markerValue);
        }

        Class<?> clazzParam = clazz == null ? type : clazz;
        checkNotInPriorView("TaxonDaoHibernateImpl.countByMarker(Class<S> clazz, DefinedTerm markerType, boolean markerValue, TaxonNode subtreeFilter)");

        boolean isTaxon = clazzParam == Taxon.class || clazzParam == TaxonBase.class;
        boolean isSynonym = clazzParam == Synonym.class || clazzParam == TaxonBase.class;

        getSession().update(subtreeFilter);  //to avoid LIE when retrieving treeindex
        String filterStr = "'" + subtreeFilter.treeIndex() + "%%'";
        String accTreeJoin = isTaxon? " LEFT JOIN c.taxonNodes tn  " : "";
        String synTreeJoin = isSynonym ? " LEFT JOIN c.acceptedTaxon acc LEFT JOIN acc.taxonNodes synTn  " : "";
        String accWhere = isTaxon ?  "tn.treeIndex like " + filterStr : "(1=0)";
        String synWhere = isSynonym  ?  "synTn.treeIndex like " + filterStr : "(1=0)";

        String queryString = "SELECT count(*)  FROM %s as c " +
                " INNER JOIN c.markers as mks " +
                accTreeJoin +
                synTreeJoin +
                " WHERE (1=1) " +
                    "  AND ( " + accWhere + " OR " + synWhere + ")";
        queryString = String.format(queryString, clazzParam.getSimpleName());

        if (markerValue != null){
            queryString += " AND mks.flag = :flag";
        }
        queryString += " AND mks.markerType = :type";

        Query<Long> query = getSession().createQuery(queryString, Long.class);
        query.setParameter("type", markerType);
        if (markerValue != null){
            query.setParameter("flag", markerValue);
        }

        Long c = query.uniqueResult();
        return c;
    }

    @Override
    public <S extends TaxonBase> List<Object[]> findByMarker(Class<S> clazz, MarkerType markerType,
            Boolean markerValue, TaxonNode subtreeFilter, boolean includeEntity,
            TaxonTitleType titleType, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        checkNotInPriorView("TaxonDaoHibernateImpl.findByMarker(T clazz, String identifier, DefinedTerm identifierType, MatchMode matchmode, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)");
        if (markerType == null){
            return new ArrayList<Object[]>();
        }
        if (titleType == null){
            titleType = TaxonTitleType.DEFAULT();
        }

        Class<?> clazzParam = clazz == null ? type : clazz;

        boolean isTaxon = clazzParam == Taxon.class || clazzParam == TaxonBase.class;
        boolean isSynonym = clazzParam == Synonym.class || clazzParam == TaxonBase.class;
        getSession().update(subtreeFilter);  //to avoid LIE when retrieving treeindex
        String filterStr = "'" + subtreeFilter.treeIndex() + "%%'";
        String accTreeJoin = isTaxon? " LEFT JOIN c.taxonNodes tn  " : "";
        String synTreeJoin = isSynonym ? " LEFT JOIN c.acceptedTaxon as acc LEFT JOIN acc.taxonNodes synTn  " : "";
        String accWhere = isTaxon ?  "tn.treeIndex like " + filterStr : "(1=0)";
        String synWhere = isSynonym  ?  "synTn.treeIndex like " + filterStr : "(1=0)";
        String selectParams = includeEntity ? "c" : titleType.hqlReplaceSelect("c.uuid, c.titleCache", "c.titleCache");
        String titleTypeJoin = includeEntity ? "" : titleType.hqlJoin();

        String queryString = "SELECT mks.markerType, mks.flag, %s " +
                " FROM %s as c " +
                " INNER JOIN c.markers as mks " +
                titleTypeJoin +
                accTreeJoin +
                synTreeJoin +
                " WHERE (1=1) " +
                    " AND ( " + accWhere + " OR " + synWhere + ")";
        queryString = String.format(queryString, selectParams, clazzParam.getSimpleName());

        //type and value
        if (markerValue != null){
            queryString += " AND mks.flag = :flag";
        }
        queryString += " AND mks.markerType = :type";
        //order
        queryString +=" ORDER BY mks.markerType.uuid, mks.flag, c.uuid ";

        Query<Object[]> query = getSession().createQuery(queryString, Object[].class);

        //parameters
        query.setParameter("type", markerType);
        if (markerValue != null){
            query.setParameter("flag", markerValue);
        }

        //paging
        addPageSizeAndNumber(query, pageSize, pageNumber);

        List<Object[]> results = query.list();
        //initialize
        if (includeEntity){
            List<S> entities = new ArrayList<S>();
            for (Object[] result : results){
                entities.add((S)result[2]);
            }
            defaultBeanInitializer.initializeAll(entities, propertyPaths);
        }
        return results;
    }

    @Override
    public long countTaxonRelationships(Set<TaxonRelationshipType> types) {
        Criteria criteria = getSession().createCriteria(TaxonRelationship.class);

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
    public List<TaxonRelationship> getTaxonRelationships(Set<TaxonRelationshipType> types,
            Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) {

        Criteria criteria = getCriteria(TaxonRelationship.class);
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
        List<TaxonRelationship> results = criteria.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);

        return results;
    }

    @Override
    public  List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCache(Integer limit, String pattern){
        Session session = getSession();
        Query<SortableTaxonNodeQueryResult> query = null;
        if (pattern != null){
            query = session.createQuery(
                    "SELECT new " + SortableTaxonNodeQueryResult.class.getName() + "("
                  + " tb.uuid, tb.id, tb.titleCache, tb.name.rank "
                  + ") "
                  + " FROM TaxonBase as tb "
                  + " WHERE tb.titleCache LIKE :pattern",
                  SortableTaxonNodeQueryResult.class);
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            pattern = pattern + "%";
            query.setParameter("pattern", pattern);
        } else {
            query = session.createQuery(
                    " SELECT new " + SortableTaxonNodeQueryResult.class.getName()
                    + "       (tb.uuid, taxonBase.id, tb.titleCache, tb.name.rank) "
                    + " FROM TaxonBase AS tb",
                    SortableTaxonNodeQueryResult.class);
        }
        if (limit != null){
           query.setMaxResults(limit);
        }

        return getUuidAndTitleCache(query);
    }

    @Override
    protected List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCache(Query query){
        List<UuidAndTitleCache<TaxonBase>> list = new ArrayList<>();

        List<SortableTaxonNodeQueryResult> result = query.list();
        if (!result.isEmpty()){
            Collections.sort(result, new SortableTaxonNodeQueryResultComparator());
        }

        for(SortableTaxonNodeQueryResult stnqr : result){
            list.add(new UuidAndTitleCache<TaxonBase>(stnqr.getTaxonNodeUuid(),stnqr.getTaxonNodeId(), stnqr.getTaxonTitleCache()));
        }
        return list;
    }


}
