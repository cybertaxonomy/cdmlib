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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonNameComparator;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.QueryParseException;
import eu.etaxonomy.cdm.persistence.dao.hibernate.AlternativeSpellingSuggestionParser;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.name.ITaxonNameDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;


/**
 * @author a.mueller
 * @created 24.11.2008
 * @version 1.0
 */
@Repository
@Qualifier("taxonDaoHibernateImpl")
public class TaxonDaoHibernateImpl extends IdentifiableDaoBase<TaxonBase> implements ITaxonDao {
    private AlternativeSpellingSuggestionParser<TaxonBase> alternativeSpellingSuggestionParser;
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

    @Autowired(required = false)   //TODO switched of because it caused problems when starting CdmApplicationController
    public void setAlternativeSpellingSuggestionParser(AlternativeSpellingSuggestionParser<TaxonBase> alternativeSpellingSuggestionParser) {
        this.alternativeSpellingSuggestionParser = alternativeSpellingSuggestionParser;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getRootTaxa(eu.etaxonomy.cdm.model.reference.Reference)
     */
    public List<Taxon> getRootTaxa(Reference sec) {
        return getRootTaxa(sec, CdmFetch.FETCH_CHILDTAXA(), true, false);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getRootTaxa(eu.etaxonomy.cdm.model.name.Rank, eu.etaxonomy.cdm.model.reference.Reference, eu.etaxonomy.cdm.persistence.fetch.CdmFetch, java.lang.Boolean, java.lang.Boolean)
     */
    public List<Taxon> getRootTaxa(Rank rank, Reference sec, CdmFetch cdmFetch, Boolean onlyWithChildren, Boolean withMisapplications, List<String> propertyPaths) {
        checkNotInPriorView("TaxonDaoHibernateImpl.getRootTaxa(Rank rank, Reference sec, CdmFetch cdmFetch, Boolean onlyWithChildren, Boolean withMisapplications)");
        if (onlyWithChildren == null){
            onlyWithChildren = true;
        }
        if (withMisapplications == null){
            withMisapplications = true;
        }
        if (cdmFetch == null){
            cdmFetch = CdmFetch.NO_FETCH();
        }

        Criteria crit = getSession().createCriteria(Taxon.class);

        crit.setFetchMode("name", FetchMode.JOIN);
        crit.createAlias("name", "name");

        if (rank != null) {
            crit.add(Restrictions.eq("name.rank", rank));
        }else{
            crit.add(Restrictions.isNull("taxonomicParentCache"));
        }

        if (sec != null){
            crit.add(Restrictions.eq("sec", sec) );
        }

        if (! cdmFetch.includes(CdmFetch.FETCH_CHILDTAXA())){
            logger.info("Not fetching child taxa");
            //TODO overwrite LAZY (SELECT) does not work (bug in hibernate?)
            crit.setFetchMode("relationsToThisTaxon.fromTaxon", FetchMode.LAZY);
        }

        List<Taxon> results = new ArrayList<Taxon>();
        List<Taxon> taxa = crit.list();
        for(Taxon taxon : taxa){


            //childTaxa
            //TODO create restriction instead
            // (a) not using cache fields
            /*Hibernate.initialize(taxon.getRelationsFromThisTaxon());
            if (onlyWithChildren == false || taxon.getRelationsFromThisTaxon().size() > 0){
                if (withMisapplications == true || ! taxon.isMisappliedName()){
                    defaultBeanInitializer.initialize(taxon, propertyPaths);
                    results.add(taxon);
                }
            }*/
            // (b) using cache fields
            if (onlyWithChildren == false || taxon.hasTaxonomicChildren()){
                if (withMisapplications == true || ! taxon.isMisapplication()){
                    defaultBeanInitializer.initialize(taxon, propertyPaths);
                    results.add(taxon);
                }
            }
        }
        return results;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getRootTaxa(eu.etaxonomy.cdm.model.reference.Reference, eu.etaxonomy.cdm.persistence.fetch.CdmFetch, java.lang.Boolean, java.lang.Boolean)
     */
    public List<Taxon> getRootTaxa(Reference sec, CdmFetch cdmFetch, Boolean onlyWithChildren, Boolean withMisapplications) {
        return getRootTaxa(null, sec, cdmFetch, onlyWithChildren, withMisapplications, null);
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.Reference)
     */
    public List<TaxonBase> getTaxaByName(String queryString, Reference sec) {

        return getTaxaByName(queryString, true, sec);
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getTaxaByName(java.lang.String, java.lang.Boolean, eu.etaxonomy.cdm.model.reference.Reference)
     */
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

        return (List<TaxonBase>)criteria.list();
    }

    public List<TaxonBase> getTaxaByName(Class<? extends TaxonBase> clazz, String queryString, MatchMode matchMode,
            Integer pageSize, Integer pageNumber) {

        return getTaxaByName(clazz, queryString, null, matchMode, null, pageSize, pageNumber, null);
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getTaxaByName(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, java.lang.Boolean, java.lang.Integer, java.lang.Integer)
     */
    public List<TaxonBase> getTaxaByName(String queryString, MatchMode matchMode,
            Boolean accepted, Integer pageSize, Integer pageNumber) {

        if (accepted == true) {
            return getTaxaByName(Taxon.class, queryString, matchMode, pageSize, pageNumber);
        } else {
            return getTaxaByName(Synonym.class, queryString, matchMode, pageSize, pageNumber);
        }
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getTaxaByName(java.lang.Class, java.lang.String, eu.etaxonomy.cdm.model.taxon.Classification, eu.etaxonomy.cdm.persistence.query.MatchMode, java.util.Set, java.lang.Integer, java.lang.Integer, java.util.List)
     */
    public List<TaxonBase> getTaxaByName(Class<? extends TaxonBase> clazz, String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths) {

        boolean doCount = false;

        Query query = prepareTaxaByName(clazz, "nameCache", queryString, classification, matchMode, namedAreas, pageSize, pageNumber, doCount, false);

        if (query != null){
            List<TaxonBase> results = query.list();

            defaultBeanInitializer.initializeAll(results, propertyPaths);
            //TaxonComparatorSearch comp = new TaxonComparatorSearch();
            //Collections.sort(results, comp);
            return results;
        }

        return new ArrayList<TaxonBase>();

    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getTaxaByName(java.lang.Class, java.lang.String, eu.etaxonomy.cdm.model.taxon.Classification, eu.etaxonomy.cdm.persistence.query.MatchMode, java.util.Set, java.lang.Integer, java.lang.Integer, java.util.List)
     */
    //new search for the editor, for performance issues the return values are only uuid and titleCache, to avoid the initialisation of all objects
    @SuppressWarnings("unchecked")
    public List<UuidAndTitleCache<TaxonBase>> getTaxaByNameForEditor(Class<? extends TaxonBase> clazz, String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas) {
        long zstVorher;
        long zstNachher;

        boolean doCount = false;
        Query query = prepareTaxaByNameForEditor(clazz, "nameCache", queryString, classification, matchMode, namedAreas, doCount);


        if (query != null){
            List<Object[]> results = query.list();

            List<UuidAndTitleCache<TaxonBase>> resultObjects = new ArrayList<UuidAndTitleCache<TaxonBase>>();
            Object[] result;
            for(int i = 0; i<results.size();i++){
                result = results.get(i);

                //differentiate taxa and synonyms
                if (clazz.equals(Taxon.class)){
                        resultObjects.add( new UuidAndTitleCache(Taxon.class, (UUID) result[0], (String)result[1]));
                }else if (clazz.equals(Synonym.class)){
                    resultObjects.add( new UuidAndTitleCache(Synonym.class, (UUID) result[0], (String)result[1]));
                } else{
                    if (result[2].equals("synonym")) {
                        resultObjects.add( new UuidAndTitleCache(Synonym.class, (UUID) result[0], (String)result[1]));
                    }
                    else {
                        resultObjects.add( new UuidAndTitleCache(Taxon.class, (UUID) result[0], (String)result[1]));
                    }
                }
            }

            return resultObjects;

        }
        return new ArrayList<UuidAndTitleCache<TaxonBase>>();

    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getTaxaByCommonName(java.lang.String, eu.etaxonomy.cdm.model.taxon.Classification, eu.etaxonomy.cdm.persistence.query.MatchMode, java.util.Set, java.lang.Integer, java.lang.Integer, java.util.List)
     */
    public List<TaxonBase> getTaxaByCommonName(String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths) {
            boolean doCount = false;
            Query query = prepareTaxaByCommonName(queryString, classification, matchMode, namedAreas, pageSize, pageNumber, doCount);
            if (query != null){
                List<TaxonBase> results = query.list();
                defaultBeanInitializer.initializeAll(results, propertyPaths);
                return results;
            }
            return new ArrayList<TaxonBase>();

    }

    /**
     * @param clazz
     * @param searchField the field in TaxonNameBase to be searched through usually either <code>nameCache</code> or <code>titleCache</code>
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
    private Query prepareTaxaByNameForEditor(Class<? extends TaxonBase> clazz, String searchField, String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, boolean doCount) {
        return prepareQuery(clazz, searchField, queryString, classification,
                matchMode, namedAreas, doCount, true, false);
    }

    /**
     * @param clazz
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
     * @return
     */
    private Query prepareQuery(Class<? extends TaxonBase> clazz, String searchField, String queryString, Classification classification,
                MatchMode matchMode, Set<NamedArea> namedAreas, boolean doCount, boolean doNotReturnFullEntities, boolean doIncludeMisappliedNames){

            String hqlQueryString = matchMode.queryStringFrom(queryString);
            String selectWhat;
            if (doNotReturnFullEntities){
                selectWhat = "t.uuid, t.titleCache ";
            }else {
                selectWhat = (doCount ? "count(t)": "t");
            }

            String hql = "";
            Set<NamedArea> areasExpanded = new HashSet<NamedArea>();
            if(namedAreas != null && namedAreas.size() > 0){
                // expand areas and restrict by distribution area
                List<NamedArea> childAreas;
                Query areaQuery = getSession().createQuery("select childArea from NamedArea as childArea left join childArea.partOf as parentArea where parentArea = :area");
                expandNamedAreas(namedAreas, areasExpanded, areaQuery);
            }
            boolean doAreaRestriction = areasExpanded.size() > 0;

            Set<UUID> namedAreasUuids = new HashSet<UUID>();
            for (NamedArea area:areasExpanded){
                namedAreasUuids.add(area.getUuid());
            }

            String taxonSubselect = null;
            String synonymSubselect = null;

            if(classification != null ){
                if (!doIncludeMisappliedNames){
                    if(doAreaRestriction){

                        taxonSubselect = "select t.id from" +
                            " Distribution e" +
                            " join e.inDescription d" +
                            " join d.taxon t" +
                            " join t.name n " +
                            " join t.taxonNodes as tn "+
                            " where" +
                            " e.area.uuid in (:namedAreasUuids) AND" +
                            " tn.classification = :classification" +
                            " AND n." + searchField +  " " + matchMode.getMatchOperator() + " :queryString";



                        synonymSubselect = "select s.id from" +
                            " Distribution e" +
                            " join e.inDescription d" +
                            " join d.taxon t" + // the taxa
                            " join t.taxonNodes as tn "+
                            " join t.synonymRelations sr" +
                            " join sr.relatedFrom s" + // the synonyms
                            " join s.name sn"+
                            " where" +
                            " e.area.uuid in (:namedAreasUuids) AND" +
                            " tn.classification = :classification" +
                            " AND sn." + searchField +  " " + matchMode.getMatchOperator() + " :queryString";

                    } else {

                        taxonSubselect = "select t.id from" +
                            " Taxon t" +
                            " join t.name n " +
                            " join t.taxonNodes as tn "+
                            " where" +
                            " tn.classification = :classification" +
                            " AND n." + searchField +  " " + matchMode.getMatchOperator() + " :queryString";

                        synonymSubselect = "select s.id from" +
                            " Taxon t" + // the taxa
                            " join t.taxonNodes as tn "+
                            " join t.synonymRelations sr" +
                            " join sr.relatedFrom s" + // the synonyms
                            " join s.name sn"+
                            " where" +
                            " tn.classification = :classification" +
                            " AND sn." + searchField +  " " + matchMode.getMatchOperator() + " :queryString";
                    }
                }else{
                    if(doAreaRestriction){

                        taxonSubselect = "select t.id from" +
                            " Distribution e" +
                            " join e.inDescription d" +
                            " join d.taxon t" +
                            " join t.name n " +
                            " join t.taxonNodes as tn "+
                            " left join t.relationsFromThisTaxon as rft" +
                            " left join rft.relatedTo as rt" +
                            " left join rt.taxonNodes as tn2" +
                            " left join rt.name as n2" +
                            " left join rft.type as rtype"+
                            " where" +
                            " e.area.uuid in (:namedAreasUuids) AND" +
                            " (tn.classification = :classification" +
                            " AND n." + searchField + " " + matchMode.getMatchOperator() + " :queryString )" +
                            " OR"+
                            " (tn.classification != :classification" +
                            " AND n." + searchField + " " + matchMode.getMatchOperator() + " :queryString" +
                            " AND tn2.classification = :classification" +
                            " AND rtype = :rType )";


                        synonymSubselect = "select s.id from" +
                            " Distribution e" +
                            " join e.inDescription d" +
                            " join d.taxon t" + // the taxa
                            " join t.taxonNodes as tn "+
                            " join t.synonymRelations sr" +
                            " join sr.relatedFrom s" + // the synonyms
                            " join s.name sn"+
                            " where" +
                            " e.area.uuid in (:namedAreasUuids) AND" +
                            " tn.classification != :classification" +
                            " AND sn." + searchField +  " " + matchMode.getMatchOperator() + " :queryString";

                    } else {

                        taxonSubselect = "select t.id from" +
                            " Taxon t" +
                            " join t.name n " +
                            " join t.taxonNodes as tn "+
                            " left join t.relationsFromThisTaxon as rft" +
                            " left join rft.relatedTo as rt" +
                            " left join rt.taxonNodes as tn2" +
                            " left join rt.name as n2" +
                            " left join rft.type as rtype"+
                            " where " +
                            " (tn.classification = :classification" +
                            " AND n." + searchField + " " + matchMode.getMatchOperator() + " :queryString )" +
                            " OR"+
                            " (tn.classification != :classification" +
                            " AND n." + searchField + " " + matchMode.getMatchOperator() + " :queryString" +
                            " AND tn2.classification = :classification" +
                            " AND rtype = :rType )";

                        synonymSubselect = "select s.id from" +
                            " Taxon t" + // the taxa
                            " join t.taxonNodes as tn "+
                            " join t.synonymRelations sr" +
                            " join sr.relatedFrom s" + // the synonyms
                            " join s.name sn"+
                            " where" +
                            " tn.classification != :classification" +
                            " AND sn." + searchField +  " " + matchMode.getMatchOperator() + " :queryString";
                    }
                }
            } else {

                if(doAreaRestriction){

                    taxonSubselect = "select t.id from " +
                        " Distribution e" +
                        " join e.inDescription d" +
                        " join d.taxon t" +
                        " join t.name n "+
                        " where" +
                        (doAreaRestriction ? " e.area.uuid in (:namedAreasUuids) AND" : "") +
                        " n." + searchField +  " " + matchMode.getMatchOperator() + " :queryString";

                    synonymSubselect = "select s.id from" +
                        " Distribution e" +
                        " join e.inDescription d" +
                        " join d.taxon t" + // the taxa
                        " join t.synonymRelations sr" +
                        " join sr.relatedFrom s" + // the synonyms
                        " join s.name sn"+
                        " where" +
                        (doAreaRestriction ? " e.area.uuid in (:namedAreasUuids) AND" : "") +
                        " sn." + searchField +  " " + matchMode.getMatchOperator() + " :queryString";

                } else {

                    taxonSubselect = "select t.id from " +
                        " Taxon t" +
                        " join t.name n "+
                        " where" +
                        " n." + searchField +  " " + matchMode.getMatchOperator() + " :queryString";

                    synonymSubselect = "select s.id from" +
                        " Taxon t" + // the taxa
                        " join t.synonymRelations sr" +
                        " join sr.relatedFrom s" + // the synonyms
                        " join s.name sn"+
                        " where" +
                        " sn." + searchField +  " " + matchMode.getMatchOperator() + " :queryString";
                }

            }

            Query subTaxon = null;
            Query subSynonym = null;
            if(clazz.equals(Taxon.class)){
                // find Taxa
                logger.debug("taxonSubselect:" + taxonSubselect);
                subTaxon = getSession().createQuery(taxonSubselect).setParameter("queryString", hqlQueryString);

                //subTaxon = getSession().createQuery(taxonSubselect);

                if(doAreaRestriction){
                    subTaxon.setParameterList("namedAreasUuids", namedAreasUuids);
                }
                if(classification != null){
                    subTaxon.setParameter("classification", classification);
                    if (doIncludeMisappliedNames){
                        subTaxon.setParameter("rType", TaxonRelationshipType.MISAPPLIED_NAME_FOR());
                    }
                }
            } else if(clazz.equals(Synonym.class)){
                // find synonyms
                subSynonym = getSession().createQuery(synonymSubselect).setParameter("queryString", hqlQueryString);

                if(doAreaRestriction){
                    subSynonym.setParameterList("namedAreasUuids", namedAreasUuids);
                }
                if(classification != null){
                    subSynonym.setParameter("classification", classification);
                }
            } else {
                // find taxa and synonyms
                subSynonym = getSession().createQuery(synonymSubselect).setParameter("queryString", hqlQueryString);
                subTaxon = getSession().createQuery(taxonSubselect).setParameter("queryString", hqlQueryString);
                if(doAreaRestriction){
                    subTaxon.setParameterList("namedAreasUuids", namedAreasUuids);
                    subSynonym.setParameterList("namedAreasUuids", namedAreasUuids);
                }
                if(classification != null){
                    subTaxon.setParameter("classification", classification);
                    subSynonym.setParameter("classification", classification);
                }
            }

            List<Integer> taxa = new ArrayList<Integer>();
            List<Integer> synonyms = new ArrayList<Integer>();
            if(clazz.equals(Taxon.class)){
                taxa = subTaxon.list();

            }else if (clazz.equals(Synonym.class)){
                synonyms = subSynonym.list();
            }else {
                taxa = subTaxon.list();
                synonyms = subSynonym.list();
            }
            if(clazz.equals(Taxon.class)){
                if  (taxa.size()>0){
                    if (doNotReturnFullEntities){
                        hql = "select " + selectWhat + ", 'taxon' from " + clazz.getSimpleName() + " t" + " where t.id in (:taxa)";
                    }else{
                        hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t" + " where t.id in (:taxa)";
                    }
                }else{
                    hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t";
                }
            } else if(clazz.equals(Synonym.class) ){
                if (synonyms.size()>0){
                    if (doNotReturnFullEntities){
                        hql = "select " + selectWhat + ", 'synonym' from " + clazz.getSimpleName() + " t" + " where t.id in (:synonyms)";
                    }else{
                        hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t" + " where t.id in (:synonyms)";
                    }
                }else{
                    hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t";
                }
            } else {

                if(synonyms.size()>0 && taxa.size()>0){
                    if (doNotReturnFullEntities &&  !doCount ){
                        // in doNotReturnFullEntities mode it is nesscary to also return the type of the matching entities:
                        hql = "select " + selectWhat + ", case when t.id in (:taxa) then 'taxon' else 'synonym' end" + " from " + clazz.getSimpleName() + " t" + " where t.id in (:taxa) OR t.id in (:synonyms)";
                    }else{
                        hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t" + " where t.id in (:taxa) OR t.id in (:synonyms)";
                    }
                }else if (synonyms.size()>0 ){
                    if (doNotReturnFullEntities &&  !doCount ){
                        // in doNotReturnFullEntities mode it is nesscary to also return the type of the matching entities:
                        hql = "select " + selectWhat + ", 'synonym' from " + clazz.getSimpleName() + " t" + " where t.id in (:synonyms)";
                    } else {
                        hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t" + " where t.id in (:synonyms)";
                    }
                } else if (taxa.size()>0 ){
                    if (doNotReturnFullEntities &&  !doCount ){
                        // in doNotReturnFullEntities mode it is nesscary to also return the type of the matching entities:
                        hql = "select " + selectWhat + ", 'taxon' from " + clazz.getSimpleName() + " t" + " where t.id in (:taxa) ";
                    } else {
                        hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t" + " where t.id in (:taxa) ";
                    }
                } else{
                    hql = "select " + selectWhat + " from " + clazz.getSimpleName() + " t";
                }
            }

            if (hql == "") return null;
            if(!doCount){
                hql += " order by t.name.genusOrUninomial, case when t.name.specificEpithet like '\"%\"' then 1 else 0 end, t.name.specificEpithet, t.name.rank desc, t.name.nameCache";
            }

            Query query = getSession().createQuery(hql);

            if(clazz.equals(Taxon.class) && taxa.size()>0){
                //find taxa
                query.setParameterList("taxa", taxa );
            } else if(clazz.equals(Synonym.class) && synonyms.size()>0){
                // find synonyms
                query.setParameterList("synonyms", synonyms);


            } else {
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
            return query;


    }


    /**
     * @param clazz
     * @param searchField the field in TaxonNameBase to be searched through usually either <code>nameCache</code> or <code>titleCache</code>
     * @param queryString
     * @param classification TODO
     * @param matchMode
     * @param namedAreas
     * @param pageSize
     * @param pageNumber
     * @param doCount
     * @return
     *
     * FIXME implement classification restriction & implement test: see {@link TaxonDaoHibernateImplTest#testCountTaxaByName()}
     */
    private Query prepareTaxaByName(Class<? extends TaxonBase> clazz, String searchField, String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize, Integer pageNumber, boolean doCount, boolean doIncludeMisappliedNames) {

        Query query = prepareQuery(clazz, searchField, queryString, classification,	matchMode, namedAreas, doCount, false, doIncludeMisappliedNames);

        if(pageSize != null &&  !doCount) {
            query.setMaxResults(pageSize);
            if(pageNumber != null) {
                query.setFirstResult(pageNumber * pageSize);
            }
        }

        return query;
    }

    private Query prepareTaxaByCommonName(String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize, Integer pageNumber, boolean doCount){

        String hql= "from Taxon t " +
        "join t.descriptions d "+
        "join d.descriptionElements e " +
        "join e.feature f " +
        "where f.supportsCommonTaxonName = true and e.name "+matchMode.getMatchOperator()+" :queryString";//and ls.text like 'common%'";

        Query query = getSession().createQuery(hql);

        query.setParameter("queryString", queryString);

        if(pageSize != null &&  !doCount) {
            query.setMaxResults(pageSize);
            if(pageNumber != null) {
                query.setFirstResult(pageNumber * pageSize);
            }
        }
        return query;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countTaxaByName(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, eu.etaxonomy.cdm.persistence.query.SelectMode, eu.etaxonomy.cdm.model.reference.Reference, java.util.Set)
     */
    public long countTaxaByName(Class<? extends TaxonBase> clazz, String queryString, Classification classification,
        MatchMode matchMode, Set<NamedArea> namedAreas) {

        boolean doCount = true;
        Query query = prepareTaxaByName(clazz, "nameCache", queryString, classification, matchMode, namedAreas, null, null, doCount, false);
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

//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countTaxaByName(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, eu.etaxonomy.cdm.persistence.query.SelectMode)
//	 */
//	public Integer countTaxaByName(String queryString, MatchMode matchMode, SelectMode selectMode) {
//		return countTaxaByName(queryString, matchMode, selectMode, null);
//	}

//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countTaxaByName(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, eu.etaxonomy.cdm.persistence.query.SelectMode, eu.etaxonomy.cdm.model.reference.Reference)
//	 */
//	public Integer countTaxaByName(String queryString,
//			MatchMode matchMode, SelectMode selectMode, Reference sec) {
//
//		Long count = countTaxaByName(queryString, matchMode, selectMode, sec, null);
//		return count.intValue();
//
//	}

//	public Integer countTaxaByName(String queryString, MatchMode matchMode, Boolean accepted) {
//
//		SelectMode selectMode = (accepted ? SelectMode.TAXA : SelectMode.SYNONYMS);
//		Long count = countTaxaByName(queryString, matchMode, selectMode, null, null);
//		return count.intValue();
//	}

    public List<TaxonBase> getAllTaxonBases(Integer pagesize, Integer page) {
        return super.list(pagesize, page);
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getAllSynonyms(java.lang.Integer, java.lang.Integer)
     */
    public List<Synonym> getAllSynonyms(Integer limit, Integer start) {
        Criteria criteria = getSession().createCriteria(Synonym.class);

        if(limit != null) {
            criteria.setFirstResult(start);
            criteria.setMaxResults(limit);
        }

        return criteria.list();
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getAllTaxa(java.lang.Integer, java.lang.Integer)
     */
    public List<Taxon> getAllTaxa(Integer limit, Integer start) {
        Criteria criteria = getSession().createCriteria(Taxon.class);

        if(limit != null) {
            criteria.setFirstResult(start);
            criteria.setMaxResults(limit);
        }

        return criteria.list();
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getAllRelationships(java.lang.Integer, java.lang.Integer)
     */
    @Override
    public List<RelationshipBase> getAllRelationships(/*Class<? extends RelationshipBase> clazz,*/ Integer limit, Integer start) {
        Class<? extends RelationshipBase> clazz = RelationshipBase.class;  //preliminary, see #2653
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(clazz);
            criteria.setFirstResult(start);
            if (limit != null){
                criteria.setMaxResults(limit);
            }
            return (List<RelationshipBase>)criteria.list();
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(clazz,auditEvent.getRevisionNumber());
            return (List<RelationshipBase>)query.getResultList();
        }
    }

    /** Sets the taxonomic parent to null. Does not handle taxonomic relationships. */
//	private boolean nullifyTaxonomicParent(Taxon taxon) {
//
//		try {
//			Method nullifyTaxonomicParent = taxon.getClass().getMethod("nullifyTaxonomicParent");
//			nullifyTaxonomicParent.invoke(taxon);
//		} catch (NoSuchMethodException ex) {
//			logger.error("NoSuchMethod: " + ex.getMessage());
//			return false;
//		} catch (IllegalArgumentException ex) {
//			logger.error("IllegalArgumentException: " + ex.getMessage());
//			return false;
//		} catch (IllegalAccessException ex) {
//			logger.error("IllegalAccessException: " + ex.getMessage());
//			return false;
//		} catch (InvocationTargetException ex) {
//			logger.error("IllegalAccessException: " + ex.getMessage());
//			return false;
//		}
//		return true;
//	}

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
        getSession().merge(taxonBase);

        if (taxonBase instanceof Taxon){ //	is Taxon
            for (Iterator<TaxonRelationship> iterator = ((Taxon)taxonBase).getRelationsFromThisTaxon().iterator(); iterator.hasNext();){
                TaxonRelationship relationFromThisTaxon = iterator.next();

                // decrease children count of taxonomic parent by one
                if (relationFromThisTaxon.getType().equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {
                    Taxon toTaxon = relationFromThisTaxon.getToTaxon(); // parent
                    if (toTaxon != null) {
                        toTaxon.setTaxonomicChildrenCount(toTaxon.getTaxonomicChildrenCount() - 1);
                    }
                }
            }
        }

        return super.delete(taxonBase);
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#findByName(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, int, int, boolean)
     */
    public List<TaxonBase> findByNameTitleCache(Class<? extends TaxonBase>clazz, String queryString, Classification classification, MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageNumber, Integer pageSize, List<String> propertyPaths) {

        boolean doCount = false;
        Query query = prepareTaxaByName(clazz, "titleCache", queryString, classification, matchMode, namedAreas, pageSize, pageNumber, doCount, false);
        if (query != null){
            List<TaxonBase> results = query.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);
            return results;
        }
        return new ArrayList<TaxonBase>();

    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countMatchesByName(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, boolean)
     */
    public int countMatchesByName(String queryString, MatchMode matchMode, boolean onlyAcccepted) {
        checkNotInPriorView("TaxonDaoHibernateImpl.countMatchesByName(String queryString, ITitledDao.MATCH_MODE matchMode, boolean onlyAcccepted)");
        Criteria crit = getSession().createCriteria(type);
        crit.add(Restrictions.ilike("titleCache", matchMode.queryStringFrom(queryString)));
        crit.setProjection(Projections.rowCount());
        int result = ((Integer)crit.list().get(0)).intValue();
        return result;
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countMatchesByName(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, boolean, java.util.List)
     */
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
        int result = ((Integer)crit.list().get(0)).intValue();
        return result;
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, eu.etaxonomy.cdm.model.common.RelationshipBase.Direction)
     */
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
            query.addProjection(AuditEntity.id().count("id"));

            if(type != null) {
                query.add(AuditEntity.relatedId("type").eq(type.getId()));
            }

            return ((Long)query.getSingleResult()).intValue();
        }
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countSynonyms(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType)
     */
    public int countSynonyms(Taxon taxon, SynonymRelationshipType type) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(SynonymRelationship.class);

            criteria.add(Restrictions.eq("relatedTo", taxon));
            if(type != null) {
                criteria.add(Restrictions.eq("type", type));
            }
            criteria.setProjection(Projections.rowCount());
            return (Integer)criteria.uniqueResult();
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(SynonymRelationship.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId("relatedTo").eq(taxon.getId()));
            query.addProjection(AuditEntity.id().count("id"));

            if(type != null) {
                query.add(AuditEntity.relatedId("type").eq(type.getId()));
            }

            return ((Long)query.getSingleResult()).intValue();
        }
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countSynonyms(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType)
     */
    public int countSynonyms(Synonym synonym, SynonymRelationshipType type) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(SynonymRelationship.class);

            criteria.add(Restrictions.eq("relatedFrom", synonym));
            if(type != null) {
                criteria.add(Restrictions.eq("type", type));
            }

            criteria.setProjection(Projections.rowCount());
            return (Integer)criteria.uniqueResult();
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(SynonymRelationship.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId("relatedFrom").eq(synonym.getId()));
            query.addProjection(AuditEntity.id().count("id"));

            if(type != null) {
                query.add(AuditEntity.relatedId("type").eq(type.getId()));
            }

            return ((Long)query.getSingleResult()).intValue();
        }
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countTaxaByName(java.lang.Class, java.lang.String, java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.name.Rank)
     */
    public int countTaxaByName(Class<? extends TaxonBase> clazz, String genusOrUninomial, String infraGenericEpithet, String specificEpithet,	String infraSpecificEpithet, Rank rank) {
        checkNotInPriorView("TaxonDaoHibernateImpl.countTaxaByName(Boolean accepted, String genusOrUninomial,	String infraGenericEpithet, String specificEpithet,	String infraSpecificEpithet, Rank rank)");
        Criteria criteria = null;

        if(clazz == null) {
            criteria = getSession().createCriteria(TaxonBase.class);
        } else {
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

        if(rank != null) {
            criteria.add(Restrictions.eq("name.rank", rank));
        }

        criteria.setProjection(Projections.projectionList().add(Projections.rowCount()));

        return (Integer)criteria.uniqueResult();
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#findTaxaByName(java.lang.Class, java.lang.String, java.lang.String, java.lang.String, java.lang.String, eu.etaxonomy.cdm.model.name.Rank, java.lang.Integer, java.lang.Integer)
     */
    public List<TaxonBase> findTaxaByName(Class<? extends TaxonBase> clazz, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, Rank rank, Integer pageSize,	Integer pageNumber) {
        checkNotInPriorView("TaxonDaoHibernateImpl.findTaxaByName(Boolean accepted, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, Rank rank, Integer pageSize,	Integer pageNumber)");
        Criteria criteria = null;

        if(clazz == null) {
            criteria = getSession().createCriteria(TaxonBase.class);
        } else {
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

        return (List<TaxonBase>)criteria.list();
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getTaxonRelationships(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List, eu.etaxonomy.cdm.model.common.RelationshipBase.Direction)
     */
    public List<TaxonRelationship> getTaxonRelationships(Taxon taxon,	TaxonRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths, Direction direction) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(TaxonRelationship.class);

            if(direction != null) {
                criteria.add(Restrictions.eq(direction.name(), taxon));
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

            List<TaxonRelationship> result = (List<TaxonRelationship>)criteria.list();
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

            List<TaxonRelationship> result = (List<TaxonRelationship>)query.getResultList();
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

        public int compare(TaxonRelationship o1, TaxonRelationship o2) {
            return o1.getFromTaxon().getTitleCache().compareTo(o2.getFromTaxon().getTitleCache());
        }

    }

    class SynonymRelationshipFromTaxonComparator implements Comparator<SynonymRelationship> {

        public int compare(SynonymRelationship o1, SynonymRelationship o2) {
            return o1.getSynonym().getTitleCache().compareTo(o2.getSynonym().getTitleCache());
        }

    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getSynonyms(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public List<SynonymRelationship> getSynonyms(Taxon taxon, SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(SynonymRelationship.class);

            criteria.add(Restrictions.eq("relatedTo", taxon));
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

            List<SynonymRelationship> result = (List<SynonymRelationship>)criteria.list();
            defaultBeanInitializer.initializeAll(result, propertyPaths);

            return result;
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(SynonymRelationship.class,auditEvent.getRevisionNumber());
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

            List<SynonymRelationship> result = (List<SynonymRelationship>)query.getResultList();
            defaultBeanInitializer.initializeAll(result, propertyPaths);

            return result;
        }
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getSynonyms(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    public List<SynonymRelationship> getSynonyms(Synonym synonym, SynonymRelationshipType type, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(SynonymRelationship.class);

            criteria.add(Restrictions.eq("relatedFrom", synonym));
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

            List<SynonymRelationship> result = (List<SynonymRelationship>)criteria.list();
            defaultBeanInitializer.initializeAll(result, propertyPaths);

            return result;
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(SynonymRelationship.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId("relatedFrom").eq(synonym.getId()));

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

            List<SynonymRelationship> result = (List<SynonymRelationship>)query.getResultList();
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
        checkNotInPriorView("TaxonDaoHibernateImpl.suggestQuery(String queryString)");
        String alternativeQueryString = null;
        if (alternativeSpellingSuggestionParser != null) {
            try {

                alternativeSpellingSuggestionParser.parse(queryString);
                org.apache.lucene.search.Query alternativeQuery = alternativeSpellingSuggestionParser.suggest(queryString);
                if (alternativeQuery != null) {
                    alternativeQueryString = alternativeQuery
                            .toString("name.titleCache");
                }

            } catch (ParseException e) {
                throw new QueryParseException(e, queryString);
            }
        }
        return alternativeQueryString;
    }

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.ITaxonService#getUuidAndTitleCacheOfAcceptedTaxa(eu.etaxonomy.cdm.model.taxon.Classification)
     */
    public List<UuidAndTitleCache<TaxonNode>> getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(Classification classification) {

        int classificationId = classification.getId();

        String queryString = "SELECT nodes.uuid, taxa.titleCache FROM TaxonNode AS nodes LEFT JOIN TaxonBase AS taxa ON nodes.taxon_id = taxa.id WHERE taxa.DTYPE = 'Taxon' AND nodes.classification_id = " + classificationId;

        List<Object[]> result = getSession().createSQLQuery(queryString).list();

        if(result.size() == 0){
            return null;
        }else{
            List<UuidAndTitleCache<TaxonNode>> list = new ArrayList<UuidAndTitleCache<TaxonNode>>(result.size());

            for (Object object : result){

                Object[] objectArray = (Object[]) object;

                UUID uuid = UUID.fromString((String) objectArray[0]);
                String titleCache = (String) objectArray[1];

                list.add(new UuidAndTitleCache(TaxonNode.class, uuid, titleCache));
            }

            return list;
        }
    }


    public class UuidAndTitleCacheOfAcceptedTaxon{
        UUID uuid;

        String titleCache;

        public UuidAndTitleCacheOfAcceptedTaxon(UUID uuid, String titleCache){
            this.uuid = uuid;
            this.titleCache = titleCache;
        }

        public UUID getUuid() {
            return uuid;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }

        public String getTitleCache() {
            return titleCache;
        }

        public void setTitleCache(String titleCache) {
            this.titleCache = titleCache;
        }
    }

    @Override
    public TaxonBase find(LSID lsid) {
        TaxonBase taxonBase = super.find(lsid);
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
            propertyPaths.add("synonymRelations");
            propertyPaths.add("synonymRelations.synonym");
            propertyPaths.add("synonymRelations.type");
            propertyPaths.add("descriptions");

            defaultBeanInitializer.initialize(taxonBase, propertyPaths);
        }
        return taxonBase;
    }

    public List<TaxonBase> getTaxaByCommonName(String queryString,
            Classification classification, MatchMode matchMode,
            Set<NamedArea> namedAreas, Integer pageSize, Integer pageNumber) {
        // TODO Auto-generated method stub
        return null;
    }


    public List<Synonym>  createAllInferredSynonyms(Taxon taxon, Classification tree){
        List <Synonym> inferredSynonyms = new ArrayList<Synonym>();

        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymRelationshipType.INFERRED_EPITHET_OF()));
        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymRelationshipType.INFERRED_GENUS_OF()));
        inferredSynonyms.addAll(createInferredSynonyms(taxon, tree, SynonymRelationshipType.POTENTIAL_COMBINATION_OF()));

        return inferredSynonyms;
    }


    /**
     * Returns an existing ZoologicalName or extends an internal hashmap if it does not exist.
     * Very likely only useful for createInferredSynonyms().
     * @param uuid
     * @param zooHashMap
     * @return
     */
    private ZoologicalName getZoologicalName(UUID uuid, HashMap <UUID, ZoologicalName> zooHashMap) {
        ZoologicalName taxonName = this.taxonNameDao.findZoologicalNameByUUID(uuid);
        if (taxonName == null) {
            taxonName = zooHashMap.get(uuid);
        }
        return taxonName;
    }

    public List<Synonym> createInferredSynonyms(Taxon taxon, Classification tree, SynonymRelationshipType type){
        List <Synonym> inferredSynonyms = new ArrayList<Synonym>();
        List<Synonym> inferredSynonymsToBeRemoved = new ArrayList<Synonym>();

        HashMap <UUID, ZoologicalName> zooHashMap = new HashMap<UUID, ZoologicalName>();
        UUID uuid;

        uuid= taxon.getName().getUuid();
        ZoologicalName taxonName = getZoologicalName(uuid, zooHashMap);
        String epithetOfTaxon = taxonName.getSpecificEpithet();
        String genusOfTaxon = taxonName.getGenusOrUninomial();
        Set<TaxonNode> nodes = taxon.getTaxonNodes();
         List<String> taxonNames = new ArrayList<String>();

        for (TaxonNode node: nodes){
            HashMap<String, String> synonymsGenus = new HashMap<String, String>(); // Changed this to be able to store the idInSource to a genusName
            List<String> synonymsEpithet = new ArrayList<String>();

            if (node.getClassification().equals(tree)){
                if (!node.isTopmostNode()){
                TaxonNode parent = (TaxonNode)node.getParent();
                parent = (TaxonNode)HibernateProxyHelper.deproxy(parent);
                TaxonNameBase parentName = parent.getTaxon().getName();
                parentName = (TaxonNameBase)HibernateProxyHelper.deproxy(parentName);

                //create inferred synonyms for species, subspecies or subgenus
                if (parentName.isGenus() || parentName.isSpecies() || parentName.getRank().equals(Rank.SUBGENUS())){

                    Synonym inferredEpithet;
                    Synonym inferredGenus = null;
                    Synonym potentialCombination;

                    List<String> propertyPaths = new ArrayList<String>();
                    propertyPaths.add("synonym");
                    propertyPaths.add("synonym.name");
                    List<OrderHint> orderHints = new ArrayList<OrderHint>();
                    orderHints.add(new OrderHint("relatedFrom.titleCache", SortOrder.ASCENDING));

                    List<SynonymRelationship> synonymRelationshipsOfGenus = getSynonyms(parent.getTaxon(), SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), null, null,orderHints,propertyPaths);
                    List<SynonymRelationship> synonymRelationshipsOfTaxon= getSynonyms(taxon, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF(), null, null,orderHints,propertyPaths);

                    if (type.equals(SynonymRelationshipType.INFERRED_EPITHET_OF())){

                        for (SynonymRelationship synonymRelationOfGenus:synonymRelationshipsOfGenus){
                            TaxonNameBase synName;
                            ZoologicalName inferredSynName;
                            Synonym syn = synonymRelationOfGenus.getSynonym();
                            HibernateProxyHelper.deproxy(syn);

                            // Determine the idInSource
                            String idInSource = getIdInSource(syn);

                            // Determine the sourceReference
                            Reference sourceReference = syn.getSec();

                            synName = syn.getName();
                            ZoologicalName zooName = getZoologicalName(synName.getUuid(), zooHashMap);
                            String synGenusName = zooName.getGenusOrUninomial();
                            if (synGenusName != null && !synonymsGenus.containsKey(synGenusName)){
                                synonymsGenus.put(synGenusName, idInSource);
                            }
                            inferredSynName = ZoologicalName.NewInstance(Rank.SPECIES());

                            // DEBUG
                            if (epithetOfTaxon == null) {
                                logger.error("This specificEpithet is NULL");
                            }

                            inferredSynName.setSpecificEpithet(epithetOfTaxon);
                            inferredSynName.setGenusOrUninomial(synGenusName);
                            inferredEpithet = Synonym.NewInstance(inferredSynName, null);

                            // Set the sourceReference
                            inferredEpithet.setSec(sourceReference);

                            // Add the original source
                            if (idInSource != null) {
                                IdentifiableSource originalSource = IdentifiableSource.NewInstance(idInSource, "InferredEpithetOf", syn.getSec(), null);

                                // Add the citation
                                Reference citation = getCitation(syn);
                                if (citation != null) {
                                    originalSource.setCitation(citation);
                                    inferredEpithet.addSource(originalSource);
                                }
                            }

                            taxon.addSynonym(inferredEpithet, SynonymRelationshipType.INFERRED_GENUS_OF());
                            inferredSynonyms.add(inferredEpithet);
                            inferredSynName.generateTitle();
                            zooHashMap.put(inferredSynName.getUuid(), inferredSynName);
                            taxonNames.add(inferredSynName.getNameCache());
                        }

                        if (!taxonNames.isEmpty()){
                        List<String> synNotInCDM = this.taxaByNameNotInDB(taxonNames);
                        ZoologicalName name;
                        if (!synNotInCDM.isEmpty()){
                            inferredSynonymsToBeRemoved.clear();

                            for (Synonym syn :inferredSynonyms){
                                name = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                                if (!synNotInCDM.contains(name.getNameCache())){
                                    inferredSynonymsToBeRemoved.add(syn);
                                }
                            }

                            // Remove identified Synonyms from inferredSynonyms
                            for (Synonym synonym : inferredSynonymsToBeRemoved) {
                                inferredSynonyms.remove(synonym);
                            }
                        }
                        }

                    }else if (type.equals(SynonymRelationshipType.INFERRED_GENUS_OF())){


                        for (SynonymRelationship synonymRelationOfTaxon:synonymRelationshipsOfTaxon){
                            TaxonNameBase synName;
                            ZoologicalName inferredSynName;

                            Synonym syn = synonymRelationOfTaxon.getSynonym();
                            synName =syn.getName();
                            HibernateProxyHelper.deproxy(syn);

                            // Determine the idInSource
                            String idInSource = getIdInSource(syn);

                            // Determine the sourceReference
                            Reference sourceReference = syn.getSec();

                            synName = syn.getName();
                            ZoologicalName zooName = getZoologicalName(synName.getUuid(), zooHashMap);
                            String speciesEpithetName = zooName.getSpecificEpithet();
                            if (synonymsEpithet != null && !synonymsEpithet.contains(speciesEpithetName)){
                                synonymsEpithet.add(speciesEpithetName);
                            }
                            inferredSynName = ZoologicalName.NewInstance(Rank.SPECIES());
                            inferredSynName.setSpecificEpithet(speciesEpithetName);
                            inferredSynName.setGenusOrUninomial(genusOfTaxon);
                            inferredGenus = Synonym.NewInstance(inferredSynName, null);

                            // Set the sourceReference
                            inferredGenus.setSec(sourceReference);

                            // Add the original source
                            if (idInSource != null) {
                                IdentifiableSource originalSource = IdentifiableSource.NewInstance(idInSource, "InferredGenusOf", syn.getSec(), null);

                                // Add the citation
                                Reference citation = getCitation(syn);
                                if (citation != null) {
                                    originalSource.setCitation(citation);
                                    inferredGenus.addSource(originalSource);
                                }
                            }

                            taxon.addSynonym(inferredGenus, SynonymRelationshipType.INFERRED_EPITHET_OF());
                            inferredSynonyms.add(inferredGenus);
                            inferredSynName.generateTitle();
                            zooHashMap.put(inferredSynName.getUuid(), inferredSynName);
                            taxonNames.add(inferredSynName.getNameCache());
                        }

                        if (!taxonNames.isEmpty()){
                            List<String> synNotInCDM = this.taxaByNameNotInDB(taxonNames);
                            ZoologicalName name;
                            if (!synNotInCDM.isEmpty()){
                                inferredSynonymsToBeRemoved.clear();

                                for (Synonym syn :inferredSynonyms){
                                    name = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                                    if (!synNotInCDM.contains(name.getNameCache())){
                                        inferredSynonymsToBeRemoved.add(syn);
                                    }
                                }

                                // Remove identified Synonyms from inferredSynonyms
                                for (Synonym synonym : inferredSynonymsToBeRemoved) {
                                    inferredSynonyms.remove(synonym);
                                }
                            }
                        }

                    }else if (type.equals(SynonymRelationshipType.POTENTIAL_COMBINATION_OF())){

                        Reference sourceReference = null; // TODO: Determination of sourceReference is redundant

                        for (SynonymRelationship synonymRelationOfGenus:synonymRelationshipsOfGenus){
                            TaxonNameBase synName;
                            Synonym syn = synonymRelationOfGenus.getSynonym();
                            synName =syn.getName();

                            HibernateProxyHelper.deproxy(syn);

                            // Set the sourceReference
                            sourceReference = syn.getSec();

                            // Determine the idInSource
                            String idInSource = getIdInSource(syn);

                            ZoologicalName zooName = getZoologicalName(synName.getUuid(), zooHashMap);
                            String synGenusName = zooName.getGenusOrUninomial();
                            if (synGenusName != null && !synonymsGenus.containsKey(synGenusName)){
                                synonymsGenus.put(synGenusName, idInSource);
                            }
                        }

                        ZoologicalName inferredSynName;
                        for (SynonymRelationship synonymRelationOfTaxon:synonymRelationshipsOfTaxon){

                            Synonym syn = synonymRelationOfTaxon.getSynonym();
                            HibernateProxyHelper.deproxy(syn);

                            // Set sourceReference
                            sourceReference = syn.getSec();

                            ZoologicalName zooName = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                            String epithetName = zooName.getSpecificEpithet();
                            if (epithetName != null && !synonymsEpithet.contains(epithetName)){
                                synonymsEpithet.add(epithetName);
                            }
                        }
                        for (String epithetName:synonymsEpithet){
                            for (String genusName: synonymsGenus.keySet()){
                                inferredSynName = ZoologicalName.NewInstance(Rank.SPECIES());
                                inferredSynName.setSpecificEpithet(epithetName);
                                inferredSynName.setGenusOrUninomial(genusName);
                                potentialCombination = Synonym.NewInstance(inferredSynName, null);

                                // Set the sourceReference
                                potentialCombination.setSec(sourceReference);

                                // Add the original source
                                String idInSource = synonymsGenus.get(genusName);
                                if (idInSource != null) {
                                    IdentifiableSource originalSource = IdentifiableSource.NewInstance(idInSource, "PotentialCombinationOf", sourceReference, null);

                                    // Add the citation
                                    if (sourceReference != null) {
                                        originalSource.setCitation(sourceReference);
                                        potentialCombination.addSource(originalSource);
                                    }
                                }

                                inferredSynonyms.add(potentialCombination);
                                inferredSynName.generateTitle();
                                zooHashMap.put(inferredSynName.getUuid(), inferredSynName);
                                taxonNames.add(inferredSynName.getNameCache());
                            }

                            if (!taxonNames.isEmpty()){
                                List<String> synNotInCDM = this.taxaByNameNotInDB(taxonNames);
                                ZoologicalName name;
                                if (!synNotInCDM.isEmpty()){
                                    inferredSynonymsToBeRemoved.clear();

                                    for (Synonym syn :inferredSynonyms){
                                        try{
                                            name = (ZoologicalName) syn.getName();
                                        }catch (ClassCastException e){
                                            name = getZoologicalName(syn.getName().getUuid(), zooHashMap);
                                        }
                                        if (!synNotInCDM.contains(name.getNameCache())){
                                            inferredSynonymsToBeRemoved.add(syn);
                                        }
                                    }

                                    // Remove identified Synonyms from inferredSynonyms
                                    for (Synonym synonym : inferredSynonymsToBeRemoved) {
                                        inferredSynonyms.remove(synonym);
                                    }
                                }
                            }
                        }
                    }else {
                        logger.info("The synonymrelationship type is not defined.");
                        return null;
                    }
                }
            }
            }
            }


        return inferredSynonyms;
    }


    /**
     * Returns the idInSource for a given Synonym.
     * @param syn
     */
    private String getIdInSource(Synonym syn) {
        String idInSource = null;
        Set<IdentifiableSource> sources = syn.getSources();
        if (sources.size() == 1) {
            IdentifiableSource source = sources.iterator().next();
            if (source != null) {
                idInSource  = source.getIdInSource();
            }
        } else if (sources.size() > 1) {
            int count = 1;
            idInSource = "";
            for (IdentifiableSource source : sources) {
                idInSource += source.getIdInSource();
                if (count < sources.size()) {
                    idInSource += "; ";
                }
                count++;
            }
        }

        return idInSource;
    }

    /**
     * Returns the citation for a given Synonym.
     * @param syn
     */
    private Reference getCitation(Synonym syn) {
        Reference citation = null;
        Set<IdentifiableSource> sources = syn.getSources();
        if (sources.size() == 1) {
            IdentifiableSource source = sources.iterator().next();
            if (source != null) {
                citation = source.getCitation();
            }
        } else if (sources.size() > 1) {
            logger.warn("This Synonym has more than one source: " + syn.getUuid() + " (" + syn.getTitleCache() +")");
        }

        return citation;
    }

/*	private void xxx(List<SynonymRelationship> synonymRelationships, HashMap <UUID, ZoologicalName> zooHashMap, SynonymRelationshipType type, String addString){

        for (SynonymRelationship synonymRelation:synonymRelationships){
            TaxonNameBase synName;
            NonViralName inferredSynName;
            Synonym syn = synonymRelation.getSynonym();
            HibernateProxyHelper.deproxy(syn);

            synName = syn.getName();
            ZoologicalName zooName = zooHashMap.get(synName.getUuid());
            String synGenusName = zooName.getGenusOrUninomial();

            switch(type.getId()){
            case SynonymRelationshipType.INFERRED_EPITHET_OF().getId():
                inferredSynName.setSpecificEpithet(addString);
                break;
            case SynonymRelationshipType.INFERRED_GENUS_OF().getId():
                break;
            case SynonymRelationshipType.POTENTIAL_COMBINATION_OF().getId():
                break;
            default:
            }
            if (!synonymsGenus.contains(synGenusName)){
                synonymsGenus.add(synGenusName);
            }
            inferredSynName = NonViralName.NewInstance(Rank.SPECIES());
            inferredSynName.setSpecificEpithet(epithetOfTaxon);
            inferredSynName.setGenusOrUninomial(synGenusName);
            inferredEpithet = Synonym.NewInstance(inferredSynName, null);
            taxon.addSynonym(inferredEpithet, SynonymRelationshipType.INFERRED_GENUS_OF());
            inferredSynonyms.add(inferredEpithet);
            inferredSynName.generateTitle();
            taxonNames.add(inferredSynName.getNameCache());
        }


        if (!taxonNames.isEmpty()){
        List<String> synNotInCDM = this.taxaByNameNotInDB(taxonNames);
        ZoologicalName name;
        if (!synNotInCDM.isEmpty()){
            for (Synonym syn :inferredSynonyms){
                name =zooHashMap.get(syn.getName().getUuid());
                if (!synNotInCDM.contains(name.getNameCache())){
                    inferredSynonyms.remove(syn);
                }
            }
        }
        }
    }*/

    /*
     * (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#countAllRelationships()
     */
    public int countAllRelationships() {
        List<RelationshipBase> relationships = this.getAllRelationships(null, 0);
        return relationships.size();
    }


    public List<String> taxaByNameNotInDB(List<String> taxonNames){
        List<TaxonBase> notInDB = new ArrayList<TaxonBase>();
        //get all taxa, already in db
        Query query = getSession().createQuery("from TaxonNameBase t where t.nameCache IN (:taxonList)");
        query.setParameterList("taxonList", taxonNames);
        List<TaxonNameBase> taxaInDB = query.list();
        //compare the original list with the result of the query
        for (TaxonNameBase taxonName: taxaInDB){
            if (taxonName.isInstanceOf(NonViralName.class)) {
                NonViralName nonViralName = CdmBase.deproxy(taxonName, NonViralName.class);
                String nameCache = nonViralName.getNameCache();
                if (taxonNames.contains(nameCache)){
                    taxonNames.remove(nameCache);
                }
            }
        }

        return taxonNames;
    }

    //TODO: mal nur mit UUID probieren (ohne fetch all properties), vielleicht geht das schneller?
    public List<UUID> findIdenticalTaxonNameIds(List<String> propertyPaths){
        Query query=getSession().createQuery("select tmb2 from ZoologicalName tmb, ZoologicalName tmb2 fetch all properties where tmb.id != tmb2.id and tmb.nameCache = tmb2.nameCache");
        List<UUID> zooNames = query.list();

        return zooNames;

    }

    public List<TaxonNameBase> findIdenticalTaxonNames(List<String> propertyPaths) {

        Query query=getSession().createQuery("select tmb2 from ZoologicalName tmb, ZoologicalName tmb2 fetch all properties where tmb.id != tmb2.id and tmb.nameCache = tmb2.nameCache");

        List<TaxonNameBase> zooNames = query.list();

        TaxonNameComparator taxComp = new TaxonNameComparator();
        Collections.sort(zooNames, taxComp);

        for (TaxonNameBase taxonNameBase: zooNames){
            defaultBeanInitializer.initialize(taxonNameBase, propertyPaths);
        }

        return zooNames;
    }

    public List<TaxonNameBase> findIdenticalNamesNew(List<String> propertyPaths){

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
        List<TaxonNameBase> result = query.list();
        TaxonNameBase temp = result.get(0);

        Iterator<OriginalSourceBase> sources = temp.getSources().iterator();

        TaxonNameComparator taxComp = new TaxonNameComparator();
        Collections.sort(result, taxComp);
        defaultBeanInitializer.initializeAll(result, propertyPaths);
        return result;

        }



    public String getPhylumName(TaxonNameBase name){
        List results = new ArrayList();
        try{
        Query query = getSession().createSQLQuery("select getPhylum("+ name.getId()+");");
        results = query.list();
        }catch(Exception e){
            System.err.println(name.getUuid());
            return null;
        }
        System.err.println("phylum of "+ name.getTitleCache() );
        return (String)results.get(0);
    }


    public long countTaxaByCommonName(String searchString,
            Classification classification, MatchMode matchMode,
            Set<NamedArea> namedAreas) {
        boolean doCount = true;
        Query query = prepareTaxaByCommonName(searchString, classification, matchMode, namedAreas, null, null, doCount);
        if (query != null && !query.list().isEmpty()) {
            Object o = query.uniqueResult();
            if(o != null) {
                return (Long)o;
            }
        }
        return 0;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#deleteSynonymRelationships(eu.etaxonomy.cdm.model.taxon.Synonym, eu.etaxonomy.cdm.model.taxon.Taxon)
     */
    public long deleteSynonymRelationships(Synonym synonym, Taxon taxon) {

        String hql = "delete SynonymRelationship sr where sr.relatedFrom = :syn ";
        if (taxon != null){
            hql += " and sr.relatedTo = :taxon";
        }
        Session session = this.getSession();
        Query q = session.createQuery(hql);

        q.setParameter("syn", synonym);
        if (taxon != null){
            q.setParameter("taxon", taxon);
        }
        long result = q.executeUpdate();

        return result;
    }


    @Override
    public Integer countSynonymRelationships(TaxonBase taxonBase,
            SynonymRelationshipType type, Direction relatedfrom) {
        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Query query = null;

            if(type == null) {
                query = getSession().createQuery("select count(synonymRelationship) from SynonymRelationship synonymRelationship where synonymRelationship."+relatedfrom+" = :relatedSynonym");
            } else {
                query = getSession().createQuery("select count(synonymRelationship) from SynonymRelationship synonymRelationship where synonymRelationship."+relatedfrom+" = :relatedSynonym and synonymRelationship.type = :type");
                query.setParameter("type",type);
            }
            query.setParameter("relatedTaxon", taxonBase);

            return ((Long)query.uniqueResult()).intValue();
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonRelationship.class,auditEvent.getRevisionNumber());
            query.add(AuditEntity.relatedId(relatedfrom.toString()).eq(taxonBase.getId()));
            query.addProjection(AuditEntity.id().count("id"));

            if(type != null) {
                query.add(AuditEntity.relatedId("type").eq(type.getId()));
            }

            return ((Long)query.getSingleResult()).intValue();
        }
    }


    @Override
    public List<SynonymRelationship> getSynonymRelationships(TaxonBase taxonBase,
            SynonymRelationshipType type, Integer pageSize, Integer pageNumber,
            List<OrderHint> orderHints, List<String> propertyPaths,
            Direction direction) {

        AuditEvent auditEvent = getAuditEventFromContext();
        if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
            Criteria criteria = getSession().createCriteria(SynonymRelationship.class);

            if (direction.equals(Direction.relatedTo)){
                criteria.add(Restrictions.eq("relatedTo", taxonBase));
            }else{
                criteria.add(Restrictions.eq("relatedFrom", taxonBase));
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

            List<SynonymRelationship> result = (List<SynonymRelationship>)criteria.list();
            defaultBeanInitializer.initializeAll(result, propertyPaths);

            return result;
        } else {
            AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(TaxonRelationship.class,auditEvent.getRevisionNumber());

            if (direction.equals(Direction.relatedTo)){
                query.add(AuditEntity.relatedId("relatedTo").eq(taxonBase.getId()));
            }else{
                query.add(AuditEntity.relatedId("relatedFrom").eq(taxonBase.getId()));
            }

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

            List<SynonymRelationship> result = (List<SynonymRelationship>)query.getResultList();
            defaultBeanInitializer.initializeAll(result, propertyPaths);

            // Ugly, but for now, there is no way to sort on a related entity property in Envers,
            // and we can't live without this functionality in CATE as it screws up the whole
            // taxon tree thing
            if(orderHints != null && !orderHints.isEmpty()) {
                SortedSet<SynonymRelationship> sortedList = new TreeSet<SynonymRelationship>(new SynonymRelationshipFromTaxonComparator());
                sortedList.addAll(result);
                return new ArrayList<SynonymRelationship>(sortedList);
            }

            return result;
        }
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getUuidAndTitleCacheTaxon()
     */
    @Override
    public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheTaxon() {
        String queryString = String.format("select uuid, titleCache from %s where DTYPE = '%s'", type.getSimpleName(), Taxon.class.getSimpleName());
        Query query = getSession().createQuery(queryString);

        List<UuidAndTitleCache<TaxonBase>> result = getUuidAndTitleCache(query);

        return result;
    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao#getUuidAndTitleCacheSynonym()
     */
    @Override
    public List<UuidAndTitleCache<TaxonBase>> getUuidAndTitleCacheSynonym() {
        String queryString = String.format("select uuid, titleCache from %s where DTYPE = '%s'", type.getSimpleName(), Synonym.class.getSimpleName());
        Query query = getSession().createQuery(queryString);

        List<UuidAndTitleCache<TaxonBase>> result = getUuidAndTitleCache(query);

        return result;
    }


    @Override
    public List<TaxonBase> getTaxaByName(Class<? extends TaxonBase> clazz,
            String queryString, Classification classification,
            MatchMode matchMode, Set<NamedArea> namedAreas, Integer pageSize,
            Integer pageNumber, List<String> propertyPaths,
            boolean doIncludeMisappliedNames) {

        boolean doCount = false;

        Query query = prepareTaxaByName(clazz, "nameCache", queryString, classification, matchMode, namedAreas, pageSize, pageNumber, doCount, doIncludeMisappliedNames);

        if (query != null){
            List<TaxonBase> results = query.list();
            defaultBeanInitializer.initializeAll(results, propertyPaths);

            return results;
        }


        return new ArrayList<TaxonBase>();
    }










}
