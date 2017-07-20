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
 * @created 24.11.2008
 */
@Repository
@Qualifier("taxonDaoHibernateImpl")
public class TaxonDaoHibernateImpl extends IdentifiableDaoBase<TaxonBase> implements ITaxonDao {
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
    public List<TaxonBase> getTaxaByName(String queryString, Reference sec) {

        return getTaxaByName(queryString, true, sec);
    }

    @Override
    public List<TaxonBase> getTaxaByName(String queryString, Boolean accepted, Reference sec) {
        checkNotInPriorView("TaxonDaoHibernateImpl.getTaxaByName(String name, Reference sec)");

        Criteria criteria = null;
        if (accepted == true) {
            criteria = getSession().createCriteria(Taxon.class);
        } else {
            criteria = getSession().createCriteria(Synonym.class);
        }

        criteria.setFetchMode( "name", FetchMode.JOIN );
        criteria.createAlias("name", "name");

        if (sec != null && sec.getId() != 0) {
            criteria.add(Restrictions.eq("sec", sec ) );
        }

        if (queryString != null) {
            criteria.add(Restrictions.ilike("name.nameCache", queryString));
        }

        return criteria.list();
    }

    public List<TaxonBase> getTaxaByName(boolean doTaxa, boolean doSynonyms, String queryString, MatchMode matchMode,
            Integer pageSize, Integer pageNumber) {

        return getTaxaByName(doTaxa, doSynonyms, false, false, false, queryString, null, matchMode, null, null, pageSize, pageNumber, null);
    }

    @Override
    public List<TaxonBase> getTaxaByName(String queryString, MatchMode matchMode,
            Boolean accepted, Integer pageSize, Integer pageNumber) {

        boolean doTaxa = true;
        boolean doSynonyms = true;

        if (accepted == true) {
            doSynonyms = false;
        } else {
           doTaxa = false;
        }
        return getTaxaByName(doTaxa, doSynonyms, queryString, matchMode, pageSize, pageNumber);
    }

    @Override
    public List<TaxonBase> getTaxaByName(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames, boolean doCommonNames,
            boolean includeAuthors,
            String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, NameSearchOrder order,
            Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

        boolean doCount = false;

        String searchField = includeAuthors ? "titleCache" : "nameCache";
        Query query = prepareTaxaByName(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, searchField, queryString, classification, matchMode, namedAreas, order, pageSize, pageNumber, doCount);

        if (query != null){
            @SuppressWarnings("unchecked")
            List<TaxonBase> results = query.list();

            defaultBeanInitializer.initializeAll(results, propertyPaths);

            //Collections.sort(results, comp);
            return results;
        }

        return new ArrayList<>();

    }


    //new search for the editor, for performance issues the return values are only uuid and titleCache, to avoid the initialisation of all objects
    @Override
    @SuppressWarnings("unchecked")
    public List<UuidAndTitleCache<IdentifiableEntity>> getTaxaByNameForEditor(boolean doTaxa, boolean doSynonyms, boolean doNamesWithoutTaxa, boolean doMisappliedNames, boolean doCommonNames, String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, NameSearchOrder order) {
//        long zstVorher;
//        long zstNachher;
        if (order == null){
            order = NameSearchOrder.ALPHA;  //TODO add to signature
        }

        boolean doCount = false;
        boolean includeAuthors = false;
        List<UuidAndTitleCache<IdentifiableEntity>> resultObjects = new ArrayList<UuidAndTitleCache<IdentifiableEntity>>();
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
        Query query = prepareTaxaByNameForEditor(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, "nameCache", queryString, classification, matchMode, namedAreas, doCount, order);


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
            List<Taxon> results = query.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        }
        return new ArrayList<Taxon>();

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
            String searchField, String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, boolean doCount, NameSearchOrder order) {
        return prepareQuery(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, searchField, queryString,
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
    private Query prepareQuery(boolean doTaxa, boolean doSynonyms, boolean doIncludeMisappliedNames, boolean doCommonNames, String searchField, String queryString,
                Classification classification, MatchMode matchMode, Set<NamedArea> namedAreas, NameSearchOrder order, boolean doCount, boolean doNotReturnFullEntities){

            if (order == null){
                order = NameSearchOrder.DEFAULT();
            }
            String hqlQueryString = matchMode.queryStringFrom(queryString);
            String selectWhat;
            if (doNotReturnFullEntities){
                selectWhat = "t.uuid, t.id, t.titleCache ";
            }else {
                selectWhat = (doCount ? "count(t)": "t");
            }



            String hql = "";
            Set<NamedArea> areasExpanded = new HashSet<NamedArea>();
            if(namedAreas != null && namedAreas.size() > 0){
                // expand areas and restrict by distribution area
                Query areaQuery = getSession().createQuery("select childArea from NamedArea as childArea left join childArea.partOf as parentArea where parentArea = :area");
                expandNamedAreas(namedAreas, areasExpanded, areaQuery);
            }
            boolean doAreaRestriction = areasExpanded.size() > 0;

            Set<UUID> namedAreasUuids = new HashSet<UUID>();
            for (NamedArea area:areasExpanded){
                namedAreasUuids.add(area.getUuid());
            }


            String [] subSelects = createHQLString(doTaxa, doSynonyms, doIncludeMisappliedNames, classification, areasExpanded, matchMode, searchField);
            String taxonSubselect = subSelects[1];
            String synonymSubselect = subSelects[2];
            String misappliedSelect = subSelects[0];
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

            if(doTaxa){
                // find Taxa
                subTaxon = getSession().createQuery(taxonSubselect).setParameter("queryString", hqlQueryString);

                if(doAreaRestriction){
                    subTaxon.setParameterList("namedAreasUuids", namedAreasUuids);
                }
                if(classification != null){
                    subTaxon.setParameter("classification", classification);

                }
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
            }
            if (doIncludeMisappliedNames ){
                subMisappliedNames = getSession().createQuery(misappliedSelect).setParameter("queryString", hqlQueryString);
                subMisappliedNames.setParameter("rType", TaxonRelationshipType.MISAPPLIED_NAME_FOR());
                if(doAreaRestriction){
                    subMisappliedNames.setParameterList("namedAreasUuids", namedAreasUuids);
                }
                if(classification != null){
                    subMisappliedNames.setParameter("classification", classification);
                }
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
            }

            List<Integer> taxa = new ArrayList<Integer>();
            List<Integer> synonyms = new ArrayList<Integer>();
            if (doSynonyms){
                synonyms = subSynonym.list();
            }
            if(doTaxa){
                taxa = subTaxon.list();
            }
            if (doIncludeMisappliedNames){
                taxa.addAll(subMisappliedNames.list());
            }
            if (doCommonNames){
                taxa.addAll(subCommonNames.list());
            }


           // if (doTaxa && doSynonyms){
                if(synonyms.size()>0 && taxa.size()>0){
                    hql = "select " + selectWhat;
                    // in doNotReturnFullEntities mode it is nesscary to also return the type of the matching entities:
                    // also return the computed isOrphaned flag
                    if (doNotReturnFullEntities &&  !doCount ){
                        hql += ", case when t.id in (:taxa) then 'taxon' else 'synonym' end, " +
                                " case when t.id in (:taxa) and t.taxonNodes is empty and t.relationsFromThisTaxon is empty and t.relationsToThisTaxon is empty then true else false end ";
                    }
                    hql +=  " from %s t " +
                            " where (t.id in (:taxa) OR t.id in (:synonyms)) ";
                }else if (synonyms.size()>0 ){
                    hql = "select " + selectWhat;
                    // in doNotReturnFullEntities mode it is nesscary to also return the type of the matching entities:
                    // also return the computed isOrphaned flag
                    if (doNotReturnFullEntities &&  !doCount ){
                        hql += ", 'synonym', 'false' ";

                    }
                    hql +=  " from %s t " +
                            " where t.id in (:synonyms) ";

                } else if (taxa.size()>0 ){
                    hql = "select " + selectWhat;
                    // in doNotReturnFullEntities mode it is nesscary to also return the type of the matching entities:
                    // also return the computed isOrphaned flag
                    if (doNotReturnFullEntities &&  !doCount ){
                        hql += ", 'taxon', " +
                                " case when t.taxonNodes is empty and t.relationsFromThisTaxon is empty and t.relationsToThisTaxon is empty then true else false end ";
                    }
                    hql +=  " from %s t " +
                            " where t.id in (:taxa) ";

                } else if (StringUtils.isBlank(queryString)){
                    hql = "select " + selectWhat + " from %s t";
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
                if (taxa.size()>0){
                    query.setParameterList("taxa", taxa);
                }
                if (synonyms.size()>0){
                    query.setParameterList("synonyms",synonyms);
                }
                if (taxa.size()== 0 && synonyms.size() == 0){
                    return null;
                }
            }
            if(doSynonyms){
                // find synonyms
                if (synonyms.size()>0){
                    query.setParameterList("synonyms", synonyms);
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
    private Query prepareTaxaByName(boolean doTaxa, boolean doSynonyms, boolean doMisappliedNames, boolean doCommonNames, String searchField, String queryString,
            Classification classification, MatchMode matchMode, Set<NamedArea> namedAreas, NameSearchOrder order, Integer pageSize, Integer pageNumber, boolean doCount) {

        Query query = prepareQuery(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, searchField, queryString, classification, matchMode, namedAreas, order, doCount, false);

        if(pageSize != null &&  !doCount && query != null) {
            query.setMaxResults(pageSize);
            if(pageNumber != null) {
                query.setFirstResult(pageNumber * pageSize);
            }
        }

        return query;
    }

    private Query prepareTaxaByCommonName(String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize, Integer pageNumber, boolean doCount, boolean doNotReturnFullEntities){

        String what = "select distinct";
        if (doNotReturnFullEntities){
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
        MatchMode matchMode, Set<NamedArea> namedAreas) {

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

        Query query = prepareTaxaByName(doTaxa, doSynonyms, doMisappliedNames, doCommonNames, searchField, queryString, classification, matchMode, namedAreas, null, null, null, doCount);
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

        return criteria.list();
    }

    @Override
    public List<Taxon> getAllTaxa(Integer limit, Integer start) {
        Criteria criteria = getSession().createCriteria(Taxon.class);

        if(limit != null) {
            criteria.setFirstResult(start);
            criteria.setMaxResults(limit);
        }

        return criteria.list();
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
    public List<TaxonBase> findByNameTitleCache(boolean doTaxa, boolean doSynonyms, String queryString, Classification classification, MatchMode matchMode, Set<NamedArea> namedAreas, NameSearchOrder order, Integer pageNumber, Integer pageSize, List<String> propertyPaths) {

        boolean doCount = false;
        Query query = prepareTaxaByName(doTaxa, doSynonyms, false, false, "titleCache", queryString, classification, matchMode, namedAreas, order, pageSize, pageNumber, doCount);
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
    public int countTaxonRelationships(Taxon taxon, TaxonRelationshipType type, Direction direction) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Query query = null;

            if(type == null) {
                query = getSession().createQuery("select count(taxonRelationship) from TaxonRelationship taxonRelationship where taxonRelationship."+direction+" = :relatedTaxon");
            } else {
                query = getSession().createQuery("select count(taxonRelationship) from TaxonRelationship taxonRelationship where taxonRelationship."+direction+" = :relatedTaxon and taxonRelationship.type = :type");
                query.setParameter("type",type);
            }
            query.setParameter("relatedTaxon", taxon);

            return ((Long)query.uniqueResult()).intValue();
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonRelationship.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId(direction.toString()).eq(taxon.getId()));
            query.addProjection(AuditEntity.id().count());

            if(type != null) {
                query.add(AuditEntity.relatedId("type").eq(type.getId()));
            }

            return ((Long)query.getSingleResult()).intValue();
        }
    }


    @Override
    public int countSynonyms(boolean onlyAttachedToTaxon) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Query query = null;

            String queryStr = "SELECT count(syn) FROM Synonym syn";
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

        return criteria.list();
    }

    @Override
    public List<TaxonRelationship> getTaxonRelationships(Taxon taxon, TaxonRelationshipType type,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
            List<String> propertyPaths, Direction direction) {

        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {

            Criteria criteria = getSession().createCriteria(TaxonRelationship.class);

            if(direction != null) {
                criteria.add(Restrictions.eq(direction.name(), taxon));
            } else {
                criteria.add(Restrictions.or(
                        Restrictions.eq(Direction.relatedFrom.name(), taxon),
                        Restrictions.eq(Direction.relatedTo.name(), taxon))
                    );
            }

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
            List<TaxonRelationship> result = criteria.list();
            defaultBeanInitializer.initializeAll(result, propertyPaths);

            return result;
        } else {
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

            List<TaxonRelationship> result = query.getResultList();
            defaultBeanInitializer.initializeAll(result, propertyPaths);

            // Ugly, but for now, there is no way to sort on a related entity property in Envers,
            // and we can't live without this functionality in CATE as it screws up the whole
            // taxon tree thing
            if(orderHints != null && !orderHints.isEmpty()) {
                SortedSet<TaxonRelationship> sortedList = new TreeSet<TaxonRelationship>(new TaxonRelationshipFromTaxonComparator());
                sortedList.addAll(result);
                return new ArrayList<TaxonRelationship>(sortedList);
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
        String hqlSelect = "SELECT " + (doCount? "COUNT(taxon)" : "taxon") + " FROM Synonym as syn JOIN syn.acceptedTaxon as taxon ";
        String hqlWhere = " WHERE syn = :synonym";

        if(classificationFilter != null){
            hqlSelect += " JOIN taxon.taxonNodes AS taxonNode";
            hqlWhere += " AND taxonNode.classification = :classificationFilter";
        }
        hql = hqlSelect + hqlWhere;
        return hql;
    }

    @Override
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(Classification classification, Integer limit, String pattern) {
        int classificationId = classification.getId();
        // StringBuffer excludeUuids = new StringBuffer();

         String queryString = "SELECT nodes.uuid, nodes.id, taxon.titleCache FROM TaxonNode AS nodes JOIN nodes.taxon as taxon WHERE nodes.classification.id = " + classificationId ;
         if (pattern != null){
             if (pattern.equals("?")){
                 limit = null;
             } else{
                 if (!pattern.endsWith("*")){
                     pattern += "%";
                 }
                 pattern = pattern.replace("*", "%");
                 pattern = pattern.replace("?", "%");
                 queryString = queryString + " AND taxon.titleCache like (:pattern)" ;
             }
         }



         Query query = getSession().createQuery(queryString);


         if (limit != null){
             query.setMaxResults(limit);
         }

         if (pattern != null && !pattern.equals("?")){
             query.setParameter("pattern", pattern);
         }
         @SuppressWarnings("unchecked")
         List<Object[]> result = query.list();

         if(result.size() == 0){
             return null;
         }else{
             List<UuidAndTitleCache<TaxonNode>> list = new ArrayList<UuidAndTitleCache<TaxonNode>>(result.size());

             for (Object object : result){

                 Object[] objectArray = (Object[]) object;

                 UUID uuid = (UUID)objectArray[0];
                 Integer id = (Integer) objectArray[1];
                 String titleCache = (String) objectArray[2];

                 list.add(new UuidAndTitleCache<TaxonNode>(TaxonNode.class, uuid, id, titleCache));
             }

             return list;
         }
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
        Query query = getSession().createQuery("from TaxonName t where t.nameCache IN (:taxonList)");
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

    //TODO: mal nur mit UUID probieren (ohne fetch all properties), vielleicht geht das schneller?
    @Override
    public List<UUID> findIdenticalTaxonNameIds(List<String> propertyPaths){
        Query query=getSession().createQuery("select tmb2 from ZoologicalName tmb, ZoologicalName tmb2 fetch all properties where tmb.id != tmb2.id and tmb.nameCache = tmb2.nameCache");
        List<UUID> zooNames = query.list();

        return zooNames;

    }

    @Override
    public List<TaxonName> findIdenticalTaxonNames(List<String> propertyPaths) {

        Query query=getSession().createQuery("select tmb2 from ZoologicalName tmb, ZoologicalName tmb2 fetch all properties where tmb.id != tmb2.id and tmb.nameCache = tmb2.nameCache");

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
        Query query = getSession().createQuery("Select id from Reference where titleCache like 'Fauna Europaea database'");
        List<String> secRefFauna = query.list();
        query = getSession().createQuery("Select id from Reference where titleCache like 'ERMS'");
        List<String> secRefErms = query.list();
        //Query query = getSession().createQuery("select tmb2.nameCache from ZoologicalName tmb, TaxonBase tb1, ZoologicalName tmb2, TaxonBase tb2 where tmb.id != tmb2.id and tb1.name = tmb and tb2.name = tmb2 and tmb.nameCache = tmb2.nameCache and tb1.sec != tb2.sec");
        //Get all names of fauna europaea
        query = getSession().createQuery("select zn.nameCache from ZoologicalName zn, TaxonBase tb where tb.name = zn and tb.sec.id = :secRefFauna");
        query.setParameter("secRefFauna", secRefFauna.get(0));
        List<String> namesFauna= query.list();

        //Get all names of erms

        query = getSession().createQuery("select zn.nameCache from ZoologicalName zn, TaxonBase tb where tb.name = zn and tb.sec.id = :secRefErms");
        query.setParameter("secRefErms", secRefErms.get(0));

        List<String> namesErms = query.list();
        /*TaxonNameComparator comp = new TaxonNameComparator();
        Collections.sort(namesFauna);
        Collections.sort(namesErms);
        */
        List <String> identicalNames = new ArrayList<String>();
        String predecessor = "";

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

    private String[] createHQLString(boolean doTaxa, boolean doSynonyms, boolean doIncludeMisappliedNames, Classification classification,  Set<NamedArea> areasExpanded, MatchMode matchMode, String searchField){

           boolean doAreaRestriction = areasExpanded.size() > 0;
           String 	doAreaRestrictionSubSelect = "select %s.id from" +
                " Distribution e" +
                " join e.inDescription d" +
                " join d.taxon t" +
                (classification != null ? " join t.taxonNodes as tn " : " ");

           String 	doAreaRestrictionMisappliedNameSubSelect = "select %s.id from" +
            " Distribution e" +
            " join e.inDescription d" +
            " join d.taxon t";

           String doTaxonSubSelect = "select %s.id from Taxon t " + (classification != null ? " join t.taxonNodes as tn " : " ");
           String doTaxonMisappliedNameSubSelect = "select %s.id from Taxon t ";

           String doTaxonNameJoin =   " join t.name n ";

           String doSynonymNameJoin =  	" join t.synonyms s join s.name sn";

           String doMisappliedNamesJoin = " left join t.relationsFromThisTaxon as rft" +
                " left join rft.relatedTo as rt" +
                (classification != null ? " left join rt.taxonNodes as tn2" : " ") +
                " left join rt.name as n2" +
                " left join rft.type as rtype";

           String doCommonNamesJoin =   "join t.descriptions as description "+
                   "left join description.descriptionElements as com " +
                   "left join com.feature f ";


           String doClassificationWhere = " tn.classification = :classification";
           String doClassificationForMisappliedNamesWhere = " tn2 .classification = :classification";

           String doAreaRestrictionWhere =  " e.area.uuid in (:namedAreasUuids)";
           String doCommonNamesRestrictionWhere = " (f.supportsCommonTaxonName = true and com.name "+matchMode.getMatchOperator()+" :queryString )";

           String doSearchFieldWhere = "%s." + searchField +  " " + matchMode.getMatchOperator() + " :queryString";

           String doRelationshipTypeComparison = " rtype = :rType ";

        String taxonSubselect = null;
        String synonymSubselect = null;
        String misappliedSelect = null;
        String commonNameSubselect = null;

        if(classification != null ){
            if (!doIncludeMisappliedNames){
                if(doAreaRestriction){
                    taxonSubselect = String.format(doAreaRestrictionSubSelect, "t") + doTaxonNameJoin +
                    " WHERE " + doAreaRestrictionWhere +
                    " AND " + doClassificationWhere +
                    " AND " + String.format(doSearchFieldWhere, "n");
                    synonymSubselect = String.format(doAreaRestrictionSubSelect, "s") + doSynonymNameJoin +
                    " WHERE " + doAreaRestrictionWhere +
                    " AND " + doClassificationWhere +
                    " AND " + String.format(doSearchFieldWhere, "sn");
                    commonNameSubselect =  String.format(doAreaRestrictionSubSelect, "t") + doCommonNamesJoin +
                            " WHERE " +  doAreaRestrictionWhere + " AND " + doClassificationWhere +
                            " AND " + String.format(doSearchFieldWhere, "n")
                            + " AND " + doCommonNamesRestrictionWhere;
                } else {
                    taxonSubselect = String.format(doTaxonSubSelect, "t" )+ doTaxonNameJoin +
                    " WHERE " + doClassificationWhere +
                    " AND " + String.format(doSearchFieldWhere, "n");
                    synonymSubselect = String.format(doTaxonSubSelect, "s" ) + doSynonymNameJoin +
                    " WHERE " + doClassificationWhere +
                    " AND " + String.format(doSearchFieldWhere, "sn");
                    commonNameSubselect =String.format(doTaxonSubSelect, "t" )+ doCommonNamesJoin +
                            " WHERE " + doClassificationWhere +
                            " AND " + doCommonNamesRestrictionWhere;
                }
            }else{ //misappliedNames included
                if(doAreaRestriction){
                    misappliedSelect = String.format(doAreaRestrictionMisappliedNameSubSelect, "t") + doTaxonNameJoin + doMisappliedNamesJoin  +
                    " WHERE " + doAreaRestrictionWhere +
                    " AND " + String.format(doSearchFieldWhere, "n") +
                    " AND " + doClassificationForMisappliedNamesWhere +
                    " AND " + doRelationshipTypeComparison;

                    taxonSubselect = String.format(doAreaRestrictionSubSelect, "t") + doTaxonNameJoin +
                    " WHERE " + doAreaRestrictionWhere +
                    " AND "+ String.format(doSearchFieldWhere, "n") + " AND "+ doClassificationWhere;

                    synonymSubselect = String.format(doAreaRestrictionSubSelect, "s") + doSynonymNameJoin +
                    " WHERE " + doAreaRestrictionWhere +
                    " AND " + doClassificationWhere + " AND " +  String.format(doSearchFieldWhere, "sn");

                    commonNameSubselect= String.format(doAreaRestrictionSubSelect, "t")+ doCommonNamesJoin +
                            " WHERE " + doAreaRestrictionWhere +
                            " AND "+ doClassificationWhere + " AND " + doCommonNamesRestrictionWhere;
                } else {
                    misappliedSelect = String.format(doTaxonMisappliedNameSubSelect, "t" ) + doTaxonNameJoin + doMisappliedNamesJoin +
                    " WHERE " + String.format(doSearchFieldWhere, "n") +
                    " AND " + doClassificationForMisappliedNamesWhere +
                    " AND " + doRelationshipTypeComparison;

                    taxonSubselect = String.format(doTaxonSubSelect, "t" ) + doTaxonNameJoin +
                    " WHERE " +  String.format(doSearchFieldWhere, "n") +
                    " AND "+ doClassificationWhere;

                    synonymSubselect = String.format(doTaxonSubSelect, "s" ) + doSynonymNameJoin +
                    " WHERE " + doClassificationWhere +
                    " AND " +  String.format(doSearchFieldWhere, "sn");

                    commonNameSubselect= String.format(doTaxonSubSelect, "t")+ doCommonNamesJoin +
                            " WHERE " + doClassificationWhere + " AND " + doCommonNamesRestrictionWhere;

                }
            }
        } else {
            if(doAreaRestriction){
                misappliedSelect = String.format(doAreaRestrictionMisappliedNameSubSelect, "t") + doTaxonNameJoin + doMisappliedNamesJoin +
                " WHERE " + doAreaRestrictionWhere +
                " AND " + String.format(doSearchFieldWhere, "n")+
                " AND " + doRelationshipTypeComparison;

                taxonSubselect = String.format(doAreaRestrictionSubSelect, "t") + doTaxonNameJoin +
                " WHERE " + doAreaRestrictionWhere +
                " AND " + String.format(doSearchFieldWhere, "n");

                synonymSubselect = String.format(doAreaRestrictionSubSelect, "s") + doSynonymNameJoin +
                " WHERE " +   doAreaRestrictionWhere +
                " AND " +  String.format(doSearchFieldWhere, "sn");
                commonNameSubselect = String.format(doAreaRestrictionSubSelect, "t")+ doCommonNamesJoin +
                        " WHERE " + doAreaRestrictionWhere +
                        " AND " + doCommonNamesRestrictionWhere;


            } else {
                misappliedSelect = String.format(doTaxonMisappliedNameSubSelect, "t" ) + doTaxonNameJoin + doMisappliedNamesJoin + " WHERE " +  String.format(doSearchFieldWhere, "n") + " AND " + doRelationshipTypeComparison;
                taxonSubselect = String.format(doTaxonSubSelect, "t" ) + doTaxonNameJoin + " WHERE " +  String.format(doSearchFieldWhere, "n");
                synonymSubselect = String.format(doTaxonSubSelect, "s" ) + doSynonymNameJoin + " WHERE " +  String.format(doSearchFieldWhere, "sn");
                commonNameSubselect = String.format(doTaxonSubSelect, "t" ) +doCommonNamesJoin + " WHERE "+  doCommonNamesRestrictionWhere;

            }
        }
        String[] result = {misappliedSelect, taxonSubselect, synonymSubselect, commonNameSubselect};

        return result;
    }

	@Override
	public List<UuidAndTitleCache<IdentifiableEntity>> getTaxaByCommonNameForEditor(
			String titleSearchStringSqlized, Classification classification,
			MatchMode matchMode, Set namedAreas) {
	    List<Object> resultArray = new ArrayList<Object>();
		Query query = prepareTaxaByCommonName(titleSearchStringSqlized, classification, matchMode, namedAreas, null, null, false, true);
        if (query != null){
            resultArray = query.list();
            List<UuidAndTitleCache<IdentifiableEntity>> returnResult = new ArrayList<UuidAndTitleCache<IdentifiableEntity>>() ;
            Object[] result;
            for(int i = 0; i<resultArray.size();i++){
            	result = (Object[]) resultArray.get(i);
            	returnResult.add(new UuidAndTitleCache(Taxon.class, (UUID) result[0],(Integer)result[1], (String)result[2], new Boolean(result[4].toString()), null));
            }
            return returnResult;
        }

		return null;
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

		String queryString = "SELECT ids.type, ids.identifier, %s " +
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


}
