/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Credit;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.IAnnotatableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dao.QueryParseException;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;
import eu.etaxonomy.cdm.persistence.dao.common.Restriction;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


public class IdentifiableDaoBase<T extends IdentifiableEntity>
        extends AnnotatableDaoImpl<T>
        implements IIdentifiableDao<T>{

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(IdentifiableDaoBase.class);

    protected String defaultField = "titleCache_tokenized";
    protected Class<? extends T> indexedClasses[];



    public IdentifiableDaoBase(Class<T> type) {
        super(type);
    }


    @Override
    public List<T> findByTitle(String queryString) {
        return findByTitle(queryString, null);
    }

    @Override
    public List<T> findByTitle(String queryString, CdmBase sessionObject) {
        /**
         *  FIXME why do we need to call update in a find* method? I don't know for sure
         *  that this is a good idea . . .
         */
        Session session = getSession();
        if ( sessionObject != null ) {
            session.update(sessionObject);
        }
        checkNotInPriorView("IdentifiableDaoBase.findByTitle(String queryString, CdmBase sessionObject)");
        Criteria crit = session.createCriteria(type);
        crit.add(Restrictions.ilike("titleCache", queryString));
        List<T> results = crit.list();
        List<String> propertyPaths = null;
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public List<T> findByTitleAndClass(String queryString, Class<T> clazz) {
        checkNotInPriorView("IdentifiableDaoBase.findByTitleAndClass(String queryString, Class<T> clazz)");
        Criteria crit = getSession().createCriteria(clazz);
        crit.add(Restrictions.ilike("titleCache", queryString));
        List<T> results = crit.list();
        return results;
    }

    @Override
    public List<T> findTitleCache(Class<? extends T> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, MatchMode matchMode){

        Query query = prepareFindTitleCache(clazz, queryString, pageSize,
                pageNumber, matchMode, false);
        List<T> result = query.list();
        return result;
    }

    @Override
    public Long countTitleCache(Class<? extends T> clazz, String queryString, MatchMode matchMode){

        Query query = prepareFindTitleCache(clazz, queryString, null,
                null, matchMode, true);
        Long result = (Long)query.uniqueResult();
        return result;
    }

    /**
     * @param clazz filter by class - can be null to include all instances of type T
     * @param queryString the query string to filter by
     * @param pageSize
     * @param pageNumber
     * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
     * @return
     */
    private Query prepareFindTitleCache(Class<? extends T> clazz,
            String queryString, Integer pageSize, Integer pageNumber,
            MatchMode matchMode, boolean doCount) {
        if(clazz == null) {
            clazz = type;
        }

        String what = (doCount ? "count(distinct e.titleCache)": "distinct e.titleCache");

        if(matchMode != null){
            queryString	= matchMode.queryStringFrom(queryString);
        }
        String hql = "select " + what + " from  " + clazz.getName() + " e where e.titleCache like '" + queryString + "'";

        Query query = getSession().createQuery(hql);

        if(pageSize != null && !doCount) {
            query.setMaxResults(pageSize);
            if(pageNumber != null) {
                query.setFirstResult(pageNumber * pageSize);
            }
        }
        return query;
    }

    @Override
    public List<T> findByTitle(Class<? extends T> clazz, String queryString, MatchMode matchmode, List<Restriction<?>> restrictions, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        return findByParamWithRestrictions(clazz, "titleCache", queryString, matchmode, restrictions, pageSize, pageNumber, orderHints, propertyPaths);
    }

    @Override
    public List<T> findByReferenceTitle(Class<? extends T> clazz, String queryString, MatchMode matchmode, List<Restriction<?>> restrictions, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        return findByParamWithRestrictions(clazz, "title", queryString, matchmode, restrictions, pageSize, pageNumber, orderHints, propertyPaths);
    }

    @Override
    public List<T> findByTitle(String queryString, MatchMode matchmode, int page, int pagesize, List<Criterion> criteria) {
        checkNotInPriorView("IdentifiableDaoBase.findByTitle(String queryString, MATCH_MODE matchmode, int page, int pagesize, List<Criterion> criteria)");
        Criteria crit = getSession().createCriteria(type);
        if (matchmode == MatchMode.EXACT) {
            crit.add(Restrictions.eq("titleCache", matchmode.queryStringFrom(queryString)));
        } else {
//			crit.add(Restrictions.ilike("titleCache", matchmode.queryStringFrom(queryString)));
            crit.add(Restrictions.like("titleCache", matchmode.queryStringFrom(queryString)));
        }
        if (pagesize >= 0) {
            crit.setMaxResults(pagesize);
        }
        if(criteria != null){
            for (Criterion criterion : criteria) {
                crit.add(criterion);
            }
        }
        crit.addOrder(Order.asc("titleCache"));
        int firstItem = (page - 1) * pagesize;
        crit.setFirstResult(firstItem);
        @SuppressWarnings("unchecked")
        List<T> results = crit.list();
        List<String> propertyPaths = null;
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public long countRights(T identifiableEntity) {
        checkNotInPriorView("IdentifiableDaoBase.countRights(T identifiableEntity)");
        Query query = getSession().createQuery("select count(rights) from " + type.getSimpleName() + " identifiableEntity join identifiableEntity.rights rights where identifiableEntity = :identifiableEntity");
        query.setParameter("identifiableEntity",identifiableEntity);
        return (Long)query.uniqueResult();
    }

    @Override
    public long countSources(T identifiableEntity) {
        checkNotInPriorView("IdentifiableDaoBase.countSources(T identifiableEntity)");
        Query query = getSession().createQuery("SELECT COUNT(source) FROM "+identifiableEntity.getClass().getName() + " ie JOIN ie.sources source WHERE ie = :identifiableEntity");
        query.setParameter("identifiableEntity", identifiableEntity);
        return (Long)query.uniqueResult();
    }

    @Override
    public List<Rights> getRights(T identifiableEntity, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        checkNotInPriorView("IdentifiableDaoBase.getRights(T identifiableEntity, Integer pageSize, Integer pageNumber, List<String> propertyPaths)");
        Query query = getSession().createQuery("select rights from " + type.getSimpleName() + " identifiableEntity join identifiableEntity.rights rights where identifiableEntity = :identifiableEntity");
        query.setParameter("identifiableEntity",identifiableEntity);
        setPagingParameter(query, pageSize, pageNumber);
        @SuppressWarnings("unchecked")
        List<Rights> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

//    @Override  //TODO add to interface, maybe add property path
    public List<Credit> getCredits(T identifiableEntity, Integer pageSize, Integer pageNumber) {
        checkNotInPriorView("IdentifiableDaoBase.getCredits(T identifiableEntity, Integer pageSize, Integer pageNumber)");
        Query query = getSession().createQuery("select credits from " + type.getSimpleName() + " identifiableEntity join identifiableEntity.credits credits where identifiableEntity = :identifiableEntity");
        query.setParameter("identifiableEntity",identifiableEntity);
        setPagingParameter(query, pageSize, pageNumber);
        @SuppressWarnings("unchecked")
        List<Credit> result = query.list();
        return result;
    }

    @Override
    public List<IdentifiableSource> getSources(T identifiableEntity, Integer pageSize, Integer pageNumber, List<String> propertyPaths) {
        checkNotInPriorView("IdentifiableDaoBase.getSources(T identifiableEntity, Integer pageSize, Integer pageNumber)");
        Query query = getSession().createQuery("SELECT source FROM "+ identifiableEntity.getClass().getName()+ " ie JOIN ie.sources source WHERE ie.id = :id");
        query.setParameter("id",identifiableEntity.getId());
        setPagingParameter(query, pageSize, pageNumber);
        @SuppressWarnings("unchecked")
        List<IdentifiableSource> results = query.list();
        defaultBeanInitializer.initializeAll(results, propertyPaths);
        return results;
    }

    @Override
    public List<T> findOriginalSourceByIdInSource(String idInSource, String idNamespace) {
        checkNotInPriorView("IdentifiableDaoBase.findOriginalSourceByIdInSource(String idInSource, String idNamespace)");
        Query query = getSession().createQuery(
                "Select c from " + type.getSimpleName() + " as c " +
                "inner join c.sources as source " +
                "where source.idInSource = :idInSource " +
                    " AND source.idNamespace = :idNamespace"
            );
        query.setString("idInSource", idInSource);
        query.setString("idNamespace", idNamespace);
        //TODO integrate reference in where

        @SuppressWarnings("unchecked")
        List<T> result = query.list();
        return result;
    }

    @Override
    public T find(LSID lsid) {
        checkNotInPriorView("IdentifiableDaoBase.find(LSID lsid)");
        Criteria criteria = getSession().createCriteria(type);
        criteria.add(Restrictions.eq("lsid.authority", lsid.getAuthority()));
        criteria.add(Restrictions.eq("lsid.namespace", lsid.getNamespace()));
        criteria.add(Restrictions.eq("lsid.object", lsid.getObject()));

        if(lsid.getRevision() != null) {
            criteria.add(Restrictions.eq("lsid.revision", lsid.getRevision()));
        }

        T object = (T)criteria.uniqueResult();
        if(object != null) {
            return object;
        } else {
            AuditQuery query = getAuditReader().createQuery().forRevisionsOfEntity(type, false, true);
            query.add(AuditEntity.property("lsid_authority").eq(lsid.getAuthority()));
            query.add(AuditEntity.property("lsid_namespace").eq(lsid.getNamespace()));
            query.add(AuditEntity.property("lsid_object").eq(lsid.getObject()));

            if(lsid.getRevision() != null) {
                query.add(AuditEntity.property("lsid_revision").eq(lsid.getRevision()));
            }

            query.addOrder(AuditEntity.revisionNumber().asc());
            query.setMaxResults(1);
            query.setFirstResult(0);
            List<Object[]> objs = query.getResultList();
            if(objs.isEmpty()) {
                return null;
            } else {
                return (T)objs.get(0)[0];
            }
        }
    }



    @SuppressWarnings("deprecation")
    @Override
    public String getTitleCache(UUID uuid, boolean refresh){
        if (! refresh){
            String queryStr = String.format("SELECT titleCache FROM %s t WHERE t.uuid = '%s'", type.getSimpleName() , uuid.toString());
            Query query = getSession().createQuery(queryStr);
            List<?> list = query.list();
            return list.isEmpty()? null : (String)list.get(0);
        }else{
            T entity = this.findByUuid(uuid);
            if (entity == null){
                return null;
            }
            entity.setTitleCache(null);
            return entity.getTitleCache();
        }
    }


    @Override
    public long countByTitle(Class<? extends T> clazz, String queryString,	MatchMode matchmode, List<Restriction<?>> restrictions) {
        return countByParamWithRestrictions(clazz, "titleCache", queryString, matchmode, restrictions);
    }

    @Override
    public long countByReferenceTitle(Class<? extends T> clazz, String queryString,	MatchMode matchmode, List<Restriction<?>> restrictions) {
        return countByParamWithRestrictions(clazz, "title", queryString, matchmode, restrictions);
    }

    @Override
    public long count(Class<? extends T> clazz, String queryString) {
        checkNotInPriorView("IdentifiableDaoBase.count(Class<? extends T> clazz, String queryString)");
        QueryParser queryParser = new QueryParser(defaultField , new StandardAnalyzer());

        try {
            org.apache.lucene.search.Query query = queryParser.parse(queryString);

            FullTextSession fullTextSession = Search.getFullTextSession(this.getSession());
            org.hibernate.search.FullTextQuery fullTextQuery = null;

            if(clazz == null) {
                fullTextQuery = fullTextSession.createFullTextQuery(query, type);
            } else {
                fullTextQuery = fullTextSession.createFullTextQuery(query, clazz);
            }

            int  result = fullTextQuery.getResultSize();
            return result;

        } catch (ParseException e) {
            throw new QueryParseException(e, queryString);
        }
    }

    @Override
    public void optimizeIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());
        SearchFactory searchFactory = fullTextSession.getSearchFactory();
        for(Class<?> clazz : indexedClasses) {
            searchFactory.optimize(clazz); // optimize the indices ()
        }
        fullTextSession.flushToIndexes();
    }

    @Override
    public void purgeIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());
        for(Class clazz : indexedClasses) {
            fullTextSession.purgeAll(clazz); // remove all objects of type t from indexes
        }
        fullTextSession.flushToIndexes();
    }

    @Override
    public void rebuildIndex() {
        FullTextSession fullTextSession = Search.getFullTextSession(getSession());

        for(T t : list(null,null)) { // re-index all objects
            fullTextSession.index(t);
        }
        fullTextSession.flushToIndexes();
    }

    @Override
    public List<T> search(Class<? extends T> clazz, String queryString,	Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths) {
        checkNotInPriorView("IdentifiableDaoBase.search(Class<? extends T> clazz, String queryString, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,List<String> propertyPaths)");
        QueryParser queryParser = new QueryParser(defaultField, new StandardAnalyzer());
        List<T> results = new ArrayList<T>();

        try {
            org.apache.lucene.search.Query query = queryParser.parse(queryString);

            FullTextSession fullTextSession = Search.getFullTextSession(getSession());
            org.hibernate.search.FullTextQuery fullTextQuery = null;

            if(clazz == null) {
                fullTextQuery = fullTextSession.createFullTextQuery(query, type);
            } else {
                fullTextQuery = fullTextSession.createFullTextQuery(query, clazz);
            }

            addOrder(fullTextQuery,orderHints);

            if(pageSize != null) {
                fullTextQuery.setMaxResults(pageSize);
                if(pageNumber != null) {
                    fullTextQuery.setFirstResult(pageNumber * pageSize);
                } else {
                    fullTextQuery.setFirstResult(0);
                }
            }

            List<T> result = fullTextQuery.list();
            defaultBeanInitializer.initializeAll(result, propertyPaths);
            return result;

        } catch (ParseException e) {
            throw new QueryParseException(e, queryString);
        }
    }

    @Override
    public String suggestQuery(String string) {
        throw new UnsupportedOperationException("suggestQuery is not supported for objects of class " + type.getName());
    }

    @Override
    public long countByTitle(String queryString) {
        return countByTitle(queryString, null);
    }

    @Override
    public long countByTitle(String queryString, CdmBase sessionObject) {
        Session session = getSession();
        if ( sessionObject != null ) {
            session.update(sessionObject);
        }
        checkNotInPriorView("IdentifiableDaoBase.countByTitle(String queryString, CdmBase sessionObject)");
        Criteria crit = session.createCriteria(type);
        crit.add(Restrictions.ilike("titleCache", queryString))
            .setProjection(Projections.rowCount());
        long result =  (Long)crit.uniqueResult();
        return result;
    }

    @Override
    public long countByTitle(String queryString, MatchMode matchMode, List<Criterion> criteria) {
        checkNotInPriorView("IdentifiableDaoBase.findByTitle(String queryString, MATCH_MODE matchmode, int page, int pagesize, List<Criterion> criteria)");
        Criteria crit = getCriteria(type);
        if (matchMode == MatchMode.EXACT) {
            crit.add(Restrictions.eq("titleCache", matchMode.queryStringFrom(queryString)));
        } else {
//			crit.add(Restrictions.ilike("titleCache", matchmode.queryStringFrom(queryString)));
            crit.add(Restrictions.like("titleCache", matchMode.queryStringFrom(queryString)));
        }

        if(criteria != null){
            for (Criterion criterion : criteria) {
                crit.add(criterion);
            }
        }
        crit.setProjection(Projections.rowCount());

        long result = (Long)crit.uniqueResult();
        return result;
    }


	@Override
	public <S extends T> int countByIdentifier(Class<S> clazz,
			String identifier, DefinedTerm identifierType, MatchMode matchmode) {
		checkNotInPriorView("IdentifiableDaoBase.countByIdentifier(T clazz, String identifier, DefinedTerm identifierType, MatchMode matchmode)");

		Class<?> clazzParam = clazz == null ? type : clazz;
		String queryString = "SELECT count(*) FROM " + clazzParam.getSimpleName() + " as c " +
	                "INNER JOIN c.identifiers as ids " +
	                "WHERE (1=1) ";
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
	public <S extends T> List<Object[]> findByIdentifier(
			Class<S> clazz, String identifier, DefinedTerm identifierType,
			MatchMode matchmode, boolean includeEntity,
			Integer pageSize, Integer pageNumber, List<String> propertyPaths) {

		checkNotInPriorView("IdentifiableDaoBase.findByIdentifier(T clazz, String identifier, DefinedTerm identifierType, MatchMode matchmode, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)");

		Class<?> clazzParam = clazz == null ? type : clazz;
		String queryString = "SELECT ids.type, ids.identifier, %s FROM %s as c " +
                " INNER JOIN c.identifiers as ids " +
                " WHERE (1=1) ";
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

	   @Override
	    public <S extends T> long countByMarker(Class<S> clazz, MarkerType markerType,
	            Boolean markerValue) {
	        checkNotInPriorView("IdentifiableDaoBase.countByMarker(T clazz, MarkerType markerType, Boolean markerValue)");

	        if (markerType == null){
	            return 0;
	        }
	        Class<?> clazzParam = clazz == null ? type : clazz;
	        String queryString = "SELECT count(*) FROM " + clazzParam.getSimpleName() + " as c " +
	                    "INNER JOIN c.markers as mks " +
	                    "WHERE (1=1) ";

	        if (markerValue != null){
	            queryString += " AND mks.flag = :flag";
	        }
            queryString += " AND mks.markerType = :type";

	        Query query = getSession().createQuery(queryString);
            query.setEntity("type", markerType);
	        if (markerValue != null){
	            query.setBoolean("flag", markerValue);
	        }

	        Long c = (Long)query.uniqueResult();
	        return c;
	    }

	@Override
    public <S extends T> List<Object[]> findByMarker(
            Class<S> clazz, MarkerType markerType,
            Boolean markerValue, boolean includeEntity, Integer pageSize, Integer pageNumber,
            List<String> propertyPaths) {

        checkNotInPriorView("IdentifiableDaoBase.findByMarker(T clazz, String identifier, DefinedTerm identifierType, MatchMode matchmode, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths)");
        if (markerType == null){
            return new ArrayList<Object[]>();
        }

        Class<?> clazzParam = clazz == null ? type : clazz;
        String queryString = "SELECT mks.markerType, mks.flag, %s FROM %s as c " +
                " INNER JOIN c.markers as mks " +
                " WHERE (1=1) ";
        queryString = String.format(queryString, (includeEntity ? "c":"c.uuid, c.titleCache") , clazzParam.getSimpleName());

        //Matchmode and identifier
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

        @SuppressWarnings("unchecked")
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
    public List<UuidAndTitleCache<T>> getUuidAndTitleCache(Integer limit, String pattern){
        return getUuidAndTitleCache(type, limit, pattern);
    }


    @Override
    public <S extends T> List<UuidAndTitleCache<S>> getUuidAndTitleCache(Class<S> clazz, Integer limit, String pattern){
        Session session = getSession();
        Query query = null;
        if (pattern != null){
            query = session.createQuery(
                      " SELECT uuid, id, titleCache "
                    + " FROM " + clazz.getSimpleName()
                    + " WHERE titleCache LIKE :pattern");
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            pattern = pattern + "%";
            query.setParameter("pattern", pattern);
        } else {
            query = session.createQuery(
                      " SELECT uuid, id, titleCache "
                    + " FROM  " + clazz.getSimpleName() );
        }
        if (limit != null){
           query.setMaxResults(limit);
        }
        return getUuidAndTitleCache(query);
    }


    @Override
    public List<UuidAndTitleCache<T>> getUuidAndTitleCache(){
        return getUuidAndTitleCache(type, null, null);
    }

    protected <E extends IAnnotatableEntity> List<UuidAndTitleCache<E>> getUuidAndAbbrevTitleCache(Query query){
        List<UuidAndTitleCache<E>> list = new ArrayList<UuidAndTitleCache<E>>();

        List<Object[]> result = query.list();

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<E>((UUID) object[0],(Integer) object[1], (String) object[3], (String) object[2]));
        }
        return list;
    }

    protected <E extends IAnnotatableEntity> List<UuidAndTitleCache<E>> getUuidAndTitleCache(Query query){
        List<UuidAndTitleCache<E>> list = new ArrayList<UuidAndTitleCache<E>>();

        List<Object[]> result = query.list();

        for(Object[] object : result){
            list.add(new UuidAndTitleCache<E>((UUID) object[0],(Integer) object[1], (String) object[2]));
        }
        return list;
    }

}
