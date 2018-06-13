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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.internal.NotNullAuditExpression;
import org.hibernate.envers.query.internal.property.EntityPropertyName;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameComparator;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.taxon.UuidAndTitleCacheTaxonComparator;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
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
    private static final Logger logger = Logger.getLogger(TaxonDaoHibernateImpl.class);

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
    public List<TaxonBase> getTaxaByName(String queryString, boolean includeUnpublished, Reference sec) {

        return getTaxaByName(queryString, true, includeUnpublished, sec);
    }

    @Override
    public List<TaxonBase> getTaxaByName(String queryString, Boolean accepted, boolean includeUnpublished, Reference sec) {
        checkNotInPriorView("TaxonDaoHibernateImpl.getTaxaByName(String name, Reference sec)");

        Criteria criteria = null;
        if (accepted == true) {
            criteria = getSession().createCriteria(Taxon.class);
        } else {
            criteria = getSession().createCriteria(Synonym.class);
        }

        criteria.setFetchMode( "name", FetchMode.JOIN );
        criteria.createAlias("name", "name");

        if (!includeUnpublished){
            criteria.add(Restrictions.eq("publish", Boolean.TRUE ));
        }

        if (sec != null && sec.getId() != 0) {
            criteria.add(Restrictions.eq("sec", sec ) );
        }

        if (queryString != null) {
            criteria.add(Restrictions.ilike("name.nameCache", queryString));
        }

        @SuppressWarnings("unchecked")
        List<TaxonBase> result = criteria.list();
        return result;
    }

    public List<TaxonBase> getTaxaByName(boolean doTaxa, boolean doSynonyms, boolean includeUnpublished,
            String queryString, MatchMode matchMode, Integer pageSize, Integer pageNumber) {
        return getTaxaByName(doTaxa, doSynonyms, false, false, false,
                queryString, null, matchMode, null, includeUnpublished, null, pageSize, pageNumber, null);
    }

    @Override
    public List<TaxonBase> getTaxaByName(String queryString, MatchMode matchMode,
            Boolean accepted, boolean includeUnpublished, Integer pageSize, Integer pageNumber) {

        boolean doTaxa = true;
        boolean doSynonyms = true;

        if (accepted == true) {
            doSynonyms = false;
        } else {
           doTaxa = false;
        }
        return getTaxaByName(doTaxa, doSynonyms, includeUnpublished, queryString, matchMode, pageSize, pageNumber);
    }

    @Override
    public List<TaxonBase> getTaxaByName(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames, boolean doCommonNames,
            boolean includeAuthors,
            String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, boolean includeUnpublished, NameSearchOrder order,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

        boolean doCount = false;

        String searchField = includeAuthors ? "titleCache" : "nameCache";
        Query query = prepareTaxaByName(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, includeUnpublished, searchField, queryString, classification, matchMode, namedAreas, order, pageSize, pageNumber, doCount);

        if (query != null){
            @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
    public List<UuidAndTitleCache<IdentifiableEntity>> getTaxaByNameForEditor(boolean doTaxa, boolean doSynonyms, boolean doNamesWithoutTaxa,
            boolean doMisappliedNames, boolean doCommonNames, boolean includeUnpublished, String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, NameSearchOrder order) {

        if (order == null){
            order = NameSearchOrder.ALPHA;  //TODO add to signature
        }

        boolean doCount = false;
        boolean includeAuthors = false;
        List<UuidAndTitleCache<IdentifiableEntity>> resultObjects = new ArrayList<>();
        if (doNamesWithoutTaxa){
        	List<? extends TaxonName> nameResult = taxonNameDao.findByName(
        	        includeAuthors, queryString, matchMode, null, null, null, null);

        	for (TaxonName name: nameResult){
        		if (name.getTaxonBases().size() == 0){
        			resultObjects.add(new UuidAndTitleCache(TaxonName.class, name.getUuid(), name.getId(), name.getTitleCache()));
        		}
        	}
        	if (!doSynonyms && !doTaxa && !doCommonNames){
        		return resultObjects;
        	}
        }
        Query query = prepareTaxaByNameForEditor(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, includeUnpublished,
                "nameCache", queryString, classification, matchMode, namedAreas, doCount, order);

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
                        resultObjects.add( new UuidAndTitleCache(Synonym.class, (UUID) result[0], (Integer) result[1], (String)result[2], new Boolean(result[4].toString()), null));
                    }
                    else {
                        resultObjects.add( new UuidAndTitleCache(Taxon.class, (UUID) result[0], (Integer) result[1], (String)result[2], new Boolean(result[4].toString()), null));
                    }

                }else if (doSynonyms){
                    resultObjects.add( new UuidAndTitleCache(Synonym.class, (UUID) result[0], (Integer) result[1], (String)result[2], new Boolean(result[4].toString()), null));
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
        Query query = prepareTaxaByCommonName(queryString, classification, matchMode, namedAreas, pageSize, pageNumber, doCount, false);
        if (query != null){
            @SuppressWarnings("unchecked")
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
     *
     *
     */
    private Query prepareTaxaByNameForEditor(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames, boolean doCommonNames,
            boolean includeUnpublished, String searchField, String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, boolean doCount, NameSearchOrder order) {
        return prepareByNameQuery(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, includeUnpublished,
                searchField, queryString,
                classification, matchMode, namedAreas, order, doCount, true);
    }

    /**
     * @param searchField
     * @param queryString
     * @param classification
     * @param matchMode
     * @param namedAreas
     * @param doCount
     * @param doNotReturnFullEntities
     *            if set true the seach method will not return synonym and taxon
     *            entities but an array containing the uuid, titleCache, and the
     *            DTYPE in lowercase letters.
     * @param order
     * @param clazz
     * @return
     */
    private Query prepareByNameQuery(boolean doTaxa, boolean doSynonyms, boolean doIncludeMisappliedNames,
                boolean doCommonNames, boolean includeUnpublished, String searchField, String queryString,
                Classification classification, MatchMode matchMode, Set<NamedArea> namedAreas,
                NameSearchOrder order, boolean doCount, boolean returnIdAndTitle){

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
                Query areaQuery = getSession().createQuery("SELECT childArea "
                        + " FROM NamedArea AS childArea LEFT JOIN childArea.partOf as parentArea "
                        + " WHERE parentArea = :area");
                expandNamedAreas(namedAreas, areasExpanded, areaQuery);
            }
            boolean doAreaRestriction = areasExpanded.size() > 0;

            Set<UUID> namedAreasUuids = new HashSet<>();
            for (NamedArea area:areasExpanded){
                namedAreasUuids.add(area.getUuid());
            }


            String [] subSelects = createByNameHQLString(doTaxa, doSynonyms, doIncludeMisappliedNames,
                    includeUnpublished, classification, areasExpanded, matchMode, searchField);
            String taxonSubselect = subSelects[0];
            String synonymSubselect = subSelects[1];
            String misappliedSelect = subSelects[2];
            String commonNameSubSelect = subSelects[3];


            if (logger.isDebugEnabled()) {
                logger.debug("taxonSubselect: " + (taxonSubselect != null ? taxonSubselect: "NULL"));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("synonymSubselect: " + (synonymSubselect != null ? synonymSubselect: "NULL"));
            }

            Query subTaxon = null;
            Query subSynonym = null;
            Query subMisappliedNames = null;
            Query subCommonNames = null;
            List<Integer> taxonIDs = new ArrayList<>();
            List<Integer> synonymIDs = new ArrayList<>();

            if(doTaxa){
                // find Taxa
                subTaxon = getSession().createQuery(taxonSubselect).setParameter("queryString", hqlQueryString);

                if(doAreaRestriction){
                    subTaxon.setParameterList("namedAreasUuids", namedAreasUuids);
                }
                if(classification != null){
                    subTaxon.setParameter("classification", classification);
                }
                if(!includeUnpublished){
                    subTaxon.setBoolean("publish", true);
                }
                taxonIDs = subTaxon.list();
            }

            if(doSynonyms){
                // find synonyms
                subSynonym = getSession().createQuery(synonymSubselect).setParameter("queryString", hqlQueryString);

                if(doAreaRestriction){
                    subSynonym.setParameterList("namedAreasUuids", namedAreasUuids);
                }
                if(classification != null){
                    subSynonym.setParameter("classification", classification);
                }
                if(!includeUnpublished){
                    subSynonym.setBoolean("publish", true);
                }
                synonymIDs = subSynonym.list();
            }
            if (doIncludeMisappliedNames ){
                subMisappliedNames = getSession().createQuery(misappliedSelect).setParameter("queryString", hqlQueryString);
                Set<TaxonRelationshipType> relTypeSet = new HashSet<>();
                relTypeSet.add(TaxonRelationshipType.MISAPPLIED_NAME_FOR());
                relTypeSet.add(TaxonRelationshipType.PRO_PARTE_MISAPPLIED_NAME_FOR());
                subMisappliedNames.setParameterList("rTypeSet", relTypeSet);
                if(doAreaRestriction){
                    subMisappliedNames.setParameterList("namedAreasUuids", namedAreasUuids);
                }
                if(classification != null){
                    subMisappliedNames.setParameter("classification", classification);
                }
                if(!includeUnpublished){
                    subMisappliedNames.setBoolean("publish", true);
                }
                taxonIDs.addAll(subMisappliedNames.list());
            }

            if(doCommonNames){
                // find Taxa
                subCommonNames = getSession().createQuery(commonNameSubSelect).setParameter("queryString", hqlQueryString);

                if(doAreaRestriction){
                    subCommonNames.setParameterList("namedAreasUuids", namedAreasUuids);
                }
                if(classification != null){
                    subCommonNames.setParameter("classification", classification);
                }
                if(!includeUnpublished){
                    subCommonNames.setBoolean("publish", true);
                }
                taxonIDs.addAll(subCommonNames.list());
            }


            if(synonymIDs.size()>0 && taxonIDs.size()>0){
                hql = "SELECT " + selectWhat;
                // in doNotReturnFullEntities mode it is nesscary to also return the type of the matching entities:
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
                // in doNotReturnFullEntities mode it is nesscary to also return the type of the matching entities:
                // also return the computed isOrphaned flag
                if (returnIdAndTitle &&  !doCount ){
                    hql += ", 'synonym', 'false' ";

                }
                hql +=  " FROM %s t " +
                        " WHERE t.id in (:synonyms) ";

            } else if (taxonIDs.size()>0 ){
                hql = "SELECT " + selectWhat;
                // in doNotReturnFullEntities mode it is nesscary to also return the type of the matching entities:
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
            if ((doTaxa || doCommonNames || doIncludeMisappliedNames) && doSynonyms){
                classString = "TaxonBase";
            } else if (doTaxa || doCommonNames){
                classString = "Taxon";
            } else if (doSynonyms && !(doCommonNames|| doTaxa || doIncludeMisappliedNames)){
                classString = "Synonym";
            } else{//only misappliedNames
                classString = "Taxon";
            }

            hql = String.format(hql, classString);

            if (hql == "") {
                return null;
            }
            if(!doCount){
                String orderBy = " ORDER BY ";
                String alphabeticBase = " t.name.genusOrUninomial, case when t.name.specificEpithet like '\"%\"' then 1 else 0 end, t.name.specificEpithet, t.name.rank desc, t.name.nameCache";

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
            Query query = getSession().createQuery(hql);


            if ((doTaxa || doCommonNames || doIncludeMisappliedNames) ){
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
            }
            if(doSynonyms){
                // find synonyms
                if (synonymIDs.size()>0){
                    query.setParameterList("synonyms", synonymIDs);
                }else if (!doTaxa && !doCommonNames && !doIncludeMisappliedNames){
                    return null;
                }
            }

            return query;
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
    private Query prepareTaxaByName(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames,
            boolean doCommonNames, boolean includeUnpublished, String searchField, String queryString,
            Classification classification, MatchMode matchMode, Set<NamedArea> namedAreas, NameSearchOrder order, Integer pageSize, Integer pageNumber, boolean doCount) {

        Query query = prepareByNameQuery(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, includeUnpublished,
                searchField, queryString, classification, matchMode, namedAreas, order, doCount, false);

        if(pageSize != null && !doCount && query != null) {
            query.setMaxResults(pageSize);
            if(pageNumber != null) {
                query.setFirstResult(pageNumber * pageSize);
            }
        }

        return query;
    }

    private Query prepareTaxaByCommonName(String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize, Integer pageNumber,
            boolean doCount, boolean returnIdAndTitle){

        String what = "select distinct";
        if (returnIdAndTitle){
        	what += " t.uuid, t.id, t.titleCache, \'taxon\', case when t.taxonNodes is empty and t.relationsFromThisTaxon is empty and t.relationsToThisTaxon is empty then true else false end ";
        }else {
        	what += (doCount ? " count(t)": " t");
        }
        String hql= what + " from Taxon t " +
        "join t.descriptions d "+
        "join d.descriptionElements e " +
        "join e.feature f " +
        "where f.supportsCommonTaxonName = true and e.name "+matchMode.getMatchOperator()+" :queryString";//and ls.text like 'common%'";

        Query query = getSession().createQuery(hql);

        query.setParameter("queryString", matchMode.queryStringFrom(queryString));

        if(pageSize != null &&  !doCount) {
            query.setMaxResults(pageSize);
            if(pageNumber != null) {
                query.setFirstResult(pageNumber * pageSize);
            }
        }
        return query;
    }

    @Override
    public long countTaxaByName(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames, boolean doCommonNames,
            boolean doIncludeAuthors, String queryString, Classification classification,
        MatchMode matchMode, Set<NamedArea> namedAreas, boolean includeUnpublished) {

        boolean doCount = true;
        /*
        boolean doTaxa = true;
        boolean doSynonyms = true;
        if (clazz.equals(Taxon.class)){
            doSynonyms = false;
        } else if (clazz.equals(Synonym.class)){
            doTaxa = false;
        }
        */
        String searchField = doIncludeAuthors ? "titleCache": "nameCache";

        Query query = prepareTaxaByName(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, includeUnpublished,
                searchField, queryString, classification, matchMode, namedAreas, null, null, null, doCount);
        if (query != null) {
            return (Long)query.uniqueResult();
        }else{
            return 0;
        }
    }

    /**
     * @param namedAreas
     * @param areasExpanded
     * @param areaQuery
     */
    private void expandNamedAreas(Collection<NamedArea> namedAreas, Set<NamedArea> areasExpanded, Query areaQuery) {
        List<NamedArea> childAreas;
        for(NamedArea a : namedAreas){
            areasExpanded.add(a);
            areaQuery.setParameter("area", a);
            childAreas = areaQuery.list();
            if(childAreas.size() > 0){
                areasExpanded.addAll(childAreas);
                expandNamedAreas(childAreas, areasExpanded, areaQuery);
            }
        }
    }


    @Override
    public List<TaxonBase> getAllTaxonBases(Integer pagesize, Integer page) {
        return super.list(pagesize, page);
    }

    @Override
    public List<Synonym> getAllSynonyms(Integer limit, Integer start) {
        Criteria criteria = getSession().createCriteria(Synonym.class);

        if(limit != null) {
            criteria.setFirstResult(start);
            criteria.setMaxResults(limit);
        }

        @SuppressWarnings("unchecked")
        List<Synonym> result = criteria.list();
        return result;
    }

    @Override
    public List<Taxon> getAllTaxa(Integer limit, Integer start) {
        Criteria criteria = getSession().createCriteria(Taxon.class);

        if(limit != null) {
            criteria.setFirstResult(start);
            criteria.setMaxResults(limit);
        }

        @SuppressWarnings("unchecked")
        List<Taxon> result = criteria.list();
        return result;
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
    public List<TaxonBase> findByNameTitleCache(boolean doTaxa, boolean doSynonyms, boolean includeUnpublished, String queryString, Classification classification, MatchMode matchMode, Set<NamedArea> namedAreas, NameSearchOrder order, Integer pageNumber, Integer pageSize, List<String> propertyPaths) {

        boolean doCount = false;
        Query query = prepareTaxaByName(doTaxa, doSynonyms, false, false, includeUnpublished, "titleCache", queryString, classification, matchMode, namedAreas, order, pageSize, pageNumber, doCount);
        if (query != null){
            List<TaxonBase> results = query.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        }
        return new ArrayList<TaxonBase>();

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

        List<? extends TaxonBase> results = crit.list();
        if (results.size() == 1) {
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            TaxonBase taxon = results.iterator().next();
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

        List<? extends TaxonBase> results = crit.list();

        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public int countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted) {
        checkNotInPriorView("TaxonDaoHibernateImpl.countMatchesByName(String queryString, ITitledDao.MATCH_MODE matchMode, boolean onlyAcccepted)");

        Criteria crit = getSession().createCriteria(type);
        crit.add(Restrictions.ilike("titleCache", matchMode.queryStringFrom(queryString)));
        crit.setProjection(Projections.rowCount());
        int result = ((Number)crit.list().get(0)).intValue();
        return result;
    }


    @Override
    public int countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted, List<Criterion> criteria) {
        checkNotInPriorView("TaxonDaoHibernateImpl.countMatchesByName(String queryString, ITitledDao.MATCH_MODE matchMode, boolean onlyAcccepted, List<Criterion> criteria)");

        Criteria crit = getSession().createCriteria(type);
        crit.add(Restrictions.ilike("titleCache", matchMode.queryStringFrom(queryString)));
        if(criteria != null){
            for (Criterion criterion : criteria) {
                crit.add(criterion);
            }
        }
        crit.setProjection(Projections.rowCount());
        int result = ((Number)crit.list().get(0)).intValue();
        return result;
    }


    @Override
    public int countSynonyms(boolean onlyAttachedToTaxon) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Query query = null;

            String queryStr =
                    " SELECT count(syn) "
                  + " FROM Synonym syn";
            if (onlyAttachedToTaxon){
                queryStr += " WHERE syn.acceptedTaxon IS NOT NULL";
            }
            query = getSession().createQuery(queryStr);

            return ((Long)query.uniqueResult()).intValue();
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Synonym.class,auditEvent.getRevisionNumber());
            if (onlyAttachedToTaxon){
                query.add(new NotNullAuditExpression(new EntityPropertyName("acceptedTaxon")));
            }
            query.addProjection(AuditEntity.id().count());

            return ((Long)query.getSingleResult()).intValue();
        }
    }

    @Override
    public long countSynonyms(Taxon taxon, SynonymType type) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(Synonym.class);

            criteria.add(Restrictions.eq("acceptedTaxon", taxon));
            if(type != null) {
                criteria.add(Restrictions.eq("type", type));
            }
            criteria.setProjection(Projections.rowCount());
            return ((Number)criteria.uniqueResult()).intValue();
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Synonym.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId("acceptedTaxon").eq(taxon.getId()));
            query.addProjection(AuditEntity.id().count());

            if(type != null) {
                query.add(AuditEntity.relatedId("type").eq(type.getId()));
            }

            return (Long)query.getSingleResult();
        }
    }

    @Override
    public int countSynonyms(Synonym synonym, SynonymType type) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(Synonym.class);

            criteria.add(Restrictions.isNotNull("acceptedTaxon"));
            if(type != null) {
                criteria.add(Restrictions.eq("type", type));
            }

            criteria.setProjection(Projections.rowCount());
            return ((Number)criteria.uniqueResult()).intValue();
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Synonym.class,auditEvent.getRevisionNumber());
            query.add(new NotNullAuditExpression(new EntityPropertyName("acceptedTaxon")));
            query.addProjection(AuditEntity.id().count());

            if(type != null) {
                query.add(AuditEntity.relatedId("type").eq(type.getId()));
            }

            return ((Long)query.getSingleResult()).intValue();
        }
    }

    @Override
    public int countTaxaByName(Class<? extends TaxonBase> clazz, String genusOrUninomial, String infraGenericEpithet, String specificEpithet,	String infraSpecificEpithet, Rank rank) {
        checkNotInPriorView("TaxonDaoHibernateImpl.countTaxaByName(Boolean accepted, String genusOrUninomial,	String infraGenericEpithet, String specificEpithet,	String infraSpecificEpithet, Rank rank)");
        Criteria criteria = null;

        criteria = getSession().createCriteria(clazz);

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

        if(rank != null) {
            criteria.add(Restrictions.eq("name.rank", rank));
        }

        criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));

        return ((Number)criteria.uniqueResult()).intValue();
    }

    @Override
    public List<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, String authorship, Rank rank, Integer pageSize,	Integer pageNumber) {
        checkNotInPriorView("TaxonDaoHibernateImpl.findTaxaByName(Boolean accepted, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, String authorship, Rank rank, Integer pageSize,	Integer pageNumber)");
        Criteria criteria = null;
        if (clazz == null){
            criteria = getSession().createCriteria(TaxonBase.class);
        } else{
            criteria = getSession().createCriteria(clazz);
        }
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

        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<TaxonBase> result = criteria.list();
        return result;
    }


    @Override
    public int countTaxonRelationships(Taxon taxon, TaxonRelationshipType type,
            boolean includePublished, Direction direction) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {

            String queryString = prepareTaxonRelationshipQuery(type, includePublished, direction, true);
            Query query = getSession().createQuery(queryString);
            query.setParameter("relatedTaxon", taxon);
            if(type != null) {
                query.setParameter("type",type);
            }
            if(! includePublished) {
                query.setBoolean("publish",Boolean.TRUE);
            }


//            if(type == null) {
//                query = getSession().createQuery(
//                        "select count(taxonRelationship) from TaxonRelationship taxonRelationship where taxonRelationship."+direction+" = :relatedTaxon");
//            } else {
//                query = getSession().createQuery("select count(taxonRelationship) from TaxonRelationship taxonRelationship where taxonRelationship."+direction+" = :relatedTaxon and taxonRelationship.type = :type");
//                query.setParameter("type",type);
//            }
//            query.setParameter("relatedTaxon", taxon);

            return ((Long)query.uniqueResult()).intValue();
        } else {
          //TODO unpublished

            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonRelationship.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId(direction.toString()).eq(taxon.getId()));
            query.addProjection(AuditEntity.id().count());

            if(type != null) {
                query.add(AuditEntity.relatedId("type").eq(type.getId()));
            }

            return ((Long)query.getSingleResult()).intValue();
        }
    }


    /**
     * @param type
     * @param includePublished
     * @param direction
     * @param b
     * @return
     */
    private String prepareTaxonRelationshipQuery(TaxonRelationshipType type, boolean includePublished,
            Direction direction, boolean isCount) {
        String selectStr = isCount? " count(rel) as n ":" rel ";
        String result = "SELECT " + selectStr +
             " FROM TaxonRelationship rel " +
             " WHERE rel."+direction+" = :relatedTaxon";
        if (type != null){
            result += " AND rel.type = :type ";
        }
        if(! includePublished) {
            result += " AND rel."+direction+".publish = :publish";
        }
        return result;
    }

    @Override
    public List<TaxonRelationship> getTaxonRelationships(Taxon taxon, TaxonRelationshipType type,
            boolean includePublished, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths, Direction direction) {

        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {

            String queryString = prepareTaxonRelationshipQuery(type, includePublished, direction, false);

            queryString += orderByClause(orderHints, "rel");

            Query query = getSession().createQuery(queryString);
            query.setParameter("relatedTaxon", taxon);
            if(type != null) {
                query.setParameter("type",type);
            }
            if(! includePublished) {
                query.setBoolean("publish",Boolean.TRUE);
            }
            setPagingParameter(query, pageSize, pageNumber);

//            Criteria criteria = getSession().createCriteria(TaxonRelationship.class);
//
//            if(direction != null) {
//                criteria.add(Restrictions.eq(direction.name(), taxon));
//            } else {
//                criteria.add(Restrictions.or(
//                        Restrictions.eq(Direction.relatedFrom.name(), taxon),
//                        Restrictions.eq(Direction.relatedTo.name(), taxon))
//                    );
//            }
//
//            if(type != null) {
//                criteria.add(Restrictions.eq("type", type));
//            }
//
//            addOrder(criteria,orderHints);
//
//            if(pageSize != null) {
//                criteria.setMaxResults(pageSize);
//                if(pageNumber != null) {
//                    criteria.setFirstResult(pageNumber * pageSize);
//                } else {
//                    criteria.setFirstResult(0);
//                }
//            }

            @SuppressWarnings("unchecked")
            List<TaxonRelationship> result = query.list();
            defaultBeanInitializer.initializeAll(result, propertyPaths);

            return result;
        } else {
            //TODO unpublished
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonRelationship.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId("relatedTo").eq(taxon.getId()));

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

            List<Synonym> result = query.getResultList();
            defaultBeanInitializer.initializeAll(result, propertyPaths);

            return result;
        }
    }

    @Override
    public void rebuildIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());

        for(TaxonBase taxonBase : list(null,null)) { // re-index all taxon base
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

        Query query = getSession().createQuery(hql);
        query.setParameter("synonym", synonym);
        if(classificationFilter != null){
            query.setParameter("classificationFilter", classificationFilter);
        }

        @SuppressWarnings("unchecked")
        List<Taxon> result = query.list();

        defaultBeanInitializer.initializeAll(result, propertyPaths);

        return result.isEmpty()? null: result.get(0);
    }

    @Override
    public long countAcceptedTaxonFor(Synonym synonym, Classification classificationFilter){

        String hql = prepareListAcceptedTaxaFor(classificationFilter, true);

        Query query = getSession().createQuery(hql);
        query.setParameter("synonym", synonym);
        if(classificationFilter != null){
            query.setParameter("classificationFilter", classificationFilter);
        }

        Long count = Long.parseLong(query.uniqueResult().toString());
        return count;

    }


    /**
     * @param classificationFilter
     * @param orderHints
     * @return
     */
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
            List<String> propertyPaths = new ArrayList<String>();
            propertyPaths.add("createdBy");
            propertyPaths.add("updatedBy");
            propertyPaths.add("name");
            propertyPaths.add("sec");
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
        Query query = getSession().createQuery(
                 " FROM TaxonName t "
                +" WHERE t.nameCache IN (:taxonList)");
        query.setParameterList("taxonList", taxonNames);
        @SuppressWarnings("unchecked")
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

    //TODO: mal nur mit UUID probieren (ohne fetch all properties), vielleicht geht das schneller?
    @Override
    public List<UUID> findIdenticalTaxonNameIds(List<String> propertyPaths){
        Query query=getSession().createQuery(
                   "SELECT tmb2 "
                + " FROM ZoologicalName tmb, ZoologicalName tmb2 FETCH ALL properties "
                + " WHERE tmb.id != tmb2.id AND tmb.nameCache = tmb2.nameCache");
        @SuppressWarnings("unchecked")
        List<UUID> zooNames = query.list();

        return zooNames;

    }

    @Override
    public List<TaxonName> findIdenticalTaxonNames(List<String> propertyPaths) {

        Query query=getSession().createQuery(
                  " SELECT tmb2 "
                + " FROM ZoologicalName tmb, ZoologicalName tmb2 FETCH ALL properties "
                + " WHERE tmb.id != tmb2.id AND tmb.nameCache = tmb2.nameCache");

        @SuppressWarnings("unchecked")
        List<TaxonName> zooNames = query.list();

        TaxonNameComparator taxComp = new TaxonNameComparator();
        Collections.sort(zooNames, taxComp);

        for (TaxonName taxonName: zooNames){
            defaultBeanInitializer.initialize(taxonName, propertyPaths);
        }

        return zooNames;
    }

    @Override
    public List<TaxonName> findIdenticalNamesNew(List<String> propertyPaths){

        //Hole die beiden Source_ids von "Fauna Europaea" und "Erms" und in sources der names darf jeweils nur das entgegengesetzte auftreten (i member of tmb.taxonBases)
        Query query = getSession().createQuery("SELECT id "
                + "FROM Reference "
                + " WHERE titleCache LIKE 'Fauna Europaea database'");
        @SuppressWarnings("unchecked")
        List<String> secRefFauna = query.list();
        query = getSession().createQuery("Select id from Reference where titleCache like 'ERMS'");
        @SuppressWarnings("unchecked")
        List<String> secRefErms = query.list();
        //Query query = getSession().createQuery("select tmb2.nameCache from ZoologicalName tmb, TaxonBase tb1, ZoologicalName tmb2, TaxonBase tb2 where tmb.id != tmb2.id and tb1.name = tmb and tb2.name = tmb2 and tmb.nameCache = tmb2.nameCache and tb1.sec != tb2.sec");
        //Get all names of fauna europaea
        query = getSession().createQuery("select zn.nameCache from ZoologicalName zn, TaxonBase tb where tb.name = zn and tb.sec.id = :secRefFauna");
        query.setParameter("secRefFauna", secRefFauna.get(0));
        @SuppressWarnings("unchecked")
        List<String> namesFauna= query.list();

        //Get all names of erms

        query = getSession().createQuery("select zn.nameCache from ZoologicalName zn, TaxonBase tb where tb.name = zn and tb.sec.id = :secRefErms");
        query.setParameter("secRefErms", secRefErms.get(0));

        @SuppressWarnings("unchecked")
        List<String> namesErms = query.list();
        /*TaxonNameComparator comp = new TaxonNameComparator();
        Collections.sort(namesFauna);
        Collections.sort(namesErms);
        */
        List <String> identicalNames = new ArrayList<>();

        for (String nameFauna: namesFauna){
            if (namesErms.contains(nameFauna)){
                identicalNames.add(nameFauna);
            }
        }


        query = getSession().createQuery("from ZoologicalName zn where zn.nameCache IN (:identicalNames)");
        query.setParameterList("identicalNames", identicalNames);
        List<TaxonName> result = query.list();
        TaxonName tempName = result.get(0);

        Iterator<IdentifiableSource> sources = tempName.getSources().iterator();

        TaxonNameComparator taxComp = new TaxonNameComparator();
        Collections.sort(result, taxComp);
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;

    }

//
//
//    @Override
//    public String getPhylumName(TaxonName name){
//        List results = new ArrayList();
//        try{
//        Query query = getSession().createSQLQuery("select getPhylum("+ name.getId()+");");
//        results = query.list();
//        }catch(Exception e){
//            System.err.println(name.getUuid());
//            return null;
//        }
//        System.err.println("phylum of "+ name.getTitleCache() );
//        return (String)results.get(0);
//    }


    @Override
    public long countTaxaByCommonName(String searchString,
            Classification classification, MatchMode matchMode,
            Set<NamedArea> namedAreas) {
        boolean doCount = true;
        Query query = prepareTaxaByCommonName(searchString, classification, matchMode, namedAreas, null, null, doCount, false);
        if (query != null && !query.list().isEmpty()) {
            Object o = query.uniqueResult();
            if(o != null) {
                return (Long)o;
            }
        }
        return 0;
    }

    private String[] createByNameHQLString(boolean doTaxa, boolean doSynonyms, boolean doIncludeMisappliedNames,
                boolean includeUnpublished, Classification classification,  Set<NamedArea> areasExpanded,
                MatchMode matchMode, String searchField){

        boolean doAreaRestriction = areasExpanded.size() > 0;
        String doAreaRestrictionSubSelect =
                     " SELECT %s.id "
                   + " FROM Distribution e "
                   + "    JOIN e.inDescription d "
                   + "    JOIN d.taxon t " +
                (classification != null ? " JOIN t.taxonNodes AS tn " : " ");

        String doAreaRestrictionMisappliedNameSubSelect =
                   "SELECT %s.id "
                   + " FROM Distribution e "
                   + "   JOIN e.inDescription d"
                   + "   JOIN d.taxon t";

        String doTaxonSubSelect =
                     " SELECT %s.id "
                   + " FROM Taxon t " + (classification != null ? " "
                           + " JOIN t.taxonNodes AS tn " : " ");

        String doTaxonMisappliedNameSubSelect =
                     " SELECT %s.id "
                   + " FROM Taxon t ";

        String doTaxonNameJoin = " JOIN t.name n ";

        String doSynonymNameJoin =
                   " JOIN t.synonyms s "
                 + " JOIN s.name sn";

        String doMisappliedNamesJoin =
                   " LEFT JOIN t.relationsFromThisTaxon AS rft " +
                   " LEFT JOIN rft.relatedTo AS rt " +
                      (classification != null ? " LEFT JOIN rt.taxonNodes AS tn2 " : " ") +
                   " LEFT JOIN rt.name AS n2" +
                   " LEFT JOIN rft.type as rtype";

        String doCommonNamesJoin =
                   " JOIN t.descriptions AS description "+
                   " LEFT JOIN description.descriptionElements AS com " +
                   " LEFT JOIN com.feature f ";


        String doClassificationWhere = " tn.classification = :classification";
        String doClassificationForMisappliedNamesWhere = " tn2.classification = :classification";

        String doAreaRestrictionWhere =  " e.area.uuid in (:namedAreasUuids)";
        String doCommonNamesRestrictionWhere = " (f.supportsCommonTaxonName = true and com.name "+matchMode.getMatchOperator()+" :queryString )";

        String doSearchFieldWhere = "%s." + searchField + " " + matchMode.getMatchOperator() + " :queryString";

        String doRelationshipTypeComparison = " rtype in (:rTypeSet) ";

        String taxonSubselect = null;
        String synonymSubselect = null;
        String misappliedSelect = null;
        String commonNameSubselect = null;

        if(classification != null ){
            if (!doIncludeMisappliedNames){
                if(doAreaRestriction){
                    taxonSubselect = String.format(doAreaRestrictionSubSelect, "t") + doTaxonNameJoin +
                            " WHERE " + doAreaRestrictionWhere +
                            "  AND " + doClassificationWhere +
                            "  AND " + String.format(doSearchFieldWhere, "n");
                    synonymSubselect = String.format(doAreaRestrictionSubSelect, "s") + doSynonymNameJoin +
                            " WHERE " + doAreaRestrictionWhere +
                            "  AND " + doClassificationWhere +
                            "  AND " + String.format(doSearchFieldWhere, "sn");
                    commonNameSubselect =  String.format(doAreaRestrictionSubSelect, "t") + doCommonNamesJoin +
                            " WHERE " +  doAreaRestrictionWhere +
                            "  AND " + doClassificationWhere +
                            "  AND " + String.format(doSearchFieldWhere, "n") +
                            "  AND " + doCommonNamesRestrictionWhere;
                } else {//no area restriction
                    taxonSubselect = String.format(doTaxonSubSelect, "t" )+ doTaxonNameJoin +
                            " WHERE " + doClassificationWhere +
                            "  AND " + String.format(doSearchFieldWhere, "n");
                    synonymSubselect = String.format(doTaxonSubSelect, "s" ) + doSynonymNameJoin +
                            " WHERE " + doClassificationWhere +
                            "  AND " + String.format(doSearchFieldWhere, "sn");
                    commonNameSubselect =String.format(doTaxonSubSelect, "t" )+ doCommonNamesJoin +
                            " WHERE " + doClassificationWhere +
                            "  AND " + doCommonNamesRestrictionWhere;
                }
            }else{ //misappliedNames included
                if(doAreaRestriction){
                    misappliedSelect = String.format(doAreaRestrictionMisappliedNameSubSelect, "t") + doTaxonNameJoin + doMisappliedNamesJoin  +
                            " WHERE " + doAreaRestrictionWhere +
                            "  AND " + String.format(doSearchFieldWhere, "n") +
                            "  AND " + doClassificationForMisappliedNamesWhere +
                            "  AND " + doRelationshipTypeComparison;
                    taxonSubselect = String.format(doAreaRestrictionSubSelect, "t") + doTaxonNameJoin +
                            " WHERE " + doAreaRestrictionWhere +
                            "  AND " + String.format(doSearchFieldWhere, "n") +
                            "  AND " + doClassificationWhere;
                    synonymSubselect = String.format(doAreaRestrictionSubSelect, "s") + doSynonymNameJoin +
                            " WHERE " + doAreaRestrictionWhere +
                            "  AND " + doClassificationWhere +
                            "  AND " + String.format(doSearchFieldWhere, "sn");
                    commonNameSubselect= String.format(doAreaRestrictionSubSelect, "t")+ doCommonNamesJoin +
                            " WHERE " + doAreaRestrictionWhere +
                            "  AND " + doClassificationWhere +
                            "  AND " + doCommonNamesRestrictionWhere;
                } else {//no area restriction
                    misappliedSelect = String.format(doTaxonMisappliedNameSubSelect, "t" ) + doTaxonNameJoin + doMisappliedNamesJoin +
                            " WHERE " + String.format(doSearchFieldWhere, "n") +
                            "  AND " + doClassificationForMisappliedNamesWhere +
                            "  AND " + doRelationshipTypeComparison;
                    taxonSubselect = String.format(doTaxonSubSelect, "t" ) + doTaxonNameJoin +
                            " WHERE " +  String.format(doSearchFieldWhere, "n") +
                            " AND "+ doClassificationWhere;
                    synonymSubselect = String.format(doTaxonSubSelect, "s" ) + doSynonymNameJoin +
                            " WHERE " + doClassificationWhere +
                            "  AND " +  String.format(doSearchFieldWhere, "sn");
                    commonNameSubselect= String.format(doTaxonSubSelect, "t")+ doCommonNamesJoin +
                            " WHERE " + doClassificationWhere +
                            "  AND " + doCommonNamesRestrictionWhere;
                }
            }
        } else { //classification = null
            if(doAreaRestriction){
                misappliedSelect = String.format(doAreaRestrictionMisappliedNameSubSelect, "t") + doTaxonNameJoin + doMisappliedNamesJoin +
                        " WHERE " + doAreaRestrictionWhere +
                        "  AND " + String.format(doSearchFieldWhere, "n")+
                        "  AND " + doRelationshipTypeComparison;
                taxonSubselect = String.format(doAreaRestrictionSubSelect, "t") + doTaxonNameJoin +
                        " WHERE " + doAreaRestrictionWhere +
                        "  AND " + String.format(doSearchFieldWhere, "n");
                synonymSubselect = String.format(doAreaRestrictionSubSelect, "s") + doSynonymNameJoin +
                        " WHERE " + doAreaRestrictionWhere +
                        "  AND " + String.format(doSearchFieldWhere, "sn");
                commonNameSubselect = String.format(doAreaRestrictionSubSelect, "t")+ doCommonNamesJoin +
                        " WHERE " + doAreaRestrictionWhere +
                        "  AND " + doCommonNamesRestrictionWhere;
            } else { //no area restriction
                misappliedSelect = String.format(doTaxonMisappliedNameSubSelect, "t" ) + doTaxonNameJoin + doMisappliedNamesJoin +
                        " WHERE " +  String.format(doSearchFieldWhere, "n") +
                        " AND " + doRelationshipTypeComparison;
                taxonSubselect = String.format(doTaxonSubSelect, "t" ) + doTaxonNameJoin +
                        " WHERE " +  String.format(doSearchFieldWhere, "n");
                synonymSubselect = String.format(doTaxonSubSelect, "s" ) + doSynonymNameJoin +
                        " WHERE " +  String.format(doSearchFieldWhere, "sn");
                commonNameSubselect = String.format(doTaxonSubSelect, "t" ) +doCommonNamesJoin +
                        " WHERE "+  doCommonNamesRestrictionWhere;
            }
        }

        if (!includeUnpublished){
            taxonSubselect   += " AND t.publish = :publish ";
            synonymSubselect += " AND s.publish = :publish AND t.publish = :publish ";
            commonNameSubselect += " AND t.publish = :publish ";
            misappliedSelect += " AND t.publish = :publish AND rt.publish = :publish ";
        }

        String[] result = {taxonSubselect, synonymSubselect, misappliedSelect, commonNameSubselect};

        return result;
    }

	@Override
	public List<UuidAndTitleCache<IdentifiableEntity>> getTaxaByCommonNameForEditor(
			String titleSearchStringSqlized, Classification classification,
			MatchMode matchMode, Set<NamedArea> namedAreas) {
		Query query = prepareTaxaByCommonName(titleSearchStringSqlized, classification, matchMode, namedAreas, null, null, false, true);
        if (query != null){
            @SuppressWarnings("unchecked")
            List<Object> resultArray = query.list();
            List<UuidAndTitleCache<IdentifiableEntity>> returnResult = new ArrayList<>() ;
            Object[] result;
            for(int i = 0; i<resultArray.size();i++){
            	result = (Object[]) resultArray.get(i);
            	returnResult.add(new UuidAndTitleCache(Taxon.class, (UUID) result[0],(Integer)result[1], (String)result[2], new Boolean(result[4].toString()), null));
            }
            return returnResult;
        }else{
            return new ArrayList<>();
        }
	}


	/**
	 * @param
	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countByIdentifier(java.lang.Class, java.lang.String, eu.etaxonomy.cdm.model.common.DefinedTerm, eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.persistence.query.MatchMode)
	 */
	@Override
	public <S extends TaxonBase> int countByIdentifier(Class<S> clazz,
			String identifier, DefinedTerm identifierType, TaxonNode subtreeFilter, MatchMode matchmode) {
		if (subtreeFilter == null){
			return countByIdentifier(clazz, identifier, identifierType, matchmode);
		}

		Class<?> clazzParam = clazz == null ? type : clazz;
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

		Query query = getSession().createQuery(queryString);
        if (identifierType != null){
        	query.setEntity("type", identifierType);
        }

		Long c = (Long)query.uniqueResult();
        return c.intValue();
	}

	@Override
	public <S extends TaxonBase> List<Object[]> findByIdentifier(
			Class<S> clazz, String identifier, DefinedTerm identifierType, TaxonNode subtreeFilter,
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

		Query query = getSession().createQuery(queryString);

		//parameters
		if (identifierType != null){
        	query.setEntity("type", identifierType);
        }

        //paging
        setPagingParameter(query, pageSize, pageNumber);

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

    /**
     * {@inheritDoc}
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
        if (markerType != null){
            queryString += " AND mks.markerType = :type";
        }

        Query query = getSession().createQuery(queryString);
        if (markerType != null){
            query.setEntity("type", markerType);
        }
        if (markerValue != null){
            query.setBoolean("flag", markerValue);
        }

        Long c = (Long)query.uniqueResult();
        return c;
    }

    /**
     * {@inheritDoc}
     */
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

        Query query = getSession().createQuery(queryString);

        //parameters
        query.setEntity("type", markerType);
        if (markerValue != null){
            query.setBoolean("flag", markerValue);
        }

        //paging
        setPagingParameter(query, pageSize, pageNumber);

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
        long result = ((Number)criteria.uniqueResult()).longValue();

        return result;
    }

    @Override
    public List<TaxonRelationship> getTaxonRelationships(Set<TaxonRelationshipType> types,
            Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths) {
        Criteria criteria = getSession().createCriteria(TaxonRelationship.class);

        if (types != null) {
            if (types.isEmpty()){
                return new ArrayList<>();
            }else{
                criteria.add(Restrictions.in("type", types) );
            }
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

        List<TaxonRelationship> results = criteria.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);

        return results;
    }

    @Override
    public  List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCache(Integer limit, String pattern){
        Session session = getSession();
        Query query = null;
        if (pattern != null){
            query = session.createQuery(
                  " SELECT tb.uuid, tb.id, tb.titleCache, tb.name.rank "
                  + " FROM TaxonBase as tb "
                  + " WHERE tb.titleCache LIKE :pattern");
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            pattern = pattern + "%";
            query.setParameter("pattern", pattern);
        } else {
            query = session.createQuery(
                    " SELECT tb.uuid, taxonBase.id, tb.titleCache, tb.name.rank "
                  + " FROM TaxonBase AS tb");
        }
        if (limit != null){
           query.setMaxResults(limit);
        }

        return getUuidAndTitleCache(query);
    }

    @Override
    protected List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCache(Query query){
        List<UuidAndTitleCache<TaxonBase>> list = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.list();
        if (!result.isEmpty()){
            if (result.iterator().next().length == 4){
                Collections.sort(result, new UuidAndTitleCacheTaxonComparator());
            }
        }

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<TaxonBase>((UUID) object[0],(Integer) object[1], (String) object[2]));
        }
        return list;
    }


}
